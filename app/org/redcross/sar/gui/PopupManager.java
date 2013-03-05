/**
 * 
 */
package org.redcross.sar.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.EventListenerList;

import org.redcross.sar.gui.factory.DiskoIconFactory;

/**
 * @author kennetgu
 *
 */
public class PopupManager implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	final protected EventListenerList m_listeners = new EventListenerList();
	final protected MenuItemListener m_menuItemHandler = new MenuItemListener();
	final protected Map<String,JMenuItem> m_menuItems = new HashMap<String,JMenuItem>();
	final protected Map<JComponent,PopupHandler> m_popups = new HashMap<JComponent, PopupHandler>();
	final protected Map<String,PopupHandler> m_popupMenus = new HashMap<String, PopupHandler>();
	
	/* ===============================================================
	 * Constructors
	 * =============================================================== */
	
	public PopupManager() {
		
		// forward
		super();
		
	}		

	/* ===============================================================
	 * Public methods
	 * =============================================================== */

	
	public void addActionListener(ActionListener listener) {
		m_listeners.add(ActionListener.class, listener);		
	}
	
	public void removeActionListener(ActionListener listener) {
		m_listeners.remove(ActionListener.class, listener);		
	}
		
	public JPopupMenu createPopupMenu(String name) {
		if(!m_popupMenus.containsKey(name)) {
			PopupHandler handler = new PopupHandler();
			m_popupMenus.put(name, handler);
			return handler.getMenu();			
		}
		return null;
	}
	
	public boolean installPopup(String name, JComponent component, boolean onMouseAction) {
		// install?
		if(m_popupMenus.containsKey(name) && !m_popups.containsKey(component)) {
			PopupHandler handler = m_popupMenus.get(name);
			if(onMouseAction) {
				component.addMouseListener(new PopupAdapter(handler));
			}
			m_popups.put(component, handler);
			return true;
		}
		return false;		
	}
	
	public JPopupMenu getPopupMenu(String name) {
		return m_popupMenus.containsKey(name) ? m_popupMenus.get(name).getMenu() : null;
	}	
	
	public JPopupMenu getPopupMenu(JComponent component) {
		return m_popups.containsKey(component) ? m_popups.get(component).getMenu() : null;
	}	
		
	public List<JComponent> getInstalledPopups(String menu) {
		List<JComponent> list = new ArrayList<JComponent>();
		if(m_popupMenus.containsKey(menu)) {
			PopupHandler handler = m_popupMenus.get(menu);
			if(handler!=null) {
				for(JComponent it : m_popups.keySet()) {
					if(m_popups.get(it)==handler) {
						list.add(it);
					}
				}
			}
		}
		return list;
	}	
	
	public boolean addMenuItem(
			String menu, 
			String caption, 
			String name) {
		JMenuItem item = new JMenuItem(caption);
		return addMenuItem(menu, item, name);
	}
	
	public boolean addMenuItem(
			String menu, 
			String caption, 
			String icon, 
			String catalog,
			String name) {
		JMenuItem item = new JMenuItem(caption,DiskoIconFactory.getIcon(icon, catalog));
		return addMenuItem(menu, item, name);
	}
		
	public boolean addMenuItem(String menu, Component item, String name) {
		JPopupMenu m = getPopupMenu(menu);
		if(menu!=null) {
			if(findMenuItem(menu, name) == null) {
				// prepare
				item.setName(name);
				if(item instanceof AbstractButton) {
					AbstractButton button = (AbstractButton)item;
					button.setActionCommand(name);
					button.addActionListener(m_menuItemHandler);
				}
				m.add(item);
				return true;
			}
		}
		return false;
	}
	
	public boolean removeMenuItem(String menu, String name) {
		JPopupMenu m = getPopupMenu(menu);
		if(menu!=null) {
			Component item = findMenuItem(menu, name);
			if(item instanceof AbstractButton) {
				// prepare
				((AbstractButton)item).removeActionListener(m_menuItemHandler);
				m.remove(item);
				return true;
			}
		}
		return false;
	}
	
	public Component findMenuItem(String menu, String name) {
		JPopupMenu m = getPopupMenu(menu);
		if(menu!=null) {
			// get components
			Component[] c = m.getComponents();
			// find component
			for(int i=0;i<c.length;i++) {
				String cmd = c[i].getName();					
				if(cmd!=null && cmd.equals(name))
					return (JMenuItem)c[i];
			}
		}
		return null;
	}
	
	public boolean isMenuItemEnabled(String menu, String name) {
		Component item = findMenuItem(menu,name);
		if(item!=null) {
			return item.isEnabled();
		}
		return false;
	}
	
	public void setMenuItemEnabled(String menu, String name, boolean isEnabled) {
		Component item = findMenuItem(menu,name);
		if(item!=null) {
			item.setEnabled(isEnabled);
		}
	}
		
	public boolean isMenuItemVisible(String menu, String name) {
		Component item = findMenuItem(menu,name);
		if(item!=null) {
			return item.isVisible();
		}
		return false;
	}
	
	public void setMenuItemVisible(String menu, String name, boolean isVisible) {
		Component item = findMenuItem(menu,name);
		if(item!=null) {
			item.setVisible(isVisible);
		}
	}
			
	
	/* ===============================================================
	 * ActionListener implementation
	 * =============================================================== */
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof JComponent) {
			// cast to component
			JComponent c = (JComponent)e.getSource();
			// get installed popup
			JPopupMenu menu = getPopupMenu(c);
			// popup was installed?
			if(menu!=null) {
				menu.show(c, 0, 0);
				menu.show(c, -menu.getWidth(), 0);
			}
		}				
	}

	/* ===============================================================
	 * Helper methods
	 * =============================================================== */
	

	protected void fireActionPerformed(ActionEvent e) {
		ActionListener[] list = m_listeners.getListeners(ActionListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].actionPerformed(e);
		}		
	}
	
	/* ===============================================================
	 * Inner classes
	 * =============================================================== */
	
	private class MenuItemListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			fireActionPerformed(e);
		}
		
	}
	
	
}
