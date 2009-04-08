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
import org.redcross.sar.map.LayerModel;
import org.redcross.sar.map.MapLayerModel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.MsoLayerModel;
import org.redcross.sar.map.WmsLayerModel;
import org.redcross.sar.map.layer.IMapLayer.LayerCode;
import org.redcross.sar.util.Utils;

import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.interop.AutomationException;

public class TocPanel extends TogglePanel {

	private static final long serialVersionUID = 1L;

	private JButton allButton;
	private JButton noneButton;

	private TogglePanel msoLayersPanel;
	private TogglePanel wmsLayersPanel;
	private TogglePanel mapLayersPanel;

	private MsoLayerModel msoLayerModel;
	private WmsLayerModel wmsLayerModel;
	private MapLayerModel mapLayerModel;

	private IDiskoMap map;

	private Map<String,String> myInterests;
	private Map<String,JCheckBox> checkBoxes;
	private Map<String,ILayer> layers;
	private Map<String,LayerModel> models;

	public TocPanel() {

		// forward
		super("Vis kartlag",true,true,ButtonSize.SMALL);

		// prepare
		layers = new HashMap<String,ILayer>();
		models = new HashMap<String,LayerModel>();
		checkBoxes = new HashMap<String,JCheckBox>();
		myInterests = new HashMap<String,String>();
		myInterests.put(LayerCode.OPERATION_AREA_LAYER.name(),LayerCode.OPERATION_AREA_LAYER.name());
		myInterests.put(LayerCode.OPERATION_AREA_MASK_LAYER.name(),LayerCode.OPERATION_AREA_MASK_LAYER.name());
		myInterests.put(LayerCode.SEARCH_AREA_LAYER.name(),LayerCode.SEARCH_AREA_LAYER.name());
		myInterests.put(LayerCode.ROUTE_LAYER.name(),LayerCode.ROUTE_LAYER.name());
		myInterests.put(LayerCode.FLANK_LAYER.name(),LayerCode.FLANK_LAYER.name());
		myInterests.put(LayerCode.POI_LAYER.name(),LayerCode.POI_LAYER.name());
		myInterests.put(LayerCode.UNIT_LAYER.name(),LayerCode.UNIT_LAYER.name());

		// initialize GUI
		initialize();

	}

	private void initialize() {
		try {
			// allow vertical scroll bar
			setScrollBarPolicies(
					VERTICAL_SCROLLBAR_AS_NEEDED,
					HORIZONTAL_SCROLLBAR_NEVER);

			// set border
			setContainerBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			// set layout
			setContainerLayout(new BoxLayout(getContainer(),BoxLayout.Y_AXIS));

			// add content
			addToContainer(getMsoLayersPanel());
			addToContainer(Box.createVerticalStrut(5));
			addToContainer(getWmsLayersPanel());
			addToContainer(Box.createVerticalStrut(5));
			addToContainer(getMapLayersPanel());

			// set toggle limits
			setToggleLimits(280,minimumCollapsedHeight,true);

		}
		catch (java.lang.Throwable e) {
			//  Do Something
			e.printStackTrace();
		}
	}

	private TogglePanel getMsoLayersPanel() {
		if (msoLayersPanel == null) {
			try {
				msoLayersPanel = new TogglePanel("Aksjon",false,false);
				msoLayersPanel.setPreferredExpandedHeight(240);
				Utils.setFixedHeight(msoLayersPanel, 240);
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
				msoLayersPanel.addToggleListener(toggleListener);

				// prepare container
				JPanel panel = (JPanel)msoLayersPanel.getContainer();
				panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

			} catch (java.lang.Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return msoLayersPanel;
	}

	private TogglePanel getWmsLayersPanel() {
		if (wmsLayersPanel == null) {
			try {
				wmsLayersPanel = new TogglePanel("Tillegg",false,false);
				wmsLayersPanel.setExpanded(false);
				wmsLayersPanel.setPreferredExpandedHeight(100);
				wmsLayersPanel.addToggleListener(toggleListener);

				// prepare container
				JPanel panel = (JPanel)wmsLayersPanel.getContainer();
				panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

			} catch (java.lang.Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return wmsLayersPanel;
	}

	private TogglePanel getMapLayersPanel() {
		if (mapLayersPanel == null) {
			try {
				mapLayersPanel = new TogglePanel("Kart",false,false);
				mapLayersPanel.setExpanded(false);
				mapLayersPanel.setPreferredExpandedHeight(100);
				mapLayersPanel.addToggleListener(toggleListener);

				// prepare container
				JPanel panel = (JPanel)mapLayersPanel.getContainer();
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
			allButton = DiskoButtonFactory.createButton("GENERAL.ALL",ButtonSize.SMALL);
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
			noneButton = DiskoButtonFactory.createButton("GENERAL.NONE",ButtonSize.SMALL);
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
				this.msoLayerModel = map.getMsoLayerModel();
				this.mapLayerModel = map.getMapLayerModel();
				this.wmsLayerModel = map.getWmsLayerModel();

				// load models into panel
				load((JPanel)getMsoLayersPanel().getContainer(),msoLayerModel);
				load((JPanel)getWmsLayersPanel().getContainer(),wmsLayerModel);
				load((JPanel)getMapLayersPanel().getContainer(),mapLayerModel);

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

	private void load(JPanel panel, final LayerModel model) {

		try {

			// synchronize current selections with visible state
			model.synchronize();

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
				cb.setSelected(model.isIncluded(i));
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
		((JPanel)getMsoLayersPanel().getContainer()).removeAll();
		((JPanel)getWmsLayersPanel().getContainer()).removeAll();
		((JPanel)getMapLayersPanel().getContainer()).removeAll();
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
		LayerModel model = models.get(cb.getName());
		int i = model.findIndex(layers.get(cb.getName()));
		if(i!=-1) model.setIncluded(i, cb.isSelected());
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
