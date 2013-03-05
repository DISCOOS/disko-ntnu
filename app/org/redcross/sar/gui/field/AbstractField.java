package org.redcross.sar.gui.field;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.undo.UndoableEdit;

import org.apache.log4j.Logger;
import org.redcross.sar.Application;
import org.redcross.sar.data.IData.DataOrigin;
import org.redcross.sar.data.IData.DataState;
import org.redcross.sar.gui.event.FieldModelEvent;
import org.redcross.sar.gui.event.IFieldListener;
import org.redcross.sar.gui.event.FieldEvent;
import org.redcross.sar.gui.event.IFieldModelListener;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.IChangeIf;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.AttributeImpl.MsoPolygon;
import org.redcross.sar.mso.data.AttributeImpl.MsoRoute;
import org.redcross.sar.mso.data.AttributeImpl.MsoTrack;
import org.redcross.sar.undo.FieldEdit;
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.AbstractWork;
import org.redcross.sar.work.IWorkLoop;
import org.redcross.sar.work.WorkPool;
import org.redcross.sar.work.event.IFlowListener;
import org.redcross.sar.work.event.FlowEvent;
import org.redcross.sar.wp.IDiskoWpModule;

/**
 * 
 * @author Administrator
 *
 * @param I - field value type
 * @param M - field model value type
 * @param E - edit component type
 * @param V - view component type
 */
@SuppressWarnings("unchecked")
public abstract class AbstractField<F, E extends Component, V extends Component> extends JPanel implements IField<F> {

	private static final long serialVersionUID = 1L;	
	
	private static final int REMOTE_STATE_RESET = 3000;

	protected static final int MINIMUM_COMPONENT_WIDTH = 50;
	protected static final int DEFAULT_CAPTION_WIDTH = 80;
	protected static final int DEFAULT_MAXIMUM_HEIGHT = 25;

	protected static final Logger m_logger = Logger.getLogger(AbstractField.class);
	
	protected static UpdateWork m_updateWork;
	
	protected JLabel m_valueLabel;
	protected JLabel m_captionLabel;
	protected E m_editComponent;
	protected V m_viewComponent;
	protected AbstractButton m_button;

	protected String m_caption;
	
	protected F m_oldEditValue;

	protected IFieldModel<F> m_model;

	protected int m_isMarked = 0;

	protected boolean m_isBufferMode = true;

	private int m_editableCount = 0;
	private int m_changeableCount = 0;

	private int m_fixedWidth;
	private int m_fixedHeight;
	
	private boolean m_isDirty = false;
	private boolean m_isTrackingFocus = false;
	private boolean m_isValueAdjusting = false;
	private boolean m_isEditValueAdjusting = false;
	
	private Color m_vBg;					// remote data origin view background 
	private Color m_eBg;					// remote data origin edit background 
	private Color m_lBg = Color.YELLOW;		// remote data origin background
	private Color m_cBg = Color.RED;		// conflict data origin change background
	private Color m_sBg = Color.GREEN;		// change in remote data background
	
	private DataOrigin m_origin = DataOrigin.NONE;
	
	private final EventListenerList m_listeners = new EventListenerList();

	/*==================================================================
	 * Anonymous classes
	 *================================================================== */

	private FocusListener m_focusListener = new FocusListener() {

		@Override
		public void focusGained(FocusEvent e) {
			focusChanged(true);
		}

		@Override
		public void focusLost(FocusEvent e) {
			focusChanged(false);
		}

	};
	
	protected final DocumentListener m_documentListener = new DocumentListener() {

		@Override
		public void changedUpdate(DocumentEvent e) { /*NOP*/ }
		@Override
		public void insertUpdate(DocumentEvent e) { onEditValueChanged(); }
		@Override
		public void removeUpdate(DocumentEvent e) { onEditValueChanged(); }
		
	};
	
	protected final IFieldModelListener m_modelListener = new IFieldModelListener() {

		@Override
		public void onFieldModelChanged(FieldModelEvent e) {
			// update field value?
			if(!isEditValueAdjusting() && e.isValueChanged()) {
				// consume edit events
				setEditValueAdjusting(true);
				// update edit values
				setNewEditValue(m_model.getValue());
				setOldEditValue(getEditValue());
				// resume handling of edit events
				setEditValueAdjusting(false);
				// reset flag
				m_isDirty = false;
			}
			// update background?
			if(e.isOriginChanged() || e.isStateChanged()) {
				// forward
				setFieldBackground();
			}
		}
		
	};

	/* ==================================================================
	 *  Constructors
	 * ================================================================== */

	protected AbstractField(String name, String caption, boolean isEditable) {
		this(name,caption,isEditable,DEFAULT_CAPTION_WIDTH,DEFAULT_MAXIMUM_HEIGHT,null);
	}

	protected AbstractField(String name, String caption, boolean isEditable, int width, int height, Object value) {
		// forward
		super();
		// suspend listeners
		setChangeable(false);
		// initialize GUI
		initialize(width, height);
		// get default background colors
		m_vBg = getBackground();
		m_eBg = getEditComponent().getBackground();
		// initialize model
		setModel(new DefaultFieldModel<F>());
		// update
		setName(name);
		setCaptionText(caption);
		setValue(value);
		setEditable(isEditable);
		// resume listeners
		setChangeable(true);
	}

	protected AbstractField(IMsoAttributeIf<F> attribute, String caption, boolean isEditable) {
		// forward
		this(attribute.getName(),caption,isEditable);
		// set attribute
		if(!setMsoAttribute(attribute)) throw new IllegalArgumentException("Attribute datatype not supported");
		// get value from attribute
		reset();
	}

	protected AbstractField(IMsoAttributeIf<F> attribute,
			String caption, boolean isEditable,
			int width, int height) {
		// forward
		this(attribute.getName(),caption,isEditable,width,height,null);
		// set attribute
		if(!setMsoAttribute(attribute)) throw new IllegalArgumentException("Attribute datatype not supported");
		// get value from attribute
		reset();
	}


	/* ==================================================================
	 *  Protected methods
	 * ================================================================== */
	
	/**
	 * This method checks if the given component implements the Scrollable
	 * interface. The method is used to determine if view and edit components 
	 * should be wrapped by a JScrollPane. Extenders of this class that have
	 * view or edit components that implements Scrollable that should not be 
	 * wrapped by a JScrollPane automatically, must override this method
	 * and return false accordingly.
	 * 
	 * @param c - the component to check if scrollable
	 * @return boolean
	 */
	protected boolean isScrollable(Component c) {
		return (c instanceof Scrollable);
	}
	
	protected JScrollPane createDefaultScrollPane(Component c) {
		JScrollPane scrollPane = UIFactory.createScrollPane(c,false);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		return scrollPane;
	}
	
	protected int getDefaultMaximumHeight() {
		Component c = getComponent();
		return isScrollable(c)?UIManager.getInt("ScrollBar.height")+DEFAULT_MAXIMUM_HEIGHT:DEFAULT_MAXIMUM_HEIGHT;
	}
	
	protected JLabel getCaption() {
		if(m_captionLabel==null) {
			m_captionLabel = new JLabel(m_caption);
			m_captionLabel.setOpaque(false);
			m_captionLabel.setVerticalAlignment(SwingConstants.TOP);
			m_captionLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			m_captionLabel.setAlignmentY(JComponent.CENTER_ALIGNMENT);
		}
		return m_captionLabel;
	}

	protected IDiskoMap getInstalledMap() {
		// try to get map from current
		IDiskoWpModule module = Application.getInstance().getCurrentRole().getCurrentDiskoWpModule();
		if(module!=null) {
			if(module.isMapInstalled())
				return module.getMap();
		}
		// no map available
		return null;
	}

	/**
	 * This method initializes Button
	 *
	 * @return {@link AbstractButton}
	 */
	protected AbstractButton getButton() {
		if (m_button == null) {
			m_button = DiskoButtonFactory.createButton("GENERAL.EDIT", ButtonSize.SMALL);
			m_button.setVisible(false);
		}
		return m_button;
	}

	protected void focusChanged(boolean inFocus) {
		if(m_isTrackingFocus) {
			if(inFocus) {
				setBorder(UIFactory.createBorder(1, 1, 1, 1, Color.BLUE));
			}
			else {
				setBorder(BorderFactory.createEmptyBorder());
			}
		}
	}
	
	protected F getOldEditValue() {
		return m_oldEditValue;
	}
	
	protected void setOldEditValue(F value) {
		m_oldEditValue = value;
	}
	
	/**
	 * Call this method when edit value 
	 * is changed in the edit control. 
	 */
	protected void onEditValueChanged() {
		if(!isEditValueAdjusting()) {
			// consume edit events
			setEditValueAdjusting(true);
			/* update field value. This will cause a 
			 * FlowEvent to be fired. */
			setEditValue(getEditValue());
			// resume handling of edit events 
			setEditValueAdjusting(false);
		}
	}
	
	/**
	 * Set the value adjusting flag. This flag indicate that
	 * a change in value is occurring, and that event handling
	 * is in progress. Use this method to indicate that
	 * the value is changing, and that any change events should
	 * be handled accordingly.
	 */
	protected void setValueAdjusting(boolean isAdjusting) {
		m_isValueAdjusting = isAdjusting;
	}
	
	/**
	 * Set the edit value adjusting flag. This flag indicate that
	 * a change in edit value has occurred, and that event handling
	 * is in progress. Use this method to indicate that
	 * the edit value has changed, and that any change events should
	 * be handled accordingly.
	 */
	protected void setEditValueAdjusting(boolean isAdjusting) {
		m_isEditValueAdjusting = isAdjusting;
	}
		
	protected static boolean isValueChanged(Object oldValue, Object newValue) {
		return !isEqual(oldValue, newValue);
	}
	
	protected void setFieldBackground() {
		// get data origin
		DataOrigin origin = m_model.getOrigin();
		// get flags
		boolean bFlag = 
			m_model.isState(DataState.LOOPBACK) ||
			m_model.isState(DataState.ROLLBACK);
		// set background
		switch(origin) {
		case LOCAL: 
			getComponent().setBackground(m_lBg);
			break;
		case REMOTE:
			// any change?
			if(!(bFlag || m_origin.equals(origin) 
					   || m_origin.equals(DataOrigin.NONE))) {
				// set server change indication
				getComponent().setBackground(m_sBg);
				/* ==========================================
				 * This adds the reset to default background
				 * using the static UpdateWork instance 
				 * m_updateWork. This method ensures that
				 * every change of attribute state to REMOTE 
				 * is shown the duration REMOTE_UPDATE_RESET 
				 * using the color m_sBg. 
				 * ========================================== */
				setUpdateWork(this);
			} else if(m_updateWork==null || !m_updateWork.contains(this)) {
				getComponent().setBackground(isEditable()?m_eBg:m_vBg);
			}
			break;
		case CONFLICT:
			getComponent().setBackground(m_cBg);
			break;
		default:
			getComponent().setBackground(isEditable()?m_eBg:m_vBg);			
			break;
		}
		// update state
		m_origin = origin;		
	}
	
	protected abstract boolean setNewEditValue(Object value);
	protected abstract boolean isMsoAttributeSettable(IMsoAttributeIf<?> attr);
	
	/* ==================================================================
	 *  Private methods
	 * ================================================================== */

	private void initialize(int width, int height) {
		// update fixed sizes
		setFixedCaptionWidth(width);
		setFixedHeight(height);
		// add focus listeners
		getEditComponent().addFocusListener(m_focusListener);
		getButton().addFocusListener(m_focusListener);
		// USE TO DEBUG LAYOUT PROBLEMS
		// setBorder(BorderFactory.createLineBorder(Color.RED)); 		
	}
	
	private void build() {
		this.removeAll();
		this.setLayout(new BorderLayout(5,5));
		this.add(getCaption(),BorderLayout.WEST);
		Component c = null;
		if(isEditable()) {
			c = getEditComponent();
			if(c instanceof JComponent) {
				((JComponent)c).setAlignmentX(JComponent.LEFT_ALIGNMENT);
				((JComponent)c).setAlignmentY(JComponent.CENTER_ALIGNMENT);
			}
		} else {
			c = getViewComponent();
		}
		add(getFieldComponent(c),BorderLayout.CENTER);
		add(getButton(),BorderLayout.EAST);
		setOpaque(false);		
		getCaption().setLabelFor(c);			
	}
	
	private Component getFieldComponent(Component c) {
		if(isScrollable(c)) {
			c = createDefaultScrollPane(c);
		}
		return c;
	}
	
	private void fireOnWorkChange(UndoableEdit edit) {

		// initialize
		FlowEvent e = null;
		
		// consume?
		if(!isChangeable()) return;

		// save change now?
		if(isBufferMode()) {
			m_isDirty = true;
			e = new FlowEvent(this,getEditValue(),edit,FlowEvent.EVENT_CHANGE);
		}
		else {
			//Application.getInstance().getMsoModel().suspendClientUpdate();
			if(finish()) {
				m_isDirty = false;
				e = new FlowEvent(this,getEditValue(),edit,FlowEvent.EVENT_FINISH);
			}
			else {
				// buffer change
				m_isDirty = true;
				// notify change instead
				e = new FlowEvent(this,getEditValue(),edit,FlowEvent.EVENT_CHANGE);
			}
			//Application.getInstance().getMsoModel().resumeClientUpdate(true);
		}
		// notify
		fireOnWorkChange(e);
	}

	private void fireOnWorkChange(FlowEvent e) {
		// get listeners
		IFlowListener[] list = m_listeners.getListeners(IFlowListener.class);
		// forward
		for(int i=0; i<list.length; i++) {
			list[i].onFlowPerformed(e);
		}
	}

	private void fireFieldChanged(int type) {
		fireFieldChange(new FieldEvent(this,type));
	}
	
	private void fireFieldChange(FieldEvent e) {
		// get listeners
		IFieldListener[] list = m_listeners.getListeners(IFieldListener.class);
		// forward
		for(int i=0; i<list.length; i++) {
			list[i].onFieldChanged(e);
		}
		
	}
	
	/* ==================================================================
	 *  Public methods
	 * ================================================================== */

	public boolean isTrackingFocus() {
		return m_isTrackingFocus;
	}

	public void setTrackingFocus(boolean isTrackingFocus) {
		m_isTrackingFocus = isTrackingFocus;
	}
	
	/**
	 * Check if a value or edit value is in progress.
	 * @return Returns {@code true} if change is in progress.
	 */
	public boolean isAdjusting() {
		return m_isValueAdjusting || m_isEditValueAdjusting;
	}	
	

	/**
	 * Get the value adjusting flag. This flag indicate that
	 * a change in value is occurring, and that  event 
	 * handling is in progress.
	 *  
	 * @return Returns {@code true} if value is adjusting. 
	 */
	public boolean isValueAdjusting() {
		return m_isValueAdjusting;
	}	
	
	/**
	 * Get the edit value adjusting flag. This flag indicate that
	 * a change in edit value is occurring, and that  event 
	 * handling is in progress.
	 *  
	 * @return Returns {@code true} if edit value is adjusting. 
	 */
	public boolean isEditValueAdjusting() {
		return m_isEditValueAdjusting;
	}		
	
	public int getVerticalAlignment() {
		return getCaption().getVerticalAlignment();
	}

	public int getHorizontalAlignment() {
		return getCaption().getHorizontalAlignment();
	}

	public void setVerticalAlignment(int alignment) {
		getCaption().setVerticalAlignment(alignment);
	}

	public void setHorizontalAlignment(int alignment) {
		getCaption().setHorizontalAlignment(alignment);
	}

	public boolean isChangeable() {
		return (m_changeableCount==0);
	}

	public void setChangeable(boolean isChangeable) {
		if(!isChangeable)
			m_changeableCount++;
		else if(m_changeableCount>0)
			m_changeableCount--;
	}
	
	public int clearChangeableCount() {
		int count = m_changeableCount;
		m_changeableCount = 0;
		return count;
	}	

	public boolean isDirty() {
		return m_isDirty;
	}
	
	public void setDirty(boolean isDirty) {
		if(m_isDirty)
		{
			parse();
		}
		m_isDirty = isDirty;
	}

	public int isMarked() {
		return m_isMarked;
	}

	public void setMarked(int isMarked) {
		if(m_isMarked != isMarked) {
			m_isMarked = isMarked;
			fireFieldChanged(FieldEvent.EVENT_FIELD_CHANGED);
		}
	}

	public int getFixedCaptionWidth() {
		return m_fixedWidth;
	}

	public void setFixedCaptionWidth(int width) {
		// save width
		m_fixedWidth = width;
		// translate
		width = (width==-1 ? DEFAULT_CAPTION_WIDTH : width);
		// set fixed caption width
		Utils.setFixedWidth(getCaption(), width);
		// set minimum size
		Dimension d = getButton().getPreferredSize();
		this.setMinimumSize(new Dimension(width+MINIMUM_COMPONENT_WIDTH+d.width,d.height));
		this.setPreferredSize(new Dimension(width+MINIMUM_COMPONENT_WIDTH+d.width,d.height));
	}

	public int getFixedHeight() {
		return m_fixedHeight;
	}

	public void setFixedHeight(int height) {
		// save height
		m_fixedHeight = height;
		// constrain height to button
		height = getButton()!=null && getButton().isVisible() ? Math.max(height,getButton().getPreferredSize().height) : height;
		// translate
		height = (height==-1 ? getDefaultMaximumHeight() : height);
		// set fixed height
		Utils.setFixedHeight(this, height);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		// forward
		super.setEnabled(isEnabled);
		// forward
		getEditComponent().setEnabled(isEnabled);
		getViewComponent().setEnabled(isEnabled);
		// update button
		getButton().setEnabled(isEnabled);
	}

	public boolean isEditable() {
		return m_editableCount==0;
	}
	
	public final void setEditable(boolean isEditable) {
		if(!isEditable)
			m_editableCount++;
		else if(m_editableCount>0)
			m_editableCount--;
		if(m_editableCount<2) {
			getButton().setEnabled(isEditable());
			build();
		}
	}

	public int clearEditableCount() {
		int count = m_editableCount;
		m_editableCount = 0;
		if(count>0) {
			getButton().setEnabled(true);
			build();			
		}
		return count;
	}	
	
	@Override
	public void setBufferMode(boolean isBufferMode) {
		m_isBufferMode = isBufferMode;
	}

	@Override
	public boolean isBufferMode() {
		return m_isBufferMode;
	}
	
	@Override
	public void setToolTipText(String text) {
		// TODO Auto-generated method stub
		super.setToolTipText(text);
		getCaption().setToolTipText(text);
		if(getEditComponent() instanceof JComponent)
			((JComponent)getEditComponent()).setToolTipText(text);
	}

	public String getCaptionText() {
		return getCaption().getText();
	}

	public void setCaptionText(String text) {
		getCaption().setText(text);
	}

	/**
	 * This method sets the caption colors
	 *
	 */
	public void setCaptionColor(Color foreground,Color background) {
		this.setForeground(foreground);
		this.setBackground(background);
		getCaption().setForeground(foreground);
		getCaption().setBackground(background);
	}

	public boolean fill(Object values) { return true; };

	public boolean finish() {

		// consume?
		if(!(isChangeable() || m_isDirty)) return false;

		// reset flag
		m_isDirty = false;

		// consume flow events
		setChangeable(false);

		// forward
		m_model.setValue(getEditValue());
		
		// forward
		m_model.parse();

		// resume handling of flow events
		setChangeable(true);

		// finished
		return true;
	}
	
	public boolean cancel() {

		// consume?
		if(!(isChangeable() || m_isDirty)) return false;

		// reset flag
		m_isDirty = false;

		// load old value
		parse();
		
		// finished
		return true;
		
	}

	public void reset() {		
		
		// consume?
		if(!isChangeable()) return;
		
		// reset model
		m_model.reset();
		
		// reset flag
		m_isDirty = false;
		
	}
	
	public void parse() {
		// forward
		m_model.parse();
	}
	
	public boolean isChanged() {
		return m_model.isChanged();
	}
	
	@Override
	public IFieldModel<F> getModel() {
		return m_model;
	}

	@Override
	public void setModel(IFieldModel<F> model) {
		// not allowed?
		if(model==null) 
			throw new IllegalArgumentException("IFieldModel can not be null");
		// unregister model listener?
		if(m_model!=null) {
			// remove listener
			m_model.removeFieldModelListener(m_modelListener);
			// notify listeners of model reset
			fireFieldChanged(FieldEvent.EVENT_MODEL_RESET);
		}
		// replace model
		m_model = model;
		// add model listener
		m_model.addFieldModelListener(m_modelListener);
		// initialize states
		m_origin = DataOrigin.NONE; 
		// notify field of change
		m_model.reset();
		// notify listeners of model set
		fireFieldChanged(FieldEvent.EVENT_MODEL_SET);
	}
	
	public List<IChangeIf> getChanges()
	{
		List<IChangeIf> changes = new Vector<IChangeIf>();
		if(m_model instanceof MsoAttributeFieldModel) {
			IChangeIf change = ((MsoAttributeFieldModel)m_model).getChange();
			if(change!=null)
			{
				changes.add(change);
			}
		}
		else if(m_model instanceof MsoFormatFieldModel) {
			changes.addAll(((MsoFormatFieldModel)m_model).getChanges());
		}
		else if(m_model instanceof MsoParserFieldModel) {
			changes.addAll(((MsoParserFieldModel)m_model).getChanges());
		}
		return changes;
	}

	public IMsoAttributeIf<F> getMsoAttribute() {
		if(m_model instanceof MsoAttributeFieldModel) {
			return ((MsoAttributeFieldModel)m_model).getMsoAttribute(); 
		}
		return null;
	}

	public void clearModel() {
		setModel(new DefaultFieldModel<F>());
	}
	
	public void clearModel(Object newEditValue) {
		clearModel();
		setEditValue(newEditValue);
	}
	
	/**
	 * Method for testing if the attribute is testing 
	 * @param attribute - the attribute to test
	 * @return 
	 */
  	public static boolean isMsoAttributeSupported(IMsoAttributeIf<?> attribute) {
		return !(attribute instanceof MsoPolygon ||
				attribute instanceof MsoRoute ||
				attribute instanceof MsoTrack);
  	}

	public void addFlowListener(IFlowListener listener) {
		m_listeners.add(IFlowListener.class,listener);
	}

	public void removeFlowListener(IFlowListener listener) {
		m_listeners.remove(IFlowListener.class,listener);

	}

	public void addFieldListener(IFieldListener listener) {
		m_listeners.add(IFieldListener.class,listener);
	}

	public void removeFieldListener(IFieldListener listener) {
		m_listeners.remove(IFieldListener.class,listener);
	}
	
	public void installButton(AbstractButton button, boolean isVisible) {
		// remove current?
		if(m_button!=null) {
			m_button.removeFocusListener(m_focusListener);
			this.remove(m_button);

		}
		// prepare
		m_button = button;
		// force?
		if(m_button==null) getButton();
		// update
		m_button.setVisible(isVisible);
		m_button.setEnabled(isEnabled() && isEditable());
		m_button.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		m_button.setAlignmentY(JComponent.CENTER_ALIGNMENT);
		if(button!=null) this.add(button,BorderLayout.EAST);
		setFixedHeight(m_fixedHeight);
		m_button.addFocusListener(m_focusListener);
		m_button.setEnabled(isEditable());
	}

	public String getButtonText() {
		return getButton().getText();
	}

	public void setButtonText(String text) {
		getButton().setText(text);
	}

	public String getButtonTooltipText() {
		return getButton().getToolTipText();
	}

	public void setButtonTooltipText(String text) {
		getButton().setToolTipText(text);
	}

	public boolean isButtonVisible() {
		return getButton().isVisible();
	}

	public void setButtonVisible(boolean isVisible) {
		if(getButton().isVisible()!=isVisible) {
			getButton().setVisible(isVisible);
			setFixedHeight(m_fixedHeight);
		}
	}

	public boolean isButtonEnabled() {
		return getButton().isEnabled();
	}

	public void setButtonEnabled(boolean isEnabled) {
		getButton().setEnabled(isEnabled);
	}

	public Icon getButtonIcon() {
		return getButton().getIcon();
	}

	public void setButtonIcon(Icon icon) {
		getButton().setIcon(icon);
	}

	public String getButtonCommand() {
		return getButton().getActionCommand();
	}

	public void setButtonCommand(String name) {
		getButton().setActionCommand(name);

	}

	public boolean addButtonActionListener(ActionListener listener) {
		if(listener!=null && m_button!=null) {
			getButton().addActionListener(listener);
			return true;
		}
		return false;
	}

	public boolean removeButtonActionListener(ActionListener listener) {
		if(listener!=null && m_button!=null) {
			getButton().removeActionListener(listener);
			return true;
		}
		return false;
	}
	
	public Component getComponent() {
		return isEditable()?getEditComponent():getViewComponent();
	}

	/*==================================================================
	 * Abstract public methods
	 *================================================================== */

	public abstract E getEditComponent();
	public abstract V getViewComponent();

	public abstract String getFormattedText();
	
	public final F getValue() {
		if(m_isDirty) {
			return getEditValue();
		}
		return m_model.getValue();
	}
	
	public abstract F getEditValue();
	
	public final boolean setEditValue(Object value) {
		/* get old edit value (the value stored last
		 * time the edit value was changed) */
		F oldValue = getOldEditValue();
		/* if edit value is already adjusting, 
		 * calling setNewEditValue is not need since 
		 * the edit value already is changed. */
		if(isEditValueAdjusting() || setNewEditValue(value)) {
			// notify change?
			if(isValueChanged(oldValue, value)) {
				fireOnWorkChange(new FieldEdit(this,oldValue,value));
			}
			// replace old edit value with current 
			setOldEditValue(getEditValue());
			// success
			return true;
		}
		// failure
		return false;		
	}
	
	public final boolean setEditValue(Object value, boolean notify) {
		setChangeable(notify);
		boolean bFlag = setEditValue(value);
		setChangeable(!notify);
		return bFlag;
	}
	
	public final boolean setValue(Object value) {
		// set value adjustment in progress flag
		setValueAdjusting(true);
		// initialize result flag
		boolean bFlag = false;
		// get old value 
		F oldValue = getValue();
		// set current edit value 
		setEditValue(value);			
		// get new edit value
		F newValue = getEditValue();
		// update model?
		if(isValueChanged(oldValue, newValue)) {
			// set model value
			m_model.setLocalValue(newValue);
			// reset dirty flag
			m_isDirty = false;
			// success
			bFlag = true;
		}
		// value change is completed
		setValueAdjusting(false);
		// finished
		return bFlag;
	}
	
	public final boolean setValue(Object value, boolean notify) {
		setChangeable(notify);
		boolean bFlag = setValue(value);
		setChangeable(!notify);
		return bFlag;
	}
	
	public final boolean setMsoAttribute(IMsoAttributeIf attribute) {
		// is supported?
		if(isMsoAttributeSupported(attribute)) {
			// match component type and attribute
			if(isMsoAttributeSettable(attribute)) {
				// update name
				setName(attribute.getName());
				// create model
				setModel(new MsoAttributeFieldModel<F>(attribute));
				// success
				return true;
			}
		}
		// failure
		return false;		
	}
	
	/*==================================================================
	 * Static helper methods
	 *================================================================== */

	protected static boolean isEqual(Object o1, Object o2) {
		if(o1==o2) return true;
		if(o1!=null) return o1.equals(o2);
		return false;
	}
	
	protected static JFormattedTextField createDefaultComponent(boolean isEditable) {
		return createDefaultComponent(isEditable,null);
	}
	
	protected static JFormattedTextField createDefaultComponent(boolean isEditable, final DocumentListener listener) {
		JFormattedTextField textField = listener!=null ? new JFormattedTextField() {
			
			private static final long serialVersionUID = 1L;
										
			@Override
			public void setDocument(Document doc) {
				// cleanup?
				if(super.getDocument()!=null) 
					super.getDocument().removeDocumentListener(listener);  
				// replace 
				super.setDocument(doc);
				// add listener?
				if(doc!=null) doc.addDocumentListener(listener);
			}
			
		} : new JFormattedTextField();
		textField.setEditable(isEditable);
		return textField;
	}	

	protected static JLabel createLabelComponent() {
		JLabel label = new JLabel();
		Border outer = (new JTextField()).getBorder();
		Border inner = BorderFactory.createEmptyBorder(2,2,2,2);
		label.setBorder(BorderFactory.createCompoundBorder(outer,inner));
		return label;
	}
	
	protected static JTextArea createTextAreaComponent(boolean isEditable) {
		return createTextAreaComponent(isEditable,null,null);
	}
	
	protected static JTextArea createTextAreaComponent(boolean isEditable, final JTextComponent coobject, final DocumentListener listener) {
		JTextArea area = listener!=null ? new JTextArea() {
			
			private static final long serialVersionUID = 1L;
										
			@Override
			public void setDocument(Document doc) {
				// cleanup?
				if(super.getDocument()!=null) 
					super.getDocument().removeDocumentListener(listener);  
				// replace 
				super.setDocument(doc);
				coobject.setDocument(doc);
				// add listener?
				if(doc!=null) doc.addDocumentListener(listener);
			}
			
		} : new JTextArea();
		area.setRows(2);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setWrapStyleWord(true);
		area.setBorder((new JTextField()).getBorder());
		return area;
	}

	protected static JTextPane createTextPaneComponent(String contentType, boolean isEditable) {
		JTextPane pane = new JTextPane();
		pane.setEditable(isEditable);
		pane.setContentType(contentType);
		pane.setBorder((new JTextField()).getBorder());
		// add a CSS rule to force body tags to use the default label font
        // instead of the value in javax.swing.text.html.default.css
        Font font = UIManager.getFont("EditorPane.font");
        String bodyRule = "body { font-family: " 
        	+ font.getFamily() + "; " 
        	+ "font-size: " + font.getSize() + "pt; }";
        ((HTMLDocument)pane.getDocument()).getStyleSheet().addRule(bodyRule);
        return pane;
	}
	
	private static final void setUpdateWork(AbstractField field) {
		// mark?
		if(m_updateWork == null) {
			try {
				m_updateWork = new UpdateWork(field);
				WorkPool.getInstance().schedule(m_updateWork);
			} catch (Exception e) {
				m_logger.error("Failed to create update work",e);
			}
		} else {
			m_updateWork.add(field);
		}
	}
	
	private static final void resetUpdateWork() {
		// clear mark
		m_updateWork = null;
	}
	
	/*==================================================================
	 * inner classes
	 *================================================================== */
	
	private static class UpdateWork extends AbstractWork {

		private ConcurrentLinkedQueue<RemoteUpdate> 
			m_fields = new ConcurrentLinkedQueue<RemoteUpdate>();
		
		public UpdateWork(AbstractField field) throws Exception {
			// forward
			super(NORMAL_PRIORITY, true, false, 
					WorkerType.UNSAFE, "",0
					,false,false,false);
			add(field);
		}
		
		public void add(AbstractField field) {
			// prepare
			m_fields.add(new RemoteUpdate(field));			
		}
		
		public boolean contains(AbstractField field) {
			return m_fields.contains(field);
		}

		@Override
		public Object doWork(IWorkLoop loop) {
			RemoteUpdate it;
			final List<AbstractField> update = new Vector<AbstractField>(m_fields.size());
			List<RemoteUpdate> reschedule = new Vector<RemoteUpdate>(m_fields.size()); 
			// loop over all
			while((it=m_fields.poll())!=null) {
				// update?
				if(System.currentTimeMillis()-it.getTimeMillis()>REMOTE_STATE_RESET) {
					update.add(it.getField());
				} else {
					reschedule.add(it);
				}
			}
			// invoke on EDT
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					for(AbstractField it : update) {
						it.parse();
					}
				}
				
			});
			// reschedule?
			m_isLoop = (reschedule.size()>0);
			if(m_isLoop) {
				m_fields.addAll(reschedule);
			} else {
				resetUpdateWork();
			}
			// success
			return true;
		}
		
		private static class RemoteUpdate {
			
			private AbstractField m_field;
			private long m_tic = System.currentTimeMillis();
			
			public RemoteUpdate(AbstractField field) {
				m_field = field;
			}
			
			public AbstractField getField() {
				return m_field;
			}
			
			public long getTimeMillis() {
				return m_tic;
			}
		}
		
	}
		
	
}
