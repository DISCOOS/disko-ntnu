package org.disco.io.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client implements Runnable {

	/**
	 * Supported data types (see page 17 in APRS specification 1.0.1 for complete list)
	 * 
	 * DTI = Data Type Identifier
	 */
	private static final String DTI = 
		  new Character((char)28).toString()  	// Current Mic-E Data (Rev 0 beta)
		+ new Character((char)29).toString()	// Old Mic-E Data (Rev 0 beta)
		+ '!'	// Position without timestamp (no APRS messaging (or Ultimeter 2000 WX Station, not supported)
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
	private final static String REGEXP_SPH = "^\\r}*\\w{1,6}(-\\d{1,2}>|>)[^:]*:";
	/** finds first occurrence of source call sign and ssid pattern if exists */
	private final static String REGEXP_CSS = "\\w{1,6}(-\\d{1,2}|.{0})";
	/** finds first occurrence of ssid pattern if exists */
	private final static String REGEXP_SSID = "-\\d{1,2}";
	/** finds first occurrence of Data Type Identifier pat if exists */
	private final static String REGEXP_DTI = "(" + DTI.charAt(2) + '|' + DTI.charAt(3) + '|' 
			+ DTI.charAt(4) + '|' + DTI.charAt(5) + '|' + DTI.charAt(6) + "){1}";
	/** finds first occurrence of NMEA GPS data data pattern if exists */
	private final static String REGEXP_GP = "\\$GP";
	/** finds first occurrence of proprietary Garmin GPS data data pattern if exists */
	private final static String REGEXP_PG = "\\$PG";
	/** finds first occurrence of altitude in comment field if exists */
	private final static String REGEXP_ALT = "/A=\\d{6}";
	/** finds first occurrence of DTI '!' or '=' (position report with no time stamp) if exists */
	private final static String REGEXP_PR_NTS = "(!|=)\\d{4}.\\d{2}[N|S][\\|/]\\d{5}.\\d{2}[W|E]\\W";
	/** finds first occurrence of DTI '!' or '=' (position report with time stamp) if exists */
	private final static String REGEXP_PR_WTS = "(/|@)\\d{6}[z|/|h]\\d{4}.\\d{2}[N|S][\\|/]\\d{5}.\\d{2}[W|E]\\W";
	/** finds first occurrence of COURSE/SPEED Data Extension or DF report field BRG/NRQ if exists */
	private final static String REGEXP_PR_CS = "\\W\\d{3}/\\d{3}";
	/** finds first occurrence of DF report with no time stamp if exist */
	private final static String REGEXP_DF_CS_NTS = "(!|=)\\d{4}.\\d{2}[N|S]/\\d{5}.\\d{2}[W|E]\\" + REGEXP_PR_CS + "/" + REGEXP_PR_CS;
	/** finds first occurrence of COURSE/SPEED Data Extension if exists */
	private final static String REGEXP_DF_CS_WTS = "(/|@)\\d{6}[z|/|h]\\d{4}.\\d{2}[N|S][\\|/]\\d{5}.\\d{2}[W|E]\\W" + REGEXP_PR_CS + "/" + REGEXP_PR_CS;

	public static void main(String[] args) {
		
		// initialize
		String data;
		Pattern pattern;
		Matcher matcher;
		
		//String REGEXP_GPS = "(!|=)\\d{4}\\.\\d{2}[N|S].\\d{5}\\.\\d{2}[W|E]."; //"(\\$GP[A-Z]{3}|\\$PG[A-Z]{3})+";
		String REGEXP_GPS = "(/|@).{26}\\d{3}\\/\\d{3}"; //"(\\$GP[A-Z]{3}|\\$PG[A-Z]{3})+";
		
		//data = "!4903.50N/07201.75W-Test 001234"; //"$PGRMC,040302.663,A,3939.7,N,10506.6,W,0.27,358.86,200804,,*1A$GPEMC,040302.663,A,3939.7,N,10506.6,W,0.27,358.86,200804,,*1A";
		data =	"@131328z6037.35N/01636.50E_045/002g006t043r000p000P000h80b10072/WX Report {UIV32N}";
		
		pattern = Pattern.compile(REGEXP_GPS);
		matcher = pattern.matcher(data);
		if(matcher.find()) {
			System.out.println("GPS found: " + matcher.group()); //data.substring(matcher.start(),matcher.end()));			
		}
		else {
			System.out.println("GPS not found!");
		}		
		
		//data = "!4903.50N/07201.75W-Test 001234";
		//data = "=4903.50N/07201.75W-Test 001234";
		//data = "/092345z4903.50N/07201.75W>Test1234";
		//data = "@092345z4903.50N/07201.75W>Test1234";
		//data = "!4903.50N/07201.75W>088/036";
		/*
		pattern = Pattern.compile(REGEXP_PR_WTS);
		matcher = pattern.matcher(data);
		if(matcher.find()) {
			System.out.println("DTI found: " + data.substring(matcher.start(),matcher.end()));			
		}
		else {
			System.out.println("DTI not found!");
		}		
		*/
		// string to match
		data = "\rla3mxa>kengu:";
		
		// Source Path Header, discard third-party header suffix if exists
		pattern = Pattern.compile("^\\r}*\\w{1,6}(-\\d{1,2}>|>)[^:]*:");
		matcher = pattern.matcher(data);
		if(matcher.find()) {
			//System.out.println("Source Path Header format found: " + data.substring(matcher.start(),matcher.end()));
			pattern = Pattern.compile("\\w{1,6}(-\\d{1,2}|.{0})");
			matcher = pattern.matcher(data);
			if(matcher.find()) {
				//System.out.println("Source callsign found: " + data.substring(matcher.start(),matcher.end()));
				pattern = Pattern.compile("-\\d{1,2}");
				matcher = pattern.matcher(data);
				if(matcher.find()) {
					//System.out.println("SSID found: " + data.substring(matcher.start(),matcher.end()));					
				} else {
					//System.out.println("SSID found: -0");
				}				
			}
		}
		else {
			//System.out.println("Source Path Header format not found!");			
		}
		
		data = "/A=012345";
		pattern = Pattern.compile("/A=\\d{6}");
		matcher = pattern.matcher(data);
		if(matcher.find()) {
			//System.out.println("Altitude found: " + data.substring(matcher.start()+3,matcher.end()));			
		}
		else {
			//System.out.println("Altitude not found!");
		}		
		
		data = "sfsdf4545634dfdf/eefsdfcsdf";
		pattern = Pattern.compile(REGEXP_DTI);
		matcher = pattern.matcher(data);
		if(matcher.find()) {
			//System.out.println("DTI found: " + data.substring(matcher.start(),matcher.end()));			
		}
		else {
			//System.out.println("DTI not found!");
		}		
						
		
		//(new Thread(new Client("www.aprs2.net",14580))).start();
		//(new Thread(new Client("mb7uxx.dns2go.com",10152))).start();
		//(new Thread(new Client("ahubswe.net",14579))).start();
		//(new Thread(new Client("srv.aprs.la",14580))).start();
		//(new Thread(new Client("ahubswe.net",2023))).start();
	}
	
	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private PrintStream pout;
	private Thread tin;
	private SerialReader rin;
	
	public Client(String host, int port) {

		try {
			
			socket = new Socket(host, port);
			in = socket.getInputStream();
			out = socket.getOutputStream();
			pout = new PrintStream(out);
			
			rin = new SerialReader(in);
			tin = new Thread(rin);
			tin.start();
			
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // port 8080
		
	}
	
	@Override
	public void run() {
		try {
				
			// wait 5 seconds...
			Thread.sleep(5000);
			
			// send login
			//send("\ruser kengu pass -1");
			send("\ruser kengu pass -1 filter r/64/10/200\n");
			//send("\ruser la3xma-4 pass -16826 filter r/64/10/200\n");
			
			// wait 10 seconds...
			Thread.sleep(40000);
			
			// terminate input thread and wait
			rin.shutdown();
			tin.join();

			// terminate output thread and wait
			//tout.interrupt();
			//tout.join();
			
			// close resources
			pout.close();
			socket.close();			
						
			// notify user
			System.out.println("Client terminated successfully");
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // port 8080
		
	}
	
	private void send(String msg) {
		// log message
		System.out.println(msg);
		// forward
		pout.print(msg);		
	}
	
    public static class SerialReader implements Runnable 
    {
        InputStream in;
        
        boolean isRunning = true;
        
        public SerialReader ( InputStream in )
        {
            this.in = in;
        }
        
        public void run ()
        {
            byte[] buffer = new byte[1024];
            int len = -1;
            try
            {
                while (isRunning && ( len = this.in.read(buffer)) > -1 )
                {
                    System.out.print(new String(buffer,0,len));
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }            
        }
        
        public void shutdown() {
        	isRunning = false;
        }
    }	
   	
}
