package org.redcross.sar.gui.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.redcross.sar.gui.DiskoIcon;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;

public class TogglePanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private JToggleButton toggleButton;
	private JButton finishButton;
	private JButton cancelButton;
	private DiskoIcon finishIcon;
	private DiskoIcon cancelIcon;

	private Insets insets;
	private Dimension minSize;
	private Dimension preSize;
	private Dimension maxSize;

	private final EventListenerList listeners = new EventListenerList();

	/* ===========================================
	 * Constructors
	 * =========================================== */

	public TogglePanel() {
		this("");
	}

	public TogglePanel(String caption) {
		// forward
		this(caption,true,true,ButtonSize.SMALL);
	}

	public TogglePanel(ButtonSize buttonSize) {
		// forward
		this("",true,true,buttonSize);
	}

	public TogglePanel(String caption, boolean finish, boolean cancel) {
		this(caption,finish,cancel,ButtonSize.SMALL);
	}

	public TogglePanel(String caption, boolean finish, boolean cancel, ButtonSize buttonSize) {
		// forward
		super(caption,buttonSize);
		// prepare
		insets = getInsets();
		// initialize gui
		initialize();
		// hide default buttons
		setButtonVisible("finish", finish);
		setButtonVisible("cancel", cancel);
	}

	/* ===========================================
	 * Public methods
	 * =========================================== */

	public boolean toggle() {
		// forward
		getToggleButton().doClick();
		// return toggle state
		return isExpanded();
	}

	public void expand() {
		setExpanded(true);
	}

	public void collapse() {
		setExpanded(false);
	}

	public void setExpanded(boolean isExpanded) {
		if(getToggleButton().isSelected()!=isExpanded) {
			getToggleButton().doClick();
		}
		change();
	}

	public boolean isExpanded() {
		// return toggle state
		return getToggleButton().isSelected();
	}

	public void addChangeListener(ChangeListener listener) {
		listeners.add(ChangeListener.class, listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		listeners.remove(ChangeListener.class, listener);
	}

	/* ===========================================
	 * Overridden methods
	 * =========================================== */

	@Override
	public void setInsets(int t, int l, int b, int r) {
		// update
		insets = new Insets(t,l,b,r);
		// forward?
		if(isExpanded()) super.setInsets(t,l,b,r);
	}

	@Override
	public final void setMaximumSize(Dimension d) {
		// update
		maxSize = d;
		// forward?
		if(isExpanded()) super.setMaximumSize(d);
		// apply
		change();
	}

	@Override
	public final void setMinimumSize(Dimension d) {
		// update
		minSize = d;
		// forward?
		if(isExpanded()) super.setMinimumSize(d);
		// apply
		change();
	}

	@Override
	public final void setPreferredSize(Dimension d) {
		// update
		preSize = d;
		// forward?
		if(isExpanded()) super.setMaximumSize(d);
		// apply
		change();
	}


	/* ===========================================
	 * IPanel interface implementation
	 * =========================================== */

	public void update() {

		// update attributes
		finishIcon.setColored(isDirty());
		cancelIcon.setColored(isDirty());
		finishButton.repaint();
		cancelButton.repaint();

	}

	/* ===========================================
	 * Protected methods
	 * =========================================== */

	protected void change() {
		// translate selection to view
		if(getToggleButton().isSelected()) {
			// show content11
			getScrollPane().setVisible(true);
			// resume sizes
			super.setMinimumSize(minSize);
			super.setPreferredSize(preSize);
			super.setMaximumSize(maxSize);
			// resume old border
			super.setInsets(insets.top, insets.left, insets.bottom, insets.right);
			super.setBorder(createBorder());
	    	// update tool tip text
	    	getToggleButton().setToolTipText("Lukk");
		}
		else {
			// hide content
			getScrollPane().setVisible(false);
			// get collapsed height
			int h = getHeaderPanel().getPreferredSize().height+1;
			// set sizes
			super.setMinimumSize(new Dimension(minSize!=null ? minSize.width : super.getWidth(),h));
			super.setPreferredSize(new Dimension(preSize!=null ? preSize.width : super.getWidth(),h));
			super.setMaximumSize(new Dimension(maxSize!=null ? maxSize.width : super.getWidth(),h));
			// remove bottom border line
			super.setInsets(insets.top, insets.left, 0, insets.right);
			super.setBorder(createBorder());
	    	// update tool tip text
	    	getToggleButton().setToolTipText("Åpne");
		}
		// apply change to container
		if(getParent()!=null) {
			getParent().validate();
		}
		else {
			validate();
		}
		// notify
		fireChangeEvent();
	}

	/* ===========================================
	 * Private methods
	 * =========================================== */

	/**
	 * This method initializes the panel
	 *
	 */
	private void initialize() {

		// add default buttons
		addButton(getToggleButton(),"toggle");
		addButton(getFinishButton(),"finish");
		addButton(getCancelButton(),"cancel");
		addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				if("toggle".equalsIgnoreCase(cmd)) change();
				else if("finish".equalsIgnoreCase(cmd)) finish();
				else if("cancel".equalsIgnoreCase(cmd)) cancel();
			}

		});

		// update layout
		change();

	}

	private JToggleButton getToggleButton() {
		if (toggleButton == null) {
			try {
				toggleButton = DiskoButtonFactory.createToggleButton(getButtonSize());
				toggleButton.setIcon(DiskoIconFactory.getIcon("GENERAL.EXPAND", "32x32"));
				toggleButton.setSelectedIcon(DiskoIconFactory.getIcon("GENERAL.COLLAPSE", "32x32"));
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return toggleButton;
	}

	private JButton getFinishButton() {
		if (finishButton == null) {
			try {
				finishButton = DiskoButtonFactory.createButton("GENERAL.FINISH",getButtonSize());
				finishIcon = new DiskoIcon(finishButton.getIcon(),Color.GREEN,0.4f);
				finishButton.setIcon(finishIcon);
				finishButton.setDefaultCapable(true);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return finishButton;
	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			try {
				cancelButton = DiskoButtonFactory.createButton("GENERAL.CANCEL",getButtonSize());
				cancelIcon = new DiskoIcon(cancelButton.getIcon(),Color.RED,0.4f);
				cancelButton.setIcon(cancelIcon);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return cancelButton;
	}

	private void fireChangeEvent() {
		ChangeEvent e = new ChangeEvent(this);
		ChangeListener[] list = listeners.getListeners(ChangeListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].stateChanged(e);
		}
	}

}
