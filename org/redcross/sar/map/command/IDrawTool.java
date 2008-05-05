package org.redcross.sar.map.command;

import org.redcross.sar.map.DrawAdapter;
import org.redcross.sar.map.SnapAdapter;

public interface IDrawTool extends IDiskoTool {
	
	public enum FeatureType {
		FEATURE_POINT,
		FEATURE_POLYLINE,
		FEATURE_POLYGON
	}

	public enum DrawMode {
		MODE_UNDEFINED,
		MODE_CREATE,
		MODE_REPLACE,
		MODE_APPEND,
		MODE_CONTINUE,
		MODE_DELETE,
		MODE_SNAPTO
	}	
	
	public SnapAdapter getSnapAdapter();
	
	public DrawAdapter getDrawAdapter();
	
	public void reset();

	public boolean apply();
	public boolean cancel();
	
	public boolean isDrawing();
	public boolean isSnapToMode();
	public boolean isCreateMode();
	public boolean isReplaceMode();
	public boolean isAppendMode();
	public boolean isContinueMode();
	public boolean isConstrainMode();
	//public boolean isBufferedMode();
	public boolean isBatchUpdate();
	public boolean isDirty();
	
	public void setSnapToMode(boolean isSnapToMode);
	public void setDrawMode(DrawMode mode);
	public void setConstrainMode(boolean isConstrainMode);
	//public void setBufferedMode(boolean isBufferedMode);
	public void setBatchUpdate(boolean isBatchUpdate);	
	
	public int getMaxStep();
	public void setMaxStep(int distance);
	
	public int getMinStep();
	public void setMinStep(int distance);	
	
	public FeatureType getFeatureType();
	
}
