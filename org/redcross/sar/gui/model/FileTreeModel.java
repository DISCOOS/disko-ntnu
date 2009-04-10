package org.redcross.sar.gui.model;

import java.io.File;

import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;

import org.redcross.sar.gui.event.TreeFilterEvent;
import org.redcross.sar.gui.event.TreeFilterListener;
import org.redcross.sar.gui.tree.TreeFilter;

public class FileTreeModel extends AbstractTreeModel {

	public static final String COMPUTER = "Computer";
	
	protected String root;
	protected TreeFilter filter;
		
	/* =====================================================
	 * Constructors
	 * ===================================================== */
	
	public FileTreeModel() {
		this(COMPUTER);
	}
	
	public FileTreeModel(File root) {
		this(root!=null ? root.getAbsolutePath() : COMPUTER);
	}
	
	public FileTreeModel(String root) {
		this.root = root;
	}	

	/* =====================================================
	 * Public methods
	 * ===================================================== */

	public void setFilter(TreeFilter filter) {
		if(this.filter != filter) {
			if(this.filter != null) {
				filter.removeTreeFilterListener(m_listener);
			}
			this.filter = filter;
			if(this.filter != null) {
				filter.addTreeFilterListener(m_listener);
			}			
			// notify change
			fireTreeStructureChanged();
		}
	}
	
	public TreeFilter getFilter() {
		return filter;
	}
	
	public void setRoot(String file) {
		root = file;
		fireTreeStructureChanged();
	}
	
	public void setRoot(File file) {
		root = file.getAbsolutePath();		
	}
	
	/* =====================================================
	 * TreeModel implementation
	 * ===================================================== */
	
	public File getRoot()
	{
		return new File(root);
	}

	public boolean isLeaf(Object node)
	{
		return ((File)node).isFile();
	}

	public int getChildCount(Object parent)
	{
		return getChildCount(parent,true);
	}

	public int getChildCount(Object parent, boolean filtered)
	{
		if(filtered && filter!=null) {
			int shown = 0;
			int count = getChildCount(parent,false);			
			for (int i=0; i<count; i++) {
			  Object node = getChild(parent,i,false);
			  if (filter.isShown(node)) shown++;
			}
			return shown;
		}
		else { 
			File file = ((File)parent);
			if(COMPUTER.equals(file.toString())) {
				// get system roots
				File[] roots = File.listRoots();
				if(roots == null) return 0;
				return roots.length;
			}
			else {
				String[] children = file.list();
				if(children == null) return 0;
				return children.length;
			}
		}
	}
	
	public File getChild(Object parent, int index)
	{
		return getChild(parent,index,true);
	}

	public File getChild(Object parent, int index, boolean filtered)
	{
		if(filtered && filter!=null) {
			int count = getChildCount(parent,false);
			int shown=-1;
		    for (int i=0; i<count; i++) {
		      File child = getChild(parent,i,false);
		      if (filter.isShown(child)) shown++;
		      if (shown==index) return child;
		    }
		    return (File)parent;
		}
		else {
			File file = ((File)parent);
			if(COMPUTER.equals(file.toString())) {
				// get system roots
				File[] roots = File.listRoots();
				if ((roots == null) || (index >= roots.length))
					return null;		
				return roots[index];
			}
			else {
				String[] children = file.list();
				if ((children == null) || (index >= children.length))
					return null;		
				File child = new File(file,children[index]);
				return child;
			}
		}
	}
	
	public int getIndexOfChild(Object parent,Object child)
	{
		return getIndexOfChild(parent,child,true);
	}

	public int getIndexOfChild(Object parent, Object child, boolean filtered)
	{
		
		// is filtered?
		if(filtered && filter!=null && !filter.isShown(child)) {
			return -1;
		}
		
		File file = ((File)parent);
		String childname = ((File)child).getName();
		
		// loop over roots or children?
		if(COMPUTER.equals(file.toString())) {
			// get system roots
			File[] roots = File.listRoots();
			for(int i=0;i < roots.length;i++)
			{
				if(childname.equals(roots[i].getName())) return i;
			}
		}
		else {
		
			String[] children = file.list();     
			if (children == null) return -1;
			for(int i=0;i < children.length;i++)
			{
				if(childname.equals(children[i])) return i;
			}
		}
		// failed
		return -1;
	}
	
	public void valueForPathChanged(TreePath path, Object newvalue){ /*NOP*/ }
	
	/* =====================================================
	 * Helper methods
	 * ===================================================== */

	private void fireTreeStructureChanged() {		
	    Object[] path = {new File(root)};
	    int count = getChildCount(path[0]);
	    int[] childIndices  = new int[count];
	    Object[] children  = new Object[count];
	    for (int i = 0; i < count; i++) {
	      childIndices[i] = i;
	      children[i] = getChild(path[0],i);
	    }	    
	    // forward
	    fireTreeStructureChanged(new TreeModelEvent(this,path,childIndices,children));
	}
	
	/* =====================================================
	 * Anonymous classes
	 * ===================================================== */
	
	private final TreeFilterListener m_listener = new TreeFilterListener() {

		@Override
		public void filterChanged(TreeFilterEvent e) {
			// forward
			fireTreeStructureChanged();			
		}
		
	};
	

	
}
