package org.redcross.sar.test;

import static org.testng.Assert.assertTrue;

import javax.swing.SwingUtilities;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.redcross.sar.mso.IDispatcherIf;
import org.redcross.sar.mso.IDispatcherListenerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.SaraDispatcherImpl;
import org.redcross.sar.work.IWorkPool;
import org.redcross.sar.work.WorkPool;

/**
 * Class implementing MSO model tests used to validate code integrity
 * 
 * @author kenneth
 */
public class MsoModelTester 
{

	private String oprID;
	private IWorkPool m_pool;
    private IMsoModelIf m_model;
    private IDispatcherIf m_dispatcher;
    
    /**
     * Initialization before testing begins.
     * 
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public void setUp() throws Exception 
	{
		
		//initiate model dispatcher
        m_dispatcher = new SaraDispatcherImpl();
		m_dispatcher.addDispatcherListener(new IDispatcherListenerIf() {

			@Override
			public void onOperationCreated(String oprID, boolean isLoopback) 
			{
				// set id?
				if(isLoopback)
				{
					MsoModelTester.this.oprID = oprID;
				}
				System.out.println("Created OprID:=" + oprID + ", isLoopback:=" + isLoopback);
			}

			@Override
			public void onOperationFinished(String oprID, boolean isLoopback) 
			{
				// reset id?
				if(isLoopback)
				{
					MsoModelTester.this.oprID = null;				
				}
				System.out.println("Finished OprID:=" + oprID + ", isLoopback:=" + isLoopback);
			}			

			@Override
			public void onOperationActivated(String oprID) 
			{ 
				MsoModelTester.this.oprID = oprID;				
				System.out.println("Activated OprID:=" + oprID);
			}

			@Override
			public void onOperationDeactivated(String oprID) 
			{ 
				if(MsoModelTester.this.oprID==oprID)
				{
					MsoModelTester.this.oprID = null;
				}
				System.out.println("Dectivated OprID:=" + oprID);				
			}

		});
		// initiate model
		m_model = new MsoModelImpl();
		// initiate work pool on EDT (required by work pool)
		SwingUtilities.invokeAndWait(new Runnable() {

			@Override
			public void run() {
				
				try {
					m_pool = WorkPool.getInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
		});
	}
	
	/**
	 * Cleanup after testing completed.
	 * 
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public void tearDown() throws Exception 
	{
		// cleanup
		m_dispatcher.shutdown();
	}
	
	@Test(timeOut = 25000)
	public void testInitiateDispatcher() throws InterruptedException 
	{
		// initiate dispatcher
		assertTrue(m_dispatcher.initiate(m_pool));
		// ensure that startup procedure succeeds within 25 seconds. This
		// should suffice. The delay is connected with the time the 
		// dispatcher need to discover all active operations 
		while(!m_dispatcher.isReady())
		{
			Thread.sleep(100);
		}
	}
	
	@Test(dependsOnMethods = {"testInitiateDispatcher"}, timeOut = 5000)
	public void testInitiateModel() throws InterruptedException 
	{
		// initiate model (and dispatcher if not already initiated)
		assertTrue(m_dispatcher.initiate(m_model));
		// ensure that startup procedure succeeds within 5 seconds.
		while(!m_dispatcher.isReady())
		{
			Thread.sleep(100);
		}
	}

	@Test(dependsOnMethods = {"testInitiateModel"}, timeOut = 5000)
	public void testCreateNewOperation() throws InterruptedException
	{
		// create a new operation
		assertTrue(m_dispatcher.createNewOperation(5000));
		// ensure that creation process succeeds within 5 seconds.
		while(oprID==null) {
			Thread.sleep(100);
		}
	}
	
	@Test(dependsOnMethods = {"testInitiateModel","testCreateNewOperation"})
	public void testActivateOperation()
	{
		System.out.println("Activate OprID:=" + oprID);
		// set current operation
		assertTrue(m_dispatcher.setCurrentOperation(oprID),"OprID:=" + oprID);
	}
	
	@Test(dependsOnMethods = {"testCreateNewOperation","testActivateOperation"})
	public void testBuildModel()
	{
		System.out.println("BuildModel");
	}
	
	@Test(dependsOnMethods = {"testCreateNewOperation","testActivateOperation","testBuildModel"})//, timeOut = 15000)
	public void testFinishOperation() throws InterruptedException 
	{
		System.out.println("Finish OprID:=" + oprID);
		// finish current operation
		assertTrue(m_dispatcher.finishCurrentOperation());		
		// ensure that finishing process succeeds within 5 seconds.
		while(oprID!=null) {
			Thread.sleep(100);
		}
	}
	
}
