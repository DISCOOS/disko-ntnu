package org.redcross.sar.map;

import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.redcross.sar.Application;
import org.redcross.sar.gui.DiskoProgressPanel.ProgressStyleType;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.ProgressDialog;
import org.redcross.sar.gui.factory.DiskoStringFactory;
import org.redcross.sar.map.event.IProgressorListener;
import org.redcross.sar.util.Utils;

import com.esri.arcgis.system.IStepProgressor;

public class Progressor implements IStepProgressor {

	private static final long serialVersionUID = 1L;
	private static final String PROGRESS_DEFAULT_MAP_TEXT = "PROGRESS.DEFAULT.MAP";

	private boolean isActive = false;
	private JProgressBar bar;
	private EventListenerList listeners;
	private ProgressDialog progressDialog;

	public Progressor(Component locationAt)  {
		this.progressDialog = new ProgressDialog(Application.getInstance(),false,ProgressStyleType.ICON_STYLE);
		this.progressDialog.setTrancluent(true);
		this.progressDialog.setSnapToLocation(locationAt, DefaultDialog.POS_CENTER, 0, true, false);
		this.bar = progressDialog.getProgressPanel().getProgressBar();
		this.listeners = new EventListenerList();
		this.bar.setString(DiskoStringFactory.getText(PROGRESS_DEFAULT_MAP_TEXT));
		this.bar.setIndeterminate(true);
	}


	public JProgressBar getProgressBar() {
		return bar;
	}

	public int getMaxRange() {
		return bar.getMaximum();
	}

	public void setMaxRange(final int max) {
		if(SwingUtilities.isEventDispatchThread()) {
			// prepare
			bar.setMaximum(max);
			bar.setIndeterminate(bar.getMinimum() == max);
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setMaxRange(max);
				}
			});
		}
	}

	public int getMinRange() {
		return bar.getMinimum();
	}

	public void setMinRange(final int min) {
		if(SwingUtilities.isEventDispatchThread()) {
			// prepare
			bar.setMaximum(min);
			bar.setIndeterminate(bar.getMaximum() == min);
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setMinRange(min);
				}
			});
		}
	}

	public int getStepValue() {
		return bar.getValue();
	}

	public void setStepValue(final int value) {
		if(SwingUtilities.isEventDispatchThread()) {
			// prepare
			bar.setValue(value);
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setStepValue(value);
				}
			});
		}
	}

	public String getMessage() {
		return bar.getString();
	}

	public void setMessage(final String message) {
		if(SwingUtilities.isEventDispatchThread()) {
			// prepare
			progressDialog.getProgressPanel().setProgress(bar.getValue(), message, message);
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setMessage(message);
				}
			});
		}
	}


	public void show() {
		try {
			// is already active?
			if(isActive) return;
			// set flag
			isActive = true;
			// notify
			fireOnShow();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void step() {
		try {
			// forward
			if(isActive) fireOnChange();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void hide() {
		try {
			// is already inactive?
			if(!isActive) return;
			// reset flag
			isActive = false;
			// notify
			fireOnHide();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setPosition(int position) { }
	public int getPosition() { return 0; }
	public int offsetPosition(int position) { return 0; }

	public void addProgressorListener(IProgressorListener listener) {
		listeners.add(IProgressorListener.class,listener);
	}

	public void removeProgressorListener(IProgressorListener listener) {
		listeners.remove(IProgressorListener.class,listener);
	}

	private void fireOnShow() {
		if(SwingUtilities.isEventDispatchThread()) {
			progressDialog.setVisible(true);
			progressDialog.snapTo();
			for (IProgressorListener it : listeners.getListeners(IProgressorListener.class))
				it.onShow();
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireOnShow();
				}
			});
		}
	}

	private void fireOnChange() {
		if(SwingUtilities.isEventDispatchThread()) {
			for (IProgressorListener it : listeners.getListeners(IProgressorListener.class))
				it.onChange();

		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireOnChange();
				}
			});
		}
	}

	private void fireOnHide() {
		if(SwingUtilities.isEventDispatchThread()) {
			for (IProgressorListener it : listeners.getListeners(IProgressorListener.class)) {
				it.onHide();
			}
			progressDialog.setVisible(false);
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireOnHide();
				}
			});
		}
	}

}
