package org.redcross.sar.gui.dialog;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.redcross.sar.IApplication;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoStringFactory;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.map.CustomMapData;
import org.redcross.sar.map.MapSourceInfo;
import org.redcross.sar.map.MapSourceTable;
import org.redcross.sar.util.Utils;

public class MapOptionDialog extends DefaultDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	private DefaultPanel contentPanel = null;
	private JTabbedPane tabbedPane = null;
	private DefaultPanel sourcePanel = null;
	private MapSourceTable mapSourceTable = null;
	private JPanel browsePanel = null;
	
	private JButton buttonBrowse = null;
	
	private JLabel labelCurrentMxd = null;
	private JLabel labelCurrentMxdShow = null;
	
	private JTextField textFieldBrowse = null;
	
	private IApplication app = null;
	
	private File file = null;
	
	private boolean cancel = true;
	
	public MapOptionDialog(IApplication app){
		super(app.getFrame());
		this.app = app;
		initalize();
	}
	
	private void initalize(){
		try {
            this.setPreferredSize(new Dimension(475, 300));
            this.setUndecorated(true);
            this.setModal(true);
            this.setContentPane(getContentPanel());
            this.pack();
		}
		catch (java.lang.Throwable e) {
			e.printStackTrace();
		}
	}
		
	/**
	 * This method initializes jTabbedMapPane	
	 * 	
	 * @return {@link DefaultPanel}	
	 */
	private DefaultPanel getContentPanel() {		
		if(contentPanel == null) {
			contentPanel = new DefaultPanel("Oppsett kartografi");
			contentPanel.insertButton("finish", 
					DiskoButtonFactory.createButton("GENERAL.SYNCHRONIZE",contentPanel.getButtonSize()), 
					"synchronize");
			contentPanel.setContainer(getTabbedPane());
			contentPanel.setScrollBarPolicies(
					JScrollPane.VERTICAL_SCROLLBAR_NEVER, 
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			contentPanel.addActionListener(this);
		}
		return contentPanel;
	}
	
	
	/**
	 * This method initializes centerPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JTabbedPane getTabbedPane(){		
		if (tabbedPane == null) {
			
			// create
			tabbedPane = new JTabbedPane();
			tabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			tabbedPane.setPreferredSize(new Dimension(425, 200));
			tabbedPane.addTab("Kartkilder", null, getSourcePanel(), null);
			//tabbedPane.addTab("Legg til data", null, getBrowsePanel(), null);
			
			// listen for tab changes
			tabbedPane.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					// translate
					switch(tabbedPane.getSelectedIndex()) {
					case 0: break;
					case 1:
						labelCurrentMxdShow.setText(app.getMapManager().getMxdDoc());			
						labelCurrentMxd.setText("Legg data til dokument: " + app.getMapManager().getMxdDoc()); 
						break;
					}
					
				}
				
			});
		}
		return tabbedPane;
	}
	
	/**
	 * This method initializes panelMapSources	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private DefaultPanel getSourcePanel() {
		if (sourcePanel == null) {
			try {
				sourcePanel = new DefaultPanel("",false,false);
				sourcePanel.setHeaderVisible(false);
				sourcePanel.setBorderVisible(false);
				sourcePanel.setContainer(getMapSourceTable());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}		
		}
		return sourcePanel;
	}
		
	private JPanel getBrowsePanel() {
		if (browsePanel == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints3.gridy = 3;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 1;
			gridBagConstraints21.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints21.gridy = 1;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints11.gridy = 1;
			gridBagConstraints11.anchor = GridBagConstraints.WEST;
			gridBagConstraints11.insets = new Insets(5, 10, 5, 5);
			gridBagConstraints11.gridx = 0;
			/*GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.gridy = 0;*/
			labelCurrentMxdShow = new JLabel();
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.weightx = 2.0;
			gridBagConstraints.insets = new Insets(10, 10, 10, 0);
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.gridy = 0;
			labelCurrentMxd = new JLabel();			
			browsePanel = new JPanel();
			
			//må oppdatere mxdsti utifra hva som er valgt som primær kart.
			//browsePanel.addFocusListener(arg0);
			
			browsePanel.setLayout(new GridBagLayout());
			browsePanel.add(labelCurrentMxd, gridBagConstraints);
			browsePanel.add(getTextFieldBrowse(), gridBagConstraints11);
			browsePanel.add(getButtonBrowse(), gridBagConstraints21);
			//browsePanel.add(getButtonOK(), gridBagConstraints3);
		}
		return browsePanel;
	}
	
	
	/**
	 * This method initializes MapSourceTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private MapSourceTable getMapSourceTable() {
		if (mapSourceTable == null) {
			mapSourceTable = new MapSourceTable(app.getMapManager().getMapInfoList());

		}
		return mapSourceTable;
	}
	
	private boolean setMxdDoc(String mxd){
		// no change?
		if(app.getMapManager().getMxdDoc().equalsIgnoreCase(mxd)) return true;
		// set new mxd document and load visible maps
		if(app.getMapManager().setMxdDoc(mxd)) return app.getMapManager().loadMxdDoc();
		// failed
		return false;
	}
	
	/**
	 * This method initializes textFieldBrowse	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTextFieldBrowse() {
		if (textFieldBrowse == null) {
			textFieldBrowse = new JTextField();
			textFieldBrowse.setPreferredSize(new Dimension(300, 40));
			textFieldBrowse.setHorizontalAlignment(JTextField.LEADING);
		}
		return textFieldBrowse;
	}

	/**
	 * This method initializes buttonBrowse	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getButtonBrowse() {
		if (buttonBrowse == null) {
			buttonBrowse = new JButton();
			buttonBrowse.setPreferredSize(new Dimension(80, 40));
			buttonBrowse.setMnemonic(KeyEvent.VK_UNDEFINED);
			buttonBrowse.setText("Browse");
			buttonBrowse.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed(), Browse"); // TODO Auto-generated Event stub actionPerformed()
					file = openFileDialog();
					if(file != null){
						String fname = file.getAbsolutePath();
						textFieldBrowse.setText(fname);
					}
				}
			});
		}
		return buttonBrowse;
	}

	/**
	 * 
	 *
	 */
	private File openFileDialog(){
	
		//File file = new File("C:\\shape\\poi.shp");		
		
		/*
		final JFileChooser fc = new JFileChooser();
		//FileNameExtensionFilter filter = new FileNameExtensionFilter("Shape og tiff", "shp", "tiff", "tif");
		//fc.addChoosableFileFilter(filter);
		int returnVal = fc.showDialog(new JFrame(), "Legg til");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            //This is where a real application would open the file.
            System.out.println("Opening: " + file.getName() + "." );
        } else {
        	System.out.println("Open command cancelled by user.");
        } 
        
		return file;
		*/
		
		/*
		final DiskoFileChooser dfc = new DiskoFileChooser();
		file = dfc.getSelectedFile();
		return file;
		*/
		
		
		//FileDialog fDialog = new FileDialog(this, "Legg til", 0);
		//fDialog.setVisible(true);
		//fDialog.setFilenameFilter();
		//fDialog.setFilenameFilter(arg0)
		
		FileDialog fileDialog = new FileDialog(this, "Legg til .shp, .tif eller .tiff fil", FileDialog.LOAD);
		
		FilenameFilter filter = new FilenameFilter(){
			public boolean accept(File dir, String name) {
				return name.endsWith(".shp");
	        }
	    };
	    fileDialog.setFilenameFilter(filter);
	    fileDialog.setDirectory(app.getProperty("MxdDocument.catalog.path"));
	    
	    
	    fileDialog.setVisible(true);	
		String path = fileDialog.getDirectory();
		String name = fileDialog.getFile();
		if (name == null)
			return null;
		String sFileChosen = path + name;
		System.out.println("Valgt fil: " + sFileChosen);
		file = new File(sFileChosen);
		return file;		 
		 
	}

	public void actionPerformed(ActionEvent e) {
		// get command
		String cmd = e.getActionCommand();
		// translate
		if("finish".equalsIgnoreCase(cmd)) {
			switch(getTabbedPane().getSelectedIndex()) {
			case 0: 
				// initialize
				int count = getMapSourceTable().getRowCount();
				// loop over all check boxes
				for(int i =0;i<count;i++){
					// is selected?
					if ((Boolean)getMapSourceTable().getValueAt(i, 0)){
						// get document
						String mxddoc = (String)getMapSourceTable().getValueAt(i, 1);
						// get map info
						MapSourceInfo info = app.getMapManager().getMapInfo(mxddoc);
						// initalize choice
						int ans = JOptionPane.YES_OPTION;
						// show warning?
						if(info!=null && info.getCoverage()==0) 
							ans = Utils.showConfirm(
									DiskoStringFactory.getText("WARNING_HEADER_DEFAULT"), 
									DiskoStringFactory.getText("WARNING_MAP_NO_COVERAGE"),
									JOptionPane.YES_NO_OPTION);
						
						// cancel?
						if(ans==JOptionPane.NO_OPTION) break;
						// try to set document
						if(!setMxdDoc(mxddoc)) {
							// notify!
							Utils.showWarning(String.format(DiskoStringFactory.getText("WARNING_MAP_SET_FAILED"),mxddoc));
							// finished
							return;
						}
						else break;
					}	
				}	
				break;
			case 1: 
				if (file != null){
					//legger inn et hack for å sjekke filtype. Burde vært fikset med et filter.
					String fname = file.getName();	
					int i = fname.lastIndexOf(".") + 1;
					String ext = fname.substring(i, fname.length());
					if(ext.equalsIgnoreCase("tif") || ext.equalsIgnoreCase("tiff") || ext.equalsIgnoreCase("shp")){
						CustomMapData addData = new CustomMapData();					
						addData.AddCustomData(app, file);
					}
					else{
						//åpne meldingsboks
						System.out.println("Feil fil format");
					}
					textFieldBrowse.setText("");
					file = null;//nullstiller
					//System.out.println("FinishButton: add data");
				}
				break;
			}
			// finished, hide dialog
			setVisible(false);			
		}
		else if("cancel".equalsIgnoreCase(cmd)) {
			// set flag
			cancel = true;
			// finished
			setVisible(false);
		}
		else if("synchronize".equalsIgnoreCase(cmd)) {
			// forward
			if(app.getMapManager().synchronizeMxdDocs())
				getMapSourceTable().load(app.getMapManager().getMapInfoList());
		}
		
	}
	
	public boolean selectMap(String caption, List<MapSourceInfo> list) {
		// reset flag
		cancel = false;
		// prepare
		getContentPanel().setCaptionText(caption);
		getMapSourceTable().load(list);
		// blocking on this
		setVisible(true);
		// finished
		return !cancel;
	}
	
	
}
