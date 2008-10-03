package org.redcross.sar.mso.data;

import java.util.Comparator;

/**
 *
 */
public interface ICommunicatorIf extends IMsoObjectIf
{
    public static final Comparator<ICommunicatorIf> COMMUNICATOR_COMPARATOR = new Comparator<ICommunicatorIf>()
    {
        public int compare(ICommunicatorIf c1, ICommunicatorIf c2)
        {
			if(c1.getCommunicatorNumberPrefix() == c2.getCommunicatorNumberPrefix())
			{
				return c1.getCommunicatorNumber() - c2.getCommunicatorNumber();
			}
			else
			{
				return c1.getCommunicatorNumberPrefix() - c2.getCommunicatorNumberPrefix();
			}
        }
    };

    public void setCallSign(String aCallsign);

    public String getCallSign();

    public void setToneID(String toneId);

    public String getToneID();

    public char getCommunicatorNumberPrefix();

    public int getCommunicatorNumber();
    
    public String getCommunicatorShortName();
    
}
