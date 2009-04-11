package org.redcross.sar.wp.simulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.redcross.sar.app.Application;
import org.redcross.sar.gui.DiskoBorder;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.field.NumericField;
import org.redcross.sar.gui.field.TextLineField;
import org.redcross.sar.gui.panel.FieldsPanel;
import org.redcross.sar.gui.panel.CompassPanel;
import org.redcross.sar.gui.panel.TogglePanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.work.event.IWorkFlowListener;
import org.redcross.sar.work.event.WorkFlowEvent;
import org.redcross.sar.wp.IDiskoWpModule;

public class UnitStatusPanel extends TogglePanel {

	private static final int FIXED_WIDTH = 420;
	private static final int FIXED_HEIGHT = 185;
	private static final long serialVersionUID = 1L;
	private static final String[] ATTRIBUTES = new String[]{"averagespeed","maxspeed","position"};
	private static final String[] CAPTIONS = new String[]{"Hastighet (snitt)","Hastighet (max)","Posisjon"};

	private IUnitIf m_unit;

	private JButton m_gotoButton;
	private JButton m_playButton;
	private JButton m_pauseButton;
	private JButton m_centerAtAssignmentButton;
	private JPanel m_bearingPanel;
	private CompassPanel m_compassPanel;
	private NumericField m_bearingAttr;
	private TextLineField m_activeAttr;
	private FieldsPanel m_attribsPanel;

	public UnitStatusPanel(IUnitIf unit) {
		// forward
		super("",false,false,ButtonSize.SMALL);
		// prepare
		m_unit = unit;
		// initialize gui
		initialize();
		// set unit
		setUnit(unit);
	}

	private void initialize() {
		// set alignment
		setAlignmentX(Component.LEFT_ALIGNMENT);
		setAlignmentY(Component.CENTER_ALIGNMENT);
		// add actions
		addButton(getGotoButton(), "goto");
		addButton(getPlayButton(), "play");
		addButton(getPauseButton(), "pause");
		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// prepare
				IUnitIf unit = getUnit();
				String cmd = e.getActionCommand();
				if("play".equals(cmd)) {

				}
				else if("pause".equals(cmd)) {

				}
				else if("goto".equals(cmd)) {
					 if(unit!=null) {
						centerAtPosition(unit.getPosition());
					 }
				}
			}

		});
		// initialize panel body
    	setNotScrollBars();
		// ensure fixed size
    	Utils.setFixedSize(this,FIXED_WIDTH,FIXED_HEIGHT);
		// add bearing panel to the left side
		addToContainer(getBearingPanel(),BorderLayout.WEST);
		// add attributes in the center of panel
		addToContainer(getAttribsPanel(),BorderLayout.CENTER);
		// forward
		collapse();
	}

	/**
	 * This method initializes GotoButton
	 *
	 * @return {@link JButton}
	 */
	private JButton getGotoButton() {
		if (m_gotoButton == null) {
			m_gotoButton = DiskoButtonFactory.createButton("MAP.CENTERAT",ButtonSize.SMALL);
		}
		return m_gotoButton;
	}

	private JButton getPlayButton() {
		if(m_playButton==null) {
			m_playButton = DiskoButtonFactory.createButton("GENERAL.PLAY", ButtonSize.SMALL);
		}
		return m_playButton;
	}

	private JButton getPauseButton() {
		if(m_pauseButton==null) {
			m_pauseButton = DiskoButtonFactory.createButton("GENERAL.PAUSE", ButtonSize.SMALL);
		}
		return m_pauseButton;
	}

	private JButton getCenterAtAssignmentButton() {
		if(m_centerAtAssignmentButton==null) {
			m_centerAtAssignmentButton = DiskoButtonFactory.createButton("MAP.CENTERAT", ButtonSize.SMALL);
			m_centerAtAssignmentButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					centerAtMsoObject(getUnitAssignment());
				}

			});
		}
		return m_centerAtAssignmentButton;
	}

	private JPanel getBearingPanel()
    {
        if (m_bearingPanel == null)
        {
        	m_bearingPanel = new JPanel();
        	m_bearingPanel.setLayout(new BoxLayout(m_bearingPanel,BoxLayout.Y_AXIS));
        	m_bearingPanel.add(getCompassPanel());
        	m_bearingPanel.add(Box.createVerticalStrut(5));
        	m_bearingPanel.add(getBearingAttr());
        	m_bearingPanel.add(Box.createVerticalGlue());
        	m_bearingPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        	Utils.setFixedSize(m_bearingPanel,90,FIXED_HEIGHT-37);
        }
        return m_bearingPanel;
    }

	private CompassPanel getCompassPanel()
    {
        if (m_compassPanel == null)
        {
        	m_compassPanel = new CompassPanel(0,25);
        	m_compassPanel.setBorder(new DiskoBorder(1,1,1,1,Color.LIGHT_GRAY));
        }
        return m_compassPanel;
    }

	private NumericField getBearingAttr()
    {
        if (m_bearingAttr == null)
        {
        	m_bearingAttr = new NumericField("Bearing","Grad", false, 30, 25, 0);
        	m_bearingAttr.setMaxDigits(3);
        	m_bearingAttr.setDecimalPrecision(0);
        	m_bearingAttr.setAllowNegative(false);
        }
        return m_bearingAttr;
    }

	private TextLineField getActiveAttr()
    {
        if (m_activeAttr == null)
        {
        	m_activeAttr = new TextLineField("active","Aktivt oppdrag",false,100,25,"Ingen oppdrag");
        	m_activeAttr.installButton(getCenterAtAssignmentButton(), true);
        	getCenterAtAssignmentButton().setEnabled(true);
        }
        return m_activeAttr;
    }

	private FieldsPanel getAttribsPanel()
    {
        if (m_attribsPanel == null)
        {
        	m_attribsPanel = new FieldsPanel("","Ingen egenskaper funnet",false,false);
        	m_attribsPanel.setHeaderVisible(false);
        	m_attribsPanel.setBorderVisible(false);
        	m_attribsPanel.setNotScrollBars();
        	m_attribsPanel.setPreferredContainerSize(new Dimension(FIXED_WIDTH-90,FIXED_HEIGHT-37));
        	m_attribsPanel.addWorkFlowListener(this);
        }
        return m_attribsPanel;
    }

	public IUnitIf getUnit() {
		return m_unit;
	}

	public double getCurrentBearing() {
		return m_unit !=null ? m_unit.getBearing() : 0;
	}

	public IAssignmentIf getUnitAssignment() {
		IAssignmentIf assignment = null;
		if(m_unit !=null) {
			assignment =  m_unit.getActiveAssignment();
			if(assignment==null) {
				assignment = m_unit.getAllocatedAssignment();
				if(assignment == null) {
					int count = m_unit.getEnqueuedAssignments().size();
					if(count>0) {
						assignment = m_unit.getEnqueuedAssignments().get(0);
					}
				}
			}
		}
		return assignment;

	}

	public String getActiveAssignmentText() {
		String text = "Ingen registrert";
		IAssignmentIf assignment = null;
		if(m_unit !=null) {
			assignment =  m_unit.getActiveAssignment();
			if(assignment==null) {
				assignment = m_unit.getAllocatedAssignment();
				if(assignment == null) {
					int count = m_unit.getEnqueuedAssignments().size();
					if(count>0) {
						text = count + " oppdrag i kø";
					}
				}
			}
			if(assignment!=null) {
				text = MsoUtils.getAssignmentName(assignment, 1);
			}
		}
		return text;
	}

	public void update() {

		// forward
		super.update();

		// consume?
		if(!isChangeable() || m_unit==null) return;

		// consume changes
		setChangeable(false);

		// update caption
		setCaptionIcon(DiskoIconFactory.getIcon(DiskoEnumFactory.getIcon(m_unit.getType()), "32x32"));
		setCaptionText("Simulering - <b>"+MsoUtils.getUnitName(m_unit, true)+"</b>");

		// update attributes
		getCompassPanel().setBearing((int)getCurrentBearing());
		getBearingAttr().setValue((int)getCurrentBearing());
		getAttribsPanel().reset();
		getActiveAttr().setValue(getActiveAssignmentText());

		// resume changes
		setChangeable(true);

	}

	public void setUnit(IUnitIf unit) {

		// save unit
		m_unit = unit;

		// connect to attributes
		getAttribsPanel().create(unit, ATTRIBUTES, CAPTIONS, true, 100, 25, true);
		getAttribsPanel().setAutoSave(true);
		getAttribsPanel().addField(getActiveAttr());

		// listener for changes in position
		getAttribsPanel().getField(
				ATTRIBUTES[2]).addWorkFlowListener(m_positionListener);

		// forward
		update();

	}

	private static void centerAtPosition(Position p) {
		// get installed map
		IDiskoMap map = getInstalledMap();
		// has map?
		if(map!=null) {
			try {
				// center at position?
				if(p!=null) {
					map.centerAtPosition(p.getGeoPos());
					map.flashPosition(p.getGeoPos());
				}
				else
					Utils.showWarning("Ingen posisjon funnet");
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
	}

	private static void centerAtMsoObject(IMsoObjectIf msoObj) {
		// get installed map
		IDiskoMap map = getInstalledMap();
		// has map?
		if(map!=null) {
			try {
				// center at position?
				if(msoObj!=null) {
					map.centerAtMsoObject(msoObj);
					map.flashMsoObject(msoObj);
				}
				else
					Utils.showWarning("Ingen posisjon funnet");
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
	}

	private static IDiskoMap getInstalledMap() {
		// try to get map from current
		IDiskoWpModule module = Application.getInstance().getCurrentRole().getCurrentDiskoWpModule();
		if(module!=null) {
			if(module.isMapInstalled())
				return module.getMap();
		}
		// no map available
		return null;
	}

	private final IWorkFlowListener m_positionListener = new IWorkFlowListener() {

		@Override
		public void onFlowPerformed(WorkFlowEvent e) {
			// is position changed?
			if(e.isFinish()) {
				// log position
				m_unit.logPosition();
			}
		}

	};

}
