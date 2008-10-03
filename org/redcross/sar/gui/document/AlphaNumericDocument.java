/**
 * 
 */
package org.redcross.sar.gui.document;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * @author kennetgu
 *
 */
public class AlphaNumericDocument extends PlainDocument {
   
	private static final long serialVersionUID = 1L;
	
	public static final String ALPNUM_ASCII = "\\w";
	public static final String ALPNUM_ASCII_EXT = ALPNUM_ASCII + "\\xC0-\\xFF";
	
	/* ===========================================================
	 * Overridden methods
	 * =========================================================== */
	
	@Override
	public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str != null){                    	
        	super.insertString(offset, str.replaceAll("[^" + ALPNUM_ASCII_EXT+ "]", ""), attr);
        }
	}
      
}
