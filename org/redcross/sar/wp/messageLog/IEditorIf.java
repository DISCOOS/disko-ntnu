package org.redcross.sar.wp.messageLog;

import javax.swing.JComponent;

import org.redcross.sar.mso.data.IMessageIf;

/**
 * Implemented by all components editing messages. Needed for updating, showing, hiding, etc.
 * 
 * @author thomasl
 */
public interface IEditorIf
{
	/**
	 * Updates the contents of the component when a new message is selected, or an old one gets a field updated.
	 * @param message The updated message
	 */
	public void setMessage(IMessageIf message);
	
	/**
	 * Displays the component. {@link JComponent#setVisible(boolean)} doesn't suffice, components might need to show
	 * other components such as dialogs. Dialogs have application frame as parent, and are thus not implicitly 
	 * affected by setting the visibility of the component that "owns" them
	 */
	public void showEditor();
	
	/**
	 * Hides the component. {@link JComponent#setVisible(boolean)} doesn't suffice, components need to hide
	 * child components(e.g. dialogs) that are not affected by the setVisible call.
	 */
	public void hideEditor();
	
	/**
	 * Clears the content of child components for the components, e.g. resets text fields in a panel
	 */
	public void resetEditor();
}
