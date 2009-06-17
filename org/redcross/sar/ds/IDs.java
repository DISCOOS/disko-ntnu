package org.redcross.sar.ds;

import java.util.List;

import org.redcross.sar.IService;
import org.redcross.sar.data.IDataSource;
import org.redcross.sar.ds.event.DsEvent;
import org.redcross.sar.ds.event.IDsChangeListener;

public interface IDs<T extends IDsObject> extends IDataSource<DsEvent.Update>, IService {

	/**
	 * Decision support (DS) class codes
	 * 
	 * @author kenneth
	 *
	 */
	public enum DsClassCode {
		/**
		 * Decision cue class code. Decision cues are used
		 * to evaluate which decision to make.
		 */
		CLASSCODE_CUE,
		/**
		 * Decision class code. Decision objects define
		 * a proposed or made decision.
		 */
		CLASSCODE_DECISION
	}	
	
	/**
	 * Decision support data types.
	 * 
	 * @author kenneth
	 *
	 */
	public enum DsDataType {
		/**
		 * No data type defined.
		 */
		NONE,
		/**
		 * Intrinsic data type java.lang.Integer 
		 */
		INTEGER,
		/**
		 * Intrinsic data type java.lang.Double 
		 */
		DOUBLE,
		/**
		 * Intrinsic data type java.lang.Long 
		 */
		LONG,
	}
	
	public Object getID();

	public Class<T> getDataClass();
	public boolean isSupported(Class<?> dataClass);

	public List<T> getItems();

	public void addChangeListener(IDsChangeListener listener);
	public void removeChangeListener(IDsChangeListener listener);

}