package org.redcross.sar.gui.dialog;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import org.redcross.sar.Application;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.field.TextLineField;
import org.redcross.sar.gui.model.FileTreeModel;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.tree.FileTree;
import org.redcross.sar.util.Utils;

public class DirectoryChooserDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;

	private boolean isCancel;
	private Object selected;
	private FileTree fileTree;
	private DefaultPanel contentPanel;
	private TextLineField selectedAttr;

	/* ==================================================
	 * Constructors
	 * ================================================== */

	public DirectoryChooserDialog() {
		// forward
		super(Application.getInstance());
		// initialize GUI
		initialize();
	}

	public DirectoryChooserDialog(Frame owner) {
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
		FileTreeModel model = (FileTreeModel)getFileTree().getModel();
		model.setRoot(file);
	}

	public void setRoot(File file) {
		FileTreeModel model = (FileTreeModel)getFileTree().getModel();
		model.setRoot(file);
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

		getContentPanel().setCaptionIcon(icon);
		getContentPanel().setCaptionText(title);
		// set flags
		this.isCancel = false;
		// select in tree
		TreePath path = getFileTree().getPath(selected);
		getFileTree().setSelectionPath(path);
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
			contentPanel = new DefaultPanel("Velg katalog");
			contentPanel.setPreferredSize(new Dimension(375,300));
			contentPanel.setRequestHideOnFinish(true);
			contentPanel.setRequestHideOnCancel(true);
			contentPanel.setContainerLayout(new BorderLayout(5,5));
			contentPanel.setContainerBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			contentPanel.addToContainer(getFileTree(),BorderLayout.CENTER);
			contentPanel.addToContainer(getSelectedAttr(),BorderLayout.SOUTH);
			contentPanel.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String cmd = e.getActionCommand();
					if("finish".equalsIgnoreCase(cmd)) {
						TreePath path = getFileTree().getSelectionPath();
						selected = path!=null ? path.getLastPathComponent() : null;
					}
					else if("cancel".equalsIgnoreCase(cmd)) {
						isCancel = true;
					}
				}

			});
		}
		return contentPanel;
	}

	private FileTree getFileTree() {
		if(fileTree==null) {
			fileTree = new FileTree();
			fileTree.setBorder(UIFactory.createBorder());
			FileTreeModel model = (FileTreeModel)fileTree.getModel();
			model.setFilter(FileTree.createCatalogFilter());
			fileTree.addTreeSelectionListener(new TreeSelectionListener() {

				@Override
				public void valueChanged(TreeSelectionEvent e) {
					TreePath path = getFileTree().getSelectionPath();
					File file = (path!=null ? (File)path.getLastPathComponent() : null);
					getSelectedAttr().setValue(file!=null ? file.getName() : "");
				}

			});
		}
		return fileTree;
	}

	private TextLineField getSelectedAttr() {
		if(selectedAttr==null) {
			selectedAttr = new TextLineField("selected","Katalog",false);
		}
		return selectedAttr;
	}


}
