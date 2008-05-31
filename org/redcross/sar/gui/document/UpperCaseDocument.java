package org.redcross.sar.gui.document;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class UpperCaseDocument extends PlainDocument {
    public void insertString( int off, String string, AttributeSet attr ) {
      try {
    	  if( string != null )
    		  super.insertString( off, string.toUpperCase(), attr );
      } catch ( BadLocationException e ) {}
    }
}
