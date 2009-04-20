package org.redcross.sar.gui.format;

import java.text.ParseException;

import javax.swing.text.MaskFormatter;

@SuppressWarnings("serial")
public abstract class CoordinateFormatter extends MaskFormatter {
	
	protected String hemisphere;
	
	public CoordinateFormatter(String pattern, String hemisphere) throws ParseException {
		super(pattern);
		this.hemisphere = hemisphere;
	}
	
	public abstract int getMaxDigits();
	
	public String getHemisphere() {
		return hemisphere;
	}
	
	protected static int limit(int digits, int max) {
		digits = (digits<0 ? max : digits);
		digits = (digits>max ? max : digits);
		return digits;
	}
	
	protected static String validate(String hemisphere) throws ParseException {
		if(!hemisphere.matches("^[NnSsWwEe]$"))
			throw new ParseException(hemisphere + " is invalid hemisphere character",0);
		return hemisphere;
	}

	protected static String getSequence(String token, int count) {
		String seq = "";
		for(int i=0;i<count;i++)
			seq = seq.concat(token);
		return seq; 
	}	
	
}