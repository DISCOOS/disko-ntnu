package org.redcross.sar.map.tool;

import javax.swing.JToggleButton;



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
	public boolean isShowDrawFrame();
	public boolean isWorkPoolMode();
	
	public void setSnapToMode(boolean isSnapToMode);
	public void setDrawMode(DrawMode mode);
	public void setConstrainMode(boolean isConstrainMode);
	public void setBatchUpdate(boolean isBatchUpdate);	
	public void setShowDrawFrame(boolean isShowDrawFrame);
	public boolean setWorkPoolMode(boolean isWorkPoolMode);
	
	public int getMaxStep();
	public void setMaxStep(int distance);
	
	public int getMinStep();
	public void setMinStep(int distance);	
	
	public FeatureType getFeatureType();
	public boolean isInterchangable(FeatureType type);
	
}
