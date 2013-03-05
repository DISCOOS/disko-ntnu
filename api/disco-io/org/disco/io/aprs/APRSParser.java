package org.disco.io.aprs;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.disco.core.geo.GeoPos;
import org.disco.core.geo.GeoUtils;
import org.disco.io.IPacket;
import org.disco.io.Parser;

public class APRSParser extends Parser {
	
	private final String name;
	private final char prefix;
	private final char postfix;
	
	/** duplicate lifetime, duplicates are discarded after this */
	private final long DUPLICATE_LIFETIME = 12000; // 2 minutes in milliseconds (should be more than enough)
	
	/** local buffer used to detect TNC-2 message trains and duplicate messages **/  
	private final Map<Long,APRSPacket> buffer = new HashMap<Long, APRSPacket>();

	/** local packet buffer (UI/TNC-2 frames may be broken into several consecutive packets) */
    protected IPacket incomplete;
    
    /** last parsed APRS packet (UI/TNC-2 frames may be broken into several consecutive packets) */
    protected APRSPacket aprsPacket;
	
	public APRSParser(String name, char prefix, char postfix) {
		this.name = name;
		this.prefix = prefix;
		this.postfix = postfix;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isBOP(byte b) {
        // beginning of packet?
		return (b==prefix);		
	}
	
	public boolean isEOP(byte b) {
        // beginning of packet?
		return (b==postfix);
	}
	
	public boolean parse(byte b) {
		return isEOP(b);
	}
	
	/**
	 * Supported data types (see page 17 in APRS specification 1.0.1 for complete list)
	 * 
	 * DTI = Data Type Identifier
	 */
	private static final String DTI = "" + 
		'!'		// Position without timestamp (no APRS messaging (or Ultimeter 2000 WX Station, not supported)
				// Note: There is one exception to the requirement for the Data Type Identifier
				// to be the first character in the Information field — this is the Position without
				// Timestamp (indicated by the ! DTI). The ! character may occur anywhere
				// up to and including the 40th character position in the Information field. This
				// variability is required to support X1J TNC digipeaters which have a string of
				// unmodifiable text at the beginning of the field.
		+ '='	// Position without timestamp (with APRS messaging)
		+ '/'	// Position with timestamp (no APRS messaging)
		+ '@'	// Position with timestamp (with APRS messaging)
		+ '$';	// Raw GPS data (or Ultimeter 2000, not supported)
	
	/** finds first occurrence of Source Path Header pattern 
	 * (TNC-2 or AEA, soon to be the standard for APRS packets) */
	private final static String REGEXP_SPH = "}*\\w{1,6}(-\\w{1,2}>|>)[^:]*:";
	/** finds first occurrence of source call sign and ssid pattern if exists */
	private final static String REGEXP_CSS = "\\w{1,6}(-\\w{1,2}|.{0})";
	/** finds first occurrence of Data Type Identifier if exists */
	private final static String REGEXP_DTI = "(" + DTI.charAt(0) + '|' + DTI.charAt(1) + '|' 
			+ DTI.charAt(2) + '|' + DTI.charAt(3) + '|' + DTI.charAt(4) + "){1}";
	/** finds first occurrence of POSIT Data Type Identifier if exists */
	private final static String REGEXP_POSIT = "(" + DTI.charAt(0) + '|' + DTI.charAt(1) + '|' 
			+ DTI.charAt(2) + '|' + DTI.charAt(3) + "){1}";
	/** finds first occurrence of NMEA GPS data data pattern if exists */
	private final static String REGEXP_GP = "\\$GP[A-Z]{3}";
	/** finds first occurrence of proprietary Garmin GPS data data pattern if exists */
	private final static String REGEXP_PG = "\\$PG[A-Z]{3}";
	/** finds first occurrence of raw GPS data data pattern if exists */
	private final static String REGEXP_GPS = "(" + REGEXP_GP + "|" + REGEXP_PG + ")+";
	/** finds first occurrence of altitude in comment field if exists */
	private final static String REGEXP_ALT = "\\/A=\\d{6}.";
	/** finds first occurrence of DTI '!' or '=' (position report with no time stamp) if exists */
	private final static String REGEXP_PR_NTS = "(!|=)\\d{4}\\.\\d{2}[N|S].\\d{5}\\.\\d{2}[W|E].";
	/** finds first occurrence of DTI '!' or '=' (position report with time stamp) if exists */
	private final static String REGEXP_PR_WTS = "(/|@)\\d{6}[z|/|h]\\d{4}\\.\\d{2}[N|S].\\d{5}\\.\\d{2}[W|E].";
	/** finds first occurrence of the CRS/SPD Data Extension (COURSE/SPEED) with no time stamp if exists */
	private final static String REGEXP_DE_NTS = "(!|=).{19}\\d{3}\\/\\d{3}";
	/** finds first occurrence of the CRS/SPD Data Extension (COURSE/SPEED) with time stamp if exists */
	private final static String REGEXP_DE_WTS = "(/|@).{26}\\d{3}\\/\\d{3}";
	/** finds first occurrence of the unique field of a DF report if exist (BRG/NRQ*/
	private final static String REGEXP_DF = "\\\\d{3}\\/\\d{3}\\/\\d{3}\\/\\d{3}";
	
	
	@Override
	public IPacket parseRX(String message) {
		
		// prepare
		APRSPacket report = null;
		
		// remove prefix and postfix
		message = trim(message);
		
		// forward
		IPacket packet = super.parseRX(message);

		// parse as APRS message?
		if(packet.isUnknown()) {
			
	        try
	        {   
	        	/* ========================================================
	        	 * is packet completed? (postfix without message received)
	        	 * ======================================================== */
	        	if(message.length()==0) {
	        		packet = aprsPacket;
	        	} else {
	        	
		        	/* =========================================================
		        	 * Validate source path header (assumes TNC-2 or AEA format)
		        	 * ========================================================= */
		        	Matcher matcher = getMatcher(REGEXP_SPH,message);
	
		        	// span to end of all matches
		            if(matcher.lookingAt())
		            {            	                
		            	
		    			// parse ID
		    			report = ID.parse(matcher.group());
		    			
		                // get message residue
		    			message = residue(message,matcher);
		    			
		            }
		            	
		        	/* =========================================================
		        	 * Check for data type identifier (all formats)
		        	 * ========================================================= */
		        	matcher = getMatcher(REGEXP_DTI,message);
		        	
		        	// span to end of all matches
		            if(matcher.lookingAt())
		            {               	                
	                	
			        	/* =========================================================
			        	 * Check for raw GPS data (subset of NMEA and Garmin)
			        	 * ========================================================= */
			        	matcher = getMatcher(REGEXP_GPS,message);
			        	
			        	// span to end of all matches
			            if(matcher.lookingAt())
			            {            	                
		                	// parse raw GPS data
			            	packet = GPS.parse(report, message);		                                
			                
		                } else {
		                
				        	/* =========================================================
				        	 * Check for data type identifier (all formats)
				        	 * ========================================================= */
				        	matcher = getMatcher(REGEXP_POSIT,message);
		
				        	// span to end of all matches
				            if(matcher.lookingAt())
				            {            	                
			                	// forward position data only
				            	packet = POSIT.parse(report, message);
				            	
				            }
		                }
		            }
	        	}
	        }
	        catch(Exception e) 
	        { 
	        	e.printStackTrace();
	        }
		}
		
		// detect if message is part of TNC-2 frame or an duplicate message  
		packet = detect(packet);
		
        // finished
        return packet;
    }
	
	private IPacket detect(IPacket packet) {
		// is APRS packet?
		if(packet instanceof APRSPacket) {
			// cast to APRSPacket
			APRSPacket aprs = (APRSPacket)packet;
			// has header?
			if(aprs.isHeaderSet()) {
				// overwrite current APRS packet
				aprsPacket = aprs;
			} else { 
				// Is this a part of a TNC-2 (UI) frame?
				if(aprsPacket!=null) {
					// concatenate message with current packet
					aprsPacket = (APRSPacket)union(aprsPacket,aprs);
					// replace
					aprs = aprsPacket;
					packet = aprsPacket;
				}
			
			}
			// is packet complete?
			if(!aprs.isIncomplete()) {
				// get CRC-32 checksum
				long crc = aprs.createCheckSum(aprs.getMessage());
				if(buffer.containsKey(crc)) { 
					// is duplicate
					aprs.setIsDuplicate();
				} else { 
					// forward
					buffer(crc, aprs);
				}				
			}
		
		}
		// nothing to do
		return packet;
	}
	
	private void buffer(long crc, APRSPacket aprs) {
		// initialize
		List<Long> garbage = new ArrayList<Long>();
		// save
		buffer.put(crc, aprs);
		// cleanup
		for(Long it: buffer.keySet()) {
			APRSPacket packet = buffer.get(it);
			if(packet.getCheckSumAge()>DUPLICATE_LIFETIME) {
				garbage.add(it);
			}
		}
		// remove garbage
		for(Long it: garbage)
			buffer.remove(it);
	}
	
	private String trim(String message) {
		return message.replaceAll(String.valueOf(prefix),"").replaceAll(String.valueOf(postfix), "");
	}
	
	private static Matcher getMatcher(String regexp, String data) {
		Pattern pattern = Pattern.compile(regexp);
		return pattern.matcher(data);
		
	}
	
	private static String residue(String data, Matcher matcher) {
    	return data.length()>matcher.end() ? data.substring(matcher.end()) : "";		
	}
	private static IPacket union(IPacket packet, APRSPacket report) {
		// prepare report
		if(packet == null) 
			packet = report;
		else {
			packet.union(report,true);
		}
		return packet;
		
	}
	
    public byte[] getMessage(String message) {
        return (prefix+message+postfix).getBytes();  
    }  
	
	protected final static class ID {
		
    	/**
    	 * <b>Parse third party source path header ("de facto" standard)</b></p>
    	 * 
    	 * The method requires a valid source path header (assumes TNC-2 or AEA format).
    	 * This should be verified using the reqexp pattern REGEXP_SPH just before
    	 * calling this method.  
    	 * 
    	 **/
		protected final static APRSPacket parse(String header)
	    {			
			// initialize
			String field;
			APRSPacket report = null;
			
        	/* =========================================================
        	 * Check for NETWORK TUNNELING or THIRD-PARTY DIGIPEATING
        	 * =========================================================
        	 * Since the APRS Work group has agreed to commit to 
        	 * the TNC-2 format of the Source Path Header, this is the
        	 * only type of source identification supported.
        	 * ========================================================= */
        	Matcher matcher = getMatcher(REGEXP_CSS,header);
        	
        	// span to end of all matches
            if(matcher.lookingAt())
            {          
	        	// create report with no time stamp (real time)
	        	report = createPacket("APRS",header,false,false,true);
	        	
            	// strip down to source call sign field only
	        	field = matcher.group();
            	
		    	// adjust for AEA format (may include '*' in source field)
		        int idx = field.indexOf('>');
		        int pos = field.indexOf('*');
		        if(pos!=-1 && pos < idx) 
		        {
			        // trim to source field only
		            idx = field.indexOf('*');
			        field = field.substring(0, idx);
		        }
		        // has ssid?
		        idx = field.indexOf('-');
		        if(idx!=-1) {
		        	report.callSign = field.substring(0,idx);
		        	report.ssid = field.substring(idx);
            	}
		        else {
		        	report.callSign = field;
		        	report.ssid = "-0";
		        }
		        		        
		        // decode ssid
		        char c = '.';
		        if(report.ssid.indexOf("-15") > 0)
		            c = 'v';
		        else
		        if(report.ssid.indexOf("-14") > 0)
		            c = 'k';
		        else
		        if(report.ssid.indexOf("-13") > 0)
		            c = 'R';
		        else
		        if(report.ssid.indexOf("-12") > 0)
		            c = 'j';
		        else
		        if(report.ssid.indexOf("-11") > 0)
		            c = 'O';
		        else
		        if(report.ssid.indexOf("-10") > 0)
		            c = 'S';
		        else
		        if(report.ssid.indexOf("-9") > 0)
		            c = '>';
		        else
		        if(report.ssid.indexOf("-8") > 0)
		            c = 's';
		        else
		        if(report.ssid.indexOf("-7") > 0)
		            c = '\'';
		        else
		        if(report.ssid.indexOf("-6") > 0)
		            c = 'X';
		        else
		        if(report.ssid.indexOf("-5") > 0)
		            c = 'Y';
		        else
		        if(report.ssid.indexOf("-4") > 0)
		            c = 'b';
		        else
		        if(report.ssid.indexOf("-3") > 0)
		            c = 'f';
		        else
		        if(report.ssid.indexOf("-2") > 0)
		            c = 'U';
		        else
		        if(report.ssid.indexOf("-1") > 0)
		            c = 'a';
		        report.icon = c - 32;
		        
		        // search for general id
		        pos = header.indexOf("GPS");
		        if(pos > 0 && header.indexOf(':') > pos && 
		        	header.charAt(pos + 3) != ':' && 
		        	header.charAt(pos + 3) != ',' && 
		        	header.charAt(pos + 3) != '>')
		        {
		            int x1 = -999;
		            char c1 = header.charAt(pos + 3);
		            if(c == 'B')
		                pos = -33;
		            else
		            if(c1 == 'P')
		                x1 = 0;
		            else
		            if(c1 == 'C')
		                x1 = 0;
		            else
		            if(c1 == 'M')
		                x1 = -24;
		            else
		            if(c1 == 'H')
		                x1 = 8;
		            else
		            if(c1 == 'L')
		                x1 = 32;
		            else
		            if(c1 == 'J')
		                x1 = 74;
		            else
		            if(c1 == '0')
		            {
		                x1 = -33;
		                report.isAltIcons = true;
		            } else
		            if(c1 == 'A')
		            {
		                x1 = 0;
		                report.isAltIcons = true;
		            } else
		            if(c1 == 'N')
		            {
		                x1 = -24;
		                report.isAltIcons = true;
		            } else
		            if(c1 == 'D')
		            {
		                x1 = 8;
		                report.isAltIcons = true;
		            } else
		            if(c1 == 'S')
		            {
		                x1 = 32;
		                report.isAltIcons = true;
		            } else
		            if(c1 == 'Q')
		            {
		                x1 = 74;
		                report.isAltIcons = true;
		            }
		            if(pos > -100)
		                report.icon = (header.charAt(pos + 4) - 32) + x1;
		        }
		        
		        // TODO: Force the Icon to the stations selected Icon
		        /*
		        if( report.id.equals( APRS.getMyCall() ) ) {
		        	report.icon = APRS.getSymChar();
		        	int tbl = APRS.getSymTable();
		        	report.isAltIcons = (tbl != '/');
		        	report.overlay = tbl;                        		
		        }
		        */
            }
	        
	        // finished
	        return report;
	        
	    }
	       
	}	
	
	protected final static class POSIT {
		
		private final static DecimalFormat format = new DecimalFormat("000000");
		
    	/**
    	 * <b>Parse time and position data types</b> </p>
    	 * 
    	 * The method requires a valid DTI field. This should be verified 
    	 * using the reqexp pattern REGEXP_DTI just before calling this method.
    	 * 
    	 * 
    	 **/
		protected final static IPacket parse(IPacket packet, String message)
	    {			
			
			// initialize
			String field;
			APRSPacket report = null;
			
			// store message
			String tmp = message;
			
        	/* =========================================================
        	 * Position report format with no time stamp?
        	 * ========================================================= */
        	Matcher matcher = getMatcher(REGEXP_PR_NTS,message);
        	
        	// span to end of all matches
            if(matcher.lookingAt())
            {          
            	// get field
            	field = matcher.group();
            	
	        	// create report with no time stamp (real time)
	        	report = createPacket(String.valueOf(field.charAt(0))+"NTS",matcher.group(),false,false,true);			          
	        	
	            // set fields
	            report.time = System.currentTimeMillis();
	            
	            // get character
	            char slat = field.charAt(8);
	            // validate format
	            if(slat=='N' || slat=='S') {
	            	double lat = Double.valueOf(field.substring(1,8));
	                char slon = field.charAt(18);
	            	if(slon=='W' || slon=='E') {
	                	double lon = Double.valueOf(field.substring(10,18));        
	                	report.setPosition(GeoUtils.fixLatLon(
	                			"," + String.valueOf(slat) + 
	               			 	"," + String.valueOf(slon) + ",", 
	                			GeoUtils.convertNMEA(lat),
	                			GeoUtils.convertNMEA(lon)));

	            	}
	            }
	            
	            // get message residue
	        	message = residue(message, matcher);
	            
	        	/* =========================================================
	        	 * has COURSE/SPEED data extension?
	        	 * ========================================================= */
	        	matcher = getMatcher(REGEXP_DE_NTS,tmp);

	        	// span to end of all matches
	            if(matcher.lookingAt())
	            {          	            	
	            	// get ?NTS-DE field
	            	field = matcher.group();
	            	
	            	// get DE field
	            	field = field.substring(field.length()-7);
	            
		        	// create report with no time stamp (real time)
		        	report = (APRSPacket)union(report,createPacket("DE",field,false,false,false));
		        	
		        	// get course in degrees
		        	report.course = Double.valueOf(field.substring(0, 3));

		        	// calculate m/s
		        	report.speed = Double.valueOf(field.substring(4, 7)) / 1.9439;
		        	
		            // get message residue
		        	message = residue(tmp, matcher);
		        	
	            }
	            
	        } else {
	        
	        	/* =========================================================
	        	 * Position report format with time stamp?
	        	 * ========================================================= */
	        	matcher = getMatcher(REGEXP_PR_WTS,message);
	
	        	// span to end of all matches
	            if(matcher.lookingAt())
	            {          
	            	// get field
	            	field = matcher.group();
	            	
		        	// create report with no time stamp (real time)
		        	report = createPacket(String.valueOf(field.charAt(0))+"WTS",matcher.group(),false,false,true);			          
		            
		            // get time zone character
		            char c = field.charAt(7);
		            
		            // validate format
		            if(c=='z' || c=='/' || c=='h') {
		            	
		            	// initialize date
		                Calendar t = Calendar.getInstance();
		                t.setTimeZone(c=='z' || c=='h' ? TimeZone.getTimeZone("GMT+0") : t.getTimeZone());
		                String utc = "000000";
		                if(c!='h') {
		                    String date = format.format(Integer.valueOf(message.substring(1,7)));
		                    t.set(Calendar.DAY_OF_MONTH, Integer.valueOf(date.substring(0, 2)));
		                    utc = date.substring(2, 6) + "00";
		                }
		                else {
		                    utc = message.substring(1,7);
		                }
		                t.set(Calendar.HOUR, Integer.valueOf(utc.substring(0, 2)));
		                t.set(Calendar.MINUTE, Integer.valueOf(utc.substring(2, 4)));
		                t.set(Calendar.SECOND, Integer.valueOf(utc.substring(4, 6)));
		                report.time = t.getTimeInMillis();
		                
		                // get latitude hemisphere 
		                char slat = field.charAt(15);
		                if(slat=='N' || slat=='S') {
			            	double lat = Double.valueOf(field.substring(8,15));
			            	// get longitude hemisphere
			                char slon = field.charAt(25);
			            	if(slon=='W' || slon=='E') {	            		
			                	double lon = Double.valueOf(field.substring(17,25));        
			                	report.setPosition(GeoUtils.fixLatLon(
			                			"," + String.valueOf(slat) + 
			                			 "," + String.valueOf(slon) + ",", 
			                			GeoUtils.convertNMEA(lat),
			                			GeoUtils.convertNMEA(lon)));
			                	
			            	}
		                }
		            }                 
		            
		            // get message residue
		        	message = residue(message, matcher);
		            
		        	/* =========================================================
		        	 * has COURSE/SPEED data extension?
		        	 * ========================================================= */
		        	matcher = getMatcher(REGEXP_DE_WTS,tmp);
	
		        	// span to end of all matches
		            if(matcher.lookingAt())
		            {          	          
		            	// get ?WTS-DE field
		            	field = matcher.group();
		            	
		            	// get DE field
		            	field = field.substring(field.length()-7);
		            
			        	// create report with no time stamp (real time)
			        	report = (APRSPacket)union(report,createPacket("DE",field,false,false,false));
			        	
			        	// get course in degrees
			        	report.course = Double.valueOf(field.substring(0, 3));
	
			        	// calculate m/s
			        	report.speed = Double.valueOf(field.substring(4, 7)) / 1.9439;
			        	
			            // get message residue
			        	message = residue(tmp, matcher);
			        	
		            }	      
		            
		        }
	        }
            
        	/* =========================================================
        	 * DF format BEARING/NRG (Number/Range/Quality) found?
        	 * ========================================================= */
        	matcher = getMatcher(REGEXP_DF,tmp);

        	// span to end of all matches
            if(matcher.lookingAt())
            {          
            	// get field
            	field = matcher.group();
            	
	        	// create DF report 
	        	report = (APRSPacket)union(report,createPacket("DF",field,false,false,false));
            	
	            // get message residue
	        	message = residue(tmp, matcher);
            	
            }
            
        	/* =========================================================
        	 * Altitude in comment field?
        	 * ========================================================= */
        	matcher = getMatcher(REGEXP_ALT,tmp);
        	
        	// span to end of all matches
            if(matcher.lookingAt())
            {          
            	// get field
            	field = matcher.group();
            	
	        	// create report with no time stamp (real time)
	        	report = (APRSPacket)union(report,createPacket("ALT",field,false,false,false));
	        	
	        	// get altitude in meters?
	        	if(report.isPositionSet())
	        		report.getPosition().setAltitude(Double.valueOf(field.substring(3)).doubleValue() / 3.2808);

	            // get message residue
	        	message = residue(tmp, matcher);
            	
            }

        	/* =========================================================
        	 * The rest is assumed to be comment if report exists
        	 * ========================================================= */
            if(report!=null) 
            {
            	// save comment
            	report.comment = message;
            }

            // finished
            return union(packet,report);
	            	        
	    }		
	}
	
	protected final static class GPS {
		
    	/**
    	 * <b>Parse raw GPS data types</b> </p>
    	 * 
    	 * The method requires a valid RAW GPS data field. This should be verified 
    	 * using the reqexp pattern REGEXP_GPS just before calling this method.
    	 * 
    	 * 
    	 **/
		protected final static IPacket parse(IPacket packet, String message)
	    {
			
			// initialize
			APRSPacket report = null;
			
			/* =========================================================
        	 * Check for raw GPS data (subset of NMEA and Garmin)
        	 * ========================================================= */
			String[] data = message.split("\\$");
        	
        	// loop until end of groups in
        	for(String it : data)
            {      
                // get sentence
            	String sentence = it;
            	
            	// get check sum
            	String cs = sentence.substring(sentence.indexOf("*") + 2);
            	
            	// validate sentence using the checksum
            	if(cs.equalsIgnoreCase(calcChecksum(sentence))) {
            	
		            // prepare packet
		            message = preprocess(message);
		            double fields[] = tokenizeString(message.substring(6));
		            
			    	// RMC format?
			        if(message.startsWith("$GPRMC"))
			        {
			        	// create report
			        	report = createPacket("GPRMC",message,false,false,false);
			            
			        	// initialize date and time from satellite-derived time (UTC +0, accurate to the millisecond)
			            Calendar t = Calendar.getInstance();
			            t.setTimeZone(TimeZone.getTimeZone("GMT+0"));
			            DecimalFormat format = new DecimalFormat("000000");
			            String date = format.format(fields[8]);
			            t.set(Calendar.DAY_OF_MONTH, Integer.valueOf(date.substring(0, 2)));
			            t.set(Calendar.MONTH, Integer.valueOf(date.substring(2, 4)));
			            t.set(Calendar.YEAR, Integer.valueOf(String.valueOf(t.get(Calendar.YEAR)).substring(0,2) + date.substring(4, 6)));
			            String utc = format.format(fields[0]);
			            t.set(Calendar.HOUR, Integer.valueOf(utc.substring(0, 2)));
			            t.set(Calendar.MINUTE, Integer.valueOf(utc.substring(2, 4)));
			            t.set(Calendar.SECOND, Integer.valueOf(utc.substring(4, 6)));
			            report.time = t.getTimeInMillis();
			            
			            // set remaining fields
			            report.setPosition(GeoUtils.fixLatLon(message, 
			            		GeoUtils.convertNMEA(fields[2]),
			            		GeoUtils.convertNMEA(fields[4])));
			            report.speed = (double)Math.round(fields[6] * 1.1539999999999999D * 10D) / 10D;
			            report.course = fields[7];
			            
			            // append to packet
			            packet = union(packet,report);
			            
			        }
			        
			    	// GLL format?
			        else if(message.startsWith("$GPGLL"))
			        {
			        	// create report
			        	report = createPacket("$GPGLL",message,false,false,false);			          
			            
			            // set fields
			            report.setPosition(GeoUtils.fixLatLon(message, 
			            		GeoUtils.convertNMEA(fields[0]),
			            		GeoUtils.convertNMEA(fields[2])));
			            
			            report.time = System.currentTimeMillis();
			            
			            // append to packet
			            packet = union(packet,report);
			        }
			        
			    	// GLL format?
			        else if(message.startsWith("$GPGGA"))
			        {
			        	// create report
			        	report = createPacket("$GPGGA",message,false,false,false);			          
		
			            // set fields
			            GeoPos position = GeoUtils.fixLatLon(message, 
			            		GeoUtils.convertNMEA(fields[1]),
			            		GeoUtils.convertNMEA(fields[3]));	         
			            position.setAltitude(fields[8]);
			            report.setPosition(position);
			            Calendar t = Calendar.getInstance();
			            t.setTimeZone(TimeZone.getTimeZone("GMT+0"));
			            DecimalFormat format = new DecimalFormat("000000");
			            String utc = format.format(fields[0]);
			            t.set(Calendar.HOUR, Integer.valueOf(utc.substring(0, 2)));
			            t.set(Calendar.MINUTE, Integer.valueOf(utc.substring(2, 4)));
			            t.set(Calendar.SECOND, Integer.valueOf(utc.substring(4, 6)));
			            report.time = t.getTimeInMillis();
			            
			            // append to packet
			            packet = union(packet,report);
			            
			        }

			        // WPL format?
			        else if(message.startsWith("$GPWPL"))
			        {
			        	/*
			        	1    5128.62   Latitude of nth waypoint on list
			            2    N         North/South
			            3    00027.58  Longitude of nth waypoint
			            4    W         East/West
			            5    EGLL      Ident of nth waypoint
			            6    *59       checksum
			            */
			        	// create report
			        	report = createPacket("$GPWPL",message,false,false,false);			          
		
			            // set fields
			            GeoPos position = GeoUtils.fixLatLon(message, 
			            		GeoUtils.convertNMEA(fields[1]),
			            		GeoUtils.convertNMEA(fields[3]));	         
			            report.setPosition(position);
			            Calendar t = Calendar.getInstance();
			            t.setTimeZone(TimeZone.getTimeZone("GMT+0"));
			            report.time = t.getTimeInMillis();
			            
			            // append to packet
			            packet = union(packet,report);
			            
			        }
			        			    
			        // RME format?
			        else if(message.startsWith("$PGRME"))
			        {		
			        	// create report
			        	report = createPacket("$PGRME",message,false,false,false);			          
			        	
			        	// initialize date
			            Calendar t = Calendar.getInstance();
			            t.setTimeZone(TimeZone.getTimeZone("GMT+0"));
			            report.time = t.getTimeInMillis();
			            report.hpe = Double.valueOf(fields[1]);
			            report.vpe = Double.valueOf(fields[3]);
			            report.epe = Double.valueOf(fields[5]);
			            
			            // finished
			            packet = union(packet,report);
			            
			        }			        
	            }
            }
        	
	        // finished
	        return packet;
	        
	    }
		
		protected static double[] tokenizeString(String packet) {
			StringTokenizer tokenizer = new StringTokenizer(packet,"/ ,");
			double doubleArray[] = new double[tokenizer.countTokens()];
			int i = 0;
			boolean notDone = true;
			while(notDone) {
				try {
					doubleArray[i] = Double.valueOf(tokenizer.nextToken()).doubleValue();
				} catch(NumberFormatException ex) {
					doubleArray[i] = 0.0D;
				} catch(NoSuchElementException ex) {
					notDone = false;
				}
				i++;
			}
			return doubleArray;
		}	
		
	    protected static String preprocess(String packet)
	    {
	        int idx;
	        while((idx = packet.indexOf(",,")) != -1) 
	            packet = packet.substring(0, idx) + ",0" + packet.substring(idx + 1);
	        return packet;
	    }
	    
	    // Calculates the checksum for a sentence
	    protected static String calcChecksum(String sentence)
	    {
	    	byte[] bytes = sentence.getBytes();
			//Start with first Item
			int checksum = bytes[sentence.indexOf('$')]+1;
			// Loop through all chars to get a checksum
			for (int i=sentence.indexOf('$')+2 ; i<sentence.indexOf('*') ; i++){
					// No. XOR the checksum with this character's value
					checksum^=bytes[i];				
			}
			// Return the checksum formatted as a two-character hexadecimal
			return Integer.toHexString(checksum);
	    }	    
			       
	}
	
	/*
	protected final static class MICE {
		
		protected final static APRSPacket parse(APRSPacket report, String packet)
	    {
			
	    	// prepare report
	    	report = (report == null ? new APRSPacket("MICE",packet,false,false,false) : report);
	        
	        // set fields
	        report.data = packet;
	        report.time = System.currentTimeMillis();
	        int i = packet.indexOf(62);
	        report.ssid = packet.substring(0, i);
	        String s1 = packet.substring(i + 1, i + 7);
	        int c = s1.charAt(0) - 32;
	        int mes = 0;
	        if(c < 31)
	            mes += 4;
	        int d = (c & 0xf) * 10;
	        c = s1.charAt(1) - 32;
	        if(c < 31)
	            mes += 2;
	        d += c & 0xf;
	        c = s1.charAt(2) - 32;
	        if(c < 31)
	            mes++;
	        int m = (c & 0xf) * 10;
	        c = s1.charAt(3) - 32;
	        boolean north = true;
	        if(c < 31)
	            north = false;
	        m += c & 0xf;
	        c = s1.charAt(4) - 32;
	        boolean hund = true;
	        if(c < 31)
	            hund = false;
	        int s = (c & 0xf) * 10;
	        c = s1.charAt(5) - 32;
	        boolean west = true;
	        if(c < 31)
	            west = false;
	        s += c & 0xf;
	        double lat = (double)d + (double)m / 60D + (double)s / 6000D;
	        if(!north) lat -= lat;
	        i = packet.indexOf(58) + 1;
	        char ch = packet.charAt(i + 8);
	        report.icon = packet.charAt(i + 7) - 32;
	        if(ch != '/' && ch != '\\' && (ch < 'A' || ch > 'Z' || ch >= '0' && ch <= '9'))
	            return null;
	        if(ch == '\\')
	            report.isAltIcons = true;
	        else
	        if(ch != '/')
	            return null;
	        d = packet.charAt(i + 1) - 28;
	        m = packet.charAt(i + 2) - 28;
	        s = packet.charAt(i + 3) - 28;
	        if(m >= 60) m -= 60;
	        double lon = (double)d + (double)m / 60D + (double)s / 6000D;
	        if(hund) lon = lon + 100D;
	        if(lon >= 190D)
	            lon = lon - 190D;
	        else {
		        if(lon >= 180D)
		            lon = lon - 80D;
	        }
	        if(!west) lon -= lon;
	        report.setPosition(lon,lat);
	        /*
	        m = packet.charAt(i + 5) - 28;
	        d = (packet.charAt(i + 4) - 28) * 10 + m / 10;
	        int m1 = m % 10;
	        s = m1 * 100 + (packet.charAt(i + 6) - 28);
	        if(d >= 800)
	            d -= 800;
	        if(s >= 400)
	            s -= 400;
	        /**//*
	        
	        return report;
	        
	    }
	}
	*/
	
	public static APRSPacket createPacket(
			String type, String message, boolean isUnknown, boolean autoReplyExists, boolean isRealtime) {
		return new APRSPacket(type,message,isUnknown,autoReplyExists,isRealtime);
	}

	/*
	public static void main(String[] args) {
		
		SimpleDateFormat format = new SimpleDateFormat("ddmmyy hh:MM:ss");
		APRSparser aprs = new APRSparser();
		String packet = "WD0AFQ>APOT03,KA4BNI-3*,WIDE2-1,qAR,WB4YDL-3:/060422z3647.44N/08958.46W>158/000/A=000149Classic GMC is Home Again";
		//String packet = "KL7GG>WX,WIDE1-1,WIDE2-2,qAR,KL7AA-2:@060621z6117.82N/14926.38W_000/000g004t036r000p000P000h43b10106.DsVP";
		//String packet = "AL0I>APU25N,TCPIP*,qAC,T2APRSWST:=3612.58NI08141.88W&Boone, NC iGate, Testing/Temporary {UIV32N}";
		//String packet = "K4DSO-B>APJI23,TCPIP*,qAC,K4DSO-BS:!3323.43ND08639.96W&RNG0030 440 Voice 443.200 +5 MHz";
		//String packet = "KC0QBU>GPSSK,WIDE1-1,WIDE2-1,qAR,KC0QBU-2:$GPRMC,042232,A,3855.184,N,09440.662,W,000.0,084.2,060409,003.2,E*68";
		//String packet = "KC0QBU>GPSSK,WIDE1-1,WIDE2-2,qAR,KC0QBU-2:$GPGGA,042232,3855.184,N,09440.662,W,1,10,1.4,308.8,M,-29.6,M,,*7F";
		IReport report = aprs.parse(null, packet);
		
		System.out.println(packet);
        System.out.println(report.getID() + " : " 
        		+ report.getPosition().toString() + " : " 
        		+ format.format(new Date(report.getTime()))+ "z : "
        		+ report.getComment());
        		
	}	
	*/
	
	public static void main(String[] args) {
		
		/*
		//SimpleDateFormat format = new SimpleDateFormat("ddmmyy hh:MM:ss");
		
		APRSparser aprs = new APRSparser();
		//String message = "WD0AFQ>APOT03,KA4BNI-3*,WIDE2-1,qAR,WB4YDL-3:/060422z3647.44N/08958.46W>158/000/A=000149Classic GMC is Home Again";
		//String message = "KL7GG>WX,WIDE1-1,WIDE2-2,qAR,KL7AA-2:@060621z6117.82N/14926.38W_000/000g004t036r000p000P000h43b10106.DsVP";
		//String message = "AL0I>APU25N,TCPIP*,qAC,T2APRSWST:=3612.58NI08141.88W&Boone, NC iGate, Testing/Temporary {UIV32N}";
		//String message = "K4DSO-B>APJI23,TCPIP*,qAC,K4DSO-BS:!3323.43ND08639.96W&RNG0030 440 Voice 443.200 +5 MHz";
		//String message = "KC0QBU>GPSSK,WIDE1-1,WIDE2-1,qAR,KC0QBU-2:$GPRMC,042232,A,3855.184,N,09440.662,W,000.0,084.2,060409,003.2,E*68";
		//String message = "KC0QBU>GPSSK,WIDE1-1,WIDE2-2,qAR,KC0QBU-2:$GPGGA,042232,3855.184,N,09440.662,W,1,10,1.4,308.8,M,-29.6,M,,*7F";
		
		// create session
		SerialSession session = IOManager.getInstance().getSerialSession(aprs);
		
		// add to port
		session.connect("COM4", 38400, 
				SerialLink.DATABITS_8, 
		        SerialLink.STOPBITS_1, 
		        SerialLink.PARITY_NONE, 
		        SerialLink.FLOWCONTROL_NONE);
		
		//session.send(message);
		
		//APRSPacket packet = aprs.parse(str,false);
		
		System.out.println(message);
        System.out.println(packet.getID() + " : " 
        		+ packet.getPosition().toString() + " : " 
        		+ format.format(new Date(packet.getTime()))+ "z : "
        		+ packet.getComment());
        */
		
	}		
	
}
