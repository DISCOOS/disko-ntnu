package org.redcross.sar.mso;

import org.redcross.sar.IApplication;
import org.redcross.sar.mso.data.AbstractMsoObject;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoTransactionListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.work.IWorkPool;

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

    public IMsoObjectIf.IObjectIdIf createObjectId()
    {
        int rand = m_rand.nextInt(1000);
        long time = new Date().getTime();
        return new AbstractMsoObject.ObjectId(Long.toString(time) + "." + Integer.toString(rand), Calendar.getInstance().getTime());

    }

    public boolean initiate(IMsoModelIf aMsoModel)
    {
    	return true;
    }

    public boolean isReady()
    {
        return true;
    }

    public List<String[]> getActiveOperations()
    {
        List<String[]> list = new ArrayList<String[]>();
        list.add(new String[]{"2", "2"});
        return list;
    }

    public boolean setCurrentOperation(String operationid)
    {
        return true;
    }

	public String getCurrentOperationID() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCurrentOperationName() {
		// TODO Auto-generated method stub
		return null;
	}    
	
    public boolean createNewOperation(long timeOutMillis)
    {
        return false;
    }

	public boolean finishCurrentOperation()
    {
        return false;
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
        List<IChangeRecordIf> objectList = wrapper.getRecords();
        for (IChangeRecordIf it : objectList)
        {
            //IF created, create SARA object
            //if modified, modify Saraobject.
            //if deleted remove Sara object
        }

        List<IChangeIf.IChangeRelationIf> attrList = wrapper.getObjectRelationChanges();
        for (IChangeIf.IChangeRelationIf ico : attrList)
        {
            String attName = ico.getName();
            IMsoObjectIf owner = ico.getRelatingObject();
            IMsoObjectIf attribute = ico.getRelatedObject();
        }

        List<IChangeIf.IChangeRelationIf> listList = wrapper.getListRelationChanges();
        for (IChangeIf.IChangeRelationIf ico : listList)
        {
            String refName = ico.getName();
            IMsoObjectIf owner = ico.getRelatingObject();
            IMsoObjectIf ref = ico.getRelatedObject();
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

	@Override
	public boolean initiate(IWorkPool pool) {
		return false;
	}

	@Override
	public boolean isCreationInProgress() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInitiated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInitiated(IMsoModelIf model) {
		// TODO Auto-generated method stub
		return false;
	}

}
