package org.redcross.sar.mso;

import org.redcross.sar.IApplication;
import org.redcross.sar.mso.TransactionManagerImpl;
import org.redcross.sar.mso.IChangeIf.ChangeType;
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
        List<IChangeIf.IChangeObjectIf> objectList = wrapper.getObjects();
        for (IChangeIf.IChangeObjectIf ico : objectList)
        {
            //IF created, create SARA object
            //if modified, modify Saraobject.
            //if deleted remove Sara object
        }

        List<IChangeIf.IChangeReferenceIf> attrList = wrapper.getAttributeReferences();
        for (IChangeIf.IChangeReferenceIf ico : attrList)
        {
			ChangeType ct = ico.getType(); //CommitManager.CommitType.COMMIT_CREATED/CommitManager.CommitType.COMMIT_DELETED
            String attName = ico.getReferenceName();
            IMsoObjectIf owner = ico.getReferringObject();
            IMsoObjectIf attribute = ico.getReferredObject();
        }

        List<IChangeIf.IChangeReferenceIf> listList = wrapper.getListReferences();
        for (IChangeIf.IChangeReferenceIf ico : listList)
        {
            ChangeType ct = ico.getType(); 
            String refName = ico.getReferenceName();
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
