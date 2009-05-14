package org.redcross.sar.gui.format;

import java.text.ParseException;

import javax.swing.text.MaskFormatter;

@SuppressWarnings("serial")
public class DTGFormatter extends MaskFormatter {
	String direction;
	
	public DTGFormatter() throws ParseException {
		super("######");
		this.setPlaceholder("000000");
		this.setPlaceholderCharacter('0');
		this.setCommitsOnValidEdit(true);
		this.setOverwriteMode(true);		
	}

}
