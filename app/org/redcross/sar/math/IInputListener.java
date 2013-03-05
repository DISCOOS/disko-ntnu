package org.redcross.sar.math;

import java.util.EventListener;

import javax.swing.event.ChangeEvent;

public interface IInputListener extends EventListener {

	public void onInputChanged(ChangeEvent e);

}
