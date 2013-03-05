package org.redcross.sar.gui.format;

import java.text.ParseException;

@SuppressWarnings("serial")
public class MGRSFormatter extends CoordinateFormatter {
	
	public static final int MAX_DIGITS = 5;
	
	public MGRSFormatter(String hemisphere) throws ParseException {
		this(hemisphere,-1);
	}
	
	public MGRSFormatter(String hemisphere, int digits) throws ParseException {
		super(getSequence("#",limit(digits,MAX_DIGITS))+validate(hemisphere),hemisphere);
		this.setPlaceholder(getSequence("0",limit(digits,MAX_DIGITS))+hemisphere);
		this.setPlaceholderCharacter('0');
		this.setAllowsInvalid(false);
		this.setCommitsOnValidEdit(true);
	}
	
	public int getMaxDigits() {
		return MAX_DIGITS;
	}
		
}