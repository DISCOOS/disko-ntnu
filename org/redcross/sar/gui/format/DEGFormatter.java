package org.redcross.sar.gui.format;

import java.text.ParseException;

@SuppressWarnings("serial")
public class DEGFormatter extends CoordinateFormatter {

	public static final int MIN_DIGITS = 6;
	
	public DEGFormatter(String hemisphere) throws ParseException {		
		super(getPattern(hemisphere,"#"),hemisphere);
		String init = getPattern(hemisphere,"0");
		this.setPlaceholder(init);
		this.setPlaceholderCharacter('0');
		this.setAllowsInvalid(false);
		this.setCommitsOnValidEdit(true);
	}
	
	private static String getPattern(String hemisphere, String token) throws ParseException {
		hemisphere = validate(hemisphere);
		String field = getSequence(token,2);
		if(hemisphere.matches("^[NnSs]$")) { 
			// 00 - 90 degrees
			return field + "-" + field + "-" + field;
		}
		else { 
			// 000 - 180 degrees
			return token + field + "-" + field + "-" + field;
		}
	}
	
	@Override
	public int getMaxDigits() {
		return hemisphere.matches("^[NnSs]$") ? MIN_DIGITS : MIN_DIGITS + 1;
	}
	
}