package org.redcross.sar.gui.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public abstract class DiskoTableModel extends AbstractTableModel  
							implements IDiskoTableModel {

	private static final long serialVersionUID = 1L;

	protected List<String> names;
	protected List<String> captions;
	protected List<String> tooltips;
	protected List<Boolean> editable;
	protected List<String> editors;
	
	/* =============================================================================
	 * Constructors
	 * ============================================================================= */
	
	public DiskoTableModel() {
		super();
	}
	
	public DiskoTableModel(String[] names, String[] captions) {
		// forward
		this(names,captions,captions.clone(),
				defaultEditable(names.length),
				defaultEditors(names.length,"button"));
	}
	
	public DiskoTableModel(String[] names, String[] captions, String[] tooltips) {
		// forward
		this(names,captions,tooltips,
				defaultEditable(names.length),
				defaultEditors(names.length,"button"));		
	}
	
	public DiskoTableModel(String[] names,
						   String[] captions,
						   String[] tooltips,
						   Boolean[] editable,
						   String[] editors) {
		// forward
		super();
		
		// get size
		int size = names.length;
		
		// prepare			
		this.captions = new ArrayList<String>(size);
		this.tooltips = new ArrayList<String>(size);
		this.editable = new ArrayList<Boolean>(size);
		this.names = new ArrayList<String>(size);
		this.editors = new ArrayList<String>(size);
		
		// create lists
		if(names!=null) {
			for(int i=0;i<names.length;i++) {
				this.names.add(names[i].toString());
				this.captions.add(captions[i].toString());
				this.tooltips.add(tooltips[i].toString());
				this.editable.add(editable[i]);
				this.editors.add(editors[i]);
			}
		}
		
	}
	
	/* =============================================================================
	 * Public methods
	 * ============================================================================= */
	
	public void install(Object[] names, Object[] captions) {
		// use captions as tooltips
		install(names, captions, captions.clone(),
				defaultEditable(names.length),
				defaultEditors(names.length,"button"));
	}
			
	public void install(Object[] names, Object[] captions, Object[] tooltips, Object[] editable, Object[] editors) {
		// uninstall
		this.names.clear();
		this.captions.clear();
		this.tooltips.clear();
		this.editable.clear();
		this.editors.clear();
		// add attributes?
		if(names!=null) {
			for(int i=0;i<names.length;i++) {
				this.names.add(names[i].toString());
				this.captions.add(captions[i].toString());
				this.tooltips.add(tooltips[i].toString());
				this.editable.add(editable[i] instanceof Boolean ? (Boolean)editable[i] : false);
				this.editors.add(editors[i].toString());
			}
		}
	}
	
	/* =============================================================================
	 * IDiskoTableModel implementation
	 * ============================================================================= */
		
	public String getHeaderTooltipText(int column) {
		return tooltips!=null ? tooltips.get(column) : getColumnName(column);
	}
	
	public void setHeaderTooltipText(int column, String text) {
		if(tooltips!=null) tooltips.set(column,text);
	}
	
	public boolean isHeaderEditable(int column) {
		return editable!=null ? editable.get(column) : false;
	}
	
	public void setHeaderEditable(int column, boolean isEditable) {
		if(editable!=null) editable.set(column,isEditable);
	}
		
	public String getHeaderEditor(int column) {
		return editors!=null ? editors.get(column) : "button";
	}
	
	public void setHeaderEditor(int column, String name) {
		if(editors!=null) editors.set(column,name);
	}
	
	/* =============================================================================
	 * Protected methods
	 * ============================================================================= */
	
	protected static Boolean[] defaultEditable(int size) {
		Boolean[] editable = new Boolean[size];
		for(int i=0;i<size;i++)
			editable[i] = false;
		return editable;
	}
	
	protected static String[] defaultEditors(int size, String name) {
		String[] editors = new String[size];
		for(int i=0;i<size;i++)
			editors[i] = name;
		return editors;
	}
	
	/* =============================================================================
	 * AbstractTableModel methods
	 * ============================================================================= */
	
	public int getColumnCount() {
		return names!=null ? names.size() : 0;
	}

	@Override
	public String getColumnName(int column) {
		return captions!=null ? captions.get(column) : super.getColumnName(column);
	}
		
	@Override
	public int findColumn(String name) {
		if(captions!=null) {
			for(int i=0; i<captions.size();i++) {
				if(captions.get(i).equals(name))
					return i;
			}
			return -1;
		}
		return super.findColumn(name);
	}
	
	/* =============================================================================
	 * Required methods
	 * ============================================================================= */
	
	@Override
	public abstract int getRowCount();

	@Override
	public abstract Object getValueAt(int row, int col);
	
}
	
