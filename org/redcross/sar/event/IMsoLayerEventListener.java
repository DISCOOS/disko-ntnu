package org.redcross.sar.event;

import java.io.IOException;
import java.util.EventListener;

import com.esri.arcgis.interop.AutomationException;

public interface IMsoLayerEventListener extends EventListener {
	
	public void onSelectionChanged(MsoLayerEvent e) throws IOException, AutomationException;
	
}
