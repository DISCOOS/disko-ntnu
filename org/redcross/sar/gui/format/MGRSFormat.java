package org.redcross.sar.gui.format;

import java.text.ParseException;

import javax.swing.text.MaskFormatter;

@SuppressWarnings("serial")
public class MGRSFormat extends MaskFormatter {
	
	String direction;
	
	public MGRSFormat(String direction) throws ParseException {
		super("#####"+direction);
		this.setPlaceholder("00000"+direction);
		this.setPlaceholderCharacter('0');
		this.setAllowsInvalid(false);
		this.setCommitsOnValidEdit(true);
		this.direction = direction;
	}
	
}