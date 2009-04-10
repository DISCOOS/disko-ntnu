package org.redcross.sar.gui.format;

import java.text.ParseException;

import javax.swing.text.MaskFormatter;;


@SuppressWarnings("serial")
public class UTMFormat extends MaskFormatter {

	String direction;
	
	public UTMFormat(String direction) throws ParseException {
		super("#######"+direction);
		this.setPlaceholder("0000000"+direction);
		this.setPlaceholderCharacter('0');
		this.setAllowsInvalid(false);
		this.setCommitsOnValidEdit(true);
		this.direction = direction;
	}
		
}