package org.redcross.sar.wp.tactics;

import java.awt.Dimension;
import java.util.EnumSet;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoStringFactory;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.renderer.RadioButtonListCellRenderer;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchAreaIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.wp.IDiskoWpModule;

public class PriorityDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;
	private DefaultPanel contentPanel = null;
	private JList priorityList = null;
	
	private IDiskoWpModule wp = null;
	
	public PriorityDialog(IDiskoWpModule wp) {
		// forward
		super(wp.getApplication().getFrame());
		// prepare
		this.wp = wp;
		// initialize GUI
		initialize();
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
			e.printStackTrace();
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

	private static Object[] getPriData() {
		Object[] data = new Object[5];
		data[0] = DiskoStringFactory.getText("PRIMARY_SEARCH_AREA");
		data[1] = DiskoStringFactory.getText("SECONDARY_SEARCH_AREA");
		data[2] = DiskoStringFactory.getText("PRIORITY3_SEARCH_AREA");
		data[3] = DiskoStringFactory.getText("PRIORITY4_SEARCH_AREA");
		data[4] = DiskoStringFactory.getText("PRIORITY5_SEARCH_AREA");
		return data;
	}
	
	/**
	 * This method initializes contentPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private DefaultPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				contentPanel = new DefaultPanel("",true,true) {

					private static final long serialVersionUID = 1L;
					
					@Override
					protected boolean beforeFinish() {
						// update mso model
						setPriority(getPriority(), false, true);
						// finished
						return true;
					}


					@Override
					public void setMsoObject(IMsoObjectIf msoObj) {
						// consume changes
						setChangeable(false);
						// initialize
						int pri = 1;
						ISearchAreaIf area = null;
						// dispatch type
						if (msoObj instanceof ISearchAreaIf) {
							area = (ISearchAreaIf)msoObj;
							pri = area.getPriority();
						}
						
						// update
						msoObject = area;
						setPriority(pri,true,false);						

						// resume changes
						setChangeable(true);
						
						// update
						setDirty(false,false);
						update();
												
					}	
					
					@Override
					public void update() {
						super.update();
						setup();
					}
					
					
				};
				contentPanel.setInterests(wp.getMsoModel(),getMyInterest());
				contentPanel.setMsoLayers(wp.getMap(),getMyLayers());				
				contentPanel.setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "32x32"));
				contentPanel.setContainer(getPriorityList());
				
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
				priorityList.setCellRenderer(new RadioButtonListCellRenderer());
				priorityList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);				
				priorityList.setFixedCellHeight(25);
				priorityList.setListData(getPriData());
				priorityList.setSelectedIndex(0);
				priorityList.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						// consume?
						if(e.getValueIsAdjusting() || !isChangeable());
						// notify
						setDirty(true);
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return priorityList;
	}
	
	private int getPriority() {
		return getPriorityList().getSelectedIndex()+1;
	}
	
	private void setPriority(int priority, boolean gui, boolean mso) {
		// update gui?
		if(gui)
			getPriorityList().setSelectedIndex(priority-1);
		// update mso? (list is updated from Mso Update event)
		if(mso) {
			// dispatch type
			if(getMsoObject() instanceof ISearchAreaIf) {
				// cast to ISearchAreaIf
				ISearchAreaIf searchArea = (ISearchAreaIf)getMsoObject();
				// update?
				if(searchArea.getPriority()!=priority-1) {
					searchArea.setPriority(priority-1);
				}
			}
		}
	}

	private void setup() {
		
		// consume?
		if(!isChangeable()) return;
		
		// consume changes
		setChangeable(false);
		
		// try to get mso object?
		if(getMsoObject()==null) 
			getContentPanel().setSelectedMsoFeature(wp.getMap());

		// get search area
		ISearchAreaIf area = (ISearchAreaIf)getMsoObject(); 
		
		// update icon
		if(area!=null) {
			getContentPanel().setCaptionIcon(
					DiskoIconFactory.getIcon("MAP.POLYGON","32x32"));			
			getContentPanel().setCaptionText("Endre prioritet for <b>" + 
					MsoUtils.getMsoObjectName(area,0).toLowerCase() + "</b>");
			getPriorityList().setEnabled(true);
		}
		else {
			getContentPanel().setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "32x32"));
			getContentPanel().setCaptionText("Du må først velge et søkeområde");			
			getPriorityList().setEnabled(false);
		}		
		
		// resume changes
		setChangeable(true);
	}	
	
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
