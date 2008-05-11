package org.redcross.sar.gui.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.event.MsoLayerEvent;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.DefaultMapLayerSelectionModel;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.MsoLayerSelectionModel;
import org.redcross.sar.map.WMSLayerSelectionModel;
import org.redcross.sar.map.layer.IMsoFeatureLayer.LayerCode;

import com.borland.jbcl.layout.VerticalFlowLayout;
import com.esri.arcgis.beans.map.MapBean;
import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.carto.IFeatureLayer;
import com.esri.arcgis.carto.WMSMapLayer;
import com.esri.arcgis.controls.IMapControlEvents2Adapter;
import com.esri.arcgis.controls.IMapControlEvents2OnMapReplacedEvent;
import com.esri.arcgis.interop.AutomationException;

public class TocDialog extends DiskoDialog {
	
	private static final long serialVersionUID = 1L;
	private JPanel mainPanel = null;
	private JPanel contentPanel = null;
	private JPanel layerSelectionPanel = null;
	private JPanel buttonPanel = null;
	private JButton selectAllButton = null;
	private JButton selectNoneButton = null;
	private JButton selectApplyButton = null;
	private JScrollPane scrollPane = null;
	private MsoLayerSelectionModel msoLayerSelectionModel = null;  //  @jve:decl-index=0:
	private DefaultMapLayerSelectionModel defaultMapLayerSelectionModel = null;
	private WMSLayerSelectionModel wmsLayerSelectionModel = null;
	private JLabel labelMsoLayers = null;
	private JLabel labelWMSLayers = null;
	private JLabel labelDefMapLayers = null;
	private IDiskoMap map = null;
	private HashMap<String,String> myInterests = null;
	private HashMap<String,JCheckBox> checkBoxes = null;
	private HashMap<JCheckBox,ILayer> layers = null;
	
	public TocDialog(IDiskoApplication app) {
		super(app.getFrame());
		myInterests = new HashMap<String,String>();
		myInterests.put(LayerCode.OPERATION_AREA_LAYER.name(),LayerCode.OPERATION_AREA_LAYER.name());
		myInterests.put(LayerCode.OPERATION_AREA_MASK_LAYER.name(),LayerCode.OPERATION_AREA_MASK_LAYER.name());
		myInterests.put(LayerCode.SEARCH_AREA_LAYER.name(),LayerCode.SEARCH_AREA_LAYER.name());
		myInterests.put(LayerCode.ROUTE_LAYER.name(),LayerCode.ROUTE_LAYER.name());
		myInterests.put(LayerCode.FLANK_LAYER.name(),LayerCode.FLANK_LAYER.name());
		myInterests.put(LayerCode.POI_LAYER.name(),LayerCode.POI_LAYER.name());
		myInterests.put(LayerCode.UNIT_LAYER.name(),LayerCode.UNIT_LAYER.name());
		checkBoxes = new HashMap<String,JCheckBox>();
		layers = new HashMap<JCheckBox,ILayer>();		
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		try {
			
            this.setContentPane(getContentPanel());
            this.setPreferredSize(new Dimension(220, 400));
            this.pack();
		}
		catch (java.lang.Throwable e) {
			e.printStackTrace();
		}
	}
	
	public void onLoad(IDiskoMap map) throws IOException {
		this.map = map;
		this.msoLayerSelectionModel = map.getMsoLayerSelectionModel();
		this.defaultMapLayerSelectionModel = map.getDefaultMapLayerSelectionModel();
		this.wmsLayerSelectionModel = map.getWMSLayerSelectionModel();
		
		//listen to do actions when the map is loaded
		((MapBean)map).addIMapControlEvents2Listener(new IMapControlEvents2Adapter() {
			private static final long serialVersionUID = 1L;
			public void onMapReplaced(IMapControlEvents2OnMapReplacedEvent e)
                   	throws java.io.IOException, AutomationException {
				update();
			}
		});
		updateLayerSelection(map);
	}
	
	private void update() throws IOException, AutomationException{		
		//Må oppdaterer kartlagsliste
		
		//MsoLayerSelectionModel er den samme
		map.setMsoLayerSelectionModel();
		this.msoLayerSelectionModel = map.getMsoLayerSelectionModel();

		//DefaultMapLayerSelectionModel må oppdateres		
		map.setDefaultMapLayerSelectionModel();
		this.defaultMapLayerSelectionModel = map.getDefaultMapLayerSelectionModel();
		
		//WMSLayerSelectionModel må oppdateres
		map.setWMSLayerSelectionModel();
		this.wmsLayerSelectionModel = map.getWMSLayerSelectionModel();
		
		//todo - må oppdatere snapLayerModell og flankLayerModell også 
		
		updateLayerSelection(map);

		apply();
		
		this.layerSelectionPanel.updateUI();
			
	}
	
	/**
	 * This method initializes contentPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getContentPanel() {
		if (contentPanel == null) {			
			try {
				BorderLayout borderLayout = new BorderLayout();
				borderLayout.setVgap(5);
				contentPanel = new JPanel();
				contentPanel.setLayout(borderLayout);
				contentPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				contentPanel.add(getMainPanel(), BorderLayout.CENTER);

			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}			
		}
		return contentPanel;
	}
	
	/**
	 * This method initializes mainPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getMainPanel() {
		if (mainPanel == null) {			
			try {
				BorderLayout bl = new BorderLayout();
				mainPanel = new JPanel();
				mainPanel.setLayout(bl);
				mainPanel.setBorder(BorderFactory.createTitledBorder(null, 
						"Endre kartvisning", TitledBorder.LEFT, TitledBorder.TOP, 
						new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
				mainPanel.add(getScrollPane(), BorderLayout.CENTER);
				mainPanel.add(getButtonPanel(), BorderLayout.SOUTH);

			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}			
		}
		return mainPanel;
	}
	
	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			try {
				scrollPane = new JScrollPane(getLayerPanel());
				scrollPane.setBorder(null);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return scrollPane;
	}
	
	private JPanel getLayerPanel() {
		if (layerSelectionPanel == null) {
			try {						
							
				layerSelectionPanel = new JPanel();
				VerticalFlowLayout vfl = new VerticalFlowLayout();
				vfl.setVgap(0);
				vfl.setHgap(0);
				vfl.setAlignment(VerticalFlowLayout.TOP);
				layerSelectionPanel.setLayout(vfl);
			} catch (java.lang.Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return layerSelectionPanel;
	}
	
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			try {
				
				// create layer
				buttonPanel = new JPanel();
				buttonPanel.setLayout(new FlowLayout());
				// create buttons
				selectAllButton = DiskoButtonFactory.createButton("GENERAL.ALL",ButtonSize.NORMAL);
				selectNoneButton = DiskoButtonFactory.createButton("GENERAL.NONE",ButtonSize.NORMAL);
				selectApplyButton = DiskoButtonFactory.createButton("GENERAL.APPLY",ButtonSize.NORMAL);
				// add action listeners
				selectAllButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						checkAll();
					}
				});
				selectNoneButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						checkNone();
					}
				});
				selectApplyButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						// apply selections
						apply();
					}
				});
				// add buttons to panel
				buttonPanel.add(selectAllButton);
				buttonPanel.add(selectNoneButton);
				buttonPanel.add(selectApplyButton);
				
			} catch (java.lang.Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return buttonPanel;
	}
	
	private void updateLayerSelection(IDiskoMap map) {
		//adding checkboxes
		try {
			
			// hide all current checkboxes
			clearAll();
			
			// add label for mso labels
			labelMsoLayers = new JLabel();
			labelMsoLayers.setText("Mso kartlag");
			labelMsoLayers.setFont(new Font("Dialog", Font.BOLD, 14));
			labelMsoLayers.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			getLayerPanel().add(labelMsoLayers);						
						
			// add checkboxes for MSO layers
			for (int i = 0; i < msoLayerSelectionModel.getLayerCount(); i++) {
				IFeatureLayer flayer = msoLayerSelectionModel.getFeatureLayer(i);				
				JCheckBox cb = getCheckBox(flayer.getName());				
				if (cb == null) {
					cb = new JCheckBox(getLayerCaption(flayer.getName()));
					checkBoxes.put(flayer.getName(),cb);
					cb.setSelected(msoLayerSelectionModel.isSelected(i));
				}
				layers.put(cb,flayer);
				getLayerPanel().add(cb);	
				cb.setVisible(true);
			}		
			
			//adding checkboxes for WMS layers
			if(wmsLayerSelectionModel.getLayerCount() > 0){
				// add separator
				JSeparator jSep = new JSeparator();
				getLayerPanel().add(jSep);
				
				// add label
				labelWMSLayers = new JLabel();
				labelWMSLayers.setText("WMS kartlag");
				labelWMSLayers.setFont(new Font("Dialog", Font.BOLD, 14));
				labelWMSLayers.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
				getLayerPanel().add(labelWMSLayers);
				
				// add wms layers
				for (int i = 0; i < wmsLayerSelectionModel.getLayerCount(); i++) {
					WMSMapLayer wmslayer = (WMSMapLayer) wmsLayerSelectionModel.getFeatureLayer(i);
					JCheckBox cb = getCheckBox(wmslayer.getName());				
					if (cb == null) {
						cb = new JCheckBox(getLayerCaption(wmslayer.getName()));
						checkBoxes.put(wmslayer.getName(),cb);
						cb.setSelected(wmsLayerSelectionModel.isSelected(i));
					}
					layers.put(cb,wmslayer);
					getLayerPanel().add(cb);	
					cb.setVisible(true);
				}	
			}
			
			//adding one checkbox for default mapdatalayers
			if (defaultMapLayerSelectionModel.getLayerCount() > 0){
				
				// add separator
				JSeparator jSep2 = new JSeparator();
				getLayerPanel().add(jSep2);
				
				// add label
				labelDefMapLayers = new JLabel();
				labelDefMapLayers.setText("Grunnkart");
				labelDefMapLayers.setFont(new Font("Dialog", Font.BOLD, 14));
				getLayerPanel().add(labelDefMapLayers);
				
				JCheckBox cb = getCheckBox("Vis alle lag");				
				if (cb == null) {
					cb = new JCheckBox("Vis alle lag");
					checkBoxes.put("Vis alle lag",cb);
					cb.setSelected(true);
				}
				layers.put(cb,null);
				getLayerPanel().add(cb);	
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

	private JCheckBox getCheckBox(String name) {
		if(checkBoxes.containsKey(name)) {
			return checkBoxes.get(name);
		}
		return null;
	}

	private void clearAll() {
		// clear lists
		layers.clear();
		getLayerPanel().removeAll();		
		// get key iterator
		Iterator it = checkBoxes.keySet().iterator();
		// loop over all
		while(it.hasNext()){
		    checkBoxes.get(it.next()).setVisible(false);
		}
	}
	
	private void checkAll() {
		// get key iterator
		Iterator it = checkBoxes.keySet().iterator();
		// loop over all
		while(it.hasNext()){
		    checkBoxes.get(it.next()).setSelected(true);
		}
	}
	
	private void checkNone() {
		// get key iterator
		Iterator it = checkBoxes.keySet().iterator();
		// loop over all
		while(it.hasNext()){
		    checkBoxes.get(it.next()).setSelected(false);
		}
	}
	
	private void apply() {
		int count = 0;
		
		try{
			// hide me
			setVisible(false);
			// prevent redraw
			map.setSupressDrawing(true);
			map.suspendNotify();
			// get key iterator
			Iterator it = checkBoxes.keySet().iterator();
			// loop over all
			while(it.hasNext()){
			    JCheckBox cb = checkBoxes.get(it.next());
			    ILayer layer = layers.get(cb);
			    if(layer!=null) {
				    if(!cb.isSelected() && layer.isVisible()){
				    	layer.setVisible(false);
				    	count++;
				    }
				    if(cb.isSelected() && !layer.isVisible()){
				    	layer.setVisible(true);
				    	count++;
				    }
			    }
			    else {
			    	// setr base map
			    	defaultMapLayerSelectionModel.setAllLayerVisibility(cb.isSelected());
			    }
			}
			// resume
			map.setSupressDrawing(false);
			map.resumeNotify();
			// refresh map?
			if(count>0) {
				((DiskoMap)map).refreshMsoLayers();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
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
   
	public void onLayerChanged(MsoLayerEvent e) throws IOException, AutomationException {
		// TODO Auto-generated method stub
	}
}
