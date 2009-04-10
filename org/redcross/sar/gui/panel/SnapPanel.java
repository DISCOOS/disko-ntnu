package org.redcross.sar.gui.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.tool.SnapAdapter;
import org.redcross.sar.map.tool.SnapAdapter.SnapListener;
import org.redcross.sar.util.Utils;

import com.borland.jbcl.layout.VerticalFlowLayout;

import com.esri.arcgis.carto.IFeatureLayer;
import com.esri.arcgis.interop.AutomationException;

public class SnapPanel extends TogglePanel implements SnapListener {

	private static final long serialVersionUID = 1L;

	private DefaultPanel layersPanel;
	private TogglePanel tolerancePanel;
	private JSlider toleranceSlider;
	private JPanel messagePanel;
	private JLabel messageLabel;
	private JPanel selectorPanel;
	private JButton allButton;
	private JButton noneButton;
	private HashMap<String,JCheckBox> checkBoxes;
	private HashMap<String,IFeatureLayer> layers;
	private HashMap<String,String> myInterests;
	private SnapAdapter adapter;

	public SnapPanel() {

		// forward
		super("Snapping",true,true);

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

		// initialize GUI
		initialize();

	}

	private void initialize() {
		try {

			// prepare
			setContainerBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			setContainerLayout(new BoxLayout(getContainer(),BoxLayout.Y_AXIS));

			// build container
			addToContainer(getTolerancePanel());
			addToContainer(Box.createVerticalStrut(5));
			addToContainer(getLayersPanel());

			// set toggle limits
			setToggleLimits(280,true);

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
		// any change?
		if(super.finish()) {
			// is changing
			setChangeable(false);
			try {
				// get layers
				List<IFeatureLayer> list = getSnapToLayers();
				// apply snapping?
				if(adapter!=null) adapter.setSnapToLayers(list);
				// changes
				bFlag = true;
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// is finished
			setChangeable(true);
		}
		// notify?
		if(bFlag) onSnapToChanged();
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

	private DefaultPanel getLayersPanel() {
		if (layersPanel == null) {
			try {
				// get body component
				layersPanel = new DefaultPanel("Snap til lag",false,false,ButtonSize.SMALL);
				layersPanel.setPreferredExpandedHeight(450);
				layersPanel.setScrollBarPolicies(DefaultPanel.VERTICAL_SCROLLBAR_AS_NEEDED, DefaultPanel.HORIZONTAL_SCROLLBAR_NEVER);
				layersPanel.insertButton("finish",getNoneButton(), "none");
				layersPanel.insertButton("finish",getAllButton(), "all");
				layersPanel.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						String cmd = e.getActionCommand();
						// canceled?
						if("all".equalsIgnoreCase(cmd))
							checkAll();
						else if("none".equalsIgnoreCase(cmd))
							checkNone();
					}

				});

				// build container
				layersPanel.setContainerLayout(new BoxLayout(layersPanel.getContainer(),BoxLayout.Y_AXIS));
				layersPanel.addToContainer(getMessagePanel());
				layersPanel.addToContainer(getSelectorPanel());

			} catch (java.lang.Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return layersPanel;
	}

	private JPanel getMessagePanel() {
		if(messagePanel==null) {
			messagePanel = new JPanel();
			messagePanel.setLayout(new BorderLayout());
			messagePanel.setPreferredSize(new Dimension(230,100));
			messagePanel.add(getMessageLabel(),BorderLayout.CENTER);
		}
		return messagePanel;
	}

	private JLabel getMessageLabel() {
		if(messageLabel==null) {
			messageLabel = new JLabel(getMessage());
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
			selectorPanel.setPreferredSize(new Dimension(230,100));
			selectorPanel.setVisible(false);
			VerticalFlowLayout vfl = new VerticalFlowLayout();
			vfl.setAlignment(VerticalFlowLayout.TOP);
			selectorPanel.setLayout(vfl);

		}
		return selectorPanel;
	}


	private TogglePanel getTolerancePanel() {
		if (tolerancePanel == null) {
			try {

				// create panel
				tolerancePanel = new TogglePanel("Sett toleranse (meter)",false,false);
				tolerancePanel.setExpanded(false);
				Utils.setFixedHeight(tolerancePanel,100);
				tolerancePanel.setPreferredExpandedHeight(100);
				tolerancePanel.setContainer(getToleranceSlider());
				tolerancePanel.addToggleListener(toggleListener);

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
		for(JCheckBox it : checkBoxes.values()) {
			it.setVisible(false);
		}
	}

	private void checkAll() {
		for(JCheckBox it : checkBoxes.values()) {
			it.setSelected(true);
		}
	}

	private void checkNone() {
		for(JCheckBox it : checkBoxes.values()) {
			it.setSelected(false);
		}
	}

	private String getLayerCaption(String name)
	{
		// search for MSO layers
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

	public void setSnapableLayers(List<IFeatureLayer> list) {
		try {
			// initialize
			hideAll();
			layers.clear();
			JCheckBox cb = null;
			int h = 0;
			// get selector panel
			JPanel selector = getSelectorPanel();
			// loop over all
			for (int i = 0; i < list.size(); i++) {
				IFeatureLayer flayer = (IFeatureLayer)list.get(i);
				String name = flayer.getName();
				cb = getCheckBox(name);
				if (cb == null) {
					cb = createCheckBox(name);
				}
				// update text
				cb.setText(getLayerCaption(name));
				// put combobox
				checkBoxes.put(name,cb);
				// put layer
				layers.put(name,flayer);
				// show box
				cb.setVisible(true);
				// add to height
				h += cb.getPreferredSize().height;
			}

			// sort checkboxes on captions
			sortOnCaptions();

			// limit height
			h = Math.max(h,100);

			// set preferred size
			selector.setPreferredSize(new Dimension(230,h));

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
			// initialize
			List<IFeatureLayer> snapTo = null;
			// get list?
			if(adapter!=null) snapTo = adapter.getSnapToLayers();
			// has no layers?
			if(snapTo==null) return;
			// initialize height change
			int dy = 0;
			// update selection
			for(int i=0;i<snapTo.size();i++) {
				IFeatureLayer flayer = snapTo.get(i);
				try {
					String name = flayer.getName();
					JCheckBox cb = getCheckBox(name);
					if (cb == null) {
						cb = createCheckBox(name);
						// update text
						cb.setText(getLayerCaption(name));
						// put combobox
						checkBoxes.put(name,cb);
						// put layer
						layers.put(name,flayer);
						// show box
						cb.setVisible(true);
						// add height
						dy += cb.getPreferredSize().height;
					}
					cb.setSelected(true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// sort checkboxes on captions
			sortOnCaptions();

			// forward
			setDirty(true);
		}
	}

	private void sortOnCaptions() {
		// get selector panel
		JPanel selector = getSelectorPanel();
		// initialize
		selector.removeAll();
		// get checkbox array
		List<JCheckBox> list = new ArrayList<JCheckBox>(checkBoxes.values());
		// sort checkboxes
		Collections.sort(list, comparator);
		// add sorted checkboxes
		for(JCheckBox it : list) {
			selector.add(it);
		}
	}

	private JCheckBox createCheckBox(String name) {
		JCheckBox cb = new JCheckBox();
		cb.setName(name);
		cb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!isChangeable()) return;
				setDirty(true);
			}
		}) ;
		return cb;
	}

	public void onSnapableChanged() {
		// get flag
		boolean bFlag = adapter!=null ? adapter.isSnappingAllowed() : false;
		// update
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
		// update snapable layers?
		if(adapter!=null) {
			setSnapableLayers(adapter.getSnapableLayers());
		}
		// get card layout
		showSelector(bFlag);
		// reset changes
		setDirty(false);
	}

	private void showSelector(boolean isVisible) {

		// any change?
		if(getSelectorPanel().isVisible()!=isVisible) {

			// set selector status
			getSelectorPanel().setVisible(isVisible);

			// set message status
			getMessagePanel().setVisible(!isVisible);

		}

		// update message?
		if(!isVisible) getMessageLabel().setText(getMessage());

	}

	private String getMessage() {

		return "<html><center>Snapping er kun mulig på <br> kartskala mindre enn 1:"
				+ ((adapter!=null) ? (int)adapter.getMaxSnapScale() : "<nothing>")
				+ "</center></html>";

	}

	private final Comparator<JCheckBox> comparator = new Comparator<JCheckBox>() {

		@Override
		public int compare(JCheckBox o1, JCheckBox o2) {
			return o1.getText().compareTo(o2.getText());
		}

	};


}
