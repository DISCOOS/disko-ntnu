package org.redcross.sar.thread;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.redcross.sar.gui.factory.DiskoStringFactory;

import com.esri.arcgis.system.IStepProgressor;

public class DiskoMapProgressor implements IStepProgressor {

	private static final long serialVersionUID = 1L;
	private static final String PROGRESS_DEFAULT_MAP_TEXT = "PROGRESS.DEFAULT.MAP";
	
	private boolean isActive = false;
	private JProgressBar bar = null;
	private List<ProgressorListener> listeners = null;
	
	public DiskoMapProgressor()  {
		this.bar = new JProgressBar();
		this.listeners = new ArrayList<ProgressorListener>();
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
					bar.setMaximum(max);
					bar.setIndeterminate(bar.getMinimum() == max);
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
					bar.setMaximum(min);
					bar.setIndeterminate(bar.getMaximum() == min);
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
					bar.setValue(value);
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
			bar.setString(message);
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// prepare
					bar.setString(message);
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
			// firwar
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

	public boolean addListener(ProgressorListener listener) {
		if(!listeners.contains(listener))
			return listeners.add(listener);
		return false;
	}
	
	public boolean removeListener(ProgressorListener listener) {
		if(listeners.contains(listener))
			return listeners.remove(listener);
		return false;
	}
	
	private void fireOnShow() {
		if(SwingUtilities.isEventDispatchThread()) {
			for (ProgressorListener it : listeners)
				it.onShow();
			
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					for (ProgressorListener it : listeners)
						it.onShow();
				}
			});		
		}
	}
	
	private void fireOnChange() {
		if(SwingUtilities.isEventDispatchThread()) {
			for (ProgressorListener it : listeners)
				it.onChange();
			
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					for (ProgressorListener it : listeners)
						it.onChange();
				}
			});
		}
	}
	
	private void fireOnHide() {
		if(SwingUtilities.isEventDispatchThread()) {
			for (ProgressorListener it : listeners)
				it.onHide();			
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					for (ProgressorListener it : listeners)
						it.onHide();
				}
			});		
		}
	}
	
	public interface ProgressorListener {
		public void onShow();
		public void onChange();
		public void onHide();
	}

}
