
package org.redcross.sar.gui.panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.model.DiskoTableModel;
import org.redcross.sar.gui.table.DiskoTable;


public class LogPanel extends BasePanel {

	private static final long serialVersionUID = 1L;
	

	private DiskoTable table;
	private LogTableModel model;
	private JButton clearButton;
	
	private Logger logger;
	
	public LogPanel(Logger logger) {
		// forward
		this(logger, "Log");
	}
	
	public LogPanel(Logger logger, String caption) {
		// forward
		super(caption);
		// prepare
		this.logger = logger;
		// initialize GUI
		initialize();
	}
	
	/**
	 * Initialize this
	 */
	private void initialize() {
		// insert button
		insertButton("finish", getCreateButton(), "clear");
		// set table
		setContainer(getTable());
		// add listener
		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// get command
				String cmd = e.getActionCommand();
				// clear logged events?
				if("clear".equalsIgnoreCase(cmd)) {
					getTable().getModel();
				}				
			}
			
		});
	}
	
	/**
	 * Initialize the table 
	 */
	private DiskoTable getTable() {
		if(table == null) {
			table = new DiskoTable(getModel());
			table.setTableHeader(null);
		}
		return table;
	}
	
	/**
	 * Initialize the table model 
	 */
	private LogTableModel getModel() {
		if(model == null) {
			model = new LogTableModel();
		}
		return model;
	}
	
	/**
	 * Initialize the create button
	 */
	private JButton getCreateButton() {
		if(clearButton == null) {
			clearButton = DiskoButtonFactory.createButton("SYSTEM.ROLLBACK", getButtonSize());
		}
		return clearButton;
	}
	
	private class LogTableModel extends DiskoTableModel implements Appender {

		private static final long serialVersionUID = 1L;
		
		private final Appender appender;
		
		private final List<LoggingEvent> rows = new ArrayList<LoggingEvent>();

		public LogTableModel() {
			// create appender
			appender = new AppenderSkeleton() {

				@Override
				protected void append(LoggingEvent e) {
					// add to table
					rows.add(e);	
					// notify
					fireTableDataChanged();
				}

				@Override
				public void close() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public boolean requiresLayout() {
					// TODO Auto-generated method stub
					return false;
				}
				
			};
			// set layout
			setLayout(new PatternLayout("%d{dd MMM yyyy HH:mm:ss,SSS}: %m%n"));
			// listen for events
			logger.addAppender(this);
		}
		
		@Override
		public int getRowCount() {
			return rows.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			// get event
			LoggingEvent e = rows.get(row);
			// apply format
			return getLayout().format(e);
		}

		/* ================================================
		 * Appender wrapper
		 * ================================================ */
		
		public void addFilter(Filter filter) {
			appender.addFilter(filter);
		}

		public void clearFilters() {
			appender.clearFilters();
		}

		public void close() {
			appender.close();
		}

		public void doAppend(LoggingEvent event) {
			appender.doAppend(event);			
		}

		public ErrorHandler getErrorHandler() {
			return appender.getErrorHandler();
		}

		public Filter getFilter() {
			return appender.getFilter();
		}

		public Layout getLayout() {
			return appender.getLayout();
		}

		public String getName() {
			return appender.getName();
		}

		public boolean requiresLayout() {
			return appender.requiresLayout();
		}

		public void setErrorHandler(ErrorHandler handler) {
			appender.setErrorHandler(handler);			
		}

		public void setLayout(Layout layout) {
			appender.setLayout(layout);		
		}

		public void setName(String name) {
			appender.setName(name);
		}
		
	}
	
}
