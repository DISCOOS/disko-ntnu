package org.redcross.sar.gui.table;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.redcross.sar.gui.PopupManager;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.model.IDiskoTableModel;
import org.redcross.sar.gui.renderer.DefaultHeaderRenderer;
import org.redcross.sar.gui.renderer.ITableHeaderRenderer;
import org.redcross.sar.util.Utils;

public class DiskoTableHeader extends JTableHeader {

	private static final long serialVersionUID = 1L;

	final private ColumnHeaderToolTips m_tips = new ColumnHeaderToolTips();
	final private PopupManager m_popupMananger = new PopupManager();
	final private EventListenerList m_listeners = new EventListenerList();

	private ITableHeaderRenderer m_renderer;
	private ITableHeaderRenderer m_editor;

	private int m_editColumn;
	private int m_mouseColumn;
	private int m_fixedHeight;

	/* ===================================================================
	 * Constructors
	 * =================================================================== */

	public DiskoTableHeader() {
		this(false);
	}

	public DiskoTableHeader(boolean drawVerticalLine) {

		// forward
		super();

		// create default renderers
		m_renderer = new DefaultHeaderRenderer(drawVerticalLine);
		m_editor = new DefaultHeaderRenderer(drawVerticalLine);

		// set header renderer
		setDefaultRenderer(m_renderer);

		// add tooltip list
		addMouseMotionListener(m_tips);

		// add action listener to editor
		m_editor.addActionListener(m_popupMananger);

		// listen to menu item actions
		m_popupMananger.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				fireActionPerformed(e);
			}

		});

		// set default header size
		setFixedHeight(ButtonSize.SMALL);

	}

	/* ===================================================================
	 * Overridden methods
	 * =================================================================== */

	@Override
	public void updateUI() {
		setUI(new EditableHeaderUI());
		resizeAndRepaint();
		invalidate();
	}

	@Override
	public String getToolTipText(MouseEvent e) {
		return getToolTipText(getColumn(e));
	}

	@Override
	public void setToolTipText(String text) {
		setToolTipText(getToolTipColumn(),text);
	}

	/**
	 * Paints a arrow above or below current header text of sort column
	 *
	 */

	@Override
	public void paintComponent(Graphics g)  {

		/* ===================================================================
		 * This solution is described in Java Bug ID 4473075. It solves the
		 * problem with garbled paint of headers in JScrollPane when
		 * scrolling horizontally.
		 * =================================================================== */
		if(getTable()!=null && getParent() instanceof JViewport) {
			setPreferredSize(new Dimension(getTable().getWidth(),getPreferredSize().height));
		}

		// forward
		super.paintComponent(g);

		// get row sorter
		RowSorter<?> sorter = getRowSorter();

		// has row sorter and header is high enough?
		if(sorter!=null && getHeight()>24) {

			// get key count
			int count = sorter.getSortKeys().size();

			// update according to sort orders
			for(int i = 0; i < count; i++) {

				// get root sort column
				int column = sorter.getSortKeys().get(i).getColumn();

				// get alignment
				int alignment = SwingConstants.LEFT;
				if(table.getModel() instanceof IDiskoTableModel) {
					IDiskoTableModel model = (IDiskoTableModel)table.getModel();
					alignment = model.getColumnAlignment(column);
				}

				// convert to view
				column = getTable().convertColumnIndexToView(column);

				// get width of column
				int w = getRendererWidth(g,column);

				// get rectangle of column header
				Rectangle rc = getHeaderRect(column);

				// get center of arrow
				int xCenter = rc.x + (alignment==SwingConstants.LEFT ? w/2 + 3 : rc.width - w/2 - 5);
				int yCenter = rc.y + 2;

				// get direction
				switch(sorter.getSortKeys().get(i).getSortOrder()) {
				case ASCENDING:
					drawArrow((Graphics2D)g,xCenter,yCenter,xCenter,yCenter,2,count>1 ? i+1 : -1);
					break;
				case DESCENDING:
					drawArrow((Graphics2D)g,xCenter,yCenter,xCenter,yCenter+7,2,count>1 ? i+1 : -1);
					break;
				}

			}
		}

	}

	@Override
	public void setDefaultRenderer(TableCellRenderer defaultRenderer) {
		if(defaultRenderer instanceof ITableHeaderRenderer)
			m_renderer = (ITableHeaderRenderer)defaultRenderer;
		else
			m_renderer = null;
		// forward
		super.setDefaultRenderer(defaultRenderer);
	}

	/* ===================================================================
	 * Public methods
	 * =================================================================== */

	public boolean isEditorInstalled() {
		return (m_editor!=null);
	}

	public void setDefaultEditor(TableCellRenderer defaultRenderer) {
		// unregister current?
		if(m_editor!=null) {
			m_editor.removeActionListener(m_popupMananger);
		}
		// register new=
		if(defaultRenderer instanceof ITableHeaderRenderer) {
			m_editor = (ITableHeaderRenderer)defaultRenderer;
			m_editor.addActionListener(m_popupMananger);
		}
		else
			m_editor = null;
	}

	public boolean isFixedHeight() {
		return m_fixedHeight>-1;
	}

	public void setRendererHeight() {
		setFixedHeight(-1);
	}

	public void setFixedHeight(ButtonSize size) {
		Dimension d = DiskoButtonFactory.getButtonSize(size);
		setFixedHeight(d.height);
	}

	public void setFixedHeight(int height) {
		m_fixedHeight = height;
		if(height>-1)
			Utils.setFixedHeight(this, m_fixedHeight);
		else {
			Utils.setAnySize(this, getWidth(), m_renderer!=null ? m_renderer.getDefaultHeight() : 35);
		}
	}

	public int getRendererWidth(Graphics g, int col) {
		return m_renderer!=null ? m_renderer.getWidth(g, table, -1, col) : 0;
	}

	public int getEditorWidth(Graphics g, int col) {
		return m_editor!=null ? m_editor.getWidth(g, table, -1, col) : 0;
	}

	public int getToolTipColumn() {
		return m_tips.getToolTipColumn();
	}

	public String getToolTipText(int column) {
		TableModel model = getTable().getModel();
		if(column!=-1 && model instanceof IDiskoTableModel) {
			column = getTable().convertColumnIndexToModel(column);
			return ((IDiskoTableModel)model).getHeaderTooltipText(column);
		}
		return "";
	}

	public void setToolTipText(int column, String text) {
		TableModel model = getTable().getModel();
		if(model instanceof IDiskoTableModel) {
			column = getTable().convertColumnIndexToModel(column);
			((IDiskoTableModel)model).setHeaderTooltipText(column,text);
		}
	}

	public boolean isEditable(int column) {
		TableModel model = getTable().getModel();
		if(column!=-1 && model instanceof IDiskoTableModel) {
			column = getTable().convertColumnIndexToModel(column);
			return ((IDiskoTableModel)model).isHeaderEditable(column);
		}
		return false;
	}

	public void setEditable(int column, boolean isEditable) {
		TableModel model = getTable().getModel();
		if(model instanceof IDiskoTableModel) {
			column = getTable().convertColumnIndexToModel(column);
			((IDiskoTableModel)model).setHeaderEditable(column,isEditable);
		}
	}

	public boolean editAt(int column){
		if (m_editor!=null && isEditable(column)) {
			setEditColumn(column);
			m_editor.doClick();
			return true;
		}
		return false;
	}

	public void addActionListener(ActionListener listener) {
		m_listeners.add(ActionListener.class, listener);
	}

	public void removeActionListener(ActionListener listener) {
		m_listeners.remove(ActionListener.class, listener);
	}

	public String getEditor() {
		return m_editor!=null ? m_editor.getEditor() : null;
	}

	public void setEditor(String name) {
		if (m_editor!=null) m_editor.setEditor(name);
	}

	/* ===============================================================
	 * PopupManager implementation (wrapper)
	 * =============================================================== */

	public JPopupMenu createPopupMenu(String name) {
		return m_popupMananger.createPopupMenu(name);
	}

	public boolean installHeaderPopup(String menu) {
		boolean bFlag = false;
		bFlag = installPopup(menu,this,true);
		if(m_editor!=null) bFlag |= installPopup(menu,m_editor.getComponent(),true);
		return bFlag;
	}

	public boolean installEditorPopup(String menu, String editor) {
		if(m_editor!=null) {
			// get button
			JComponent c = m_editor.getEditorComponent(editor);
			// found button?
			if(c!=null) return installPopup(menu,c,false);
		}
		// not installed
		return false;
	}

	public boolean installPopup(String menu, JComponent component, boolean onMouseAction) {
		return m_popupMananger.installPopup(menu, component, onMouseAction);
	}

	public JPopupMenu getPopupMenu(String name) {
		return m_popupMananger.getPopupMenu(name);
	}

	public JPopupMenu getPopupMenu(JComponent component) {
		return m_popupMananger.getPopupMenu(component);
	}

	public JPopupMenu getEditorPopupMenu(String editor) {
		if(m_editor!=null) {
			// get button
			JComponent c = m_editor.getEditorComponent(editor);
			if(c!=null) {
				return getPopupMenu(c);
			}
		}
		return null;
	}

	public List<JComponent> getInstalledPopups(String menu) {
		return m_popupMananger.getInstalledPopups(menu);
	}

	public boolean addMenuItem(
			String menu,
			String caption,
			String name) {
		return m_popupMananger.addMenuItem(menu, caption, name);
	}

	public boolean addMenuItem(
			String menu,
			String caption,
			String icon,
			String catalog,
			String name) {
		return m_popupMananger.addMenuItem(menu, caption, icon, catalog, name);
	}

	public boolean addMenuItem(String menu, Component item, String name) {
		return m_popupMananger.addMenuItem(menu, item, name);
	}

	public boolean removeMenuItem(String menu, String name) {
		return m_popupMananger.removeMenuItem(menu, name);
	}

	public Component findMenuItem(String menu, String name) {
		return m_popupMananger.findMenuItem(menu, name);
	}

	public boolean isMenuItemEnabled(String menu, String name) {
		return m_popupMananger.isMenuItemEnabled(menu, name);
	}

	public void setMenuItemEnabled(String menu, String name, boolean isEnabled) {
		m_popupMananger.setMenuItemEnabled(menu, name,isEnabled);
	}

	public boolean isMenuItemVisible(String menu, String name) {
		return m_popupMananger.isMenuItemVisible(menu, name);
	}

	public void setMenuItemVisible(String menu, String name, boolean isVisible) {
		m_popupMananger.setMenuItemVisible(menu, name,isVisible);
	}

	public int getEditColumn() {
		return m_editColumn;
	}

	public int getMouseColumn() {
		return m_mouseColumn;
	}

	/* ===============================================================
	 * Helper methods
	 * =============================================================== */

	protected RowSorter<?> getRowSorter() {
		// unregister?
		if(getTable()!=null) {
			return getTable().getRowSorter();
		}
		return null;
	}

	protected void fireActionPerformed(ActionEvent e) {
		ActionListener[] list = m_listeners.getListeners(ActionListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].actionPerformed(e);
		}
	}

	protected void setEditColumn(int column){
		if(m_editor!=null) {
			// any change?
			if(m_editColumn!=column) {
				// add?
				if(column!=-1) {
					// prepare
					JTable table = getTable();
					Object value = columnModel.getColumn(column).getHeaderValue();
					// this will show edit button in the header panel if header is editable
					m_editor.getTableCellRendererComponent(table, value, false, false, -1, column);
					m_editor.setBounds(getHeaderRect(column));
					// add to header
					add(m_editor.getComponent());
				}
				else  {
					// remove from header
					remove(m_editor.getComponent());
					// refresh
					repaint(getHeaderRect(m_editColumn));
				}
				// update edit column index
				m_editColumn = column;
			}
		}
	}

	protected void setMouseColumn(int column) {
		m_mouseColumn = column;
	}

	private static int getColumn(MouseEvent e) {
		JTableHeader header = (JTableHeader)e.getSource();
		JTable table = header.getTable();
		TableColumnModel colModel = table.getColumnModel();
		return colModel.getColumnIndexAtX(e.getX());
	}

	private static void drawArrow(Graphics2D g2d, int xCenter, int yCenter, int x, int y, float stroke, int seq) {

		// get current state
		Stroke oldStroke = g2d.getStroke();
		Color oldColor = g2d.getColor();
		Font oldFont = g2d.getFont();

		// update draw state
		g2d.setStroke(new BasicStroke(1f));
		g2d.setColor(Color.WHITE);
		g2d.setFont(oldFont.deriveFont(7f));

		// get direction
		double aDir=Math.atan2(xCenter-x,yCenter-y);

		//g2d.drawLine(x,y,xCenter,yCenter);

		// make the arrow head solid even if dash pattern has been specified
		Polygon tmpPoly=new Polygon();
		int i1=4+(int)(stroke*1.5);
		// make the arrow head the same size regardless of the length length
		int i2=4+(int)stroke;
		// arrow tip
		tmpPoly.addPoint(x,y);
		tmpPoly.addPoint(x+xCor(i1,aDir+2.0),y+yCor(i1,aDir+.25));
		tmpPoly.addPoint(x+xCor(i2,aDir),y+yCor(i2,aDir));
		tmpPoly.addPoint(x+xCor(i1,aDir-2.0),y+yCor(i1,aDir-.25));
		// arrow tip
		tmpPoly.addPoint(x,y);
		g2d.drawPolygon(tmpPoly);
		// remove this line to leave arrow head unpainted
		g2d.fillPolygon(tmpPoly);
		// draw sequence number?
		if(seq>=0) {
			String text = String.valueOf(seq);
			int w = g2d.getFontMetrics().stringWidth(text);
			x -= Math.ceil(w/2)-1;
			y = yCenter-y >= 0 ? y + 6 : y-1;
			g2d.setColor(Color.GRAY);
			g2d.drawString(String.valueOf(seq), x, y);
		}
		// resume old state
		g2d.setStroke(oldStroke);
		g2d.setColor(oldColor);
		g2d.setFont(oldFont);

	}

	private static int yCor(int len, double dir) {return (int)(len * Math.cos(dir));}
	private static int xCor(int len, double dir) {return (int)(len * Math.sin(dir));}

	/* ===================================================================
	 * Inner classes
	 * =================================================================== */

	private class ColumnHeaderToolTips extends MouseMotionAdapter {

		// Current column whose tooltip is being displayed.
		// This variable is used to minimize the calls to setToolTipText().
		int m_current;

		public void mouseMoved(MouseEvent e) {
			int column = getColumn(e);
			if (column != m_current) {
				DiskoTableHeader.super.setToolTipText(getToolTipText(column));
				m_current = column;
			}

		}

		public int getToolTipColumn() {
			return m_current;
		}

	}

	/**
	 * This class is used to hook mouse events in BasicTableHeaderUI
	 *
	 * @author Nobuo Tamemas, edited by Kenneth Gulbrandsøy
	 *
	 * @see http://www.esus.com/javaindex/j2se/jdk1.2/javaxswing/editableatomiccontrols/jtable/jtableeditableheader.html
	 *
	 * @version 1.0 08/21/99, 1.1 13/09/08
	 */
	private class EditableHeaderUI extends BasicTableHeaderUI {

		@Override
		protected MouseInputListener createMouseInputListener() {
			return new MouseInputHandler((DiskoTableHeader)header);
		}

		public class MouseInputHandler extends BasicTableHeaderUI.MouseInputHandler {

			private Component m_editor;
			private DiskoTableHeader m_header;

			/* =========================================================
			 * Constructors
			 * ========================================================= */

			public MouseInputHandler(DiskoTableHeader header) {
				this.m_header = header;
			}

			/* =========================================================
			 * Overridden methods
			 * ========================================================= */

			@Override
			public void mouseMoved(MouseEvent e) {
				setMouseColumn(getColumn(e));
				// discard draw operations
				if (m_header.getResizingColumn() == null) {
					Point p = e.getPoint();
					TableColumnModel model = m_header.getColumnModel();
					int col = model.getColumnIndexAtX(p.x);
					if (col != -1) {
						if (m_header.isEditable(col)) {
							setEditColumn(col);
							setHook(e);
							repostEvent(e);
							// refresh?
							if(m_editor!=null) m_editor.validate();
						}
						else {
							setEditColumn(-1);
							m_editor=null;
						}
					}
				}
				else {
					setEditColumn(-1);
					m_editor=null;
				}
				super.mouseMoved(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				setMouseColumn(getColumn(e));
				repostEvent(e);
				super.mouseExited(e);
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				setMouseColumn(getColumn(e));
				repostEvent(e);
				super.mouseClicked(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				setMouseColumn(getColumn(e));
				repostEvent(e);
				super.mousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				setMouseColumn(getColumn(e));
				repostEvent(e);
				super.mouseReleased(e);
			}

			/* =========================================================
			 * Helper methods
			 * ========================================================= */

			private void setHook(MouseEvent e) {
				if(m_header.m_editor!=null) {
					Point p = e.getPoint();
					Component editor = m_header.m_editor.getComponent();
					Point p2 = SwingUtilities.convertPoint(m_header, p, editor);
					m_editor = SwingUtilities.getDeepestComponentAt(editor, p2.x, p2.y);
				}
			}

			private boolean repostEvent(MouseEvent e) {
				if(m_editor != null) {
					MouseEvent e2 = SwingUtilities.convertMouseEvent(m_header,e,m_editor);
					m_editor.dispatchEvent(e2);
					return true;
				}
				return false;
			}

		}
	}

}
