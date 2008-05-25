package org.redcross.sar.gui.map;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.redcross.sar.gui.DefaultDiskoPanel;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.SnapAdapter;
import org.redcross.sar.map.SnapAdapter.SnapListener;
import org.redcross.sar.map.layer.IMsoFeatureLayer;

import com.borland.jbcl.layout.VerticalFlowLayout;

import com.esri.arcgis.carto.IFeatureLayer;
import com.esri.arcgis.interop.AutomationException;

public class SnapPanel extends DefaultDiskoPanel implements SnapListener {
	
	private static final long serialVersionUID = 1L;

	private DefaultDiskoPanel layersPanel = null;
	private DefaultDiskoPanel tolerancePanel = null;
	private JSlider toleranceSlider = null;
	private JLabel messageLabel = null;
	private JPanel selectorPanel = null;
	private JButton allButton = null;
	private JButton noneButton = null;
	private HashMap<String,JCheckBox> checkBoxes = null;
	private HashMap<String,IFeatureLayer> layers = null;
	private HashMap<String,String> myInterests = null;
	private SnapAdapter adapter = null;
	
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
			
			insertButton("finish",getNoneButton(), "none");
			insertButton("finish",getAllButton(), "all");
			addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String cmd = e.getActionCommand();
					// canceled?
					if("all".equalsIgnoreCase(cmd)) 
						checkAll();
					else if("none".equalsIgnoreCase(cmd)) 
						checkNone();					
				}
				
			});
			
			VerticalFlowLayout vfl = new VerticalFlowLayout();
			vfl.setAlignment(VerticalFlowLayout.LEFT);
			vfl.setHgap(5);
			vfl.setVgap(5);
			vfl.setVerticalFill(true);
			JPanel body = (JPanel)getBodyComponent();
			body.setPreferredSize(new Dimension(200,400));
			body.setLayout(vfl);
			body.add(getTolerancePanel());
			body.add(getLayersPanel());

		}
		catch (java.lang.Throwable e) {
			//  Do Something
			e.printStackTrace();
		}
	}
			
	/**
	 * This method initializes allButton	
	 * 	
	 * @return {@link JButton}
	 */
	private JButton getAllButton() {
		if (allButton == null) {
			allButton = DiskoButtonFactory.createButton("GENERAL.ALL",ButtonSize.NORMAL);			
		}
		return allButton;
	}
	
	/**
	 * This method initializes noneButton	
	 * 	
	 * @return {@link JButton}
	 */
	private JButton getNoneButton() {
		if (noneButton == null) {
			noneButton = DiskoButtonFactory.createButton("GENERAL.NONE",ButtonSize.NORMAL);			
		}
		return noneButton;
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
	public boolean finish() {
		// get flag
		boolean bFlag = false; 
		// is changing
		setChangeable(false);
		try {
			// any change?
			if(super.finish()) {
				// get layers
				List<IFeatureLayer> list = getSnapToLayers();
				// apply snapping?
				if(adapter!=null) adapter.setSnapToLayers(list);
				// changes
				bFlag = true;
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// is finished
		setChangeable(true);
		// finished
		return bFlag;
	}
	
	@Override
	public boolean cancel() {
		// any change?
		if(super.cancel()) {
			// reload from adapter
			onSnapableChanged();
			onSnapToChanged();
			// finished
			return true;
		}
		// failure
		return false;
	}
	
	private DefaultDiskoPanel getLayersPanel() {
		if (layersPanel == null) {
			try {
				// get body component
				layersPanel = new DefaultDiskoPanel("Snap til lag",false,false);
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
	
	
	private DefaultDiskoPanel getTolerancePanel() {
		if (tolerancePanel == null) {
			try {
				tolerancePanel = new DefaultDiskoPanel("Sett toleranse (meter)",false,false);
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
	
	private String getLayerCaption(String name)
	{	   
		// search for mso layers
		for(int i=0;i<myInterests.size();i++){
			if(myInterests.containsKey(name)){
				return DiskoEnumFactory.getText("LayerCode."+name+".text",null); 
			}		   
		}	   
		// remove any suffix	   
		int pos = name.indexOf("_");
		if(pos>0 && pos<name.length()-1) {
			return name.substring(pos+1);
		}	   
		return name;
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
					cb = createCheckBox(name);
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
		if(isChangeable()) {
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
						cb = createCheckBox(name);
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
			setDirty(true);
		}
	}
	
	private JCheckBox createCheckBox(String name) {
		JCheckBox cb = new JCheckBox();
		cb.setName(name);
		cb.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setDirty(true);
			}
		}) ;
		return cb;
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
		// reset changes
		setDirty(false);
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
