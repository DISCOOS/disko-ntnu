package org.redcross.sar.map.command;

import org.redcross.sar.map.SnappingAdapter;

public interface IDrawTool extends IDiskoTool {
	
	public enum DrawFeatureType {
		DRAW_FEATURE_POINT,
		DRAW_FEATURE_POLYLINE,
		DRAW_FEATURE_POLYGON
	}
	
	public SnappingAdapter getSnappingAdapter();
	
	public void setSnappingAdapter(SnappingAdapter adapter);

	public boolean apply();
	
	public boolean cancel();
	
	public boolean isDrawing();
	public boolean isSnapToMode();
	public boolean isUpdateMode();
	public boolean isBuffered();
	public boolean isConstrainMode();
	
	public void setSnapToMode(boolean isSnapToMode);
	public void setUpdateMode(boolean isUpdateMode);
	public void setBuffered(boolean isBuffered);
	public void setConstrainMode(boolean isConstrainMode);
	
	public int getMaxStep();
	public void setMaxStep(int distance);
	
	public int getMinStep();
	public void setMinStep(int distance);	
	
	public DrawFeatureType getFeatureType();
	
}
