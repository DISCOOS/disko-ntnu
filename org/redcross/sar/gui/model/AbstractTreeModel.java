package org.redcross.sar.gui.model;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public abstract class AbstractTreeModel implements TreeModel {

	private final EventListenerList listeners = new EventListenerList();
	
	/* ============================================================
	 * TreeModel implementation
	 * ============================================================ */
	
	public abstract Object getChild(Object parent, int index);

	public abstract int getChildCount(Object parent);

	public abstract int getIndexOfChild(Object parent, Object child);

	public abstract Object getRoot();

	public abstract boolean isLeaf(Object node);
	
	public abstract void valueForPathChanged(TreePath path, Object newValue);
	
	public void addTreeModelListener(TreeModelListener listener) 
	{
		listeners.add(TreeModelListener.class, listener);
	}

	public void removeTreeModelListener(TreeModelListener listener) 
	{
		listeners.remove(TreeModelListener.class, listener);		
	}
	
	/* ============================================================
	 * Helper methods
	 * ============================================================ */

	protected void fireTreeNodesChanged(TreeModelEvent e) {
		TreeModelListener[] list = listeners.getListeners(TreeModelListener.class);
		for(int i=0;i<list.length;i++)
			list[i].treeNodesChanged(e);
	}

	protected void fireTreeNodesInserted(TreeModelEvent e) {
		TreeModelListener[] list = listeners.getListeners(TreeModelListener.class);
		for(int i=0;i<list.length;i++)
			list[i].treeNodesInserted(e);
	}
	
	protected void fireTreeNodesRemoved(TreeModelEvent e) {
		TreeModelListener[] list = listeners.getListeners(TreeModelListener.class);
		for(int i=0;i<list.length;i++)
			list[i].treeNodesRemoved(e);
	}

	protected void fireTreeStructureChanged(TreeModelEvent e) {	
		TreeModelListener[] list = listeners.getListeners(TreeModelListener.class);
		for(int i=0;i<list.length;i++)
			list[i].treeStructureChanged(e);
	}
	
}
