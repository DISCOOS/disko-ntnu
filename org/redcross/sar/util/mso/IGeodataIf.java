package org.redcross.sar.util.mso;

/**
 * Common interface for Geodata objects
 */
public interface IGeodataIf
{

    /**
     * Get Id of object
     *
     * @return The Id
     */
    public String getId();

    /**
     * Gets change count since construction. Use this counter when tracking
     * changes executed on a object. Each time the data is changed, the
     * counter is incremented<p>
     * This property enables MSO Update listeners to track changes 
     * without the need for local buffering of attribute states.
     *
     * @return The number of changes performed on the object since the construction.
     */
    public int getChangeCount();
    
    /**
     * Set layout
     *
     * @param aLayout The layout
     */
    public void setLayout(String aLayout);

    /**
     * Get layout
     *
     * @return The layout
     */
    public String getLayout();
}
