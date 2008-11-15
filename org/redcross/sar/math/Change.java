package org.redcross.sar.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Calendar;
import java.util.Map;

public class Change<D extends Number> {

	private List<D> m_values;
	private Calendar m_time;
	private Map<String,Integer> m_names;

	public Change(String[] names, D[] values, Calendar time) {
		// build value list
		m_values = new ArrayList<D>(values.length);
		for(D it : values) {
			m_values.add(it);
		}
		// build name map
		m_names = new HashMap<String, Integer>(m_values.size());
		for(int i=0; i<names.length;i++) {
			m_names.put(names[i],i);
		}
		// set time
		m_time = time;
	}

	public List<D> getValues() {
		return new ArrayList<D>(m_values);
	}

	public List<D> getNames() {
		return new ArrayList<D>(m_values);
	}

	public Calendar getTime() {
		return (Calendar)m_time.clone();
	}

	public int size() {
		return m_values.size();
	}

	public D getValue(int index) {
		return m_values.get(index);
	}

	public D getValue(String name) {
		Integer index = m_names.get(name);
		if(index!=null) {
			return m_values.get(index);
		}
		return null;
	}

	public Sample<D> get(int index) {
		return new Sample<D>(getValue(index),getTime());
	}

	public Sample<D> get(String name) {
		Integer index = m_names.get(name);
		if(index!=null) {
			return new Sample<D>(getValue(index),getTime());
		}
		return null;
	}

}
