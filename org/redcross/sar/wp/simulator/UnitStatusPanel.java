package org.redcross.sar.wp.simulator;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.gui.CompassPanel;
import org.redcross.sar.gui.DiskoBorder;
import org.redcross.sar.gui.attribute.AbstractDiskoAttribute;
import org.redcross.sar.gui.attribute.NumericAttribute;
import org.redcross.sar.gui.attribute.PositionAttribute;
import org.redcross.sar.gui.attribute.TextFieldAttribute;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.AttributesPanel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.wp.IDiskoWpModule;

public class UnitStatusPanel extends BasePanel {

	private static final int FIXED_WIDTH = 420;
	private static final int FIXED_HEIGHT = 190;
	private static final long serialVersionUID = 1L;
	private static final String[] ATTRIBUTES = new String[]{"averagespeed","maxspeed","position"};
	private static final String[] CAPTIONS = new String[]{"Hastighet (snitt)","Hastighet (max)","Posisjon"};
	
	private IUnitIf m_unit = null;
	
	private JButton m_gotoButton = null;
	private JButton m_playButton = null;
	private JButton m_pauseButton = null;
	private JButton m_centerAtAssignmentButton = null;
	private JPanel m_bearingPanel = null;
	private CompassPanel m_compassPanel = null;
	private NumericAttribute m_bearingAttr = null;
	private TextFieldAttribute m_activeAttr = null;
	private AttributesPanel m_attribsPanel = null;
	
	public UnitStatusPanel(IUnitIf unit) {
		// forward
		super(ButtonSize.SMALL);
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
				// translate
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
    	setBodyLayout(new BoxLayout((JComponent)getBodyComponent(),BoxLayout.X_AXIS));
		addBodyChild(getBearingPanel());
		addBodyChild(Box.createHorizontalStrut(5));
		addBodyChild(getAttribsPanel());
    	Utils.setFixedSize(this,FIXED_WIDTH,FIXED_HEIGHT);
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
        	Utils.setFixedSize(m_bearingPanel,90,FIXED_HEIGHT-40);
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
	
	private NumericAttribute getBearingAttr()
    {
        if (m_bearingAttr == null)
        {
        	m_bearingAttr = new NumericAttribute(m_unit.getBearingAttribute(),"Grad",30,false);
        	m_bearingAttr.setMaxDigits(3);
        	m_bearingAttr.setDecimalPrecision(0);
        	m_bearingAttr.setAllowNegative(false);
        	Utils.setFixedSize(m_bearingAttr,90,25);
        	
        }        
        return m_bearingAttr;
    }
	
	private TextFieldAttribute getActiveAttr()
    {
        if (m_activeAttr == null)
        {
        	m_activeAttr = new TextFieldAttribute("active","Aktivt oppdrag",100,"Ingen oppdrag",false);
        	m_activeAttr.setButton(getCenterAtAssignmentButton(), true);
        	getCenterAtAssignmentButton().setEnabled(true);
        }        
        return m_activeAttr;
    }
	
	private AttributesPanel getAttribsPanel()
    {
        if (m_attribsPanel == null)
        {
        	m_attribsPanel = new AttributesPanel("","",false,false);
        	m_attribsPanel.setHeaderVisible(false);
        	m_attribsPanel.setBorderVisible(false);
        	m_attribsPanel.setNotScrollBars();
        	m_attribsPanel.addDiskoWorkListener(this);
        	Utils.setFixedSize(m_attribsPanel,FIXED_WIDTH-90,FIXED_HEIGHT-40);        	
        }        
        return m_attribsPanel;
    }

	public IUnitIf getUnit() {
		return m_unit;
	}
	
	public int getCurrentBearing() {
		return m_unit !=null ? m_unit.getBearing() : 0;
	}
	
	public IAssignmentIf getUnitAssignment() {
		IAssignmentIf assignment = null;
		if(m_unit !=null) {
			assignment =  m_unit.getActiveAssignment();
			if(assignment==null) {
				assignment = m_unit.getAssignedAssignment();
				if(assignment == null) {
					int count = m_unit.getAllocatedAssignments().size();
					if(count>0) {
						assignment = m_unit.getAllocatedAssignments().get(0);
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
				assignment = m_unit.getAssignedAssignment();
				if(assignment == null) {
					int count = m_unit.getAllocatedAssignments().size();
					if(count>0) {
						text = count + " oppdrag i k�";
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
		setCaptionText("<html>Simulering - <b>"+MsoUtils.getUnitName(m_unit, true)+"</b><html>");
		
		// update attributes
		getCompassPanel().setBearing(getCurrentBearing());
		getBearingAttr().load();
		getAttribsPanel().load();
		getActiveAttr().setValue(getActiveAssignmentText());
		
		// resume changes
		setChangeable(true);
		
	}
	
	public void setUnit(IUnitIf unit) {
	
		// save unit
		m_unit = unit;
		
		// connect to attributes
		getAttribsPanel().create(unit, ATTRIBUTES, CAPTIONS, 100, true, true);
		getAttribsPanel().setAutoSave(true);
		getAttribsPanel().addAttribute(getActiveAttr());
		getAttribsPanel().getAttribute(
				ATTRIBUTES[2]).addDiskoWorkListener(m_positionListener);
		
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
					map.centerAtPosition(p);
					map.flashPosition(p);
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
		IDiskoWpModule module = Utils.getApp().getCurrentRole().getCurrentDiskoWpModule();
		if(module!=null) {
			if(module.isMapInstalled())
				return module.getMap();
		}
		// no map available
		return null;
	}	
	
	private final IDiskoWorkListener m_positionListener = new IDiskoWorkListener() {

		@Override
		public void onWorkPerformed(DiskoWorkEvent e) {
			// is position changed?
			if(e.isFinish()) {
				// log position
				m_unit.logPosition();
			}
		}
		
	};

}