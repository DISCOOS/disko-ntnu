package org.redcross.sar.util.mso;

/**
 * Common interface for Geodata objects
 */
public interface IGeodataIf
{

	public enum GeoClassCode {
		CLASSCODE_GEOPOS,
		CLASSCODE_TIMEPOS,
		CLASSCODE_ROUTE,
		CLASSCODE_TRACK,
		CLASSCODE_POLYGON,
		CLASSCODE_POSITION
	}
	
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
