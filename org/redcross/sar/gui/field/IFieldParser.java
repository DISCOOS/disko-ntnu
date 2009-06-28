package org.redcross.sar.gui.field;

/**
 * 
 * This interface implements methods for parsing a value to
 * to field data, and formatting field data into a value.
 *  
 * @author kenneth
 *
 * @param <V> - value data type
 * @param <M> - field data type
 */
public interface IFieldParser<V extends Object,M extends Object> {

	/**
	 * Parse value to field data 
	 * @param value - the value to parse into field data
	 * @return Returns field data.
	 */
	public M parse(V value);

	/**
	 * Format field data into value 
	 * @param data - the field data to format into value
	 * @return Returns formated value.
	 */
	public V format(M value);
	
}
