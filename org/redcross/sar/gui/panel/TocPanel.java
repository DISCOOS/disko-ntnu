package org.redcross.sar.gui.panel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.LayerSelectionModel;
import org.redcross.sar.map.MapLayerSelectionModel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.MsoLayerSelectionModel;
import org.redcross.sar.map.WmsLayerSelectionModel;
import org.redcross.sar.map.layer.IMsoFeatureLayer.LayerCode;

import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.interop.AutomationException;

public class TocPanel extends DefaultPanel {
	
	private static final long serialVersionUID = 1L;
	
	private JButton allButton = null;
	private JButton noneButton = null;
	
	private DefaultPanel msoLayersPanel = null;
	private DefaultPanel wmsLayersPanel = null;
	private DefaultPanel mapLayersPanel = null;

	private MsoLayerSelectionModel msoLayerSelectionModel = null; 
	private WmsLayerSelectionModel wmsLayerSelectionModel = null;
	private MapLayerSelectionModel mapLayerSelectionModel = null;

	private IDiskoMap map = null;

	private Map<String,String> myInterests = null;
	private Map<String,JCheckBox> checkBoxes = null;
	private Map<String,ILayer> layers = null;
	private Map<String,LayerSelectionModel> models = null;
	
	public TocPanel() {
		
		// forward
		super("Vis kartlag");
		
		// prepare
		layers = new HashMap<String,ILayer>();
		models = new HashMap<String,LayerSelectionModel>();
		checkBoxes = new HashMap<String,JCheckBox>();
		myInterests = new HashMap<String,String>();
		myInterests.put(LayerCode.OPERATION_AREA_LAYER.name(),LayerCode.OPERATION_AREA_LAYER.name());
		myInterests.put(LayerCode.OPERATION_AREA_MASK_LAYER.name(),LayerCode.OPERATION_AREA_MASK_LAYER.name());
		myInterests.put(LayerCode.SEARCH_AREA_LAYER.name(),LayerCode.SEARCH_AREA_LAYER.name());
		myInterests.put(LayerCode.ROUTE_LAYER.name(),LayerCode.ROUTE_LAYER.name());
		myInterests.put(LayerCode.FLANK_LAYER.name(),LayerCode.FLANK_LAYER.name());
		myInterests.put(LayerCode.POI_LAYER.name(),LayerCode.POI_LAYER.name());
		myInterests.put(LayerCode.UNIT_LAYER.name(),LayerCode.UNIT_LAYER.name());
		
		// initialize gui
		initialize();
		
	}
	
	private void initialize() {
		try {
			
			// create content
			JPanel body = (JPanel)getBodyComponent();
			body.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			body.setPreferredSize(new Dimension(200,550));
			body.setLayout(new BoxLayout(body,BoxLayout.Y_AXIS));
			body.add(getMsoLayersPanel());
			body.add(Box.createVerticalStrut(5));
			body.add(getWmsLayersPanel());
			body.add(Box.createVerticalStrut(5));
			body.add(getMapLayersPanel());

		}
		catch (java.lang.Throwable e) {
			//  Do Something
			e.printStackTrace();
		}
	}
		
	private DefaultPanel getMsoLayersPanel() {
		if (msoLayersPanel == null) {
			try {
				msoLayersPanel = new DefaultPanel("Aksjon",false,false);
				msoLayersPanel.insertButton("finish",getNoneButton(), "none");
				msoLayersPanel.insertButton("finish",getAllButton(), "all");
				msoLayersPanel.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						String cmd = e.getActionCommand();
						// canceled?
						if("all".equalsIgnoreCase(cmd)) 
							checkAll();
						else if("none".equalsIgnoreCase(cmd)) 
							checkNone();					
					}
					
				});
				
				JPanel panel = (JPanel)msoLayersPanel.getBodyComponent();
				panel.setPreferredSize(new Dimension(200, 300));
				panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
				
			} catch (java.lang.Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return msoLayersPanel;
	}
	
	private DefaultPanel getWmsLayersPanel() {
		if (wmsLayersPanel == null) {
			try {
				wmsLayersPanel = new DefaultPanel("Tillegg",false,false);
				JPanel panel = (JPanel)wmsLayersPanel.getBodyComponent();
				panel.setPreferredSize(new Dimension(200, 150));
				panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
				
			} catch (java.lang.Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return wmsLayersPanel;
	}

	private DefaultPanel getMapLayersPanel() {
		if (mapLayersPanel == null) {
			try {
				mapLayersPanel = new DefaultPanel("Kart",false,false);
				JPanel panel = (JPanel)mapLayersPanel.getBodyComponent();
				panel.setPreferredSize(new Dimension(200, 50));
				panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
				
			} catch (java.lang.Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return mapLayersPanel;
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

	public boolean reload() {
		if(map!=null) {
			return load(map);
		}
		return false;
	}
	
	public boolean load(IDiskoMap map) {		
		
		// consume?
		if(!isChangeable()) return false;
		
		// consume
		setChangeable(false);
		
		// initialize
		boolean bFlag = false;
		
		try {

			// set map
			this.map = map;
			
			// hide all current checkboxes
			hideAll();
			
			// has map?
			if(map!=null) {
				
				// get current selection models
				this.msoLayerSelectionModel = map.getMsoLayerSelectionModel();
				this.mapLayerSelectionModel = map.getMapLayerSelectionModel();
				this.wmsLayerSelectionModel = map.getWmsLayerSelectionModel();

				// load models into panel
				load((JPanel)getMsoLayersPanel().getBodyComponent(),msoLayerSelectionModel);
				load((JPanel)getWmsLayersPanel().getBodyComponent(),wmsLayerSelectionModel);
				load((JPanel)getMapLayersPanel().getBodyComponent(),mapLayerSelectionModel);

				// success
				bFlag = true;
				
			}
			
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		// resume changes
		setChangeable(true);
		
		// finished
		return bFlag;
		
	}
	
	private void load(JPanel panel, final LayerSelectionModel model) {

		try {
			
			// synchronize current selections with visible state
			model.update();
			
			// initialize
			JCheckBox cb = null;
			
			// get name
			String name = model.getName();
			
			// add checkboxes to panel
			for (int i = 0; i < model.getLayerCount(); i++) {
				ILayer layer = model.getLayer(i);				
				String key = name + "." + layer.getName(); 
				cb = getCheckBox(key);				
				if (cb == null) {
					cb = new JCheckBox(getLayerCaption(layer.getName()));
					cb.setName(key);
					cb.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							if(!isChangeable()) return;
							setDirty(true);
							JCheckBox cb = (JCheckBox)e.getSource();
							setSelected(cb, cb.isSelected());
						}
						
					});
					checkBoxes.put(key,cb);
				}
				cb.setVisible(true);
				cb.setSelected(model.isSelected(i));
				layers.put(key,layer);
				models.put(key, model);
				panel.add(cb);	
			}
			
			// calculate height
			int h = (cb!=null ? (cb.getHeight() + 5) * model.getLayerCount() - 5: 0);
			
			// set preferred size
			panel.setPreferredSize(new Dimension(panel.getWidth(),h));
						
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private JCheckBox getCheckBox(String key) {
		if(checkBoxes.containsKey(key)) {
			return checkBoxes.get(key);
		}
		return null;
	}

	private void hideAll() {
		// clear lists
		layers.clear();
		// clear
		((JPanel)getMsoLayersPanel().getBodyComponent()).removeAll();
		((JPanel)getWmsLayersPanel().getBodyComponent()).removeAll();
		((JPanel)getMapLayersPanel().getBodyComponent()).removeAll();
		// loop over all
		for(JCheckBox it : checkBoxes.values()){
		    it.setVisible(false);
		}
	}
	
	private void checkAll() {
		// loop over all
		for(String it : checkBoxes.keySet()){
			if(layers.containsKey(it)) {
				// is mso layer?
				if("mso".equals(models.get(it).getName()))
					setSelected(checkBoxes.get(it),true);
			}
		}
		setDirty(true);
	}
	
	private void checkNone() {
		// loop over all
		for(String it : checkBoxes.keySet()){
			if(layers.containsKey(it)) {
				// is mso layer?
				if("mso".equals(models.get(it).getName()))
					setSelected(checkBoxes.get(it),false);
			}
		}
		setDirty(true);
	}
	
	private void setSelected(JCheckBox cb, boolean isSelected) {
		cb.setSelected(isSelected);
		LayerSelectionModel model = models.get(cb.getName());
		int i = model.findIndex(layers.get(cb.getName()));
		if(i!=-1) model.setSelected(i, cb.isSelected());		
	}
	
	public boolean finish() {
		
		// consume?
		if(!isChangeable() || !super.finish()) return false;
		
		// initalize
		boolean bFlag = false;
		
		// consume changes?
		setChangeable(false);
		
		// has map?
		if(map!=null) {
			
			// forward
			bFlag = apply();
				
		}
		
		
		// resume changes
		setChangeable(true);
		
		// finished
		return bFlag;
		
	}
	
	public boolean cancel() {
		// consume?
		if(!isChangeable() || !super.finish()) return false;
		// consume change
		setChangeable(false);
		// initialize
		boolean bFlag = false;
		// load again?
		if(map!=null) {
			bFlag = load(map);
		}
		// resume changes
		setChangeable(true);
		// finished
		return bFlag;		
	}
	
	private boolean apply() {
		
		if(map!=null) {
		
			// execute later
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
	
						// prevent redraw
						map.setSupressDrawing(true);
						map.suspendNotify();
	
						// loop over all check boxes
						for (JCheckBox it : checkBoxes.values()) {
							// get layer
							ILayer layer = layers.get(it.getName());
							// has layer?
							if (layer != null) {
								// hide?
								if (!it.isSelected() && layer.isVisible()) {
									layer.setVisible(false);
								}
								// show?
								else if (it.isSelected() && !layer.isVisible()) {
									layer.setVisible(true);
								}
							}
						}
	
						// resume
						map.setSupressDrawing(false);
						map.refreshGeography(null, map.getExtent());
						map.resumeNotify();
						
						// reset dirty state
						setDirty(false);
						
	
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});		
			return true;
		}
		return false;
		
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
   
}
