package org.redcross.sar.gui.dialog;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.RowFilter;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.TreePath;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.model.FileTableModel;
import org.redcross.sar.gui.model.FileTreeModel;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.table.FileTable;
import org.redcross.sar.gui.tree.FileTree;
import org.redcross.sar.util.Utils;

public class FileExplorerDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;

	private boolean isCancel;
	private Object selected;
	private FileTree fileTree;
	private FileTable fileTable;
	private JSplitPane splitter;
	private DefaultPanel contentPanel;

	/* ==================================================
	 * Constructors
	 * ================================================== */

	public FileExplorerDialog() {
		// forward
		super(Utils.getApp().getFrame());
		// initialize GUI
		initialize();
	}

	public FileExplorerDialog(Frame owner) {
		// forward
		super(owner);
		// initialize GUI
		initialize();
	}

	/* ==================================================
	 * Public methods
	 * ================================================== */

	public File getRoot() {
		FileTreeModel model = (FileTreeModel)getFileTree().getModel();
		return model.getRoot();
	}

	public void setRoot(String file) {
		FileTreeModel treeModel = (FileTreeModel)getFileTree().getModel();
		treeModel.setRoot(file);
		getFileTree().setSelectionInterval(0, 0);
	}

	public void setRoot(File file) {
		FileTreeModel model = (FileTreeModel)getFileTree().getModel();
		model.setRoot(file);
	}

	@SuppressWarnings("unchecked")
	public RowFilter<FileTableModel, Integer> getFilter() {
		TableRowSorter<FileTableModel> sorter = (TableRowSorter<FileTableModel>)getFileTable().getRowSorter();
		return (RowFilter<FileTableModel, Integer>)sorter.getRowFilter();
	}

	@SuppressWarnings("unchecked")
	public void setFilter(RowFilter<FileTableModel, Integer> filter) {
		TableRowSorter<FileTableModel> sorter = (TableRowSorter<FileTableModel>)getFileTable().getRowSorter();
		sorter.setRowFilter(filter);
	}

	public Object select() {
		return select(
				getContentPanel().getCaptionText(),
				getContentPanel().getCaptionIcon());
	}

	public Object select(String title) {
		return select(title,getContentPanel().getCaptionIcon());
	}

	public Object select(String title, Icon icon) {

		getContentPanel().setCaptionIcon(icon);
		getContentPanel().setCaptionText(title);
		// set flags
		this.isCancel = false;
		// show dialog
		setVisible(true);
		// finished
		return isCancel ? null : selected;
	}

	public Object select(String selected, String title, Icon icon) {
		// update caption
		getContentPanel().setCaptionIcon(icon);
		getContentPanel().setCaptionText(title);
		// set flags
		this.isCancel = false;
		// get file
		File file = new File(selected);
		// select in tree and table?
		if(file.exists()) {
			TreePath path = getFileTree().getPath(file.getPath());
			getFileTree().setSelectionPath(path);
			int row = getFileTable().findRow(file);
			if(row!=-1) getFileTable().getSelectionModel().setSelectionInterval(row, row);
		}
		// show dialog
		setVisible(true);
		// finished
		return isCancel ? null : this.selected;
	}

	/* ==================================================
	 * Helper methods
	 * ================================================== */

	private void initialize() {
		try {
            this.setModal(true);
            this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            this.setContentPane(getContentPanel());
            this.setEscapeable(true);
            this.pack();

		}
		catch (java.lang.Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private DefaultPanel getContentPanel() {
		if (contentPanel == null) {
			contentPanel = new DefaultPanel("Velg fil");
			contentPanel.setPreferredSize(new Dimension(600,300));
			contentPanel.setRequestHideOnFinish(true);
			contentPanel.setRequestHideOnCancel(true);
			contentPanel.setContainer(getSplitter());
			contentPanel.setContainerBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			contentPanel.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String cmd = e.getActionCommand();
					if("finish".equalsIgnoreCase(cmd)) {
						int row = getFileTable().getSelectedRow();
						if(row!=-1) {
							row = getFileTable().convertRowIndexToModel(row);
							FileTableModel model = (FileTableModel)getFileTable().getModel();
							selected  = model.getFile(row);
						}
					}
					else if("cancel".equalsIgnoreCase(cmd)) {
						isCancel = true;
					}
				}

			});
		}
		return contentPanel;
	}

	private JSplitPane getSplitter() {
		if(splitter==null) {
			splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			JScrollPane pane = UIFactory.createScrollPane(getFileTree(),true);
			pane.setMinimumSize(new Dimension(200,250));
			splitter.setLeftComponent(pane);
			pane = UIFactory.createScrollPane(getFileTable(),true);
			pane.setMinimumSize(new Dimension(230,250));
			splitter.setRightComponent(pane);
			splitter.resetToPreferredSizes();
			splitter.setPreferredSize(new Dimension(500,200));
		}
		return splitter;
	}

	private FileTree getFileTree() {
		if(fileTree==null) {
			fileTree = new FileTree();
			fileTree.setVisibleRowCount(13);
			FileTreeModel model = (FileTreeModel)fileTree.getModel();
			model.setFilter(FileTree.createCatalogFilter());
			fileTree.addTreeSelectionListener(new TreeSelectionListener() {

				@Override
				public void valueChanged(TreeSelectionEvent e) {
					TreePath path = getFileTree().getSelectionPath();
					File file = (path!=null ? (File)path.getLastPathComponent() : null);
					FileTableModel model = (FileTableModel)getFileTable().getModel();
					model.setRoot(file);
				}

			});
		}

		return fileTree;
	}

	private FileTable getFileTable() {
		if(fileTable==null) {
			fileTable = new FileTable();
			//fileTree.setPreferredSize(new Dimension(400,250));
		}
		return fileTable;
	}


}
