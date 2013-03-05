package org.redcross.sar.gui.tree;

import java.io.File;

import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.TreePath;

import org.redcross.sar.gui.model.FileTreeModel;
import org.redcross.sar.gui.table.FileTreeCellRenderer;
import org.redcross.sar.util.Utils;

public class FileTree extends JTree {

	private static final long serialVersionUID = 1L;

	/* =====================================================
	 * Constructors
	 * ===================================================== */

	public FileTree() {
		this(new FileTreeModel());
		((FileTreeModel)getModel()).setFilter(FileTree.createDefaultFilter());

	}

	public FileTree(String root) {
		this(new FileTreeModel(root));
		((FileTreeModel)getModel()).setFilter(FileTree.createDefaultFilter());
	}

	public FileTree(FileTreeModel model) {
		// forward
		super(model);
		// prepare
		setRowHeight(18);
		setRootVisible(false);
		setShowsRootHandles(true);
		setExpandsSelectedPaths(true);
		setCellRenderer(new FileTreeCellRenderer());
		putClientProperty("JTree.lineStyle", "Angled");
	}

	/* =====================================================
	 * Overridden methods
	 * ===================================================== */

	@Override
	public String convertValueToText(
			Object value, boolean selected,
			boolean expanded, boolean leaf, int row,boolean hasFocus) {

		// get file name
		return value!=null ? ((File)value).getName() : null;

	}

	/* =====================================================
	 * Public methods
	 * ===================================================== */

	public TreePath getPath(String child) {
		File file = new File(child);
		if(file.exists()) {
			Object[] path = {file};
			File parent = file.getParentFile();
			if(parent!=null) path = getPath(parent,path);
			return new TreePath(path);
		}
		return null;
	}

	/* =====================================================
	 * Static methods
	 * ===================================================== */

	public static TreeFilter createDefaultFilter() {

		TreeFilter filter = new AbstractTreeFilter() {

			@Override
			public boolean isShown(Object obj) {
				File file = ((File)obj);
				FileSystemView view = FileSystemView.getFileSystemView();
				return isEnabled() ? view.isFileSystemRoot(file) || !file.isHidden() : true;
			}

		};

		return filter;

	}

	public static TreeFilter createExtensionFilter(final String ext) {

		TreeFilter filter = new AbstractTreeFilter() {

			@Override
			public boolean isShown(Object obj) {
				File file = ((File)obj);
				String found = Utils.getExtension(file);
				return isEnabled() ? !file.isHidden()
						&& (found.equalsIgnoreCase(ext) || file.isDirectory()): true;
			}

		};

		return filter;

	}

	public static TreeFilter createCatalogFilter() {

		TreeFilter filter = new AbstractTreeFilter() {

			@Override
			public boolean isShown(Object obj) {
				File file = ((File)obj);
				FileSystemView view = FileSystemView.getFileSystemView();
				return isEnabled() ? view.isFileSystemRoot(file) || !file.isHidden() && file.isDirectory() : true;
			}

		};

		return filter;

	}

	/* =====================================================
	 * Helper methods
	 * ===================================================== */

	private Object[] getPath(File child, Object[] path) {
		Object[] newPath = addHead(child, path);
		File parent = child.getParentFile();
		if(parent!=null) newPath = getPath(parent,newPath);
		return newPath;

	}

	private Object[] addHead(File file, Object[] path) {
		Object[] newPath = new Object[path.length+1];
		newPath[0] = file;
		for(int i=1;i<newPath.length;i++) {
			newPath[i] = path[i-1];
		}
		return newPath;
	}

}
