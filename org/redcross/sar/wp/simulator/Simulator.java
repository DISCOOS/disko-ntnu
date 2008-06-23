package org.redcross.sar.wp.simulator;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.redcross.sar.thread.AbstractDiskoWork;

public class Simulator extends AbstractDiskoWork<Boolean> {

	public Simulator() throws Exception {
		// forward
		super(true,false,WorkOnThreadType.WORK_ON_NEW,
				"Simulerer",0,false,false,true,1000);
	}
		
	@Override
	public Boolean doWork() {
	
		// ensure concurrent updates of simulator
		synchronized(this) {
			/*
			SimpleDateFormat format = new SimpleDateFormat("hh:MM:ss");
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(System.currentTimeMillis());
			System.out.println("Simulerer:: " + format.format(c.getTime()));
			*/
		}
		
		// finished
		return true;
	}

}
