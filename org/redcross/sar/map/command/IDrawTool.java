package org.redcross.sar.map.command;

import javax.swing.JToggleButton;

import org.redcross.sar.map.SnapAdapter;

public interface IDrawTool extends IDiskoTool {
	
	public enum FeatureType {
		FEATURE_POINT,
		FEATURE_POLYLINE,
		FEATURE_POLYGON
	}

	public enum DrawMode {
		MODE_UNDEFINED,
		MODE_LOCKED,
		MODE_CREATE,
		MODE_REPLACE,
		MODE_APPEND,
		MODE_CONTINUE,
		MODE_DELETE,
		MODE_SNAPTO
	}	
	
	public JToggleButton getButton();
	
	public SnapAdapter getSnapAdapter();
	
	public DrawAdapter getDrawAdapter();
	
	public boolean isDrawing();
	public boolean isSnapToMode();
	public boolean isCreateMode();
	public boolean isReplaceMode();
	public boolean isAppendMode();
	public boolean isContinueMode();
	public boolean isConstrainMode();
	public boolean isBatchUpdate();
	
	public void setSnapToMode(boolean isSnapToMode);
	public void setDrawMode(DrawMode mode);
	public void setConstrainMode(boolean isConstrainMode);
	public void setBatchUpdate(boolean isBatchUpdate);	
	
	public int getMaxStep();
	public void setMaxStep(int distance);
	
	public int getMinStep();
	public void setMinStep(int distance);	
	
	public FeatureType getFeatureType();
	public boolean isInterchangable(FeatureType type);
	
}
