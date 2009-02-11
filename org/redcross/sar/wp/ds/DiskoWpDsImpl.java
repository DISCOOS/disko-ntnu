package org.redcross.sar.wp.ds;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.event.ITickEventListenerIf;
import org.redcross.sar.event.TickEvent;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.MapPanel;
import org.redcross.sar.map.command.IMapCommand.MapCommandType;
import org.redcross.sar.map.layer.IMapLayer;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.tool.IMapTool.MapToolType;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.util.Utils;
import org.redcross.sar.wp.AbstractDiskoWpModule;

import com.esri.arcgis.interop.AutomationException;

public class DiskoWpDsImpl extends AbstractDiskoWpModule implements IDiskoWpDs
{

	private static final int UPDATE_TIME_DELAY = 1000;	// updates every second

	private long m_timeCounter = 0;

	private JSplitPane m_splitPane;
	private MapPanel m_mapPanel;
	private JPanel m_dsPanel;
	private ETEPanel m_etePanel;
	private AdvisorPanel m_advisorPanel;
	private JToggleButton m_eteToggleButton;
	private JToggleButton m_advisorToggleButton;

    public DiskoWpDsImpl() throws Exception
    {
		// forward
		super(getWpInterests(),getMapLayers());

		// initialize GUI
		initialize();

		// connect to operation services
		connect();

	}

	private static EnumSet<MsoClassCode> getWpInterests() {
		EnumSet<MsoClassCode> myInterests = EnumSet.of(MsoClassCode.CLASSCODE_OPERATION);
    	myInterests.add(MsoClassCode.CLASSCODE_AREA);
    	myInterests.add(MsoClassCode.CLASSCODE_UNIT);
    	myInterests.add(MsoClassCode.CLASSCODE_ASSIGNMENT);
		return myInterests;
	}

	private static EnumSet<IMsoFeatureLayer.LayerCode> getMapLayers() {
		EnumSet<IMsoFeatureLayer.LayerCode> myLayers;
		myLayers = EnumSet.of(IMsoFeatureLayer.LayerCode.OPERATION_AREA_MASK_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.OPERATION_AREA_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.SEARCH_AREA_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.ROUTE_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.POI_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.UNIT_LAYER);
		myLayers.add(IMapLayer.LayerCode.ESTIMATED_POSITION_LAYER);
	    return myLayers;
	}

	private List<Enum<?>> getDefaultNavBarButtons() {
		List<Enum<?>> myButtons = Utils.getListNoneOf(MapToolType.class);
		myButtons.add(MapToolType.ZOOM_IN_TOOL);
		myButtons.add(MapToolType.ZOOM_OUT_TOOL);
		myButtons.add(MapToolType.PAN_TOOL);
		myButtons.add(MapToolType.SELECT_TOOL);
		myButtons.add(MapCommandType.ZOOM_FULL_EXTENT_COMMAND);
		myButtons.add(MapCommandType.ZOOM_TO_LAST_EXTENT_BACKWARD_COMMAND);
		myButtons.add(MapCommandType.ZOOM_TO_LAST_EXTENT_FORWARD_COMMAND);
		myButtons.add(MapCommandType.MAP_TOGGLE_COMMAND);
		myButtons.add(MapCommandType.SCALE_COMMAND);
		myButtons.add(MapCommandType.TOC_COMMAND);
		myButtons.add(MapCommandType.GOTO_COMMAND);
		return myButtons;
	}

    private void initialize()
    {
		try {
			// get properties
			assignWpBundle(IDiskoWpDs.class);

			// forward
			installMap();

			// set layout component
			layoutComponent(getSplitPane());

			// add layout buttons on sub menu
			layoutButton(getETEToggleButton(), true);
			layoutButton(getAdvisorToggleButton(), true);

			// start update task
			registerUpdateTask();

			// make all layers unselectable
			getMap().getMsoLayerModel().setAllSelectable(false);

			// select ETE view
			getETEToggleButton().doClick();

		} catch (AutomationException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

    }

    private JSplitPane getSplitPane()
    {
        if (m_splitPane == null)
        {
        	m_splitPane = new JSplitPane();
        	m_splitPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        	m_splitPane.setLeftComponent(getMapPanel());
        	m_splitPane.setRightComponent(getDsPanel());
        }
        return m_splitPane;
    }

    private MapPanel getMapPanel()
    {
        if (m_mapPanel == null)
        {
        	m_mapPanel = new MapPanel(getMap());
        	m_mapPanel.setNorthBarVisible(true);
        	m_mapPanel.setSouthBarVisible(true);
        	m_mapPanel.setBorder(UIFactory.createBorder());
			m_mapPanel.setMinimumSize(new Dimension(350,350));
			m_mapPanel.setPreferredSize(new Dimension(450,350));

        }
        return m_mapPanel;
    }

    private JPanel getDsPanel()
    {
        if (m_dsPanel == null)
        {
        	m_dsPanel = new JPanel(new CardLayout());
        	m_dsPanel.add(getETEPanel(),"ete");
        	m_dsPanel.add(getAdvisorPanel(),"advisor");

        }
        return m_dsPanel;
    }

    private ETEPanel getETEPanel()
    {
        if (m_etePanel == null)
        {
        	try {
				m_etePanel = new ETEPanel(getMap(),MsoModelImpl.getInstance());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        return m_etePanel;
    }

    private AdvisorPanel getAdvisorPanel()
    {
        if (m_advisorPanel == null)
        {
        	try {
        		m_advisorPanel = new AdvisorPanel(MsoModelImpl.getInstance());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        return m_advisorPanel;
    }

	private JToggleButton getETEToggleButton() {
		if (m_eteToggleButton == null) {
			try {
				m_eteToggleButton = DiskoButtonFactory.createToggleButton(ButtonSize.NORMAL);
				m_eteToggleButton.setIcon(DiskoIconFactory.getIcon("SEARCH.PATROL", "48x48"));
				m_eteToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						showView("ete");
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return m_eteToggleButton;
	}

	private JToggleButton getAdvisorToggleButton() {
		if (m_advisorToggleButton == null) {
			try {
				m_advisorToggleButton = DiskoButtonFactory.createToggleButton(ButtonSize.NORMAL);
				m_advisorToggleButton.setIcon(DiskoIconFactory.getIcon("GENERAL.HYPOTHESIS", "48x48"));
				m_advisorToggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						showView("advisor");
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return m_advisorToggleButton;
	}

	private void showView(String name) {
		CardLayout cards = (CardLayout)getDsPanel().getLayout();
		cards.show(getDsPanel(), name);
	}

    /**
     * Checking if any tasks have reached their alert time, and give the appropriate role a warning
     */
    private void registerUpdateTask()
    {
        this.addTickEventListener(new ITickEventListenerIf()
        {
            public long getInterval()
            {
                return UPDATE_TIME_DELAY;
            }

            public void setTimeCounter(long counter)
            {
                m_timeCounter = counter;
            }

            public long getTimeCounter()
            {
                return m_timeCounter;
            }

            @SuppressWarnings("unchecked")
            public void handleTick(TickEvent e)
            {
            	update();
            }
        });
    }

	@Override
	public void afterOperationChange() {
		// forward
    	super.afterOperationChange();
    	// connect to operation services
    	connect();
    	// update views
		update();
	}

	private void connect() {
		getETEPanel().connect();
		getAdvisorPanel().connect();
	}

	private void update() {
		if(isActive()) {
			getETEPanel().update();
			getAdvisorPanel().update();
		}
	}

    public String getCaption() {
		return getBundleText("DS");
	}

	public void activate(IDiskoRole role) {

		// forward
		super.activate(role);

		// setup of navbar needed?
		if(isNavMenuSetupNeeded()) {
			// get set of tools visible for this wp
			setupNavMenu(getDefaultNavBarButtons(),true);
		}

		// forward
		update();

	}
}
