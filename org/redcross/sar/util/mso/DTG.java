package org.redcross.sar.util.mso;

import org.redcross.sar.util.except.IllegalMsoArgumentException;

import java.util.Calendar;

/**
 * Class for conversion between {@link java.util.Calendar} objects and DTG Strings.
 */
public class DTG
{
    /**
     * Create a {@link java.util.Calendar} object for a DTG string using current year and month.
     * @param aDTG The DTG value
     * @return The created object
     * @throws IllegalMsoArgumentException If DTG has an illegal value.
     */
    public static Calendar DTGToCal(String aDTG) throws IllegalMsoArgumentException
    {	
    	Calendar c = Calendar.getInstance();
    	return DTGToCal(c.get(Calendar.YEAR),c.get(Calendar.MONTH),aDTG);
    }
    
    /**
     * Create a {@link java.util.Calendar} object for a DTG string using current year and month.
     * @param aDTG The DTG value
     * @return The created object
     * @throws IllegalMsoArgumentException If DTG has an illegal value.
     */
    public static Calendar DTGToCal(long aDTG) throws IllegalMsoArgumentException
    {	
    	Calendar c = Calendar.getInstance();
    	return DTGToCal(c.get(Calendar.YEAR),c.get(Calendar.MONTH),aDTG);
    }
    
    /**
     * Create a {@link java.util.Calendar} object for a DTG string.
     * @param aDTG The DTG value
     * @return The created object
     * @throws IllegalMsoArgumentException If DTG has an illegal value.
     */
    public static Calendar DTGToCal(int year, int month, String aDTG) throws IllegalMsoArgumentException
    {
        try
        {
            return DTGToCal(year,month,Long.parseLong(aDTG==null||aDTG.isEmpty()?"0":aDTG));
        }
        catch (NumberFormatException e)
        {
            throw (new IllegalMsoArgumentException("Illegal number format to DTG: " + aDTG));
        }
        catch (IllegalMsoArgumentException e)
        {
            throw (e);
        }
    }

    /**
     * Create a {@link java.util.Calendar} object for a DTG number.
     * @param aDTG The DTG value
     * @return The created object
     * @throws IllegalMsoArgumentException If DTG has an illegal value.
     */
    public static Calendar DTGToCal(int year, int month, long aDTG) throws IllegalMsoArgumentException
    {
        Calendar calendar = Calendar.getInstance();
        int day = (int) aDTG / 10000;
        int hour = (int) (aDTG % 10000) / 100;
        int minute = (int) aDTG % 100;

        if (minute >= 60)
        {
            throw new IllegalMsoArgumentException("Illegal DTG minute value in " + aDTG);
        }

        if (hour >= 24)
        {
            throw new IllegalMsoArgumentException("Illegal DTG hour value in " + aDTG);
        }

        if (day < 1 || !adjustToDay(calendar, day))
        {
            throw new IllegalMsoArgumentException("Illegal DTG day value in " + aDTG);
        }

        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.MONTH,month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
//        System.out.println(aDTG + "" + calendar.getTime());
        return calendar;
    }

    /**
     * Generate a DTG string from a {@link java.util.Calendar} object value.
     * @param aCalendar Input Calendar value.
     * @return The generated DTG string, or an empty string if aCalendar is null.
     */
    public static String CalToDTG(Calendar aCalendar)
    {
		//SimpleDateFormat format = new SimpleDateFormat("HH:MM:ss");
		//System.out.println("CalToDTG:: " + format.format(aCalendar.getTime()));
		//Calendar c = Calendar.getInstance();
        return aCalendar != null ? String.format("%1$td%1$tH%1$tM", aCalendar) : "";
    }

    private static boolean adjustToDay(Calendar aCalendar, int aDay)
    {
        int today = aCalendar.get(Calendar.DAY_OF_MONTH);
        if (aDay < today - 20)
        { // Next month
            aCalendar.add(Calendar.MONTH, 1);
        } else if (today < aDay - 10)
        {
            aCalendar.add(Calendar.MONTH, -1);
        }
        return aDay <= aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    
    public static boolean isEqualDTG(Calendar c1, Calendar c2) {
    	if(c1==c2) return true;
    	if(c1!=null) {
    		if(c1.equals(c2)) return true;
    		return (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) ||
    				c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) ||
    				c1.get(Calendar.HOUR_OF_DAY) == c2.get(Calendar.HOUR_OF_DAY) ||
    				c1.get(Calendar.MINUTE) == c2.get(Calendar.MINUTE));
    	}
    	return false;
    }

    /*
    public static void main(String[] argv)
    {
        for (int year = 2007; year <= 2008; year++)
        {
            for (int month = 0; month <= 2; month++)
            {
                for (int day = 1; day <= 32; day++)
                {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.DAY_OF_MONTH, 15);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.YEAR, year);
                    System.out.print("Before: " + calendar.getTime() + ", setting day to " + day + ": ");
                    if (adjustToDay(calendar, day))
                    {
                        calendar.set(Calendar.DAY_OF_MONTH, day);
                        System.out.println(calendar.getTime());
                    } else
                    {
                        System.out.println("not legal!");
                    }
                }
            }
        }
    }
    */
}