/**
 * 
 */
package org.redcross.sar.gui.document;

import java.awt.*;

import javax.swing.text.*;
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
   protected int decimalPrecision = 0;
   protected boolean allowNegative = false;

   //Constructor
   public NumericDocument(int decimalPrecision, boolean allowNegative) {
        super();
        this.decimalPrecision = decimalPrecision;
        this.allowNegative = allowNegative;
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
             if (isNumeric(str,Double.class) == false && str.equals(",") == false && str.equals("-") == false){ //First, is it a valid character?
                  Toolkit.getDefaultToolkit().beep();
                  return;
             }
             else if (str.equals(",") == true && super.getText(0, super.getLength()).contains(",") == true 
            		 || str.equals(",") == true && decimalPrecision==0){ //Next, can we place a decimal here?
                  Toolkit.getDefaultToolkit().beep();
                  return;
             }
             else if (isNumeric(str,Double.class) == true 
            		 && super.getText(0, super.getLength()).indexOf(",") != -1 
            		 && offset>super.getText(0, super.getLength()).indexOf(",") 
            		 && super.getLength()-super.getText(0, super.getLength()).indexOf(",")>decimalPrecision 
            		 && decimalPrecision > 0){ //Next, do we get past the decimal precision limit?
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
