package org.redcross.sar.gui.format;

import java.text.ParseException;

import javax.swing.text.MaskFormatter;

@SuppressWarnings("serial")
public class DEGFormat extends MaskFormatter {

	String direction;
	
	public DEGFormat(String direction) throws ParseException {
		super("##"+"-"+"##"+"-"+"##"+ direction);
		String init = "00-00-00"+ direction;
		this.setPlaceholder(init);
		this.setPlaceholderCharacter('0');
		this.setAllowsInvalid(false);
		this.setCommitsOnValidEdit(true);
		this.direction = direction;
	}
	
}