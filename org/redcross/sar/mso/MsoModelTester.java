package org.redcross.sar.mso;

import java.util.EnumSet;
import java.util.List;

import javax.swing.SwingUtilities;

import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.mso.event.MsoEvent.UpdateList;

public class MsoModelTester {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				IDispatcherIf dispatcher = new SaraDispatcherImpl();
				dispatcher.initiate();
				IMsoModelIf model = null;
				try {
					model = new MsoModelImpl(dispatcher);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				model.getEventManager().addClientUpdateListener(new IMsoUpdateListenerIf() {

					@Override
					public EnumSet<MsoClassCode> getInterests() {
						// TODO Auto-generated method stub
						return EnumSet.of(MsoClassCode.CLASSCODE_PERSONNEL,MsoClassCode.CLASSCODE_UNIT);
					}

					@Override
					public void handleMsoUpdateEvent(UpdateList e) {
						for(Update it : e.getEvents()) {
							if(it.isLoopbackMode()) {
								IMsoObjectIf msoObj = it.getSource();
								for(IMsoAttributeIf<?> attr : msoObj.getAttributes().values()) {
									System.out.println(attr.getName() + ":isLoopback==" + attr.isLoopbackMode());
								}
							}
						}
						
					}
					
				});
				//dispatcher.createNewOperation();
				//List<String[]> ids = dispatcher.getActiveOperations();
				dispatcher.setActiveOperation("05620090072155539");
				IPersonnelIf personnel = model.getMsoManager().createPersonnel();
				personnel.setFirstname("Kenneth");
				IUnitIf unit = model.getMsoManager().createTeam("");
				unit.addUnitPersonnel(personnel);
				unit.setUnitLeader(personnel);
			}
		});
	}
	
}
