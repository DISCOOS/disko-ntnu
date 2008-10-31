package org.redcross.sar.wp.tactics;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.EnumSet;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoStringFactory;
import org.redcross.sar.gui.field.ComboBoxField;
import org.redcross.sar.gui.mso.model.HypothesisListModel;
import org.redcross.sar.gui.panel.FieldsPanel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.renderer.BundleListCellRenderer;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IHypothesisIf;
import org.redcross.sar.mso.data.IHypothesisIf.HypothesisStatus;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchAreaIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.thread.event.WorkEvent;
import org.redcross.sar.thread.event.IWorkListener;
import org.redcross.sar.util.Utils;
import org.redcross.sar.wp.IDiskoWpModule;

public class HypothesisDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;
	private IMsoModelIf msoModel = null;
	private DefaultPanel contentPanel = null;
	private JPanel hypothesisPanel = null;
	private JPanel buttonPanel = null;
	private JButton createButton = null;
	private JButton deleteButton = null;
	private JScrollPane listScrollPane = null;
	private JList hypothesisList = null;
	private JPanel centerPanel = null;
	private JLabel selectedLabel = null;
	private BasePanel descriptionPanel = null;
	private JTextArea descriptionTextArea = null;
	private FieldsPanel attribsPanel;
	private ComboBoxField statusCombo;
	private ComboBoxField priorityCombo;

	private IDiskoWpModule wp = null;

	private String[] labels = null;

	public HypothesisDialog(IDiskoWpModule wp) {
		// forward
		super(wp.getApplication().getFrame());
		// prepare
		this.wp = wp;
		// setup objects
		this.msoModel = wp.getMsoModel();
		// initialize gu
		initialize();
		// initialize data
		setup();
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		try {
			createlabels();
			this.setContentPane(getContentPanel());
            this.setPreferredSize(new Dimension(900, 300));
            this.pack();
			loadHypotheses();
		}
		catch (java.lang.Throwable e) {
			e.printStackTrace();
		}
	}

	private static EnumSet<IMsoManagerIf.MsoClassCode> getMyInterest() {
		EnumSet<IMsoManagerIf.MsoClassCode> myInterests
			= EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_SEARCHAREA);
		myInterests.add(IMsoManagerIf.MsoClassCode.CLASSCODE_HYPOTHESIS);
		return myInterests;
	}

	private static EnumSet<IMsoFeatureLayer.LayerCode> getMyLayers() {
		EnumSet<IMsoFeatureLayer.LayerCode> myLayers
			= EnumSet.of(IMsoFeatureLayer.LayerCode.SEARCH_AREA_LAYER);
	    return myLayers;
	}


	/**
	 * This method initializes contentPanel
	 *
	 * @return {@link BasePanel}
	 */
	private DefaultPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				contentPanel = new DefaultPanel("",true,true) {

					private static final long serialVersionUID = 1L;

					@Override
					protected boolean beforeCancel() {

						IHypothesisIf h = getSelectedHypothesis();
						if(h==null) {
							Utils.showWarning("Begrensning","Du m� velge en hypotese");
							return false;
						}
						return true;

					}

					@Override
					protected boolean beforeFinish() {

						IHypothesisIf h = getSelectedHypothesis();
						if(h==null) {
							Utils.showWarning("Begrensning","Du m� velge en hypotese");
							return false;
						}

						// forward
						setDescription(getDescriptionTextArea().getText(),false,true);
						setHypothesis(getSelectedHypothesis(), false, true);
						setPriority(getPriority(), false, true);
						setStatus((HypothesisStatus)getStatusCombo().getValue(), false, true);
						getDescriptionTextArea().requestFocus();
						// success
						return true;
					}

					@Override
					public void setMsoObject(IMsoObjectIf msoObj) {

						// consume changes
						setChangeable(false);

						// initialize
						ISearchAreaIf area = null;
						IHypothesisIf h = null;
						// get search area
						if (msoObj instanceof ISearchAreaIf) {
							// save reference
							area = (ISearchAreaIf)msoObj;
							// get hypothesis
							h = (area!=null ? area.getSearchAreaHypothesis() : null);
						}

						// update
						msoObject = area;
						setHypothesis(h, true, false);

						// resume changes
						setChangeable(true);

						// update
						setDirty(false,false);
						setup();

					}

					@Override
					public void update() {
						super.update();
						setup();
					}

					@Override
					protected void msoObjectCreated(IMsoObjectIf msoObj, int mask) {
						if(msoObj instanceof IHypothesisIf) {
							addHypothesis((IHypothesisIf)msoObj);
						}
					}

					@Override
					protected void msoObjectChanged(IMsoObjectIf msoObj, int mask) {
						// is same as selected?
						if(msoObj == msoObject) {
							setMsoObject(msoObj);
						}
						else if(msoObj instanceof IHypothesisIf) {
							// refresh list
							setHypothesis((IHypothesisIf)msoObj, true, false);
						}
					}

					@Override
					protected void msoObjectDeleted(IMsoObjectIf msoObj, int mask) {
						// is same as selected?
						if(msoObj == msoObject) {
							// reset selection
							setMsoObject(null);
						}
						else if(msoObj instanceof IHypothesisIf) {
							removeHypothesis((IHypothesisIf)msoObj);
						}
					}

					@Override
					protected void msoObjectClearAll(IMsoObjectIf msoObj, int mask) {
						clearHypotheses();
					}


				};
				contentPanel.setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "32x32"));
				contentPanel.setInterests(msoModel,getMyInterest());
				contentPanel.setMsoLayers(wp.getMap(),getMyLayers());
				contentPanel.setBodyComponent(getHypothesisPanel());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return contentPanel;
	}

	/**
	 * This method initializes hypothesisPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getHypothesisPanel() {
		if (hypothesisPanel == null) {
			try {
				hypothesisPanel = new JPanel();
				hypothesisPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				BorderLayout bl = new BorderLayout();
				bl.setHgap(5);
				hypothesisPanel.setLayout(bl);
				hypothesisPanel.add(getListScrollPane(), BorderLayout.WEST);
				hypothesisPanel.add(getCenterPanel(), BorderLayout.CENTER);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return hypothesisPanel;
	}

	/**
	 * This method initializes buttonPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			try {
				buttonPanel = new JPanel();
				buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
				buttonPanel.add(getCreateButton());
				buttonPanel.add(getDeleteButton());
				buttonPanel.add(Box.createVerticalGlue());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return buttonPanel;
	}

	/**
	 * This method initializes createButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getCreateButton() {
		if (createButton == null) {
			try {
				createButton = DiskoButtonFactory.createButton(
						"GENERAL.STAR",
						getContentPanel().getButtonSize());
				createButton.addActionListener(new ActionListener(){

					public void actionPerformed(ActionEvent e) {
						create();
					}

				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return createButton;
	}

	/**
	 * This method initializes deleteButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getDeleteButton() {
		if (deleteButton == null) {
			try {
				deleteButton = DiskoButtonFactory.createButton(
						"GENERAL.DELETE",
						getContentPanel().getButtonSize());
				deleteButton.addActionListener(new ActionListener(){

					public void actionPerformed(ActionEvent e) {
						delete();
					}

				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return deleteButton;
	}

	/**
	 * This method initializes listScrollPane
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getListScrollPane() {
		if (listScrollPane == null) {
			try {
				listScrollPane = new JScrollPane();
				listScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				listScrollPane.setPreferredSize(new Dimension(150, 150));
				listScrollPane.setViewportView(getHypothesisList());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return listScrollPane;
	}

	/**
	 * This method initializes hypothesisList
	 *
	 * @return javax.swing.JList
	 */
	private JList getHypothesisList() {
		if (hypothesisList == null) {
			try {
				HypothesisListModel listModel = new HypothesisListModel(msoModel);
				hypothesisList = new JList(listModel);
				hypothesisList.setCellRenderer(new HypothesisListCellRenderer());
				hypothesisList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				hypothesisList.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						// consume?
						if (e.getValueIsAdjusting() || !isChangeable()) return;
						// forward
						setHypothesis(getSelectedHypothesis(), true, false);
						setDirty(true);
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return hypothesisList;
	}

	/**
	 * This method initializes centerPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getCenterPanel() {
		if (centerPanel == null) {
			try {
				centerPanel = new JPanel();
				BorderLayout bl = new BorderLayout();
				bl.setHgap(5);
				bl.setVgap(5);
				centerPanel.setLayout(bl);
				centerPanel.add(getSelectedLabel(), BorderLayout.NORTH);
				centerPanel.add(getButtonPanel(), BorderLayout.WEST);
				centerPanel.add(getDescriptionPanel(), BorderLayout.CENTER);
				centerPanel.add(getAttribsPanel(), BorderLayout.EAST);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return centerPanel;
	}

	/**
	 * This method initializes activeLabel
	 *
	 * @return javax.swing.JPanel
	 */
	private JLabel getSelectedLabel() {
		if (selectedLabel == null) {
			try {
				selectedLabel = new JLabel("Ingen hypotese er valg");
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return selectedLabel;
	}

	/**
	 * This method initializes textAreaScrollPane
	 *
	 * @return javax.swing.JScrollPane
	 */
	private BasePanel getDescriptionPanel() {
		if (descriptionPanel == null) {
			try {
				descriptionPanel = new BasePanel("Beskrivelse");
				descriptionPanel.setBodyComponent(getDescriptionTextArea());
				descriptionPanel.setScrollBarPolicies(BasePanel.VERTICAL_SCROLLBAR_AS_NEEDED,
						BasePanel.HORIZONTAL_SCROLLBAR_NEVER);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return descriptionPanel;
	}

	/**
	 * This method initializes descriptionTextArea
	 *
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getDescriptionTextArea() {
		if (descriptionTextArea == null) {
			try {
				descriptionTextArea = new JTextArea();
				descriptionTextArea.setLineWrap(true);
				descriptionTextArea.setEnabled(false);
				// add updata manager
				descriptionTextArea.getDocument().addDocumentListener(new DocumentListener() {

					public void changedUpdate(DocumentEvent e) { change(); }
					public void insertUpdate(DocumentEvent e) { change(); }
					public void removeUpdate(DocumentEvent e) { change(); }

					private void change() {
						// consume?
						if(!isChangeable()) return;
						// force an update
						setDirty(true);
					}

				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return descriptionTextArea;
	}

	/**
	 * This method initializes AttribsPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private FieldsPanel getAttribsPanel() {
		if (attribsPanel == null) {
			try {
				attribsPanel = new FieldsPanel("Egenskaper","",false,false);
				attribsPanel.setPreferredSize(new Dimension(200,100));
				attribsPanel.setFitBodyOnResize(true);
				attribsPanel.setNotScrollBars();
				attribsPanel.addField(getPriorityCombo());
				attribsPanel.addField(getStatusCombo());
				attribsPanel.addWorkListener(new IWorkListener() {

					@Override
					public void onWorkPerformed(WorkEvent e) {
						if(e.isChange() || e.isFinish())
							setDirty(true);
					}

				});

			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return attribsPanel;
	}

	/**
	 * This method initializes priorityCombo
	 *
	 * @return {@link ComboBoxField}
	 */
	private ComboBoxField getPriorityCombo() {
		if (priorityCombo == null) {
			priorityCombo = new ComboBoxField("priority", "Prioritet", false, 50, 25, null);
			DefaultComboBoxModel model = new DefaultComboBoxModel();
			for (int i = 1; i < 6; i++) {
				model.addElement(new Integer(i));
			}
			priorityCombo.fill(model);
			priorityCombo.getComboBox().setSelectedIndex(0);
		}
		return priorityCombo;
	}

	/**
	 * This method initializes statusComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	private ComboBoxField getStatusCombo() {
		if (statusCombo == null) {
			try {
				statusCombo = new ComboBoxField("status", "Status", false, 50, 25, null);
				DefaultComboBoxModel model = new DefaultComboBoxModel();
				HypothesisStatus[] values = HypothesisStatus.values();
				for (int i = 0; i < values.length; i++) {
					model.addElement(values[i]);
				}
				statusCombo.fill(model);
				JComboBox cb = (JComboBox)statusCombo.getComponent();
				cb.setRenderer(new BundleListCellRenderer());
				cb.setSelectedIndex(0);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return statusCombo;
	}

	private void loadHypotheses() {
		if(msoModel.getMsoManager().operationExists()) {
			Collection<IHypothesisIf> c = msoModel.getMsoManager().
				getCmdPost().getHypothesisListItems();
			DefaultListModel model = new DefaultListModel();
			for(IHypothesisIf h:c) {
				model.addElement(DiskoEnumFactory.getText(IMsoManagerIf
						.MsoClassCode.CLASSCODE_HYPOTHESIS)+" "+h.getNumber());
			}
			hypothesisList.setModel(model);
		}
	}

	private void clearHypotheses() {
		hypothesisList.setModel(new DefaultListModel());
	}

	private void addHypothesis(IHypothesisIf h) {
		DefaultListModel model = (DefaultListModel)hypothesisList.getModel();
		model.addElement(DiskoEnumFactory.getText(IMsoManagerIf
				.MsoClassCode.CLASSCODE_HYPOTHESIS)+" "+h.getNumber());
	}

	private void removeHypothesis(IHypothesisIf h) {
		DefaultListModel model = (DefaultListModel)hypothesisList.getModel();
		model.removeElement(DiskoEnumFactory.getText(IMsoManagerIf
				.MsoClassCode.CLASSCODE_HYPOTHESIS)+" "+h.getNumber());
	}

	private IHypothesisIf getHypothesis(String name) {

		if(name==null || name.isEmpty()) return null;

		if(msoModel.getMsoManager().operationExists()) {
			Collection<IHypothesisIf> c = msoModel.getMsoManager()
				.getCmdPost().getHypothesisListItems();

			for(IHypothesisIf h:c) {
				if(name.equalsIgnoreCase(getHypothesisName(h))) {
					// found!
					return h;
				}
			}
		}

		return null;
	}

	private String getHypothesisName(IHypothesisIf h) {
		return h!=null ? DiskoEnumFactory.getText(IMsoManagerIf.MsoClassCode.CLASSCODE_HYPOTHESIS)+" "+h.getNumber() : null;
	}

	private void createlabels() {
		labels = new String[5];
		labels[0] = DiskoStringFactory.getText("PRIMARY_SEARCH_AREA");
		labels[1] = DiskoStringFactory.getText("SECONDARY_SEARCH_AREA");
		labels[2] = DiskoStringFactory.getText("PRIORITY3_SEARCH_AREA");
		labels[3] = DiskoStringFactory.getText("PRIORITY4_SEARCH_AREA");
		labels[4] = DiskoStringFactory.getText("PRIORITY5_SEARCH_AREA");
	}

	public IHypothesisIf getSelectedHypothesis() {
		return getHypothesis((String)getHypothesisList().getSelectedValue());
	}

	private void setHypothesis(IHypothesisIf h, boolean gui, boolean mso) {
		// update gui?
		if (gui) {
			setChangeable(false);
			// get hypothesis name
			String name = (h!=null ? DiskoEnumFactory.getText(IMsoManagerIf
					.MsoClassCode.CLASSCODE_HYPOTHESIS)+" "+h.getNumber() : null);
			getSelectedLabel().setText("<html>" + (h!=null ? "<b>" + name + "</b> er valg" :
				getHypothesisList().getModel().getSize() == 0 ? "Du m� opprette en hypotese" : "Velg en hypotese")+"</html>");
			// update all
			getHypothesisList().setSelectedValue(name, true);
			setDescription(h!=null ? h.getDescription() : null,true,mso);
			setPriority(h!=null ? h.getPriorityIndex() + 1 : 0,true,mso);
			setStatus(h!=null ? h.getStatus() : HypothesisStatus.ACTIVE ,true,mso);
			// request focus
			getDescriptionTextArea().requestFocus();
			// update
			setChangeable(true);

		}
		// update mso?
		if(mso) {
			ISearchAreaIf area = (ISearchAreaIf)getMsoObject();
			// get search area
			if(h!=null && area!=null) {
				// any change?
				if (!h.equals(area.getSearchAreaHypothesis())) {
					area.setSearchAreaHypothesis(h);
				}
			}
		}
		// update enabled state
		getDescriptionPanel().setEnabled(h!=null);
		getDescriptionTextArea().setEnabled(h!=null);
		getAttribsPanel().setEnabled(h!=null);
	}

	private void setDescription(String text, boolean gui, boolean mso) {
		IHypothesisIf h = getSelectedHypothesis();
		// has hypothesis?
		if(h!=null) {
			// update gui?
			if (gui) {
				getDescriptionTextArea().setText(text);
			}
			// update mso?
			if(mso) {
				if (!h.getDescription().equals(text)) {
					h.setDescription(text);
				}
			}
		}
	}

	private int getPriority()  {
		return getPriorityCombo().getComboBox().getSelectedIndex()+1;
	}

	private void setPriority(int priority, boolean gui, boolean mso) {
		IHypothesisIf h = getSelectedHypothesis();
		// has hypothesis?
		if(h!=null) {
			// update gui?
			if (gui) {
				getPriorityCombo().getComboBox().setSelectedIndex(priority-1);
			}
			// update mso?
			if(mso) {
				if (h.getPriorityIndex() != priority-1) {
					h.setPriorityIndex(priority-1);
				}
			}
		}
	}

	private void setStatus(HypothesisStatus status, boolean gui, boolean mso) {
		IHypothesisIf h = getSelectedHypothesis();
		// has hypothesis?
		if(h!=null) {
			// update gui?
			if (gui) {
				getStatusCombo().setValue(status);
			}
			// update mso?
			if(mso) {
				if (h.getStatus() != status) {
					h.setStatus(status);
				}
			}
		}
	}

	private void setup() {

		// consume?
		if(!isChangeable()) return;

		// consume changes
		setChangeable(false);

		// try to get mso object?
		if(getMsoObject()==null)
			getContentPanel().setSelectedMsoFeature(wp.getMap());

		// get current area
		ISearchAreaIf area = (ISearchAreaIf)getMsoObject();

		// get current hypothesis
		IHypothesisIf h  = getSelectedHypothesis();

		// update elements
		if(area!=null) {
			// update texts
			getContentPanel().setCaptionIcon(DiskoIconFactory.getIcon("MAP.POLYGON", "32x32"));
			getContentPanel().setCaptionText("Velg hypotese for <b>" +
					MsoUtils.getSearchAreaName(area).toLowerCase() + "</b>");
			// enable controls
			getHypothesisList().setEnabled(true);
			getCreateButton().setEnabled(true);
			getDeleteButton().setEnabled(true);
		}
		else {
			getContentPanel().setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "32x32"));
			getContentPanel().setCaptionText("Du m� f�rst velge et s�keomr�de");
			getSelectedLabel().setText(null);
			getHypothesisList().setEnabled(false);
			getCreateButton().setEnabled(false);
			getDeleteButton().setEnabled(false);
			getDescriptionPanel().setEnabled(false);
			getDescriptionTextArea().setEnabled(false);
			getAttribsPanel().setEnabled(false);
		}
		// these are only enabled if an hypothesis is selected
		getDescriptionPanel().setEnabled(h!=null);
		getDescriptionTextArea().setEnabled(h!=null);
		getAttribsPanel().setEnabled(h!=null);
		getAttribsPanel().update();

		// resume changes
		setChangeable(true);

	}

	private boolean create() {
		// create a new Hypothesis
		if(msoModel.getMsoManager().operationExists()) {
			ICmdPostIf cmdPost = msoModel.getMsoManager().getCmdPost();
			IHypothesisIf h = cmdPost.getHypothesisList().createHypothesis();
			if(h!=null) {
				// forward
				setHypothesis(h, true, false);
				// finished
				return true;
			}
		}
		return false;
	}

	private boolean delete() {
		// get current hypothesis
		IHypothesisIf h = getSelectedHypothesis();
		// prompt?
		if(h!=null) {
			int ans = Utils.showConfirm("Bekreft", "Dette vil slette "
					+ getHypothesisList().getSelectedValue()
					+ ". Vil du fortsette?", JOptionPane.YES_NO_OPTION);
			if(ans == JOptionPane.YES_OPTION) {
				if(MsoUtils.delete(h, 0)) {
					return true;
				}
			}
		}
		// failed
		return true;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
