package org.redcross.sar.gui.format;

import java.text.ParseException;

import javax.swing.text.MaskFormatter;

@SuppressWarnings("serial")
public class DTGFormat extends MaskFormatter {
	String direction;
	
	public DTGFormat() throws ParseException {
		super("######");
		this.setPlaceholder("00000");
		this.setPlaceholderCharacter('0');
		this.setCommitsOnValidEdit(true);
	}

}
