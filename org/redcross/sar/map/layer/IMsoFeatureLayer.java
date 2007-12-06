package org.redcross.sar.map.layer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.redcross.sar.event.IMsoLayerEventListener;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.util.mso.Selector;

import com.esri.arcgis.carto.IFeatureLayer;
import com.esri.arcgis.interop.AutomationException;

public interface IMsoFeatureLayer extends IFeatureLayer {
	
	public enum LayerCode {
		AREA_LAYER,
		ROUTE_LAYER,
		OPERATION_AREA_LAYER,
		SEARCH_AREA_LAYER,
		POI_LAYER,
		FLANK_LAYER,
		OPERATION_AREA_MASK_LAYER,
		UNIT_LAYER
    }
	
	public IMsoManagerIf.MsoClassCode getClassCode();
	
	public IMsoFeatureLayer.LayerCode getLayerCode();
	
	public void setSelected(IMsoFeature msoFeature, boolean selected);
	
	public int clearSelected() throws AutomationException, IOException;
	
	public List getSelected() throws AutomationException, IOException;
	
	public List getSelectedMsoObjects() throws AutomationException, IOException;
	
	public int getSelectionCount(boolean update) throws IOException, AutomationException;
	
	public void addDiskoLayerEventListener(IMsoLayerEventListener listener);
	
	public void removeDiskoLayerEventListener(IMsoLayerEventListener listener);
	
	public IMsoModelIf getMsoModel();
	
	public boolean isDirty();
	
	/*
	public List startEdit(IMsoObjectIf msoObject) throws IOException, AutomationException;
	
	public List stopEdit(IMsoObjectIf msoObject) throws IOException, AutomationException;
	
	public int getEditCount(boolean update) throws IOException, AutomationException;
	
	public ArrayList<IMsoFeature> getEditing() throws AutomationException, IOException;
	
	public boolean isEditing();
	*/
	
	public boolean isEnabled();
	
	public void setEnabled(boolean isEnabled);
	
	public void suspendNotify();	
	
	public void resumeNotify();	
	
	public boolean isNotifySuspended();
	
	public void setVisibleFeatures(boolean isVisible);
	
	public void setVisibleFeatures(IMsoObjectIf msoObj, boolean match, boolean others);
	
	public void setVisibleFeatures(List msoObjs, boolean match, boolean others);
	
	public int getVisibleFeatureCount(boolean update) throws AutomationException, IOException;
	
	public int getFeatureCount() throws AutomationException, IOException;

	public IMsoFeature getFeature(int index) throws AutomationException, IOException;	

	public Selector<IMsoObjectIf> getFilter();
	
	public void setFilter(Selector<IMsoObjectIf> selector);
	
	public boolean isTextShown();
	
	public void setTextShown(boolean isVisible);
	
}
