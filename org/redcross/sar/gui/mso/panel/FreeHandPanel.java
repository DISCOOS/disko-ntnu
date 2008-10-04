package org.redcross.sar.gui.mso.panel;
 
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.redcross.sar.gui.document.NumericDocument;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.field.CheckBoxField;
import org.redcross.sar.gui.field.TextLineField;
import org.redcross.sar.gui.panel.FieldsPanel;
import org.redcross.sar.gui.panel.DefaultToolPanel;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.tool.FreeHandTool;
import org.redcross.sar.map.tool.SnapAdapter;
import org.redcross.sar.map.tool.SnapAdapter.SnapListener;
import org.redcross.sar.util.Utils;

public class FreeHandPanel extends DefaultToolPanel implements SnapListener {

	private static final long serialVersionUID = 1L;
	
	private JButton snapToButton = null;
	private FieldsPanel optionsPanel = null;
	private CheckBoxField snapToAttr = null;
	private TextLineField minStepAttr = null;
	private TextLineField maxStepAttr = null;
	private CheckBoxField constraintAttr = null;
	
	public FreeHandPanel(FreeHandTool tool) {
		// forward
		this("Tegne frihånd",tool);
	}
	
	public FreeHandPanel(String caption, FreeHandTool tool) {
		
		// forward
		super(caption,tool);
		
		// initialize gui
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		
		// set preferred body size
		setPreferredBodySize(new Dimension(200,200));
		
		// set body panel
		setBodyComponent(getOptionsPanel());
		
		// add buttons
		insertButton("finish",getSnapToButton(),"snapto");

	}
	
	/**
	 * This method initializes optionsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private FieldsPanel getOptionsPanel() {
		if (optionsPanel == null) {
			try {

				optionsPanel = new FieldsPanel("Alternativer","",false,false,ButtonSize.SMALL);
				optionsPanel.setPreferredBodySize(new Dimension(200, 150));
				optionsPanel.addAttribute(getSnapToAttr());
				optionsPanel.addAttribute(getConstraintAttr());
				optionsPanel.addAttribute(getMinStepAttr());
				optionsPanel.addAttribute(getMaxStepAttr());

			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return optionsPanel;
	}
	
	private CheckBoxField getSnapToAttr() {
		if(snapToAttr == null) {
			snapToAttr = new CheckBoxField("autosnap","Automatisk snapping",true,135,35,false);
			snapToAttr.setVerticalAlignment(SwingConstants.CENTER);
			snapToAttr.setToolTipText("Snapper tegning automatisk til valgte lag");
			snapToAttr.getCheckBox().addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
				    if (e.getStateChange() == ItemEvent.DESELECTED)
				    	getTool().setSnapToMode(false);
				    else {
				    	// get snapping adapter
				    	SnapAdapter snapping = getTool().getSnapAdapter();
				    	// validate
				    	if(isSnappingAvailable(snapping)) {
					    	// update
				    		getTool().setSnapToMode(true);
				    	}
				    	else {
				    		// reset flag
				    		snapToAttr.getCheckBox().setSelected(false);
				    	}
				    }
				}
				
			});
			snapToAttr.setButtonVisible(true);
			snapToAttr.setButtonCommand("editsnap");
			snapToAttr.addButtonActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					// forward
					fireActionEvent(e);					
				}
				
			});
			addAction("editsnap");
		}
		return snapToAttr;
	}
		
	private CheckBoxField getConstraintAttr() {
		if(constraintAttr == null) {
			constraintAttr = new CheckBoxField("constaint","Begrens avstand",true,135,25,true);
			constraintAttr.setToolTipText("Begrenser avstand mellom punkter på en linje");
			constraintAttr.getCheckBox().addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {

					// get flag
					boolean mode = (e.getStateChange() == ItemEvent.SELECTED);
					
					// update
				    getTool().setConstrainMode(mode);
				    getMinStepAttr().setEnabled(mode);
				    getMaxStepAttr().setEnabled(mode);
				    
				}
				
			});
		}
		return constraintAttr;
	}	
	
	private TextLineField getMinStepAttr() {
		if(minStepAttr == null) {
			minStepAttr = new TextLineField("min","Minium avstand",true,135,25,"10");
			minStepAttr.getTextField().setDocument(new NumericDocument(-1,0,false));
			minStepAttr.setToolTipText("Minimum avstand mellom to punktet");
			minStepAttr.getTextField().getDocument().addDocumentListener(new DocumentListener() {

				public void changedUpdate(DocumentEvent e) { change(); }
				public void insertUpdate(DocumentEvent e) { change(); }
				public void removeUpdate(DocumentEvent e) { change(); }
				
				private void change() {
					// consume?
					if(!isChangeable()) return;
					// get value as string
					String value = (String)minStepAttr.getValue();
					// set value
					getTool().setMinStep(value !=null && !value.isEmpty() ? Integer.valueOf(value) : 0);
				}
				
			});
			
		}
		return minStepAttr;
	}	
	
	private TextLineField getMaxStepAttr() {
		if(maxStepAttr == null) {
			maxStepAttr = new TextLineField("max","Maximum avstand",true,135,25,"100");
			maxStepAttr.getTextField().setDocument(new NumericDocument(-1,0,false));
			maxStepAttr.setToolTipText("Maximum avstand mellom to punktet");
			maxStepAttr.getTextField().getDocument().addDocumentListener(new DocumentListener() {

				public void changedUpdate(DocumentEvent e) { change(); }
				public void insertUpdate(DocumentEvent e) { change(); }
				public void removeUpdate(DocumentEvent e) { change(); }
				
				private void change() {
					// consume?
					if(!isChangeable()) return;
					// get value as string
					String value = (String)maxStepAttr.getValue();
					// set value
					getTool().setMaxStep(value !=null && !value.isEmpty() ? Integer.valueOf(value) : 0);
				}
				
			});
		}
		return maxStepAttr;
	}	

	private JButton getSnapToButton() {
		if (snapToButton == null) {
			try {
				snapToButton = DiskoButtonFactory.createButton("MAP.SNAPTO",ButtonSize.SMALL);
				snapToButton.setActionCommand("snapto");
				snapToButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// forward
						doSnapTo();
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return snapToButton;
	}	
			
	/* ===========================================
	 * Private methods
	 * ===========================================
	 */
	
	private void doSnapTo() {
		// get adapter
		SnapAdapter snapping = getTool().getSnapAdapter();
		// validate
		if(isSnappingAvailable(snapping)) {
			// consume change events
			setChangeable(false);
			// force snapping on selected object
			boolean bFlag = getTool().doSnapTo();
			// resume changes
			setChangeable(true);
			// reset flag?
			if(bFlag) setDirty(false);
		}
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
	
	/* ===========================================
	 * Overridden public methods
	 * ===========================================
	 */

	/* ===========================================
	 * SnapListener interface implementation
	 * ===========================================
	 */
	
	public void onSnapToChanged() {
		// get adapter
		SnapAdapter adapter = getTool().getSnapAdapter();
		// enable auto snapping check?
		getSnapToAttr().getCheckBox().setEnabled(adapter.isSnapReady() && adapter.isSnappingAllowed());
	}

	public void onSnapableChanged() {
		// get adapter
		SnapAdapter adapter = getTool().getSnapAdapter();
		// enable auto snapping check?
		getSnapToAttr().getCheckBox().setEnabled(adapter.isSnapReady() && adapter.isSnappingAllowed());		
	}
	
	
	/* ===========================================
	 * IPropertyPanel interface implementation
	 * ===========================================
	 */

	@Override
	public FreeHandTool getTool() {
		return (FreeHandTool)super.getTool();
	}
	
	public void update() {
		
		// forward
		super.update();
		
		// suspend events
		setChangeable(false);
		
		try {
			
			// update attributes
			getSnapToAttr().setValue(getTool().isSnapToMode());
			getConstraintAttr().setValue(getTool().isConstrainMode());
			getMinStepAttr().setValue(String.valueOf(getTool().getMinStep()));
			getMaxStepAttr().setValue(String.valueOf(getTool().getMaxStep()));
			// update caption
			if(getTool().getMap().isEditSupportInstalled())
				setCaptionText(getTool().getMap().getDrawAdapter().getDescription());
			else 
				setCaptionText(MapUtil.getDrawText(getTool().getMsoObject(), 
						getTool().getMsoCode(), getTool().getDrawMode())); 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// resume changes
		setChangeable(true);
	}	
		
}  //  @jve:decl-index=0:visual-constraint="10,10"
