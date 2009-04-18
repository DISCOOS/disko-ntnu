package org.redcross.sar.map.layer;

import java.io.IOException;

import com.esri.arcgis.interop.AutomationException;

public interface IMapLayer {

	public enum LayerCode {
		AREA_LAYER,
		ROUTE_LAYER,
		TRACK_LAYER,
		OPERATION_AREA_LAYER,
		SEARCH_AREA_LAYER,
		POI_LAYER,
		FLANK_LAYER,
		OPERATION_AREA_MASK_LAYER,
		UNIT_LAYER,
		ESTIMATED_POSITION_LAYER
    }
	
	public LayerCode getLayerCode();
	
	public boolean isVisible() throws IOException, AutomationException;
	public void setVisible(boolean isVisible) throws IOException, AutomationException;
	
	public int getRefreshRate();
	public void setRefreshRate(int inMillis);
		
}
