package org.redcross.sar.gui.format;

import java.text.ParseException;

@SuppressWarnings("serial")
public class DEMFormatter extends CoordinateFormatter {

	public static final int MAX_DIGITS = 10;
	
	public DEMFormatter(String hemisphere) throws ParseException {
		this(hemisphere,-1);
	}
	
	public DEMFormatter(String hemisphere, int digits) throws ParseException {
		super("##-##,"+getSequence("#",limit(digits,MAX_DIGITS)-4)+validate(hemisphere),hemisphere);
		String init = "00-00,"+getSequence("0",limit(digits,MAX_DIGITS)-4)+hemisphere;
		this.setPlaceholder(init);
		this.setPlaceholderCharacter('0');
		this.setAllowsInvalid(false);
		this.setCommitsOnValidEdit(true);
	}
	
	public int getMaxDigits() {
		return MAX_DIGITS;
	}
	

}