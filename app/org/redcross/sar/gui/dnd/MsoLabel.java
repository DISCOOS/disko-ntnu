package org.redcross.sar.gui.dnd;

import org.redcross.sar.gui.DiskoRoundBorder;
import org.redcross.sar.gui.renderer.ObjectIcon;
import org.redcross.sar.gui.renderer.ObjectIcon.MsoIcon;
import org.redcross.sar.mso.data.IMsoObjectIf;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.border.Border;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Label for displaying and handling assignments info.
 */
public abstract class MsoLabel<M extends IMsoObjectIf> extends JLabel
{

	private final static long serialVersionUID = 1L;
    private final static Dimension m_size = new Dimension(150, 50);
	private final static Border m_border = new DiskoRoundBorder(2, 6, false);

    private M m_msoObject;
    private boolean m_isSelected;
    private MsoLabelActionHandler<M> m_actionHandler;

    /**
     * Constructor.
     * <p/>
     * Set icon and action handler.
     *
     * @param anIcon {@link MsoIcon} representing the object.
     * @param anActionHandler Action that shall be defined for the label.
     */
    public MsoLabel(MsoIcon<M> anIcon, MsoLabelActionHandler<M> anActionHandler)
    {
        super();
        setMsoIcon(anIcon);
        initialize(anActionHandler);
    }

    /**
     * Constructor.
     * <p/>
     * Set object and action handler.
     *
     * @param anObject - the IMsoObject instance.
     * @param anActionHandler Action that shall be defined for the label.
     */
    public MsoLabel(M anObject, MsoLabelActionHandler<M> anActionHandler)
    {
        super();
        setMsoObject(anObject);
        initialize(anActionHandler);
    }

    private void initialize(MsoLabelActionHandler<M> anActionHandler)
    {
    	// prepare
        setFocusable(true);
        addMouseListener(new MouseAdapter() {

			@Override
		    public void mouseClicked(MouseEvent e)
		    {
		        //Since the user clicked on us, let's get focus!
		        requestFocus();
		        setSelected(true);
		        if (m_actionHandler != null)
		        {
		            m_actionHandler.handleClick(getMsoObject());
		        }

		    }
        	
        });
        m_isSelected = false;
        m_actionHandler = anActionHandler;
        // forward
        setAppearence();
    }

    private void setAppearence()
    {
        if (m_msoObject != null)
        {
            setBackground(Color.WHITE);
            setOpaque(true);
            setBorder(m_border);
            if (getIcon() == null)
            {
                setMinimumSize(m_size);
            }
            setPreferredSize(m_size);
        }
    }

    public static Dimension getLabelSize() {
    	return m_size;
    }

    public void setSelected(boolean isSelected)
    {
        // update state
    	m_isSelected = isSelected;
        // has no object?
        if (m_msoObject == null){
        	// get icon
            Icon icon = getIcon();
            // is selectable icon?
            if (icon instanceof ObjectIcon){
                ((ObjectIcon)icon).setSelected(isSelected);
            }
        }
        repaint();
    }

    public boolean isSelected()
    {
        return m_isSelected;
    }
    
    @SuppressWarnings("unchecked")
	public MsoIcon<M> getMsoIcon() {
    	return getIcon() instanceof MsoIcon ? (MsoIcon<M>)getIcon() : null;
    }

    /**
     * Set icon, reset current object and text.
     *
     * @param anIcon {@link ObjectIcon.} representing the object.
     */
    public void setMsoIcon(MsoIcon<M> anIcon)
    {
    	// forward
        super.setIcon(anIcon);
        // reset any text
        setText("");
        // reset object
        m_msoObject = null;
    }

    /**
     * Define object, set text, remove icon.
     *
     * @param anObject The IMsoObjectIf instanxe
     */
    public void setMsoObject(M anObject)
    {
    	// update
        m_msoObject = anObject;
        // set text
        setText(getObjectText());
        // reset icon
        setIcon(null);
    }
    
    protected abstract String getObjectText();
    

    /**
     * Get the {@link IMsoObjectIf} associated with the label.
     *
     * @return Returns a IMsoObjectIf instance
     */
    @SuppressWarnings("unchecked")
	public M getMsoObject()
    {
        if (m_msoObject != null)
        {
            return m_msoObject;
        }
        // try to get object instance from icon
        if(getIcon() instanceof MsoIcon<?>) {
        	// cast to any MsoIcon
        	MsoIcon<?> icon = (MsoIcon<?>)getIcon();
        	// cast to object
        	return (M)icon.getMsoObject();
        }
        // failure
        return null;
    }

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

    /**
     * Interface for action handlers that shall be called from the {@link MsoLabel}.
     */
    public static interface MsoLabelActionHandler<M extends IMsoObjectIf>
    {
        /**
         * Handle the label click.
         *
         * @param anObject The IMsoObjectIf represented by the label.
         */
        public void handleClick(M anObject);
    }
}
