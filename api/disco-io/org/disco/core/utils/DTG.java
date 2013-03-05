package org.disco.core.utils;

import java.util.Calendar;

/**
 * Class for conversion between {@link java.util.Calendar} objects and DTG Strings.
 */
public class DTG
{
    /**
     * Create a {@link java.util.Calendar} object for a DTG string.
     * @param aDTG The DTG value
     * @return The created object
     * @throws IllegalArgumentException If DTG has an illegal value.
     */
    public static Calendar DTGToCal(int year, int month, String aDTG) throws IllegalArgumentException
    {
        try
        {
            return DTGToCal(year,month,Long.parseLong(aDTG));
        }
        catch (NumberFormatException e)
        {
            throw (new IllegalArgumentException("Illegal number format to DTG: " + aDTG));
        }
        catch (IllegalArgumentException e)
        {
            throw (e);
        }
    }

    /**
     * Create a {@link java.util.Calendar} object for a DTG number.
     * @param aDTG The DTG value
     * @return The created object
     * @throws IllegalArgumentException If DTG has an illegal value.
     */
    public static Calendar DTGToCal(int year, int month, long aDTG) throws IllegalArgumentException
    {
        Calendar calendar = Calendar.getInstance();
        int day = (int) aDTG / 10000;
        int hour = (int) (aDTG % 10000) / 100;
        int minute = (int) aDTG % 100;

        if (minute >= 60)
        {
            throw new IllegalArgumentException("Illegal DTG minute value in " + aDTG);
        }

        if (hour >= 24)
        {
            throw new IllegalArgumentException("Illegal DTG hour value in " + aDTG);
        }

        if (day < 1 || !adjustToDay(calendar, day))
        {
            throw new IllegalArgumentException("Illegal DTG day value in " + aDTG);
        }

        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.MONTH,month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * Generate a DTG string from a {@link java.util.Calendar} object value.
     * @param aCalendar Input Calendar value.
     * @return The generated DTG string, or an empty string if aCalendar is null.
     */
    public static String CalToDTG(Calendar aCalendar)
    {
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

}