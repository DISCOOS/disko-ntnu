package org.redcross.sar.wp.messageLog;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.redcross.sar.gui.DiskoBorder;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IMessageLineListIf;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;

/**
 * Panel displaying message lines in the currently selected message
 *
 * @author thomasl
 */
public class MessageLinePanel extends JPanel implements IEditorIf
{
	private final static long serialVersionUID = 1L;

	private JTable m_table;
	private MessageLineTableModel m_model;
	private JScrollPane m_textScrollPane;
	private IDiskoWpMessageLog m_wp;

	/* =========================================================
	 * Constructors
	 * ========================================================= */

	/**
	 * @param wp Message log work process
	 */
	public MessageLinePanel(IDiskoWpMessageLog wp)
	{
		// forward
		super();

		// prepare
		m_wp = wp;

		// add empty border
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// set border layout
		setLayout(new BorderLayout());

		// create table
		m_model = new MessageLineTableModel();
		m_table = new JTable(m_model);
		m_table.setBorder(null);
		m_table.setDefaultRenderer(IMessageLineIf.class, new MessageLineTableRenderer());
		m_table.setTableHeader(null);
		m_table.setRowHeight(32);
		m_textScrollPane = UIFactory.createScrollPane(m_table);
		m_textScrollPane.setBorder(new DiskoBorder());
		m_table.setFillsViewportHeight(true);
		m_table.setColumnSelectionAllowed(false);
		m_table.setRowSelectionAllowed(true);
		m_table.setShowVerticalLines(false);
		m_table.setShowHorizontalLines(true);

		m_table.addMouseListener(new MessageLineMouseAdapter(m_model));

		add(m_textScrollPane, BorderLayout.CENTER);
	}

	/* =========================================================
	 * IEditorIf implementation
	 * ========================================================= */

	/**
	 * Updates message line list model with message lines in message
	 */
	public void setMessage(IMessageIf message)
	{

		// get message line list
		IMessageLineListIf list = message.getMessageLines();

		// do cleanup

		// disconnect?
		if(list == null)
		{
			m_model.disconnectAll();
			m_model.clear();
		}
		else {
			// connect to source
			m_model.connect(m_wp.getMsoModel(),list,IMessageLineIf.LINE_NUMBER_COMPARATOR);
			m_model.load(list);
		}
	}

	/**
	 *
	 */
	public void showEditor()
	{
		this.setVisible(true);
	}

	/**
	 *
	 */
	public void hideEditor()
	{
		this.setVisible(false);
	}

	/**
	 *
	 */
	public void reset()
	{
		m_model.clear();
	}

	public MessageLineType getSelectedMessageLineType() {
		int row = m_table.getSelectedRow();
		int col = m_table.getSelectedColumn();
		if(row!=-1 && col!=-1) {
			IMessageLineIf line = (IMessageLineIf)m_model.getValueAt(row, col);
			return line!=null ? line.getLineType() : null;
		}
		return null;
	}
}
