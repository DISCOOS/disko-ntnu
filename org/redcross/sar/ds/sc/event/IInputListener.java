package org.redcross.sar.ds.sc.event;

import java.util.EventListener;

import javax.swing.event.ChangeEvent;

public interface IInputListener extends EventListener {

	public void onInputChanged(ChangeEvent e);

}
