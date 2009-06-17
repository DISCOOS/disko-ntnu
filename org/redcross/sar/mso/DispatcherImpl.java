package org.redcross.sar.mso;

import org.redcross.sar.IApplication;
import org.redcross.sar.mso.data.AbstractMsoObject;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoTransactionListenerIf;
import org.redcross.sar.mso.event.MsoEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Simple dispatcher implementation. For testing purposes only!
 * 
 * For documentation, see {@link  org.redcross.sar.modelDriver.IModelDriverIf}
 */
public class DispatcherImpl implements IDispatcherIf, IMsoTransactionListenerIf
{
    Random m_rand = new Random(89652467667623L);

    public IMsoObjectIf.IObjectIdIf makeObjectId()
    {
        int rand = m_rand.nextInt(1000);
        long time = new Date().getTime();
        return new AbstractMsoObject.ObjectId(Long.toString(time) + "." + Integer.toString(rand), Calendar.getInstance().getTime());

    }

    public void initiate()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isInitiated()
    {
        return true;
    }

    public List<String[]> getActiveOperations()
    {
        List<String[]> list = new ArrayList<String[]>();
        list.add(new String[]{"2", "2"});
        return list;
    }

    public boolean setActiveOperation(String operationid)
    {
        return true;
    }

	public String getActiveOperationID() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getActiveOperationName() {
		// TODO Auto-generated method stub
		return null;
	}    
	
	public void finishActiveOperation()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void createNewOperation()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void merge()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setDiskoApplication(IApplication diskoApp)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void shutdown()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @SuppressWarnings("unused")
    public void handleMsoCommitEvent(MsoEvent.Commit e)
    {
        // 
        ITransactionIf wrapper = (ITransactionIf) e.getSource();
        List<IChangeRecordIf> objectList = wrapper.getChanges();
        for (IChangeRecordIf it : objectList)
        {
            //IF created, create SARA object
            //if modified, modify Saraobject.
            //if deleted remove Sara object
        }

        List<IChangeIf.IChangeRelationIf> attrList = wrapper.getObjectRelations();
        for (IChangeIf.IChangeRelationIf ico : attrList)
        {
            String attName = ico.getName();
            IMsoObjectIf owner = ico.getReferringObject();
            IMsoObjectIf attribute = ico.getReferredObject();
        }

        List<IChangeIf.IChangeRelationIf> listList = wrapper.getListRelations();
        for (IChangeIf.IChangeRelationIf ico : listList)
        {
            String refName = ico.getName();
            IMsoObjectIf owner = ico.getReferringObject();
            IMsoObjectIf ref = ico.getReferredObject();
        }

    }

	public boolean addDispatcherListener(IDispatcherListenerIf listener) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean removeDispatcherListener(IDispatcherListenerIf listener) {
		// TODO Auto-generated method stub
		return false;
	}


}
