package org.redcross.sar.ds.advisor.event;

import java.util.EventListener;

import javax.swing.event.ChangeEvent;

public interface IInputListener extends EventListener {

	public void onInputChanged(ChangeEvent e);

}
