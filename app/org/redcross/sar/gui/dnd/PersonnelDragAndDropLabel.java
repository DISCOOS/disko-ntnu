package org.redcross.sar.gui.dnd;

import org.redcross.sar.gui.renderer.ObjectIcon;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.util.MsoUtils;

/**
 * A subclass of {@link MsoDragAndDropLabel} that supports personnel transfer using drag-and-drop (DnD).
 * 
 * @author kenneth
 */

public class PersonnelDragAndDropLabel extends MsoDragAndDropLabel<IPersonnelIf>
{

	private static final long serialVersionUID = 1L;

    public PersonnelDragAndDropLabel(ObjectIcon.PersonnelIcon anIcon, MsoLabelActionHandler<IPersonnelIf> anActionHandler)
    {
        super(anIcon, anActionHandler);
    }

    public PersonnelDragAndDropLabel(IPersonnelIf aPersonnel, MsoLabelActionHandler<IPersonnelIf> anActionHandler)
    {
        super(aPersonnel,anActionHandler);
    }
    
	@Override
    protected String getObjectText() {
    	String text = null;
    	IPersonnelIf anObject = getMsoObject();
    	if(anObject!=null) 
    	{
	        text = MsoUtils.getPersonnelName(anObject,false);
    	}
    	return text;
    }
}
