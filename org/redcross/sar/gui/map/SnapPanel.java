package org.redcross.sar.gui.map;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoPanel;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.SnapAdapter;
import org.redcross.sar.map.SnapAdapter.SnapListener;
import org.redcross.sar.map.layer.IMsoFeatureLayer;

import com.borland.jbcl.layout.VerticalFlowLayout;

import com.esri.arcgis.carto.IFeatureLayer;

public class SnapPanel extends DiskoPanel implements SnapListener {
	
	private static final long serialVersionUID = 1L;
	//private IDrawTool tool = null;
	private DiskoPanel actionsPanel = null;
	private DiskoPanel layersPanel = null;
	private DiskoPanel tolerancePanel = null;
	private JSlider toleranceSlider = null;
	private JLabel messageLabel = null;
	private JPanel selectorPanel = null;
	private JButton allButton = null;
	private JButton noneButton = null;
	private JButton applyButton = null;
	private JButton cancelButton = null;
	private HashMap<String,JCheckBox> checkBoxes = null;
	private HashMap<String,IFeatureLayer> layers = null;
	private Properties properties = null;
	private HashMap<String,String> myInterests = null;
	private SnapAdapter adapter = null;
	private boolean isSnapToChangeing = false;
	
	public SnapPanel() {
		
		// forward
		super("Snapping");
		
		// prepare
		checkBoxes = new HashMap<String,JCheckBox>();
		layers = new HashMap<String,IFeatureLayer>();
		myInterests = new HashMap<String,String>();
		myInterests.put(IMsoFeatureLayer.LayerCode.OPERATION_AREA_LAYER.name(),IMsoFeatureLayer.LayerCode.OPERATION_AREA_LAYER.name());
		myInterests.put(IMsoFeatureLayer.LayerCode.OPERATION_AREA_MASK_LAYER.name(),IMsoFeatureLayer.LayerCode.OPERATION_AREA_MASK_LAYER.name());
		myInterests.put(IMsoFeatureLayer.LayerCode.SEARCH_AREA_LAYER.name(),IMsoFeatureLayer.LayerCode.SEARCH_AREA_LAYER.name());
		myInterests.put(IMsoFeatureLayer.LayerCode.ROUTE_LAYER.name(),IMsoFeatureLayer.LayerCode.ROUTE_LAYER.name());
		myInterests.put(IMsoFeatureLayer.LayerCode.FLANK_LAYER.name(),IMsoFeatureLayer.LayerCode.FLANK_LAYER.name());
		myInterests.put(IMsoFeatureLayer.LayerCode.POI_LAYER.name(),IMsoFeatureLayer.LayerCode.POI_LAYER.name());
		// initialize gui
		initialize();
	}
	
	private void initialize() {
		try {
			VerticalFlowLayout vfl = new VerticalFlowLayout();
			vfl.setAlignment(VerticalFlowLayout.LEFT);
			vfl.setHgap(5);
			vfl.setVgap(5);
			vfl.setVerticalFill(true);
			JPanel body = (JPanel)getBodyComponent();
			body.setPreferredSize(new Dimension(200,150));
			body.setLayout(vfl);
			body.add(getActionsPanel());
			body.add(getTolerancePanel());
			body.add(getLayersPanel());

		}
		catch (java.lang.Throwable e) {
			//  Do Something
			e.printStackTrace();
		}
	}
			
	private DiskoPanel getActionsPanel() {
		if (actionsPanel == null) {
			try {
				
				// create layer
				actionsPanel = new DiskoPanel("Utfør");
				actionsPanel.setBodyComponent(null);
				// create buttons
				allButton = DiskoButtonFactory.createButton("GENERAL.ALL",ButtonSize.NORMAL);
				noneButton = DiskoButtonFactory.createButton("GENERAL.NONE",ButtonSize.NORMAL);
				applyButton = DiskoButtonFactory.createButton("GENERAL.APPLY",ButtonSize.NORMAL);
				cancelButton = DiskoButtonFactory.createButton("GENERAL.CANCEL",ButtonSize.NORMAL);
				// add action listeners
				allButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						checkAll();
					}
				});
				noneButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						checkNone();
					}
				});
				applyButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						try {
							// get layers
							List<IFeatureLayer> list = getSnapToLayers();
							// is changing
							isSnapToChangeing = true;
							// apply snapping?
							if(adapter!=null) adapter.setSnapToLayers(list);
							// forward
							fireActionEvent(e);
						} catch (Exception ex) {
							// TODO Auto-generated catch block
							ex.printStackTrace();
						}
						// finished
						isSnapToChangeing = false;
					}
				});
				cancelButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						// reset
						cancel();
						// forward
						fireActionEvent(e);
					}
				});
				// add buttons to panel
				actionsPanel.addButton(allButton,"all");
				actionsPanel.addButton(noneButton,"none");
				actionsPanel.addButton(applyButton,"apply");
				actionsPanel.addButton(cancelButton,"cancel");
				
			} catch (java.lang.Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return actionsPanel;
	}	
	
	public void setSnapAdapter(SnapAdapter adapter) {
		// release current?
		if(this.adapter!=null)
			this.adapter.removeSnapListener(this);
		// prepare
		this.adapter = adapter;
		// register?
		if(adapter!=null) {
			// add listener
			adapter.addSnapListener(this);
			// update snappable layers
			onSnapableChanged();
			// update selected layers 
			onSnapToChanged();
		}
		
	}
	
	public SnapAdapter getSnapAdapter() {
		return adapter;
	}
	
	@Override
	public void cancel() {
		// reload from adapter
		onSnapableChanged();
		onSnapToChanged();
	}
	
	private DiskoPanel getLayersPanel() {
		if (layersPanel == null) {
			try {
				// get body component
				layersPanel = new DiskoPanel("Snap til lag");
				JPanel body = ((JPanel)layersPanel.getBodyComponent());
				VerticalFlowLayout vfl = new VerticalFlowLayout();
				vfl.setAlignment(VerticalFlowLayout.LEFT);
				vfl.setHgap(0);
				vfl.setVgap(5);
				vfl.setVerticalFill(true);
				body.setLayout(vfl);
				body.add(getMessageLabel());
				body.add(getSelectorPanel());
				// set preferred size
				layersPanel.setPreferredSize(new Dimension(200,50));
				// setup
				showSelector(false);
			} catch (java.lang.Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return layersPanel;
	}
	
	private JLabel getMessageLabel() {
		if(messageLabel==null) {
			messageLabel = new JLabel();
			messageLabel.setVerticalAlignment(SwingConstants.CENTER);
			messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
			messageLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			messageLabel.setVerticalTextPosition(SwingConstants.CENTER);			
		}
		return messageLabel;
	}
	
	private JPanel getSelectorPanel() {
		if(selectorPanel==null) {
			// create selector panel
			selectorPanel = new JPanel();
			VerticalFlowLayout vfl = new VerticalFlowLayout();
			vfl.setVgap(0);
			vfl.setHgap(0);
			vfl.setAlignment(VerticalFlowLayout.TOP);
			selectorPanel.setLayout(vfl);
			
		}
		return selectorPanel;
	}
	
	
	private DiskoPanel getTolerancePanel() {
		if (tolerancePanel == null) {
			try {
				tolerancePanel = new DiskoPanel("Sett toleranse (meter)");
				tolerancePanel.setBodyComponent(getToleranceSlider());
				tolerancePanel.setPreferredSize(new Dimension(200, 100));
			} catch (java.lang.Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return tolerancePanel;
	}
	
	private JSlider getToleranceSlider() {
		if (toleranceSlider == null) {
			try {
				toleranceSlider = new JSlider();
				toleranceSlider.setOrientation(JSlider.HORIZONTAL);
				toleranceSlider.setMinorTickSpacing(5);
				toleranceSlider.setMajorTickSpacing(25);
				toleranceSlider.setPaintLabels(true);
				toleranceSlider.setPaintTicks(true);
				toleranceSlider.setMaximum(150);
				toleranceSlider.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						try {
							// get value
							int tolerance = toleranceSlider.getValue();
							// update text
							getTolerancePanel().setCaptionText("Sett toleranse (" + tolerance + " meter)");
							// apply
							if(adapter!=null) adapter.setSnapTolerance(tolerance);
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				});
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return toleranceSlider;
	}
		
	private JCheckBox getCheckBox(String name) {
		if(checkBoxes.containsKey(name)) {
			return checkBoxes.get(name);
		}
		return null;
	}
	
	private void hideAll() {
		Iterator<JCheckBox> it = checkBoxes.values().iterator();
		while(it.hasNext()) {
			it.next().setVisible(false);
		}
	}

	private void checkAll() {
		Iterator<JCheckBox> it = checkBoxes.values().iterator();
		while(it.hasNext()) {
			it.next().setSelected(true);
		}
	}
	
	private void checkNone() {
		Iterator<JCheckBox> it = checkBoxes.values().iterator();
		while(it.hasNext()) {
			it.next().setSelected(false);
		}
	}
	
	public boolean addApplyListener(ActionListener listener) {
		if(applyButton!=null) {
			applyButton.addActionListener(listener);
			return true;
		}
		return false;
	}
	
	public boolean removeApplyListener(ActionListener listener) {
		if(applyButton!=null) {
			applyButton.removeActionListener(listener);
			return true;
		}
		return false;
	}
	
	private String getLayerCaption(String name)
	{	   
		// search for mso layers
		for(int i=0;i<myInterests.size();i++){
			if(myInterests.containsKey(name)){
				return getProperties().getProperty("Layer."+name); 
			}		   
		}	   
		// remove any suffix	   
		int pos = name.indexOf("_");
		if(pos>0 && pos<name.length()-1) {
			return name.substring(pos+1);
		}	   
		return name;
	}

	private Properties getProperties()
	{
		if (properties == null)
		{
			try
			{
				properties = Utils.loadProperties("properties");
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return properties;
	}	
	
	public void setSnapTolerance(double value) {
		toleranceSlider.setValue((int)value);
	}
	
	public List<IFeatureLayer> getSnapToLayers() {
		ArrayList<IFeatureLayer> snapTo = new ArrayList<IFeatureLayer>();
		Iterator<JCheckBox> it = checkBoxes.values().iterator();
		while(it.hasNext()) {
			JCheckBox cb = it.next();
			if (cb.isVisible() && cb.isSelected()) {
				snapTo.add(layers.get(cb.getName()));
			}
		}
		return snapTo;
	}
	
	public void setSnapableLayers(List list) {
		//adding checkboxes
		try {
			// initialize
			hideAll();
			layers.clear();
			JCheckBox cb = null;
			// get layer panels
			JPanel body = (JPanel)getLayersPanel().getBodyComponent();
			JPanel selector = (JPanel)body.getComponent(1);
			// loop over all
			for (int i = 0; i < list.size(); i++) {
				IFeatureLayer flayer = (IFeatureLayer)list.get(i);
				String name = flayer.getName();
				cb = getCheckBox(name);				
				if (cb == null) {
					cb = new JCheckBox();
					cb.setName(name);
					selector.add(cb);
				}
				// update text
				cb.setText(getLayerCaption(name));
						// put combobox
				checkBoxes.put(name,cb);
				// put layer
				layers.put(name,flayer);
				// show box
				cb.setVisible(true);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onSnapToChanged() {
		// is not changing?
		if(!isSnapToChangeing) {
			// unselect all
			checkNone();
			// get selector panel
			JPanel selector = (JPanel)((JPanel)getLayersPanel().getBodyComponent()).getComponent(1);
			// initialize
			List<IFeatureLayer> snapTo = null;
			// get list?
			if(adapter!=null) snapTo = adapter.getSnapToLayers();			
			// has no layers?
			if(snapTo==null) return;			
			// update selection
			for(int i=0;i<snapTo.size();i++) {
				IFeatureLayer flayer = snapTo.get(i);
				try {
					String name = flayer.getName();
					JCheckBox cb = getCheckBox(name);
					if (cb == null) {
						cb = new JCheckBox();
						cb.setName(name);
						selector.add(cb);
						// update text
						cb.setText(getLayerCaption(name));
						// put combobox
						checkBoxes.put(name,cb);
						// put layer
						this.layers.put(name,flayer);
						// show box
						cb.setVisible(true);
					}
					cb.setSelected(true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void onSnapableChanged() {
		// get flag
		boolean bFlag = adapter!=null ? adapter.isSnappingAllowed() : false;
		// update gui
		if(bFlag) {
			try {
				if(adapter!=null) 				
					setSnapTolerance(adapter.getSnapTolerance());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// set state
		getToleranceSlider().setEnabled(bFlag);
		// update snappable layers?
		if(adapter!=null) 
			setSnapableLayers(adapter.getSnapableLayers());
		// get card layout
		showSelector(bFlag);
	}
	
	private void showSelector(boolean isVisible) {
		// get panels
		getSelectorPanel().setVisible(isVisible);
		showMessage("<html><center>Snapping er kun mulig på <br> kartskala mindre enn 1:" 
				+ ((adapter!=null) ? (int)adapter.getMaxSnapScale() : "<nothing>") 
				+ "</center></html>",!isVisible);
	}
	
	private void showMessage(String text, boolean isVisible) {
		// get panels
		getMessageLabel().setText(text);
		getMessageLabel().setVisible(isVisible);
	}
	
}
