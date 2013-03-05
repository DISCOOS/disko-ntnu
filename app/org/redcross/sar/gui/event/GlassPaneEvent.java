package org.redcross.sar.gui.event;

import java.util.EventObject;

import org.redcross.sar.gui.DiskoGlassPane;

public class GlassPaneEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public static int FOCUS_CHANGED = 0;
	public static int MOUSE_CHANGED = 1;
	public static int LOCK_CHANGED = 2;

	private int m_type;
	private EventObject m_event;

	public GlassPaneEvent(DiskoGlassPane source, EventObject event, int type) {
		super(source);
		m_type = type;
		m_event = event;
	}

	@Override
	public DiskoGlassPane getSource() {
		return (DiskoGlassPane)super.getSource();
	}

	public int getType() {
		return m_type;
	}

	public EventObject getEvent() {
		return m_event;
	}

}
