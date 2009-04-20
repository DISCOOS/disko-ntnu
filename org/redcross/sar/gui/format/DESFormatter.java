package org.redcross.sar.gui.format;

import java.text.ParseException;

@SuppressWarnings("serial")
public class DESFormatter extends CoordinateFormatter {

	public static final int MAX_DIGITS = 10;
	
	public DESFormatter(String hemisphere) throws ParseException {
		this(hemisphere,-1);
	}
	
	public DESFormatter(String hemisphere, int digits) throws ParseException {
		super("##,"+getSequence("#",limit(digits,MAX_DIGITS)-2)+validate(hemisphere),hemisphere);
		String init = "00,"+getSequence("0",limit(digits,MAX_DIGITS)-2)+hemisphere;
		this.setPlaceholder(init);
		this.setPlaceholderCharacter('0');
		this.setAllowsInvalid(false);
		this.setCommitsOnValidEdit(true);
	}
	

	public int getMaxDigits() {
		return MAX_DIGITS;
	}
	
}