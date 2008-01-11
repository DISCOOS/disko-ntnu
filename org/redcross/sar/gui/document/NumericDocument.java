/**
 * 
 */
package org.redcross.sar.gui.document;

import java.awt.Toolkit;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * @author kennetgu
 *
 */

public class NumericDocument extends PlainDocument {
   
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//Variables
	protected int maxDigits = 0;
	protected int decimalPrecision = 0;
	protected boolean allowNegative = false;

	//Constructor
	public NumericDocument(int maxDigits, int decimalPrecision, boolean allowNegative) {
        super();
        this.maxDigits = maxDigits;
        this.decimalPrecision = decimalPrecision;
        this.allowNegative = allowNegative;
	}
   
	public void setMaxDigits(int digits) {
		this.maxDigits = digits;
	}
	
	public int getMaxDigits() {
		return this.maxDigits;
	}
	
	public void setDecimalPrecision(int precision) {
		this.decimalPrecision = precision;
	}
 
	public int getDecimalPrecision() {
		return this.decimalPrecision;
	}
   
	public void setAllowNegative(boolean allow) {
		this.allowNegative = allow;
	}
 
	public boolean getAllowNegative() {
		return this.allowNegative;
	}
	
	//Insert string method
	public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str != null){
        	// get numeric flag
        	boolean isNumber = isNumeric(str,Double.class);
        	// get decimal flag
        	boolean isDecimal = getText(0, getLength()).indexOf(",") != -1;
        	// validate number
            if (!isNumber && str.equals(",") == false && str.equals("-") == false){ //First, is it a valid character?
                 Toolkit.getDefaultToolkit().beep();
                 return;
            }
            else if (str.equals(",") == true && getText(0, getLength()).contains(",") == true 
           		 || str.equals(",") == true && decimalPrecision==0){ //Next, can we place a decimal here?
                 Toolkit.getDefaultToolkit().beep();
                 return;
            }
            else if (isNumber && 
            	(!isDecimal && getLength()+1 > maxDigits || isDecimal 
            	 && offset<getText(0,getLength()).indexOf(",") 
               	 && getText(0, getText(0,getLength()).indexOf(",")-1).length()+1
                 > maxDigits) && maxDigits > -1){            	
    			 // do we get past the maximal digit count limit?
    			 Toolkit.getDefaultToolkit().beep();
    			 return;    			 
	        }
            else if(isNumber && isDecimal 
            	 && offset>getText(0, getLength()).indexOf(",") 
              	 && getLength()-getText(0, getLength()).indexOf(",")
               	 > decimalPrecision && decimalPrecision > 0){ 
     			 // do we get past the decimal precision limit?
     			 Toolkit.getDefaultToolkit().beep();
     			 return;            	
    		}            		
            else if (str.equals("-") == true && (offset != 0 || allowNegative == false)){ //Next, can we put a negative sign?
                 Toolkit.getDefaultToolkit().beep();
                 return;
            }
            //All is fine, so add the character to the text box
            super.insertString(offset, str, attr);
        }
        return;
	}
   
	public static boolean isNumeric(String str, Class<? extends Number> c)
	{
		try
		{
            if (c.equals(Byte.class))
             {
                Byte.parseByte(str);
            }
            else if (c.equals(Double.class))
            {
                Double.parseDouble(str);
            }
            else if (c.equals(Float.class))
            {
                Float.parseFloat(str);
            }
            else if (c.equals(Integer.class))
            {
                Integer.parseInt(str);
            }
            else if (c.equals(Long.class))
            {
                Long.parseLong(str);
            }
            else if (c.equals(Short.class))
            {
                Short.parseShort(str);
            }
        }
        catch (NumberFormatException nfe)
        {
            return false;
        }
 
        return true;
    }
   
}
