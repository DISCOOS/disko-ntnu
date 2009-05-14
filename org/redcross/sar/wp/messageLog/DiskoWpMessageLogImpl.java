package org.redcross.sar.wp.messageLog;

import org.apache.log4j.Logger;
import org.redcross.sar.IDiskoRole;
import org.redcross.sar.map.command.IMapCommand.MapCommandType;
import org.redcross.sar.map.tool.IMapTool.MapToolType;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.WorkPool;
import org.redcross.sar.wp.AbstractDiskoWpModule;

import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JOptionPane;

/**
 *
 */
public class DiskoWpMessageLogImpl extends AbstractDiskoWpModule implements IDiskoWpMessageLog
{
	MessageLogPanel m_logPanel;

    public DiskoWpMessageLogImpl() throws IllegalClassFormatException
    {
    	super(Logger.getLogger(DiskoWpMessageLogImpl.class));

    	// initialize GUI
        initialize();
    }

    private void initialize()
    {
        // load class resource bundle
        assignWpBundle(IDiskoWpMessageLog.class);

        // install map
        installMap();

        // get message log panel. This panel implements the GUI
        m_logPanel = new MessageLogPanel(this);

        // add the panel as main work process component
        layoutComponent(m_logPanel.getPanel());

        // ensure that work process specific layers are selectable
        m_logPanel.setSelectableLayers();

		// install draw support for changing unit position in map
		getMap().installEditSupport(EnumSet.of(MsoClassCode.CLASSCODE_UNIT));

		// hide map
		getMap().setVisible(false);

    }

    @Override
	public void activate(IDiskoRole role) {

		// setup of navbar needed?
		if(isNavMenuSetupNeeded()) {
			// get tool set
	        List<Enum<?>> myButtons = new ArrayList<Enum<?>>();
	        myButtons.add(MapToolType.ZOOM_IN_TOOL);
	        myButtons.add(MapToolType.ZOOM_OUT_TOOL);
	        myButtons.add(MapToolType.PAN_TOOL);
	        myButtons.add(MapCommandType.ZOOM_FULL_EXTENT_COMMAND);
	        myButtons.add(MapCommandType.ZOOM_TO_LAST_EXTENT_FORWARD_COMMAND);
	        myButtons.add(MapCommandType.ZOOM_TO_LAST_EXTENT_BACKWARD_COMMAND);
	        myButtons.add(MapCommandType.MAP_TOGGLE_COMMAND);
	        myButtons.add(MapCommandType.SCALE_COMMAND);
	        myButtons.add(MapCommandType.TOC_COMMAND);
	        myButtons.add(MapCommandType.GOTO_COMMAND);
	        myButtons.add(MapToolType.SELECT_TOOL);
			// forward
			setupNavMenu(myButtons,false);
		}
		if(MessageLogPanel.isMapShown())
			MessageLogPanel.showMap();
		else
			MessageLogPanel.hideMap();
		// forward
		super.activate(role);

    }

    @Override
    public boolean confirmDeactivate()
    {

		// prevent reentry
		if(isWorking()) {
			// notify
			Utils.showWarning(getBundleText("Working.header"), getBundleText("Working.text"));
			// do not allow to deactivate
			return false;
		}

		// validate data
		if(MessageLogBottomPanel.isMessageDirty()) {

			// prompt user
			String[] options = {
					getBundleText("DirtyMessageWarning.commit"),
					getBundleText("DirtyMessageWarning.rollback"),
					getBundleText("DirtyMessageWarning.cancel")};
			int ans = JOptionPane.showOptionDialog(getApplication().getFrame(),
						getBundleText("DirtyMessageWarning.text"),
						getBundleText("DirtyMessageWarning.header"),
						JOptionPane.YES_NO_CANCEL_OPTION,
		                JOptionPane.QUESTION_MESSAGE,null,options,options[0]);

			// select action
			switch(ans) {
			case JOptionPane.OK_OPTION:
				// forward
				return MessageLogBottomPanel.apply(true);
			case JOptionPane.NO_OPTION:
				// forward
				MessageLogBottomPanel.clearCurrentMessage();
				// allow deactive
				return true;
			case JOptionPane.CANCEL_OPTION:
				// do not deactivate
				return false;
			}
		}

		// forward
		m_logPanel.hidePanels();

		// allow deactivate
		return true;

    }

	public void deactivate() {
		// hide map
		getMap().setVisible(false);
		// forward
		super.deactivate();
	}


    @Override
	public String getCaption()
    {
        return getBundleText("MESSAGELOG");
    }

    @Override
	public boolean commit() {
		// TODO Auto-generated method stub
		return doCommitWork();
	}

	@Override
	public boolean rollback() {
		// TODO Auto-generated method stub
		return doRollbackWork();
	}


	private boolean doCommitWork() {
		try {
			// forward work
			WorkPool.getInstance().schedule(new MessageWork(1));
			// do work
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean doRollbackWork() {
		try {
			WorkPool.getInstance().schedule(new MessageWork(2));
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private class MessageWork extends ModuleWork {

		private int m_task = 0;

		/**
		 * Constructor
		 *
		 * @param task
		 */
		MessageWork(int task) throws Exception {
			super();
			// prepare
			m_task = task;
		}

		@Override
		public Boolean doWork() {
			try {
				// dispatch task
				switch(m_task) {
				case 1: commit(); return true;
				case 2: rollback(); return true;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return false;
		}


		@Override
		public void beforeDone() {

			try {
				// dispatch task
				switch(m_task) {
				case 1: fireOnWorkCommit(); break;
				case 2: fireOnWorkRollback(); break;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}

		private void commit() {
			try{
				getMsoModel().commit();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}

		private void rollback() {
			try{
				getMsoModel().rollback();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
