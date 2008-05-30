package org.redcross.sar.wp.logistics;

import org.redcross.sar.gui.DiskoRoundBorder;
import org.redcross.sar.gui.renderer.IconRenderer;
import org.redcross.sar.mso.data.IAssignmentIf;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;

/**
 * Label for displaying and handling assignments info.
 */
public class AssignmentLabel extends JLabel implements MouseListener, FocusListener
{

	private final static long serialVersionUID = 1L;
	private final static Border LabelBorder = new DiskoRoundBorder(2, 6, false);
    private final static Dimension LabelDimension = new Dimension(150, 50);
    
    private boolean m_isSelected;
    private IAssignmentIf m_assignment;
    private AssignmentLabelActionHandler m_actionHandler;

    /**
     * Constructor.
     * <p/>
     * Set icon and action handler.
     *
     * @param anIcon          {@link IconRenderer.AssignmentIcon} specifying the assignment.
     * @param anActionHandler Action that shall be defined for the label.
     */
    public AssignmentLabel(IconRenderer.AssignmentIcon anIcon, AssignmentLabelActionHandler anActionHandler)
    {
        super();
        setAssignmentIcon(anIcon);
        initialize(anActionHandler);
    }

    /**
     * Constructor.
     * <p/>
     * Set assignment and action handler.
     *
     * @param anAssignment    The assignment.
     * @param anActionHandler Action that shall be defined for the label.
     */
    public AssignmentLabel(IAssignmentIf anAssignment, AssignmentLabelActionHandler anActionHandler)
    {
        super();
        setAssignment(anAssignment);
        initialize(anActionHandler);
    }

    private void initialize(AssignmentLabelActionHandler anActionHandler)
    {
        m_actionHandler = anActionHandler;
        setFocusable(true);
        addMouseListener(this);
        addFocusListener(this);
        m_isSelected = false;
        setAppearence();
    }

    private void setAppearence()
    {
        if (m_assignment != null)
        {
            setBackground(Color.WHITE);
            setOpaque(true);
            setBorder(LabelBorder);
            if (getIcon() == null)
            {
                setMinimumSize(LabelDimension);
            }
            setPreferredSize(LabelDimension);
            setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    public void setSelected(boolean isSelected)
    {
        // update state
    	m_isSelected = isSelected;        
        // has no assignment?
        if (m_assignment == null){
        	// get icon
            Icon icon = getIcon();
            // is selectable icon?
            if (icon instanceof IconRenderer.AssignmentIcon){
                ((IconRenderer.AssignmentIcon)icon).setSelected(isSelected);
            }
        }
        // repaint label
        repaint();
    }

    public boolean isSelected()
    {
        return m_isSelected;
    }

    /**
     * Define icon, remove text.
     *
     * @param anIcon {@link IconRenderer.AssignmentIcon} specifying the assignment.
     */
    public void setAssignmentIcon(IconRenderer.AssignmentIcon anIcon)
    {
        super.setIcon(anIcon);
        setText("");
        m_assignment = null;
    }

    /**
     * Define assignment, set text, remove icon.
     *
     * @param anAssignment The assignment
     */
    public void setAssignment(IAssignmentIf anAssignment)
    {
        m_assignment = anAssignment;
        if (m_assignment.getStatus() == IAssignmentIf.AssignmentStatus.QUEUED)
        {
            setText(MessageFormat.format("{0}: {1} {2}", anAssignment.getPrioritySequence(), Integer.toString(anAssignment.getNumber()),
                            anAssignment.getTypeText()));
        } else
        {
            setText(MessageFormat.format("{0} {1}", Integer.toString(anAssignment.getNumber()), anAssignment.getTypeText()));
        }
        setIcon(null);
    }

    /**
     * Get the {@link IAssignmentIf} associated with the label.
     *
     * @return The assigment
     */
    public IAssignmentIf getAssignment()
    {
        if (m_assignment != null)
        {
            return m_assignment;
        }
        Icon icon = getIcon();
        return icon instanceof IconRenderer.AssignmentIcon ? ((IconRenderer.AssignmentIcon) icon).getAssignment() : null;
    }

    /**
     * Handle mouse click.
     * <p/>
     * The click is forwarded to the defined ation handler.
     *
     * @param e The event
     */
    public void mouseClicked(MouseEvent e)
    {
        //Since the user clicked on us, let's get focus!
        requestFocus();
        setSelected(true);
        if (m_actionHandler != null)
        {
            m_actionHandler.handleClick(getAssignment());
        }

    }

    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}

    /**
     * Define the label to be focusable.
     *
     * @return
     */
    @Override
    public boolean isFocusable()
    {
        return true;
    }


    public void focusGained(FocusEvent e){}

    /**
     * Deselect when focus is lost.
     *
     * @param e Not used.
     */
    public void focusLost(FocusEvent e)
    {
        //setSelected(false);
    }

    /**
     * Interrface for action handlers that shall be called from the {@link AssignmentLabel}.
     */
    public static interface AssignmentLabelActionHandler
    {
        /**
         * Handle the label click.
         *
         * @param anAssignment The assignment belonging to the label.
         */
        public void handleClick(IAssignmentIf anAssignment);
    }
}
