package org.redcross.sar.map.layer;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.redcross.sar.data.Selector;
import org.redcross.sar.map.event.IMsoLayerEventListener;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.ChangeList;

import com.esri.arcgis.carto.IFeatureLayer;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.interop.AutomationException;

public interface IMsoFeatureLayer extends IMapLayer<IMsoObjectIf,IMsoFeature>, IFeatureLayer, IMsoUpdateListenerIf {

	public enum LayerCode {
		AREA_LAYER,
		ROUTE_LAYER,
		TRACK_LAYER,
		OPERATION_AREA_LAYER,
		SEARCH_AREA_LAYER,
		POI_LAYER,
		FLANK_LAYER,
		OPERATION_AREA_MASK_LAYER,
		UNIT_LAYER
    }
	
	public MsoClassCode getClassCode();

	public LayerCode getLayerCode();

	public void setSelected(IMsoFeature msoFeature, boolean selected);

	public int clearSelected() throws AutomationException, IOException;

	public List<IMsoFeature> getSelectedFeatures() throws AutomationException, IOException;

	public List<IMsoObjectIf> getSelectedMsoObjects() throws AutomationException, IOException;

	public int getSelectionCount(boolean update) throws IOException, AutomationException;

	public void addMsoLayerEventListener(IMsoLayerEventListener listener);

	public void removeMsoLayerEventListener(IMsoLayerEventListener listener);

	public boolean isDirty();

	public boolean isDirty(boolean checkAll);

	public boolean isEnabled();

	public void setEnabled(boolean isEnabled);

	public void suspendNotify();
	public void consumeNotify();
	public void resumeNotify();
	public boolean isNotifySuspended();

	public IEnvelope getVisibleFeaturesExtent();

	public void setVisibleFeatures(boolean isVisible);
	public void setVisibleFeatures(IMsoObjectIf msoObj, boolean match, boolean others);
	public void setVisibleFeatures(List<IMsoObjectIf> msoObjs, boolean match, boolean others);
	public int getVisibleFeatureCount(boolean update) throws AutomationException, IOException;

	public int getFeatureCount();
	public IMsoFeature getFeature(int index);
	public IMsoFeature getFeature(IMsoObjectIf msoObj) throws AutomationException, IOException;

	public Selector<IMsoObjectIf> getSelector(int id);
	public boolean addSelector(Selector<IMsoObjectIf> selector, int id);
	public boolean removeSelector(int id);

	public boolean isTextShown();
	public void setTextShown(boolean isVisible);

	public IEnvelope getDirtyExtent() throws AutomationException, IOException;

	public Collection<IMsoFeature> load(Collection<IMsoObjectIf> msoObjs);

	public boolean removeAll();

	public Collection<IMsoObjectIf> getGeodataMsoObjects(IMsoObjectIf msoObject);

	public Collection<IMsoFeature> processMsoUpdateEvent(ChangeList events);




}
