package org.redcross.sar.ds.sc;

import java.util.Calendar;

public class Sample<T> {

	public final T m_data;
	public final Calendar m_time;

	/* ============================================================
	 * Constructors
	 * ============================================================ */

	public Sample(Calendar time, T data) {
		// prepare
		m_time = time;
		m_data = data;
	}

	/* ============================================================
	 * Public methods
	 * ============================================================ */

	public Calendar time() {
		return m_time;
	}

	public T data() {
		return m_data;
	}

}