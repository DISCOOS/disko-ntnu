package org.redcross.sar.map.event;

import java.util.EventListener;

public interface IProgressorListener extends EventListener {
	public void onShow();
	public void onChange();
	public void onHide();
}
