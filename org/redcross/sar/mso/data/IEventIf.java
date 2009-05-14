package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;

import java.util.Collection;
import java.util.Comparator;

public interface IEventIf extends ITimeItemIf, ISerialNumberedIf
{

    public enum EventStatus
    {
        UNCONFIRMED,
        POSTPONED,
        CONFIRMED
    }
    
    public static final Comparator<IEventIf> NUMBER_COMPARATOR = new Comparator<IEventIf>()
    {
        public int compare(IEventIf e1, IEventIf e2)
        {
           return e2.getNumber()-e1.getNumber();
        }
    };
    
    public static final Comparator<IEventIf> LEVEL_PRIORITY_NUMBER_COMPARATOR = new Comparator<IEventIf>()
    {
        public int compare(IEventIf e1, IEventIf e2)
        {
        	// sort on level first
        	if(e2.getLevel()==e1.getLevel()) {
        		// sort on priority second
            	if(e2.getPriority()==e1.getPriority()) {
	        		// sort on number third
	        		return e2.getNumber()-e1.getNumber();
            	}
        		return e2.getPriority()-e1.getPriority();
        	}
        	else
        		return e2.getLevel()-e1.getLevel();
        }
    };
    
    public static final Comparator<IEventIf> STATUS_NUMBER_COMPARATOR = new Comparator<IEventIf>()
    {
        public int compare(IEventIf e1, IEventIf e2)
        {
        	// sort on status first
        	if(e2.getStatus().compareTo(e1.getStatus())==0)
        		// sort on number second
        		return e2.getNumber()-e1.getNumber();
        	else
        		return e2.getStatus().compareTo(e1.getStatus());
        }
    };
    
    /*-------------------------------------------------------------------------------------------
    * Methods for ENUM attributes
    *-------------------------------------------------------------------------------------------*/

    public void setStatus(EventStatus aStatus);

    public void setStatus(String aStatus);

    public EventStatus getStatus();

    public IMsoModelIf.ModificationState getStatusState();

    public IMsoAttributeIf.IMsoEnumIf<EventStatus> getStatusAttribute();
    
    /*-------------------------------------------------------------------------------------------
    * Methods for attributes
    *-------------------------------------------------------------------------------------------*/
    
    public void setPriority(int aPriority);

    public int getPriority();

    public IMsoModelIf.ModificationState getPriorityState();

    public IMsoAttributeIf.IMsoIntegerIf getPriorityAttribute();

    public void setLevel(int aLevel);

    public int getLevel();

    public IMsoModelIf.ModificationState getLevelState();

    public IMsoAttributeIf.IMsoIntegerIf getLevelAttribute();
    
    public void setName(String aText);

    public String getName();

    public IMsoModelIf.ModificationState getNameState();

    public IMsoAttributeIf.IMsoStringIf getNameAttribute();
    
    public void setDescription(String aText);

    public String getDescription();

    public IMsoModelIf.ModificationState getDescriptionState();

    public IMsoAttributeIf.IMsoStringIf getDescriptionAttribute();
    
    /*-------------------------------------------------------------------------------------------
    * Methods for lists
    *-------------------------------------------------------------------------------------------*/

    public void addEventTask(ITaskIf anITaskIf);

    public ITaskListIf getEventTasks();

    public IMsoModelIf.ModificationState getEventTasksState(ITaskIf anITaskIf);

    public Collection<ITaskIf> getEventTasksItems();
    
    /*-------------------------------------------------------------------------------------------
     * Methods for references
     *-------------------------------------------------------------------------------------------*/

    /*-------------------------------------------------------------------------------------------
    * Other specified methods
    *-------------------------------------------------------------------------------------------*/

}
