
package org.redcross.sar.gui.factory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.redcross.sar.IApplication;
import org.redcross.sar.gui.DiskoBorder;
import org.redcross.sar.gui.DiskoCorner;
import org.redcross.sar.gui.DiskoGlassPane;
import org.redcross.sar.gui.DiskoGlassPaneUtils;
import org.redcross.sar.gui.DiskoRoundBorder;
import org.redcross.sar.gui.dialog.LoginDialog;
import org.redcross.sar.gui.dialog.MapOptionDialog;
import org.redcross.sar.gui.dialog.NumPadDialog;
import org.redcross.sar.gui.dialog.OperationDialog;
import org.redcross.sar.gui.dialog.ServiceManagerDialog;
import org.redcross.sar.gui.dialog.TaskDialog;
import org.redcross.sar.gui.menu.MainMenu;
import org.redcross.sar.gui.menu.NavMenu;
import org.redcross.sar.gui.menu.SubMenu;
import org.redcross.sar.gui.menu.SysMenu;
import org.redcross.sar.gui.panel.MainPanel;

public class UIFactory {

	public final static String DEFAULT_FONT_NAME = "Tahoma";
	public final static String DIALOG_FONT = "Dialog";
	public final static int FRONT_SIZE_MEDIUM = 14;
	public final static int FONT_SIZE_LARGE = 16;
	public final static Font DEFAULT_BOLD_SMALL_FONT = new Font(DEFAULT_FONT_NAME, Font.PLAIN, 10);
	public final static Font DEFAULT_PLAIN_MEDIUM_FONT = new Font(DEFAULT_FONT_NAME, Font.PLAIN, FRONT_SIZE_MEDIUM);
	public final static Font DEFAULT_PLAIN_LARGE_FONT = new Font(DEFAULT_FONT_NAME, Font.PLAIN, FONT_SIZE_LARGE);
	public final static Font DIALOG_PLAIN_MEDIUM_FONT = new Font(DIALOG_FONT, Font.PLAIN, FRONT_SIZE_MEDIUM);

	private IApplication app;
	private JPanel contentPanel;
	private DiskoGlassPane glassPane;
	private MainMenu mainMenu;
	private SubMenu subMenu;
	private NavMenu navMenuPanel;
	private SysMenu sysMenu;
	private MainPanel mainPanel;
	private JPanel menuPanel;
	private LoginDialog loginDialog;
	private OperationDialog operationDialog;
	private NumPadDialog numPadDialog;
	private MapOptionDialog mapOptionDialog;
	private TaskDialog taskDialog;
	private ServiceManagerDialog serviceManagerDialog;

	private final List<Component> components = new ArrayList<Component>();
	private final Map<Component,Boolean> states = new HashMap<Component,Boolean>();

	/* =================================================================
	 * Constructors
	 * =================================================================*/

	public UIFactory(IApplication app) {
		// prepare
		this.app = app;
		// initialize content panel
		getContentPanel();
		// hide this
		hideAll();
	}

	/* =================================================================
	 * Public static methods
	 * =================================================================*/

	public static void initLookAndFeel()
	{
		try
		{

			// use system look and feel
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			// set DISKO defaults
			UIManager.put("Button.font", DEFAULT_BOLD_SMALL_FONT);
			UIManager.put("CheckBox.font", DEFAULT_PLAIN_LARGE_FONT);
			UIManager.put("CheckBoxMenuItem.acceleratorFont", DIALOG_PLAIN_MEDIUM_FONT);
			UIManager.put("CheckBoxMenuItem.font", DEFAULT_PLAIN_LARGE_FONT);
			UIManager.put("ColorChooser.font", DIALOG_PLAIN_MEDIUM_FONT);
			UIManager.put("ComboBox.font", DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("EditorPane.font", DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("FileChooser.listFont", DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("FormattedTextField.font", DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("Label.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("List.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("Menu.acceleratorFont",DIALOG_PLAIN_MEDIUM_FONT);
			UIManager.put("Menu.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("MenuBar.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("MenuItem.acceleratorFont",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("MenuItem.font",DEFAULT_PLAIN_LARGE_FONT);
			UIManager.put("OptionPane.buttonFont",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("OptionPane.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("OptionPane.messageFont",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("Panel.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("PasswordField.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("PopupMenu.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("ProgressBar.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("RadioButton.font",DEFAULT_PLAIN_LARGE_FONT);
			UIManager.put("RadioButtonMenuItem.acceleratorFont",DIALOG_PLAIN_MEDIUM_FONT);
			UIManager.put("RadioButtonMenuItem.font", DEFAULT_PLAIN_LARGE_FONT);
			UIManager.put("ScrollPane.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("Slider.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("Spinner.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("TabbedPane.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("Table.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("TableHeader.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("TextField.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("TextPane.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("EditorPane.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("TitledBorder.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("ToggleButton.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("ToolBar.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("ToolTip.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("Tree.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("Viewport.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("ScrollBar.width", 25);
			UIManager.put("ScrollBar.height", 25);
			UIManager.put("SplitPaneDivider.border", BorderFactory.createEmptyBorder());

			// Because DISCO and ArcGIS Objects are mixing heavyweight (AWT.*)
			// and lightweight (Swing.*) component, default lightweight behaviors
			// must be turned off. If not, components JPopup and JTooltips
			// will be shown below ArcGIS AWT components (MapBean etc.
			// IMPORTANT: Do not put ArcGIS components which is implemented using
			// AWT in a JScrollPane. This will not work correctly. Instead,
			// use an AWT ScrollPane.
			JPopupMenu.setDefaultLightWeightPopupEnabled(false);
			ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static DiskoBorder createBorder() {
		return new DiskoBorder();
	}

	public static DiskoBorder createBorder(int top, int left, int bottom, int right) {
		return new DiskoBorder(top,left,bottom,right);
	}

	public static DiskoBorder createBorder(int top, int left, int bottom, int right, Color color) {
		return new DiskoBorder(top,left,bottom,right,color);
	}

	public static DiskoRoundBorder createRoundBorder(int thickness, int diameter, boolean isDouble) {
		return new DiskoRoundBorder(thickness,diameter,isDouble);
	}

	public static DiskoCorner createCorner() {
		DiskoCorner c = new DiskoCorner();
		c.setPreferredSize(new Dimension(25,32));
		return c;
	}

	public static JScrollPane createScrollPane(Component view) {
		return createScrollPane(view,false);
	}

	public static JScrollPane createScrollPane(Component view, boolean hasBorder) {
		return createScrollPane(view,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
				hasBorder);
	}

	public static JScrollPane createScrollPane(Component view, boolean hasBorder, int top, int left, int bottom, int right) {
		return createScrollPane(view,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED,
				hasBorder, top, left, bottom, right);
	}
	
	public static JScrollPane createScrollPane(Component view, int vScroll, int hScroll, boolean hasBorder) {
		JScrollPane scrollPane = new JScrollPane(view);
		scrollPane.setOpaque(true);
		scrollPane.setBorder(hasBorder ? new DiskoBorder() : BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, UIFactory.createCorner());
		scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, UIFactory.createCorner());
		scrollPane.setVerticalScrollBarPolicy(vScroll);
		scrollPane.setHorizontalScrollBarPolicy(hScroll);
		scrollPane.setWheelScrollingEnabled(true);
		return scrollPane;		
	}
	
	public static JScrollPane createScrollPane(Component view, int vScroll, int hScroll, boolean hasBorder, int top, int left, int bottom, int right) {
		JScrollPane scrollPane = createScrollPane(view, vScroll, hScroll, hasBorder);
		scrollPane.setOpaque(true);
		Border border = scrollPane.getBorder();
		scrollPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(top, left, bottom, right), border));
		return scrollPane;
	}

	/* =================================================================
	 * Public methods
	 * =================================================================*/

	/**
	 * Save current system and navigation menu states.
	 *
	 * @return State
	 */
	public State save() {
		return new State();
	}

	/**
	 * Load saved system and navigation button states
	 *
	 * @return State
	 */
	public void load(State state) {
		if(state!=null) {
			state.load();
		}
	}


	public NumPadDialog getNumPadDialog(){
		if (numPadDialog == null) {
			numPadDialog = new NumPadDialog(app.getFrame());
			register(numPadDialog);
		}
		return numPadDialog;
	}

	public LoginDialog getLoginDialog() {
		if (loginDialog == null) {
			loginDialog = new LoginDialog(app.getFrame());
			loginDialog.getUserName().setValue("disko");
			loginDialog.getPassword().setValue("disko");
			register(loginDialog);
		}
		loginDialog.load();
		return loginDialog;
	}

	public OperationDialog getOperationDialog() {
		if (operationDialog == null) {
			operationDialog= new OperationDialog(app.getFrame());
			register(operationDialog);
		}
		operationDialog.load();
		return operationDialog;
	}

	public MapOptionDialog getMapOptionDialog(){
		if (mapOptionDialog == null) {
			mapOptionDialog = new MapOptionDialog(app);
			register(mapOptionDialog);
		}
		mapOptionDialog.setLocationRelativeTo(contentPanel);
		return mapOptionDialog;
	}

	public TaskDialog getTaskDialog(){
		if(taskDialog == null){
			taskDialog = new TaskDialog(app.getFrame());
			register(taskDialog);
		}
		return taskDialog;
	}

	public ServiceManagerDialog getServiceManagerDialog(){
		if (serviceManagerDialog == null) {
			serviceManagerDialog = new ServiceManagerDialog(app.getFrame());
			register(serviceManagerDialog);
		}
		serviceManagerDialog.setLocationRelativeTo(contentPanel);
		return serviceManagerDialog;
	}


	/**
	 * This method initializes glassPane
	 *
	 * @return org.redcross.sar.gui.DiskoGlassPane
	 */
	public DiskoGlassPane getGlassPane() {
		if (glassPane == null) {
			try {
				glassPane = DiskoGlassPaneUtils.createGlassPane(app.getFrame());

			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return glassPane;
	}

	/**
	 * This method initializes contentPanel
	 *
	 * @return javax.swing.JPanel
	 */
	public JPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				contentPanel = new JPanel();
				contentPanel.setPreferredSize(new Dimension(1024,764));
				contentPanel.setLayout(new BorderLayout());
				contentPanel.add(getMenuPanel(), BorderLayout.EAST);
				contentPanel.add(getMainPanel(), BorderLayout.CENTER);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return contentPanel;
	}

	private JPanel getMenuPanel() {
		if (menuPanel == null) {
			menuPanel = new JPanel();
			menuPanel.setLayout(new BorderLayout());
			menuPanel.add(getSubMenu(), BorderLayout.WEST);
			menuPanel.add(getMainMenu(), BorderLayout.EAST);
		}
		return menuPanel;
	}

	public MainMenu getMainMenu() {
		if (mainMenu == null) {
			mainMenu = new MainMenu(this);
		}
		return mainMenu;
	}

	public SubMenu getSubMenu() {
		if (subMenu == null) {
			subMenu = new SubMenu();
		}
		return subMenu;
	}

	public NavMenu getNavMenu() {
		if (navMenuPanel == null) {
			navMenuPanel = new NavMenu(this,getMainMenu());
		}
		return navMenuPanel;
	}

	public SysMenu getSysMenu() {
		if (sysMenu == null) {
			sysMenu = new SysMenu(this,getMainMenu());
		}
		return sysMenu;
	}

	public MainPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new MainPanel(getNavMenu(),getSysMenu());
		}
		return mainPanel;
	}

	public void hideAll() {
		getContentPanel().setVisible(false);
		for(Component c: components) {
			states.put(c, c.isVisible());
			c.setVisible(false);
		}
	}

	public void showAll() {
		getContentPanel().setVisible(true);
		for(Component c: components) {
			c.setVisible(states.get(c));
		}
	}

	public void showDialogs() {
		getContentPanel().setVisible(true);
		for(Component c: components) {
			if(c instanceof JDialog)
				c.setVisible(states.get(c));
		}
	}

	public void hideDialogs() {
		for(Component c: components) {
			if(c instanceof JDialog) {
				states.put(c, c.isVisible());
				c.setVisible(false);
			}
		}
	}

	public void register(JDialog c) {
		if(!components.contains(c)) {
			components.add(c);
			states.put(c, c.isVisible());
		}
	}

	public boolean isNavMenuVisible() {
		return getNavMenu().isVisible();
	}

	public void setNavMenuVisible(boolean isVisible) {
		getNavMenu().setVisible(isVisible);
	}


	public boolean isSysMenuVisible() {
		return getSysMenu().isVisible();
	}

	public void setSysMenuVisible(boolean isVisible) {
		getSysMenu().setVisible(isVisible);
	}

	public class State {

		private MainMenu.State m_mainMenuState;
		private SubMenu.State m_subMenuState;
		private NavMenu.State m_navMenuState;
		//private SysMenuPanel.State m_sysMenuState;

		public State() {
			// forward
			save();
		}

		public void save() {
			// save states
			m_mainMenuState = getMainMenu().save();
			m_subMenuState = getSubMenu().save();
			m_navMenuState = getNavMenu().save();
			//m_sysMenuState = getSysMenuPanel().save();
		}

		public void load() {
			// load states
			getMainMenu().load(m_mainMenuState);
			getSubMenu().load(m_subMenuState);
			getNavMenu().load(m_navMenuState);
			//getSysMenuPanel().load(m_sysMenuState);
		}


	};

}
