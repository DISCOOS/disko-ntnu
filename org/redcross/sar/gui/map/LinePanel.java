package org.redcross.sar.gui.map;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoPanel;
import org.redcross.sar.gui.attribute.CheckBoxAttribute;
import org.redcross.sar.gui.attribute.TextFieldAttribute;
import org.redcross.sar.gui.document.NumericDocument;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.SnapAdapter;
import org.redcross.sar.map.SnapAdapter.SnapListener;
import org.redcross.sar.map.command.LineTool;
import org.redcross.sar.mso.data.IMsoObjectIf;

import com.borland.jbcl.layout.VerticalFlowLayout;

public class LinePanel extends DiskoPanel implements IPropertyPanel, SnapListener {

	private static final long serialVersionUID = 1L;

	private LineTool tool = null;
	private DiskoPanel optionsPanel = null;
	private DiskoPanel actionsPanel = null;
	private JButton snapToButton = null;
	private JButton applyButton = null;
	private JButton cancelButton = null;
	private CheckBoxAttribute snapToAttr = null;
	private TextFieldAttribute minStepAttr = null;
	private TextFieldAttribute maxStepAttr = null;
	private CheckBoxAttribute constraintAttr = null;
	
	private boolean isVertical = true;
	
	private List<ActionListener> listeners = null;
	
	public LinePanel(LineTool tool, boolean isVertical) {
		// forward
		this("Tegne linje",tool,isVertical);
	}
	
	public LinePanel(String caption, LineTool tool, boolean isVertical) {
		
		// forward
		super(caption);
		
		// prepare
		this.tool = tool;
		this.listeners = new ArrayList<ActionListener>();
		
		// set layout information
		this.isVertical = isVertical;
		
		// initialize gui
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		// prepare
		((JPanel)getBodyComponent()).setPreferredSize(new Dimension(200,200));
		// forward
		this.setup();
		// hide header
		setHeaderVisible(false);
		// hide borders
		setBorderVisible(false);
	}
		
	/**
	 * This method applies setup
	 *
	 */
	private void setup() {
		
		// get body panel
		JPanel panel = (JPanel)getBodyComponent();
		// remove panels from pane
		panel.removeAll();
		
		// build container
		if(isVertical) {
			VerticalFlowLayout vfl = new VerticalFlowLayout();
			vfl.setAlignment(VerticalFlowLayout.LEFT);
			vfl.setHgap(5);
			vfl.setVgap(5);
			// update
			panel.setLayout(vfl);
			panel.add(getActionsPanel());
			panel.add(getOptionsPanel());
		}
		else {
			BorderLayout bl = new BorderLayout();
			bl.setHgap(5);
			bl.setVgap(5);
			// update
			panel.setLayout(bl);
			panel.add(getActionsPanel(),BorderLayout.EAST);				
			panel.add(getOptionsPanel(),BorderLayout.CENTER);
		}
	}
	
	private void doSnapTo() {
		// get adapter
		SnapAdapter snapping = tool.getSnapAdapter();
		// validate
		if(isSnappingAvailable(snapping)) {
			// force snapping on selected object
			tool.doSnapTo();
		}
	}
	
	private void apply() {
		// forward
		tool.apply(true);
	}
	
	public void setVertical(boolean isVertical) {
		// is changed?
		if(this.isVertical != isVertical) {
			this.isVertical = isVertical;
			// setup gui
			setup();
		}
	}
	
	public boolean isVertical() {
		return isVertical;
	}
	
	public void cancel() {
		tool.cancel();
	}

	public void update() {
		getSnapToAttr().setValue(tool.isSnapToMode());
		getConstraintAttr().setValue(tool.isConstrainMode());
		getMinStepAttr().setValue(String.valueOf(tool.getMinStep()));
		getMaxStepAttr().setValue(String.valueOf(tool.getMaxStep()));
	}
		
	/*
	public void setVisible(boolean isVisible) {
		// forward
		super.setVisible(isVisible);
		// has draw tool?
		if(tool!=null) {
			// buffer changes if draw dialog is visible. 
			// if buffered, use tool.apply() to update the mso model
			tool.setBufferedMode(isVisible());
		}
	}
	*/
	
	public boolean isButtonsVisible() {
		return getActionsPanel().isVisible();
	}
	
	public void setButtonsVisible(boolean isVisible) {
		getActionsPanel().setVisible(isVisible);
	}
	
	private boolean isSnappingAvailable(SnapAdapter snapping) {
		// assume available
		boolean bFlag = true;
		// adapter available?
		if(snapping==null) {
			// notify 
			Utils.showWarning("Snapping er ikke tilgjengelig");
			// reset flag
			bFlag = false;
		}
		else if(!snapping.isSnapReady()) {
			// notify
			Utils.showWarning("Du må først velge lag å snappe til");
			// reset flag
			bFlag = false;
		}
		// finished
		return bFlag;		
	}
	
	
	/**
	 * This method initializes optionsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private DiskoPanel getOptionsPanel() {
		if (optionsPanel == null) {
			try {
				VerticalFlowLayout vfl = new VerticalFlowLayout();
				vfl.setVgap(5);
				vfl.setHgap(5);
				vfl.setAlignment(VerticalFlowLayout.LEFT);
				optionsPanel = new DiskoPanel("Alternativer");				
				JPanel panel = (JPanel)optionsPanel.getBodyComponent();
				panel.setPreferredSize(new Dimension(200, 150));
				panel.setLayout(vfl);
				panel.add(getSnapToAttr());
				panel.add(getConstraintAttr());
				panel.add(getMinStepAttr());
				panel.add(getMaxStepAttr());

			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return optionsPanel;
	}
	
	private CheckBoxAttribute getSnapToAttr() {
		if(snapToAttr == null) {
			snapToAttr = new CheckBoxAttribute("autosnap","Automatisk snapping",150,false,true);
			Dimension dim = DiskoButtonFactory.getButtonSize(ButtonSize.NORMAL);
			snapToAttr.setPreferredSize(new Dimension(200,dim.height));
			snapToAttr.setVerticalAlignment(SwingConstants.CENTER);
			snapToAttr.setToolTipText("Snapper tegning automatisk til valgte lag");
			snapToAttr.setButton(DiskoButtonFactory.createButton("GENERAL.EDIT", ButtonSize.NORMAL),true);
			snapToAttr.getCheckBox().addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
				    if (e.getStateChange() == ItemEvent.DESELECTED)
				    	tool.setSnapToMode(false);
				    else {
				    	// get snapping adapter
				    	SnapAdapter snapping = tool.getSnapAdapter();
				    	// validate
				    	if(isSnappingAvailable(snapping)) {
					    	// update
				    		tool.setSnapToMode(true);
				    	}
				    	else {
				    		// reset flag
				    		snapToAttr.getCheckBox().setSelected(false);
				    	}
				    }
				}
				
			});
			snapToAttr.getButton().setActionCommand("editsnap");
			snapToAttr.getButton().addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					// forward
					doAction(e);					
				}
				
			});
		}
		return snapToAttr;
	}
	
	private CheckBoxAttribute getConstraintAttr() {
		if(constraintAttr == null) {
			constraintAttr = new CheckBoxAttribute("constaint","Begrens avstand",150,true,true);
			constraintAttr.setToolTipText("Begrenser avstand mellom punkter på en linje");
			constraintAttr.getCheckBox().addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {

					// get flag
					boolean mode = (e.getStateChange() == ItemEvent.SELECTED);
					
					// update
				    tool.setConstrainMode(mode);
				    getMinStepAttr().setEnabled(mode);
				    getMaxStepAttr().setEnabled(mode);
				    
				}
				
			});
		}
		return constraintAttr;
	}	
	
	private TextFieldAttribute getMinStepAttr() {
		if(minStepAttr == null) {
			minStepAttr = new TextFieldAttribute("min","Minium avstand",150,"10",true);
			minStepAttr.getTextField().setDocument(new NumericDocument(-1,0,false));
			minStepAttr.setToolTipText("Minimum avstand mellom to punktet");
			minStepAttr.getTextField().addFocusListener(new FocusAdapter() {

				@Override
				public void focusLost(FocusEvent e) {
					// forward
					super.focusLost(e);
					// get value as string
					String value = (String)minStepAttr.getValue();
					// set value
					tool.setMinStep(value !=null && !value.isEmpty() ? Integer.valueOf(value) : 0);
				}
				
			});
		}
		return minStepAttr;
	}	
	
	private TextFieldAttribute getMaxStepAttr() {
		if(maxStepAttr == null) {
			maxStepAttr = new TextFieldAttribute("max","Maximum avstand",150,"100",true);
			maxStepAttr.getTextField().setDocument(new NumericDocument(-1,0,false));
			maxStepAttr.setToolTipText("Maximum avstand mellom to punktet");
			maxStepAttr.getTextField().addFocusListener(new FocusAdapter() {

				@Override
				public void focusLost(FocusEvent e) {
					// forward
					super.focusLost(e);
					// get value as string
					String value = (String)maxStepAttr.getValue();
					// set value
					tool.setMaxStep(value !=null && !value.isEmpty() ? Integer.valueOf(value) : 0);
				}
				
			});
		}
		return maxStepAttr;
	}	


	private DiskoPanel getActionsPanel() {
		if (actionsPanel == null) {
			try {
				actionsPanel = new DiskoPanel("Utfør");
				actionsPanel.addButton(getSnapToButton(),"snapto");
				actionsPanel.addButton(getApplyButton(),"apply");
				actionsPanel.addButton(getCancelButton(),"cancel");
				actionsPanel.setBodyComponent(null);
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return actionsPanel;
	}
	
	public JButton getCancelButton() {
		if (cancelButton == null) {
			try {
				cancelButton = DiskoButtonFactory.createButton("GENERAL.CANCEL",ButtonSize.NORMAL);
				cancelButton.setActionCommand("cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// forward
						cancel();
						// forward
						doAction(e);
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return cancelButton;
	}
	
	private JButton getApplyButton() {
		if (applyButton == null) {
			try {
				applyButton = DiskoButtonFactory.createButton("GENERAL.FINISH",ButtonSize.NORMAL);
				applyButton.setActionCommand("finish");
				applyButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// forward
						apply();
						// forward
						doAction(e);						
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return applyButton;
	}	
	
	
	private JButton getSnapToButton() {
		if (snapToButton == null) {
			try {
				snapToButton = DiskoButtonFactory.createButton("MAP.SNAPTO",ButtonSize.NORMAL);
				snapToButton.setActionCommand("snapTo");
				snapToButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// forward
						doSnapTo();
						// forward
						doAction(e);						
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return snapToButton;
	}	
	
	public void setMsoObject(IMsoObjectIf msoObject) {
		// consume?
		if (tool.getMap() == null) return;
	}
	
	public void addActionListener(ActionListener listener) {
		listeners.add(listener);
	}
	
	public void removeActionListener(ActionListener listener) {
		listeners.remove(listener);
		getActionsPanel().removeActionListener(listener);
	}
	
	private void doAction(ActionEvent e) {
		for(ActionListener it: listeners) {
			it.actionPerformed(e);
		}
	}
	
	public void onSnapToChanged() {
		// get adapter
		SnapAdapter adapter = tool.getSnapAdapter();
		// enable auto snapping check?
		getSnapToAttr().getCheckBox().setEnabled(adapter.isSnapReady() && adapter.isSnappingAllowed());
	}

	public void onSnapableChanged() {}
	
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
