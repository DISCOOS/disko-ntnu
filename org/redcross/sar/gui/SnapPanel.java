package org.redcross.sar.gui;

import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;

import org.redcross.sar.app.Utils;
import org.redcross.sar.map.SnappingAdapter;
import org.redcross.sar.map.SnappingAdapter.SnappingListener;
import org.redcross.sar.map.command.IDrawTool;
import org.redcross.sar.map.layer.IMsoFeatureLayer;

import com.borland.jbcl.layout.VerticalFlowLayout;

import com.esri.arcgis.carto.IFeatureLayer;
import com.esri.arcgis.interop.AutomationException;

public class SnapPanel extends JPanel implements SnappingListener {
	
	private static final long serialVersionUID = 1L;
	private IDrawTool tool = null;
	private JPanel snapPanel = null;
	private JPanel snapButtonPanel = null;
	private JPanel snapSelectionPanel = null;
	private JSlider snapToleranceSlider = null;
	private JButton snapAllButton = null;
	private JButton snapNoneButton = null;
	private JButton snapApplyButton = null;
	private JScrollPane scrollPane = null;
	private HashMap<String,JCheckBox> checkBoxes = null;
	private HashMap<String,IFeatureLayer> layers = null;
	private Properties properties = null;
	private HashMap<String,String> myInterests = null;
	private SnappingAdapter snapping = null;
	private boolean isSnapToChangeing = false;
	
	public SnapPanel(SnappingAdapter adapter) {
		// prepare
		snapping = adapter;
		checkBoxes = new HashMap<String,JCheckBox>();
		layers = new HashMap<String,IFeatureLayer>();
		myInterests = new HashMap<String,String>();
		myInterests.put(IMsoFeatureLayer.LayerCode.OPERATION_AREA_LAYER.name(),IMsoFeatureLayer.LayerCode.OPERATION_AREA_LAYER.name());
		myInterests.put(IMsoFeatureLayer.LayerCode.OPERATION_AREA_MASK_LAYER.name(),IMsoFeatureLayer.LayerCode.OPERATION_AREA_MASK_LAYER.name());
		myInterests.put(IMsoFeatureLayer.LayerCode.SEARCH_AREA_LAYER.name(),IMsoFeatureLayer.LayerCode.SEARCH_AREA_LAYER.name());
		myInterests.put(IMsoFeatureLayer.LayerCode.ROUTE_LAYER.name(),IMsoFeatureLayer.LayerCode.ROUTE_LAYER.name());
		myInterests.put(IMsoFeatureLayer.LayerCode.FLANK_LAYER.name(),IMsoFeatureLayer.LayerCode.FLANK_LAYER.name());
		myInterests.put(IMsoFeatureLayer.LayerCode.POI_LAYER.name(),IMsoFeatureLayer.LayerCode.POI_LAYER.name());
		// add listener
		snapping.addSnappingListener(this);
		// initialize gui
		initialize();
	}
	
	private void initialize() {
		try {
			BorderLayout borderLayout = new BorderLayout();
			borderLayout.setVgap(5);
			this.setLayout(borderLayout);
			this.add(getSnapToleranceSlider(), BorderLayout.NORTH);
			this.add(getScrollPane(), BorderLayout.CENTER);
		}
		catch (java.lang.Throwable e) {
			//  Do Something
		}
	}
		
	private JPanel getSnapPanel() {
		if (snapPanel == null) {
			try {
				snapPanel = new JPanel();
				VerticalFlowLayout vfl = new VerticalFlowLayout();
				vfl.setVgap(5);
				vfl.setHgap(0);
				vfl.setAlignment(VerticalFlowLayout.TOP);
				snapPanel.setLayout(vfl);
				snapPanel.setBorder(BorderFactory.createTitledBorder(null, 
						"Snapp til kartlag", TitledBorder.LEFT, TitledBorder.TOP, 
						new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
				snapPanel.add(getSnapSelectionPanel(), BorderLayout.NORTH);
				snapPanel.add(getSnapButtonPanel(), BorderLayout.CENTER);
			} catch (java.lang.Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return snapPanel;
	}
	
	private JPanel getSnapButtonPanel() {
		if (snapButtonPanel == null) {
			try {
				
				// create layer
				snapButtonPanel = new JPanel();
				snapButtonPanel.setLayout(new FlowLayout());
				// create buttons
				snapAllButton = DiskoButtonFactory.createNormalButton("Alle");
				snapNoneButton = DiskoButtonFactory.createNormalButton("Ingen");
				snapApplyButton = DiskoButtonFactory.createNormalButton("Bruk");
				// add action listeners
				snapAllButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						checkAll();
					}
				});
				snapNoneButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						checkNone();
					}
				});
				snapApplyButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						try {
							// is changing
							isSnapToChangeing = true;
							// apply snapping
							snapping.setSnapToLayers(getSnapToLayers());
						} catch (AutomationException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						// finished
						isSnapToChangeing = false;
					}
				});
				// add buttons to panel
				snapButtonPanel.add(snapAllButton);
				snapButtonPanel.add(snapNoneButton);
				snapButtonPanel.add(snapApplyButton);
				
			} catch (java.lang.Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return snapButtonPanel;
	}	
	
	private JPanel getSnapSelectionPanel() {
		if (snapSelectionPanel == null) {
			try {
				snapSelectionPanel = new JPanel();
				VerticalFlowLayout vfl = new VerticalFlowLayout();
				vfl.setVgap(0);
				vfl.setHgap(0);
				vfl.setAlignment(VerticalFlowLayout.TOP);
				snapSelectionPanel.setLayout(vfl);
			} catch (java.lang.Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return snapSelectionPanel;
	}
	
	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			try {
				scrollPane = new JScrollPane(getSnapPanel());
				scrollPane.setBorder(null);
			} catch (java.lang.Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return scrollPane;
	}
	
	private JSlider getSnapToleranceSlider() {
		if (snapToleranceSlider == null) {
			try {
				snapToleranceSlider = new JSlider();
				snapToleranceSlider.setOrientation(JSlider.HORIZONTAL);
				snapToleranceSlider.setMinorTickSpacing(10);
				snapToleranceSlider.setMajorTickSpacing(50);
				snapToleranceSlider.setPaintLabels(true);
				snapToleranceSlider.setPaintTicks(true);
				snapToleranceSlider.setMaximum(250);
				snapToleranceSlider.setBorder(BorderFactory.createTitledBorder(null, 
						"Snapping toleranse", TitledBorder.LEFT, TitledBorder.TOP, 
						new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
				
				snapToleranceSlider.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent e) {
						try {
							// forward
							snapping.setSnapTolerance(snapToleranceSlider.getValue());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				});
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return snapToleranceSlider;
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
		if(snapApplyButton!=null) {
			snapApplyButton.addActionListener(listener);
			return true;
		}
		return false;
	}
	
	public boolean removeApplyListener(ActionListener listener) {
		if(snapApplyButton!=null) {
			snapApplyButton.removeActionListener(listener);
			return true;
		}
		return false;
	}
	
	public IDrawTool getTool() {
		return tool;
	}

	public void setTool(IDrawTool tool) {
		this.tool = tool;
	}
	
	public void setSnapTolerance(int value) {
		snapToleranceSlider.setValue(value);
	}
	
	public List getSnapToLayers() {
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
	
	public void setSnapableLayers(List layers) {
		//adding checkboxes
		try {
			hideAll();
			this.layers.clear();
			JPanel panel = getSnapSelectionPanel();
			for (int i = 0; i < layers.size(); i++) {
				IFeatureLayer flayer = (IFeatureLayer)layers.get(i);
				String name = flayer.getName();
				JCheckBox cb = getCheckBox(name);				
				if (cb == null) {
					cb = new JCheckBox();
					cb.setName(name);
					panel.add(cb);
				}
				// update text
				cb.setText(getLayerCaption(name));
						// put combobox
				checkBoxes.put(name,cb);
				// put layer
				this.layers.put(name,flayer);
				// show box
				cb.setVisible(true);
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	public void onSnapToChange() {
		// is not changing?
		if(!isSnapToChangeing) {
			// unselect all
			checkNone();
			// get panel
			JPanel panel = getSnapSelectionPanel();
			// get list
			List<IFeatureLayer> snapTo = snapping.getSnapToLayers();
			// update selection
			for(int i=0;i<snapTo.size();i++) {
				IFeatureLayer flayer = snapTo.get(i);
				try {
					String name = flayer.getName();
					JCheckBox cb = getCheckBox(name);
					if (cb == null) {
						cb = new JCheckBox();
						cb.setName(name);
						panel.add(cb);
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
				} catch (AutomationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void onSnappableChange() {
		try {
			setSnapTolerance(snapping.getSnapTolerance());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setSnapableLayers(snapping.getSnappableLayers());
		
	}
}
