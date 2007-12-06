package org.redcross.sar.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.AbstractButton;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.map.CustomMapData;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.DiskoMapManagerImpl;
import org.redcross.sar.map.MapSourceInfo;

import com.borland.jbcl.layout.VerticalFlowLayout;


public class MapOptionDialog extends DiskoDialog {

	private static final long serialVersionUID = 1L;
	private JTabbedPane jTabbedMapPane = null;  //  @jve:decl-index=0:visual-constraint="102,91"
	private JPanel panelMapSources = null;
	//private JPanel panelChooseMap = null;
	private JPanel addDataPanel = null;
	private JPanel contentPanel = null;
	private JPanel buttonPanel = null;
	private JPanel mapInfoPanel = null;
	private JPanel browsePanel = null;
	
	private JScrollPane tableScrollPane = null;
	
	private JButton cancelButton = null;
	private JButton finishButton = null;
	private JButton buttonBrowse = null;
	private JLabel labelCurrentMxd = null;
	private JLabel labelCurrentMxdShow = null;
	
	private JTextField textFieldBrowse = null;
	
	private IDiskoApplication app = null;
	private String currentMxd = null;
	private File file = null;
	
	private JLabel labelHeadPrimar = null;
	private JLabel labelHeadSecond = null;
	private JLabel labelHeadMxd = null;
	private JLabel labelHeadType = null;
	private JLabel labelStatus = null;
	
	private ButtonGroup bgPrimar = new ButtonGroup();
	private ButtonGroup bgSecond = new ButtonGroup();
	private ArrayList<MapSourceInfo> mapSourceList = new ArrayList<MapSourceInfo>();
	
	
	public MapOptionDialog(IDiskoApplication app){
		super(app.getFrame());
		this.app = app;
		this.currentMxd = app.getDiskoMapManager().getCurrentMxd();	
		initalize();
	}
	
	private void initalize(){
		try {
            this.setPreferredSize(new Dimension(475, 300));
            //this.setLocationRelativeTo(null, POS_CENTER, false);
            //this.setSize(new Dimension(175, 350));
            this.setSize(new Dimension(400, 447));
			this.setTitle("DISKO kartoppsett");
            this.setContentPane(getContentPanel());
            this.pack();
		}
		catch (java.lang.Throwable e) {
			//  Do Something
		}
	}
		
	/**
	 * This method initializes jTabbedMapPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JPanel getContentPanel() {		
		contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		contentPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		
		//contentPanel.setBorder(BorderFactory.createTitledBorder(null, "Kartoppsett", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
		contentPanel.add(getCenterPanel(), BorderLayout.CENTER);
		contentPanel.add(getButtonPanel(), BorderLayout.SOUTH);
		
		return contentPanel;
	}
	
	
	/**
	 * This method initializes centerPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JTabbedPane getCenterPanel(){
		
		if (jTabbedMapPane == null) {
			jTabbedMapPane = new JTabbedPane();
			//jTabbedMapPane.setSize(new Dimension(312, 219));
			jTabbedMapPane.addTab("Kartoppsett", null, getPanelMapSources(), null);
			jTabbedMapPane.addTab("Legg til data", null, getAddDataPanel(), null);
						
		}
		return jTabbedMapPane;
	}
	
	/**
	 * This method initializes panelMapSources	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPanelMapSources() {
		if (panelMapSources == null) {
			try {
				panelMapSources = new JPanel();
				panelMapSources.setLayout(new BorderLayout());
				panelMapSources.add(getTableScrollPane(), BorderLayout.CENTER);
				//panelMapSources.add(getMapSourceTable(), BorderLayout.CENTER);
				//panelMapSources.add(getButtonPanel(), BorderLayout.SOUTH);
				panelMapSources.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				
				//panelMapSources.add(getButtonPanel(), BorderLayout.CENTER);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}		
		}
		return panelMapSources;
	}

	/**
	 * This method initializes buttonPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			try {
				FlowLayout flowLayout = new FlowLayout();
				flowLayout.setAlignment(FlowLayout.RIGHT);
				buttonPanel = new JPanel();
				buttonPanel.setLayout(flowLayout);
				
				buttonPanel.add(getCancelButton(), null);
				buttonPanel.add(getFinishButton(), null);
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return buttonPanel;
	}	
	
	/**
	 * This method initializes panelAddData	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getAddDataPanel() {
		if (addDataPanel == null) {
			try {
				addDataPanel = new JPanel();
				addDataPanel.setLayout(new BorderLayout());
				addDataPanel.add(getBrowsePanel(), BorderLayout.CENTER);
				//addDataPanel.add(getButtonPanel(), BorderLayout.SOUTH);
				addDataPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));				
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}		
		}
		return addDataPanel;
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
			labelCurrentMxdShow.setText(this.currentMxd);			
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.weightx = 2.0;
			gridBagConstraints.insets = new Insets(10, 10, 10, 0);
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.gridy = 0;
			labelCurrentMxd = new JLabel();
			
			labelCurrentMxd.setText("Gjeldende .mxd dokument: " + this.currentMxd); 
			labelCurrentMxd.setName("");
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
	 * This method initializes tableScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getTableScrollPane() {
		if (tableScrollPane == null) {
			try {
				tableScrollPane = new JScrollPane();
				tableScrollPane.getViewport().setBackground(Color.white);
				tableScrollPane.setViewportView(getMapSourceTable());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return tableScrollPane;
	}
	
	/**
	 * This method initializes assignmentTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JPanel getMapSourceTable() {
		if (mapInfoPanel == null) {
			try {
				
				VerticalFlowLayout vfl = new VerticalFlowLayout();
				mapInfoPanel = new JPanel();
				mapInfoPanel.setLayout(vfl);
				
				
				//ButtonGroup bgPrimar = new ButtonGroup();
				//ButtonGroup bgSecond = new ButtonGroup();
				
				//Gets a list of availble mxd docs
				DiskoMapManagerImpl manager = (DiskoMapManagerImpl) app.getDiskoMapManager();
				mapSourceList = manager.getMapsTable();
				
				MapSourceInfo mapInfo = new MapSourceInfo();
				Dimension dCb = new Dimension(30,16);
				Dimension dMxdPath = new Dimension(200,16);
				Dimension dType = new Dimension(50,16);
				Dimension dStatus = new Dimension(50,16);
				
				//"table "heading
				FlowLayout flHeading = new FlowLayout();
		
				flHeading.setAlignment(FlowLayout.LEFT);
				JPanel headerRow = new JPanel(flHeading);
				headerRow.setBackground(Color.LIGHT_GRAY);
				//headerRow.setBorder(arg0);
				
				labelHeadPrimar = new JLabel();
				labelHeadPrimar.setText(app.getProperty("MapSource.PRIMAR"));				
				labelHeadPrimar.setPreferredSize(dCb);
				//labelHeadPrimar.setSize(dCb);
				labelHeadSecond = new JLabel();
				labelHeadSecond.setText(app.getProperty("MapSource.SECOND"));
				labelHeadSecond.setPreferredSize(dCb);
				//labelHeadSecond.setSize(dCb);
				labelHeadMxd = new JLabel();
				labelHeadMxd.setText(app.getProperty("MapSource.MXDPATH"));
				labelHeadMxd.setPreferredSize(dMxdPath);
				labelHeadType = new JLabel();
				labelHeadType.setText(app.getProperty("MapSource.TYPE"));
				labelHeadType.setPreferredSize(dType);
				labelStatus = new JLabel();
				labelStatus.setText(app.getProperty("MapSource.STATUS"));
				labelStatus.setPreferredSize(dStatus);
				
				//labelHeadPrimar.setSize();
				
				headerRow.add(labelHeadPrimar, null );
				headerRow.add(labelHeadSecond, null);
				headerRow.add(labelHeadMxd, null);
				headerRow.add(labelHeadType, null);
				headerRow.add(labelStatus, null);
				
				mapInfoPanel.add(headerRow);
				
				//iterere igjennom lista
				for(int i = 0; i < mapSourceList.size(); i++){
					
					mapInfo = (MapSourceInfo) mapSourceList.get(i);
					final String mxd = mapInfo.getMxdPath();
					FlowLayout fl = new FlowLayout();
					fl.setAlignment(FlowLayout.LEFT);
					JPanel row = new JPanel(fl);
					
					JCheckBox cbPrimar = new JCheckBox();	
					cbPrimar.setSelected(mapInfo.getPrimarMap());
					cbPrimar.setPreferredSize(dCb);
					/*
					cbPrimar.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							try{
								setPrimaryMxd(mxd);								
							}catch(IOException ioe){
								ioe.printStackTrace();
							}							
						}
					});
					*/
					bgPrimar.add(cbPrimar);
					row.add(cbPrimar);
					
					JCheckBox cbSecond = new JCheckBox();
					cbSecond.setPreferredSize(dCb);
					cbSecond.setSelected(mapInfo.getSecondaryMap());
					
					/*
					cbSecond.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {							
							setSecondaryMxd(mxd); 							
						}
					});
					*/
					
					bgSecond.add(cbSecond);
					row.add(cbSecond);
					
					JLabel mxdPath = new JLabel(mapInfo.getMxdPath());
					mxdPath.setPreferredSize(dMxdPath);
					row.add(mxdPath);
															
					JLabel type = new JLabel(mapInfo.getType());
					type.setPreferredSize(dType);
					row.add(type);
					
					JLabel status = new JLabel(mapInfo.getStatus());
					status.setPreferredSize(dStatus);
					row.add(status);
					
					mapInfoPanel.add(row);
				}							
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return mapInfoPanel;
	}
	
	private void setPrimaryMxd(String mxd) throws IOException{
		DiskoMapManagerImpl manager = (DiskoMapManagerImpl) app.getDiskoMapManager();
		//System.out.println("mxd: " + mxd);
		manager.setPrimarMxdDoc(mxd);
		//manager.toggleMap();
		manager.setMap();
	}
	
	private void setSecondaryMxd(String mxd){
		DiskoMapManagerImpl manager = (DiskoMapManagerImpl) app.getDiskoMapManager();
		manager.setSecondaryMxdDoc(mxd);
	}	
	
	/**
	 * This method initializes finishButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getFinishButton() {
		if (finishButton == null) {
			try{
				
				
				Dimension size = app.getUIFactory().getSmallButtonSize();
				String iconName = "finish.icon";
				Icon icon = Utils.createImageIcon(app.getProperty(iconName),iconName);
				finishButton = new JButton();
				finishButton.setPreferredSize(size);
				finishButton.setIcon(icon);
				finishButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						//System.out.println("actionPerformed(): finish"); 
						//checks if active tab is addData
						
						//testing
						CustomMapData addDatatest = new CustomMapData();
						
						//addDatatest.AddWMSLayer(map, "")
						//slutt testint
						
						if (jTabbedMapPane.getSelectedIndex() == 1 && file != null){
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
						else if(jTabbedMapPane.getSelectedIndex() == 0){
							AbstractButton abstractButton = (AbstractButton) e.getSource();
							MapSourceInfo map = new MapSourceInfo();
							//loop igjennom cb og sett primær og sekundær kart.
							int j = 0;
							for (Enumeration<AbstractButton> enumerator = bgPrimar.getElements(); enumerator.hasMoreElements();){
								//System.out.println("test: " + j + ", element: " );	
								if (enumerator.nextElement().isSelected()){
									//System.out.println("isSelecta_gangsta");
									map = mapSourceList.get(j);
									map.setPrimarMap(true);
									try{
										setPrimaryMxd(map.getMxdPath());
										//System.out.println("Nytt primær kart satt: " + map.getMxdPath());
									}catch(IOException ioe){
										ioe.printStackTrace();
									}
								}	
								j=j+1;
							}
							int k = 0;
							for (Enumeration<AbstractButton> enumerator_2 = bgSecond.getElements(); enumerator_2.hasMoreElements();){
								if (enumerator_2.nextElement().isSelected()){
									map = mapSourceList.get(k);
									map.setSecondaryMap(true);
									setSecondaryMxd(map.getMxdPath());
									//System.out.println("Nytt sekundær kart satt: " + map.getMxdPath());
								}
								k = k+1;								
							}							
							
						}
						else
							//System.out.println("FinishButton: ingen action");
						setVisible(false);
					}
				});
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		return finishButton;
	}

	/**
	 * This method initializes cancelButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			try{
				Dimension size = app.getUIFactory().getSmallButtonSize();
				String iconName = "cancel.icon";
				Icon icon = Utils.createImageIcon(app.getProperty(iconName),iconName);				
				cancelButton = new JButton();
				cancelButton.setPreferredSize(size);
				cancelButton.setMnemonic(KeyEvent.VK_UNDEFINED);
				cancelButton.setIcon(icon);
				cancelButton.setText("");
				cancelButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {						
						if (jTabbedMapPane.getSelectedIndex() == 1){
							//addmapdata tab active
							textFieldBrowse.setText("");
							System.out.println("actionPerformed() blanker adddata field");
						}
						setVisible(false);
						System.out.println("actionPerformed() Cancel"); // TODO Auto-generated Event stub actionPerformed()
					}
				});
			} catch(Exception e){
				e.printStackTrace();
			}
			
		}
		return cancelButton;
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
	
}
