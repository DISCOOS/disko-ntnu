package org.redcross.sar.gui.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

import org.redcross.sar.data.IData.DataOrigin;

public abstract class DiskoTableModel extends AbstractTableModel implements ITableModel {

	private static final long serialVersionUID = 1L;

	protected List<String> names;
	protected List<String> captions;
	protected List<String> tooltips;
	protected List<Boolean> editable;
	protected List<String> editors;
	protected List<Integer> alignments;
	protected List<Integer> fixedwidths;

	/* =============================================================================
	 * Constructors
	 * ============================================================================= */

	public DiskoTableModel() {
		// forward
		super();
		// prepare
		this.captions = new ArrayList<String>(10);
		this.tooltips = new ArrayList<String>(10);
		this.editable = new ArrayList<Boolean>(10);
		this.names = new ArrayList<String>(10);
		this.editors = new ArrayList<String>(10);
		this.alignments = new ArrayList<Integer>(10);
		this.fixedwidths = new ArrayList<Integer>(10);
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
		int size = names!=null ? names.length : 0;

		// prepare
		this.captions = new ArrayList<String>(size);
		this.tooltips = new ArrayList<String>(size);
		this.editable = new ArrayList<Boolean>(size);
		this.names = new ArrayList<String>(size);
		this.editors = new ArrayList<String>(size);
		this.alignments = new ArrayList<Integer>(size);
		this.fixedwidths = new ArrayList<Integer>(size);

		// create lists
		for(int i=0;i<size;i++) {
			this.names.add(names[i].toString());
			this.captions.add(captions[i].toString());
			this.tooltips.add(tooltips[i].toString());
			this.editable.add(editable[i]);
			this.editors.add(editors[i]);
			this.alignments.add(SwingConstants.LEFT);
			this.fixedwidths.add(-1);
		}

	}

	/* =============================================================================
	 * Public methods
	 * ============================================================================= */

	public void create(Object[] names, Object[] captions) {
		// use captions as tooltips
		create(names, captions, captions.clone(),
				defaultEditable(names.length),
				defaultEditors(names.length,"button"));
	}

	public void create(Object[] names, Object[] captions, Object[] tooltips, Object[] editable, Object[] editors) {
		// forward
		create(names, captions, tooltips, editable, editors, true);

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

	public int getColumnAlignment(int column) {
		return alignments.get(column);
	}

	public void setColumnAlignment(int column, int alignment) {
		alignments.set(column,alignment);
	}

	public boolean isColumnWidthFixed(int column) {
		return (fixedwidths.get(column).intValue()>-1);
	}
	
	public int getColumnFixedWidth(int column) {
		return fixedwidths.get(column).intValue();
	}
	
	public void setColumnFixedWidth(int column, int fixedwidth) {
		fixedwidths.set(column,fixedwidth);
	}
	
	public IState getState(int row)
	{
		return new IState() {

			@Override
			public DataOrigin getOrigin() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean isLoopbackMode() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isRollbackMode() {
				// TODO Auto-generated method stub
				return false;
			}
			
		};
	}
	
	/* =============================================================================
	 * Protected static methods
	 * ============================================================================= */

	protected void create(Object[] names, Object[] captions, Object[] tooltips, Object[] editable, Object[] editors, boolean notify) {
		// uninstall
		this.names.clear();
		this.captions.clear();
		this.tooltips.clear();
		this.editable.clear();
		this.editors.clear();
		this.alignments.clear();
		this.fixedwidths.clear();

		// get size
		int size = names!=null ? names.length : 0;
		// fill lists
		for(int i=0;i<size;i++) {
			this.names.add(names[i].toString());
			this.captions.add(captions[i].toString());
			this.tooltips.add(tooltips[i].toString());
			this.editable.add(editable[i] instanceof Boolean ? (Boolean)editable[i] : false);
			this.editors.add(editors[i].toString());
			this.alignments.add(SwingConstants.LEFT);
			this.fixedwidths.add(-1);
		}
		// notify?
		if(notify) fireTableStructureChanged();
	}

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

	@Override
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

}

