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
