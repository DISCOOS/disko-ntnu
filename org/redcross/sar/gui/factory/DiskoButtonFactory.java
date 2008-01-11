package org.redcross.sar.gui.factory;

import java.awt.Dimension;
import java.awt.Font;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToggleButton;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.renderers.IconRenderer;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.ICommunicatorIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitType;
import org.redcross.sar.mso.util.MsoUtils;

/**
 * Creates buttons
 * 
 * @author thomasl
 */
public class DiskoButtonFactory
{
	private final static ResourceBundle m_bundle = 
		ResourceBundle.getBundle("org.redcross.sar.gui.factory.buttons");
	
	private final static Font BUTTON_FONT = new Font("DiskoButtonFactoryFont", Font.PLAIN, 12);
	
	private static Dimension smallSize = null;
	private static Dimension normalSize = null;
	private static Dimension longSize = null;
	
	public enum ButtonSize {	
		SMALL,
		NORMAL,
		LONG		
	}
	
	/**
	 * Creates a JButton based on size argument
	 */
	public static JButton createButton(ButtonSize size) {
		return createButton(size,0,0);
	}
	
	/**
	 * Creates a JButton based on size argument
	 */
	public static JButton createButton(ButtonSize size,int dx,int dy)
	{
		// create button
		JButton button = new JButton();
		
		// set button font
		button.setFont(BUTTON_FONT);

		// set button size
		setButtonSize(button,size,dx,dy);
		
		// remove button border
		button.setBorder(null);

		// return button
		return button;
	}

	/**
	 * Creates a JToggleButton based on size argument
	 */
	public static JToggleButton createToggleButton(ButtonSize size) {
		return createToggleButton(size,0,0);
	}
	
	/**
	 * Creates a JToggleButton based on size argument
	 */
	public static JToggleButton createToggleButton(ButtonSize size,int dx,int dy)
	{
		
		// create toggle button
		JToggleButton button = new JToggleButton();
		
		// set button font
		button.setFont(BUTTON_FONT);

		// set button size
		setButtonSize(button,size,dx,dy);

		// remove button border
		button.setBorder(null);

		// return button
		return button;
	}

	/**
	 * Creates a JButton based on size and name argument
	 */	
	public static JButton createButton(String name, ButtonSize size) {
		return createButton(name,size,0,0,null);
	}
	
	/**
	 * Creates a JButton based on size and name argument
	 */	
	public static JButton createButton(
			String name, ButtonSize size, int dx, int dy, ResourceBundle bundle)
	{
		// create button
		JButton button = createButton(size,dx,dy);
		
		// get catalog
		String catalog = getCatalog(size);
		
		// forward
		setIconAndText(button,name,catalog,bundle);
		
		// return button
		return button;
	}
	
	/**
	 * Creates a JToggleButton based on size and name arguments
	 */	
	public static JToggleButton createToggleButton(String name, ButtonSize size) {
		return createToggleButton(name,size,0,0,null);
	}
	
	/**
	 * Creates a JToggleButton based on size and name arguments
	 */	
	public static JToggleButton createToggleButton(
			String name, ButtonSize size, int dx, int dy, ResourceBundle bundle)
	{
		// create button
		JToggleButton button = createToggleButton(size,dx,dy);
		
		// get catalog
		String catalog = getCatalog(size);
		
		// forward
		setIconAndText(button,name,catalog,bundle);
				
		// return button
		return button;
	}
	
	/**
	 * Creates a JButton based on size and name argument
	 */	
	public static JButton createButton(Enum e, ButtonSize size) {
		return createButton(e,size,0,0,null);
	}
	
	/**
	 * Creates a JButton based on size and name argument
	 */	
	public static JButton createButton(
			Enum e, ButtonSize size, int dx, int dy, ResourceBundle bundle)
	{
		// create button
		JButton button = createButton(size,dx,dy);
		
		// get catalog
		String catalog = getCatalog(size);
		
		// forward
		setIconAndText(button,e,catalog, bundle);
		
		// return button
		return button;
	}
	
	/**
	 * Creates a JToggleButton based on size and name arguments
	 */	
	public static JToggleButton createToggleButton(Enum e, ButtonSize size){
		return createToggleButton(e,size,0,0,null);
	}
	
	/**
	 * Creates a JToggleButton based on size and name arguments
	 */	
	public static JToggleButton createToggleButton(
			Enum e, ButtonSize size, int dx, int dy, ResourceBundle bundle)
	{
		// create button
		JToggleButton button = createToggleButton(size,dx,dy);
		
		// get catalog
		String catalog = getCatalog(size);
		
		// forward
		setIconAndText(button,e,catalog, bundle);
				
		// return button
		return button;
	}	
	
	/**
	 * Creates a JButton based on size and text arguments
	 */	
	public static JButton createButton(
			String text, String tooltip, Icon icon,ButtonSize size){
		return createButton(text,tooltip,icon,size,0,0);
	}
	
	/**
	 * Creates a JButton based on size and text arguments
	 */	
	public static JButton createButton(
			String text, String tooltip, Icon icon, ButtonSize size, int dx, int dy)
	{
		// create button
		JButton button = createButton(size,dx,dy);
		
		// set icon		
		if(icon!=null)
			button.setIcon(icon);
		else
			button.setText(text);
		
		// set tooltip text
		button.setToolTipText(tooltip);
		
		// return button
		return button;
	}
	
	/**
	 * Creates a JToggleButton based on size and text arguments
	 */	
	public static JToggleButton createToggleButton(
			String text, String tooltip, Icon icon, ButtonSize size){
		return createToggleButton(text,tooltip,icon,size,0,0);
	}
	
	/**
	 * Creates a JToggleButton based on size and text arguments
	 */	
	public static JToggleButton createToggleButton(
			String text, String tooltip, Icon icon, ButtonSize size, int dx, int dy)
	{
		// create button
		JToggleButton button = createToggleButton(size,dx,dy);
		
		// set icon		
		if(icon!=null)
			button.setIcon(icon);
		else
			button.setText(text);
		
		// set tooltip text
		button.setToolTipText(text);
		
		// return button
		return button;
	}
	
	/**
	 * Creates a JButton based on size and communicator arguments
	 */
	public static JButton createButton(
			ICommunicatorIf communicator, ButtonSize size){
		return createButton(communicator,size,0,0,null);
		
	}
	
	/**
	 * Creates a JButton based on size and communicator arguments
	 */
	public static JButton createButton(
			ICommunicatorIf communicator, ButtonSize size,int dx,int dy, ResourceBundle bundle)
	{
		// get button of correct size
		JButton button = createButton(size,dx,dy);
		
		// get catalog
		String catalog = getCatalog(size);
		
		// apply icon
		setIconAndText(button,communicator,catalog,bundle);
		
		// return button
		return button;
	}
	
	/**
	 * Creates a JToggleButton based on size and communicator arguments
	 */
	public static JToggleButton createToggleButton(
			ICommunicatorIf communicator, ButtonSize size){
		return createToggleButton(communicator,size,0,0,null);
	}
	
	/**
	 * Creates a JToggleButton based on size and communicator arguments
	 */
	public static JToggleButton createToggleButton(
			ICommunicatorIf communicator, ButtonSize size,int dx,int dy, ResourceBundle bundle)
	{
		// get toggle button of correct size
		JToggleButton button = createToggleButton(size,dx,dy);
		
		// get catalog
		String catalog = getCatalog(size);
		
		// apply icon
		setIconAndText(button,communicator,catalog,bundle);		
		
		// return button
		return button;
	}
	
	/**
	 * Creates a JButton based on size and assignment arguments
	 */
	public static JButton createButton(
			IAssignmentIf assignment, ButtonSize size){
		return createButton(assignment,size,0,0,null);
	}
	
	/**
	 * Creates a JButton based on size and assignment arguments
	 */
	public static JButton createButton(
			IAssignmentIf assignment, ButtonSize size,int dx,int dy, ResourceBundle bundle)
	{
		// get button of correct size
		JButton button = createButton(size,dx,dy);
		
		// forward
		setIconAndText(button,assignment);
		
		// return button
		return button;
	}


	/**
	 * Creates a JToggleButton based on size and assignment arguments
	 */
	public static JToggleButton createToggleButton(
			IAssignmentIf assignment, ButtonSize size){
		return createToggleButton(assignment,size);
	}
	
	/**
	 * Creates a JToggleButton based on size and assignment arguments
	 */
	public static JToggleButton createToggleButton(
			IAssignmentIf assignment, ButtonSize size,int dx,int dy, ResourceBundle bundle)
	{
		// get toggle button of correct size
		JToggleButton button = createToggleButton(size,dx,dy);
		
		// forward
		setIconAndText(button,assignment);
		
		// return button
		return button;
	}
	
	/**
	 * Gets the default size of buttons use in the Disko application
	 * @return
	 */
	public static Dimension getButtonSize(ButtonSize size) {
		return getButtonSize(size,0,0);
	}
	
	/**
	 * Gets the default size of buttons use in the Disko application
	 * @return
	 */
	public static Dimension getButtonSize(ButtonSize size,int dx,int dy) {
		// has value?
		if(size!=null) {
			// get custom size?
			if(dx!=0 || dy!=0) {
				// get heigth and width
				int width  = parseInt(m_bundle.getString("BUTTON."+size.name()+".width"),50)+dx;
				int height = parseInt(m_bundle.getString("BUTTON."+size.name()+".height"),50)+dy;
				// return dimension
				return new Dimension(width, height);
			}
			else
				return getBundleButtonSize(size);
		}

		// failure!
		return null;
	}		
	
	private static Dimension getBundleButtonSize(ButtonSize size) {
		// compare
		if(ButtonSize.SMALL.equals(size)) {
			// create?
			if(smallSize==null) {
				// get heigth and width
				int width  = parseInt(m_bundle.getString("BUTTON.SMALL.width"),50);
				int height = parseInt(m_bundle.getString("BUTTON.SMALL.height"),50);
				smallSize = new Dimension(width, height);
			}
			return smallSize;
		} 
		else if(ButtonSize.NORMAL.equals(size)) {
			// create?
			if(normalSize==null) {
				// get heigth and width
				int width  = parseInt(m_bundle.getString("BUTTON.NORMAL.width"),50);
				int height = parseInt(m_bundle.getString("BUTTON.NORMAL.height"),50);
				normalSize = new Dimension(width, height);
			}
			return normalSize;			
		}
		else if(ButtonSize.LONG.equals(size)) {
			// create?
			if(longSize==null) {
				// get heigth and width
				int width  = parseInt(m_bundle.getString("BUTTON.LONG.width"),50);
				int height = parseInt(m_bundle.getString("BUTTON.LONG.height"),50);
				longSize = new Dimension(width, height);
			}
			return longSize;
		}
		// failure!
		return null;
		}
	
	public static String getCatalog(ButtonSize size) {
		// compare
		if(ButtonSize.SMALL.equals(size)) {
			return "32x32";
		}
		else if(ButtonSize.NORMAL.equals(size) || ButtonSize.LONG.equals(size)) {
			return "48x48";
		}
		else
			return "48x48";
	}
	
	public static void setButtonSize(AbstractButton button, ButtonSize size) {
		setButtonSize(button,size,0,0);
	}
	
	public static void setButtonSize(AbstractButton button, ButtonSize size,int dx,int dy) {
		// get button dimension
		Dimension dim = getButtonSize(size,dx,dy);	
		// set fixed button size
		button.setMinimumSize(dim);
		button.setPreferredSize(dim);
		button.setMaximumSize(dim);		
	}
	
	public static void setIcon(
			AbstractButton button, String name, String catalog) {
		setIcon(button,name,catalog,null);
	}
	
	public static void setIcon(
			AbstractButton button, String name, String catalog, ResourceBundle bundle) {
			
		// forward
		button.setIcon(getIcon(name+".icon", catalog, bundle));
		
	}
	
	public static void setIconAndText(
			AbstractButton button, String name, String catalog) {
		setIconAndText(button, name,catalog,null);
	}
	
	public static void setIconAndText(
			AbstractButton button, String name, String catalog, ResourceBundle bundle) {

		// get text
		String text = getText(name+".text",bundle);
		
		// set tooltip text
		button.setToolTipText(text);
		
		// get get icon
		button.setIcon(getIcon(name+".icon",catalog, bundle));

		// set button text?
		if(button.getIcon()==null) 
			button.setText(text);
		
	}
		
	public static void setIcon(AbstractButton button, Enum e, String catalog) {
		setIcon(button,e,catalog,null);
	}
	
	public static void setIcon(AbstractButton button, Enum e, String catalog, ResourceBundle bundle) {
		
		// get get icon
		button.setIcon(getIcon(getKey(e,"icon"),catalog,bundle));

	}
	
	public static void setIconAndText(
			AbstractButton button, Enum e, String catalog) {
		setIconAndText(button,e,catalog,null);
	}
	
	public static void setIconAndText(
			AbstractButton button, Enum e, String catalog, ResourceBundle bundle) {
		
		// get text
		String text = getText(getKey(e,"text"),bundle);
		
		// set tooltip text
		button.setToolTipText(text);
		
		// get get icon
		button.setIcon(getIcon(getKey(e,"icon"),catalog,bundle));

		// set button text?
		if(button.getIcon()==null) 
			button.setText(text);
		
	}
	
	public static void setIcon(AbstractButton button, ICommunicatorIf communicator,String catalog) {
		setIcon(button,communicator,catalog,null);
	}
	
	public static void setIcon(
			AbstractButton button, ICommunicatorIf communicator,String catalog, ResourceBundle bundle) {
		// update text and icon
		if(communicator instanceof ICmdPostIf){
			// set button icon from unit type
			setIcon(button,UnitType.CP, catalog,bundle);
		}
		else if(communicator instanceof IUnitIf) {
			// cast to IUnitIf
			IUnitIf unit = ((IUnitIf)communicator);
			// set button icon from unit type
			setIcon(button,unit.getType(), catalog,bundle);
		}		
	}
	
	public static void setIconAndText(AbstractButton button, ICommunicatorIf communicator,String catalog) {
		setIconAndText(button,communicator,catalog,null);
	}
	
	public static void setIconAndText(
			AbstractButton button, ICommunicatorIf communicator,String catalog, ResourceBundle bundle) {
		// initialize
		String name = null;
		// update text and icon
		if(communicator instanceof ICmdPostIf){
			// cast to ICmdPostIf
			ICmdPostIf cmdPost = ((ICmdPostIf)communicator);			
			// set button icon from unit type
			setIcon(button,UnitType.CP,catalog,bundle);
			// command post name
			name = cmdPost.getCallSign();
		}
		else if(communicator instanceof IUnitIf) {
			// cast to IUnitIf
			IUnitIf unit = ((IUnitIf)communicator);
			// set button icon from unit type
			setIcon(button,unit.getType(), catalog,bundle);
			// get name
			name = MsoUtils.getUnitName(unit, true);
		}		
		// set text?
		if(button.getIcon()==null)
			button.setText(name);
		// set tooltip 
		button.setToolTipText(name);
	}	
	
	public static void setIcon(AbstractButton button, IAssignmentIf assignment) {		
		// apply icon
		button.setIcon(new IconRenderer.AssignmentIcon(assignment, false, null));		
	}
	
	public static void setIconAndText(AbstractButton button, IAssignmentIf assignment) {
		
		// apply icon
		button.setIcon(new IconRenderer.AssignmentIcon(assignment, false, null));
		
		// get assignment name
		String name = MsoUtils.getAssignmentName(assignment, 1);
		
		// apply tooltip
		button.setToolTipText(name);
		
		// apply text
		if(button.getIcon()==null)
			button.setText(name);
		
	}
	
	private static String getText(String key, ResourceBundle bundle) {
		// initialize
		String text = null;
		// try custom bundle?
		if(bundle!=null && bundle.containsKey(key))
			text = bundle.getString(key);
		// try factory bundle?
		if(text==null && m_bundle.containsKey(key))
			text = m_bundle.getString(key);
		// try Utils?
		if(text==null)
			text = Utils.getProperty(key);
		// return best effort
		return text;
	}
	
	private static Icon getIcon(String key, String catalog, ResourceBundle bundle) {
		// initialize
		Icon icon = null;
		String name = null;
		// try custom bundle?
		if(bundle!=null && bundle.containsKey(key))
			name = bundle.getString(key);
		// try factory bundle?
		if((name==null || name.isEmpty()) && m_bundle.containsKey(key))
			name = m_bundle.getString(key);
		// try Utils?
		if(name==null || name.isEmpty())
			name = Utils.getProperty(key);
		// icon name?
		if(name!=null && !name.isEmpty()) {
			// try to get from icon factory
			icon = DiskoIconFactory.getIcon(name, catalog);
		}
		// return best effort
		return icon;		
	}
	
	private static int parseInt(String text, int value) {
		try {
			value = Integer.parseInt(text);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return value;
	}
	
	private static String getKey(Enum e, String suffix) {
		String key = e.getClass().getSimpleName()+"."+e.name()+"."+suffix;		
		return key;
	}
}
