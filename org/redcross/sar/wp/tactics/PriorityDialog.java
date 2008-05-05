package org.redcross.sar.wp.tactics;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;

import javax.swing.AbstractButton;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.IMsoLayerEventListener;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.DiskoPanel;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.renderers.RadioListCellRenderer;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchAreaIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.wp.IDiskoWpModule;

public class PriorityDialog extends DiskoDialog implements IMsoLayerEventListener{

	private static final long serialVersionUID = 1L;
	private DiskoPanel contentPanel = null;
	private JList priorityList = null;
	
	public PriorityDialog(IDiskoWpModule wp) {
		// forward
		super(wp.getApplication().getFrame(),wp.getMap(),getMyInterest(),getMyLayers());
		// initialize ui
		initialize();
		// get selected mso feature
		setSelectedMsoFeature(wp.getMap());
		// forward
		setup();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		try {
            this.setContentPane(getContentPanel());
            this.setPreferredSize(new Dimension(300, 400));
            this.pack();
		}
		catch (java.lang.Throwable e) {
			//  Do Something
		}
	}
	
	private static EnumSet<IMsoManagerIf.MsoClassCode> getMyInterest() {
		EnumSet<IMsoManagerIf.MsoClassCode> myInterests 
			= EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_SEARCHAREA);
		return myInterests;
	}

	private static EnumSet<IMsoFeatureLayer.LayerCode> getMyLayers() {	
		EnumSet<IMsoFeatureLayer.LayerCode> myLayers 
			= EnumSet.of(IMsoFeatureLayer.LayerCode.SEARCH_AREA_LAYER);
	    return myLayers;
	}

	private int getPriority() {
		return getPriorityList().getSelectedIndex()+1;
	}
	
	private void setPriority(int priority, boolean gui, boolean mso) {
		setIsWorking();
		// update gui?
		if(gui) {
			getPriorityList().setSelectedIndex(priority-1);
		}
		// update mso? (list is updated from Mso Update event)
		if(mso) {
			// dispatch type
			if(currentMsoObj instanceof ISearchAreaIf) {
				// cast to ISearchAreaIf
				ISearchAreaIf searchArea = (ISearchAreaIf)currentMsoObj;
				// update?
				if(searchArea.getPriority()!=priority-1) {
					searchArea.setPriority(priority-1);
				}
				fireOnWorkChange(priorityList,currentMsoObj,priority-1);
			}
		}
		setIsNotWorking();
	}
	
	/**
	 * This method initializes contentPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private DiskoPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				contentPanel = new DiskoPanel();
				contentPanel.setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
				AbstractButton button = contentPanel.addButton(
						DiskoButtonFactory.createButton("GENERAL.CANCEL", ButtonSize.NORMAL),"cancel");
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// hide me!
						setVisible(false);						
					}					
				});
				contentPanel.setBodyComponent(getPriorityList());
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return contentPanel;
	}

	/**
	 * This method initializes priorityList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getPriorityList() {
		if (priorityList == null) {
			try {
				priorityList = new JList();
				priorityList.setCellRenderer(new RadioListCellRenderer());
				Object[] listData = new Object[5];
				listData[0] = Utils.translate("PRIMARY_SEARCH_AREA");
				listData[1] = Utils.translate("SECONDARY_SEARCH_AREA");
				listData[2] = Utils.translate("PRIORITY3_SEARCH_AREA");
				listData[3] = Utils.translate("PRIORITY4_SEARCH_AREA");
				listData[4] = Utils.translate("PRIORITY5_SEARCH_AREA");
				
				priorityList.setListData(listData);
				priorityList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				priorityList.setSelectedIndex(0);
				priorityList.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if(e.getValueIsAdjusting() || isWorking()) {
							return;
						}
						// update mso model
						setPriority(getPriority(), false, true);						
						// hide me and notify change
						setVisible(false);
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return priorityList;
	}

	@Override
	public int setMsoObject(IMsoObjectIf msoObj) {
		int state = 0;
		if(isWorking()) return state;		
		// dispatch type
		if (msoObj instanceof ISearchAreaIf) {
			ISearchAreaIf searchArea = (ISearchAreaIf)msoObj;
			setPriority(searchArea.getPriority(),true,false);
			state = 1;
		}
		else {
			// reset to default priority
			setPriority(1,true,false);
			state = -1;
		}
		// forward
		setup();
		// not selected
		return state;
	}
	
	@Override
	public void msoObjectChanged(IMsoObjectIf msoObject, int mask) {
		if(isWorking()) return;
		// is same as selected?
		if(msoObject == currentMsoObj) {
			setMsoObject(msoObject);
		}
	}

	@Override
	public void msoObjectDeleted(IMsoObjectIf msoObject, int mask) {
		if(isWorking()) return;
		// is same as selected?
		if(msoObject == currentMsoObj) {
			// reset selection
			currentMsoFeature =null;
			currentMsoObj =null;
			setPriority(1,true,false);
		}
	}	
	
	private void setup() {
		// update icon
		if(currentMsoObj!=null) {
			Enum e = MsoUtils.getType(currentMsoObj,true);
			getContentPanel().setCaptionIcon(Utils.getEnumIcon(e,"48x48"));			
			getContentPanel().setCaptionText("<html>Oppgi prioritet for <b>" + 
					MsoUtils.getMsoObjectName(currentMsoObj,0).toLowerCase() + "</b></html>");
			getPriorityList().setEnabled(true);
		}
		else {
			getContentPanel().setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
			getContentPanel().setCaptionText("Du må først velge et søkeområde");			
			getPriorityList().setEnabled(false);
		}		
	}	
	
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
