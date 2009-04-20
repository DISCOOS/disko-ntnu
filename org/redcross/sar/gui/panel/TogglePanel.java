package org.redcross.sar.gui.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.event.EventListenerList;

import org.redcross.sar.gui.DiskoIcon;
import org.redcross.sar.gui.event.IToggleListener;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;

public class TogglePanel extends BasePanel implements ITogglePanel {

    private static final long serialVersionUID = 1L;

    private JToggleButton toggleButton;
    private JButton finishButton;
    private JButton cancelButton;
    private DiskoIcon finishIcon;
    private DiskoIcon cancelIcon;

	private boolean isMinSet = false;
	private boolean isPrefSet = false;
	private boolean isMaxSet = false;
    private boolean isExpanded = true;

    private Insets tmpInsets;
    private Insets collapsedInsets;

    private boolean[] isTmpSet = new boolean[4];
    private Dimension[] tmpSize = new Dimension[4];

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

    public TogglePanel(String caption, boolean finish, boolean cancel, boolean toggle) {
        this(caption,finish,cancel,toggle,ButtonSize.SMALL);
    }

    public TogglePanel(String caption, boolean finish, boolean cancel, ButtonSize buttonSize) {
        // forward
        this(caption,finish,cancel,true,buttonSize);
    }

    public TogglePanel(String caption, boolean finish, boolean cancel, boolean toggle, ButtonSize buttonSize) {
        // forward
        super(caption,buttonSize);
        // initialize GUI
        initialize();
        // hide default buttons
        setButtonVisible("finish", finish);
        setButtonVisible("cancel", cancel);
        setButtonVisible("toggle", toggle);
    }

    /* ===========================================
     * ITogglePanel implementation
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

    /**
     * Set toggle state.
     *
     */
    public void setExpanded(boolean isExpanded) {
        if(getToggleButton().isSelected()!=isExpanded) {
            if(isDisplayable())
                getToggleButton().doClick();
            else {
                // update
                getToggleButton().setSelected(isExpanded);
                // set flag
                this.isExpanded = isExpanded;
                // notify
                fireToggleEvent(0,0);
            }
        }
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void addToggleListener(IToggleListener listener) {
        listeners.add(IToggleListener.class, listener);
    }

    public void removeToggleListener(IToggleListener listener) {
        listeners.remove(IToggleListener.class, listener);
    }

    /* ===========================================
     * Overridden methods
     * =========================================== */

    @Override
    public Insets getInsets() {
        // forward
        return isExpanded() || collapsedInsets==null ? super.getInsets() : collapsedInsets;
    }

    @Override
    public Insets getInsets(Insets insets) {
        // forward
        return isExpanded() || collapsedInsets==null ? super.getInsets(insets) : collapsedInsets;
    }

    @Override
    public int getHeight() {
        // forward
        return isExpanded() ? super.getHeight() : minimumCollapsedHeight;
    }

    @Override
    public int getWidth() {
        // forward
        return isExpanded() ? super.getWidth() : super.getWidth();
    }

    @Override
    public Dimension getSize() {
        // forward
        return isExpanded() ? super.getSize() : new Dimension(super.getWidth(),minimumCollapsedHeight);
    }

    @Override
    public Dimension getSize(Dimension rv) {
        // forward
        return isExpanded() ? super.getSize(rv) : new Dimension(super.getWidth(),minimumCollapsedHeight);
    }

    @Override
    public void setSize(Dimension d) {
        // forward
        super.setSize(d!=null ? (isExpanded() ? d : new Dimension(d.width,minimumCollapsedHeight)) : null);
    }

    @Override
    public void setSize(int width, int height) {
        // forward
        super.setSize(width, (isExpanded() ? height : minimumCollapsedHeight));
    }

	@Override
	public boolean isMinimumSizeSet() {
		return isMinSet;
	}

	@Override
	public boolean isPreferredSizeSet() {
		return isPrefSet;
	}

	@Override
	public boolean isMaximumSizeSet() {
		return isMaxSet;
	}

    /**
     * Get minimum size. </p>
     *
     * Minimum size depends on the toggle state. </p>
     *
     * In expanded state, the dimension returned is a previously set minimum size, or <code>Dimension(0,minimumCollapsedHeight)</code> if
     * no minimum size is set. If <code>minimumCollapsedHeight</code> is greater then previously set minimum size, <code>height</code>  in
     * this size is overridden by <code>minimumCollapsedHeight</code>.
     *
     * In collapsed state, the dimension returned is <code>Dimension(?,minimumCollapsedHeight)</code>, where the width <code>?</code>
     * is <code>0</code> if <code>isMinimumSizeSet()</code> is <code>false</code> or <code>width</code> from previously
     * set minimum size if <code>isMinimumSizeSet()</code> is <code>true</code>.</p>
     *
     * @see JComponent#getMinimumSize(Dimension)
     *
     * @return Dimension - the minimum size of current toggle state.
     */
    @Override
    public Dimension getMinimumSize() {
    	// get current minimum size
    	Dimension d = isMinimumSizeSet() ? super.getMinimumSize() : new Dimension(0,0);
        // forward
        return isExpanded() ?
                (isMinimumSizeSet() ? new Dimension(d.width, minimumCollapsedHeight>d.height ? minimumCollapsedHeight : d.height) : new Dimension(0,minimumCollapsedHeight)) :
                (isMinimumSizeSet() ? new Dimension(d.width,minimumCollapsedHeight) : new Dimension(0,minimumCollapsedHeight));
    }

    @Override
    public void setMinimumSize(Dimension d) {
        if(isExpanded())
            super.setMinimumSize(d);
        else
            tmpSize[1] = d;
		// set flag
		isMinSet = (d!=null);
    }

    /**
     * Get preferred size. </p>
     *
     * Preferred size depends on the toggle state. </p>
     *
     * In expanded state, the dimension returned is a previously set preferred size, or <code>Dimension(0,0)</code> if
     * no preferred size or preferredExpandedHeigh is set. If <code>preferredExpandedHeight</code> is set,
     * then <code>Dimension(?,preferredExpandedHeight)</code> is returned, where the width <code>?</code> is <code>0</code>
     * if <code>isPreferredSizeSet()</code> is <code>false</code> or <code>width</code> from previously set
     * preferred size if <code>isPreferredSizeSet()</code> is <code>true</code>.</p>
     *
     * In collapsed state, the dimension returned is <code>Dimension(?,minimumCollapsedHeight)</code>, where the width <code>?</code>
     * is <code>0</code> if <code>isPreferredSizeSet()</code> is <code>false</code> or <code>width</code> from previously
     * set preferred size if <code>isPreferredSizeSet()</code> is <code>true</code>.</p>
     *
     * @see JComponent#getPreferredSize(Dimension)
     *
     * @return Dimension - the preferred size of current toggle state.
     */
    @Override
    public Dimension getPreferredSize() {
    	// get current preferred size
    	Dimension d = isPreferredSizeSet() ? super.getPreferredSize() : new Dimension(0,0);
        // forward
        return isExpanded() ?
                (isPreferredSizeSet() ?
                        (preferredExpandedHeight==0 ?
                        		d : new Dimension(d.width,preferredExpandedHeight)) :
                        new Dimension(0,preferredExpandedHeight)) :
                (isPreferredSizeSet() ?
                        new Dimension(d.width,minimumCollapsedHeight) :
                        new Dimension(0,minimumCollapsedHeight));
    }

    @Override
    public void setPreferredSize(Dimension d) {
        if(isExpanded())
            super.setPreferredSize(d);
        else
            tmpSize[2] = d;
		// set flag
		isPrefSet = (d!=null);
    }

    /**
     * Get maximum size. </p>
     *
     * Maximum size depends on the toggle state. </p>
     *
     * In expanded state, the dimension returned is a previously set maximum size, or <code>Dimension(0,0)</code> if
     * no maximum size is set.
     *
     * In collapsed state, the dimension returned is <code>Dimension(?,minimumCollapsedHeight)</code>, where the width <code>?</code>
     * is <code>0</code> if <code>isMaximumSizeSet()</code> is <code>false</code> or <code>width</code> from previously
     * set maximum size if <code>isMaximumSizeSet()</code> is <code>true</code>.</p>
     *
     * @see JComponent#getMaximumSize(Dimension)
     *
     * @return Dimension - the maximum size of current toggle state.
     */
    @Override
    public Dimension getMaximumSize() {
    	// get current preferred size
    	Dimension d = isMaximumSizeSet() ? super.getMaximumSize() : new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE);
        // forward
        return isExpanded() ? d : (isMaximumSizeSet() ?
        		new Dimension(d.width,minimumCollapsedHeight) :
        		new Dimension(Integer.MAX_VALUE,minimumCollapsedHeight));
    }

    @Override
    public void setMaximumSize(Dimension d) {
        if(isExpanded())
            super.setMaximumSize(d);
        else
            tmpSize[3] = d;
		// set flag
		isMaxSet = (d!=null);
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

        // consume?
        if(isLoop()) return;
        
        // prevent re-entry
        setLoop(true);

        // initialize
        int dx = 0;
        int dy = 0;

        // set flag
        boolean isSelected = getToggleButton().isSelected();

        // any change?
        if(isExpanded!=isSelected) {

            // set flag
            isExpanded = isSelected;

            // translate selection to view
            if(isExpanded) {

            	// get preferred size
            	int max = (preferredExpandedHeight==0 ? (isPreferredSizeSet() ? super.getPreferredSize().height : 0) : preferredExpandedHeight);

                // get height
                int h = (isTmpSet(0) ? tmpSize[0].height : max);

                // calculate change
                dy = h - minimumCollapsedHeight;

                // apply temporary stored sizes?
                if(isTmpSet(1)) super.setMinimumSize(getTmpSize(1));
                if(isTmpSet(2)) super.setPreferredSize(getTmpSize(2));
                if(isTmpSet(3)) super.setMaximumSize(getTmpSize(3));

                // reset temporary sets
                resetTmpSizes();

            }
            else {

                // get collapsed height
                int h = minimumCollapsedHeight;

                // update temporary sizes
                setTmpSize(0,isDisplayable() ? super.getSize() : getPreferredSize(),true);

                // update temporary insets
                tmpInsets = super.getInsets();

                // calculate change
                dy = h - getTmpSize(0).height;

                // set collapsed size
                minimumCollapsedHeight = h;

                // set new insets
                collapsedInsets = new Insets(tmpInsets.top, tmpInsets.left, 0, tmpInsets.right);

            }

            // apply change
            super.setBorder(createBorder());

            // update tool tip text
            getToggleButton().setToolTipText(isExpanded() ? "Lukk" : "Åpne");

            // notify
            fireToggleEvent(dx,dy);

            // notify manager hierarchy
            super.requestResize(dx, dy, true);

        }

        // resume
        setLoop(false);

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

    }

    private JToggleButton getToggleButton() {
        if (toggleButton == null) {
            try {
                toggleButton = DiskoButtonFactory.createToggleButton(getButtonSize());
                toggleButton.setIcon(DiskoIconFactory.getIcon("GENERAL.EXPAND", "32x32"));
                toggleButton.setSelectedIcon(DiskoIconFactory.getIcon("GENERAL.COLLAPSE", "32x32"));
                toggleButton.setSelected(isExpanded);
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

    private void fireToggleEvent(int dx, int dy) {
        IToggleListener[] list = listeners.getListeners(IToggleListener.class);
        for(int i=0; i<list.length; i++) {
            list[i].toggleChanged(this, isExpanded(), dx, dy);
        }
    }

    private boolean isTmpSet(int index) {
    	return isTmpSet[index];
    }

    private Dimension getTmpSize(int index) {
    	return tmpSize[index];
    }

    private void setTmpSize(int index, Dimension d, boolean set) {
		tmpSize[index] = set ? d : null;
		isTmpSet[index] = set;
    }

    private void resetTmpSizes() {
    	for(int i=0; i<4;i++) {
    		setTmpSize(i, null, false);
    	}
    }

}
