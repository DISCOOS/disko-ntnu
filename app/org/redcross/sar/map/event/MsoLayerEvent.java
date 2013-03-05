package org.redcross.sar.map.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.layer.IMsoFeatureLayer.LayerCode;
import org.redcross.sar.mso.data.IMsoObjectIf;

import com.esri.arcgis.interop.AutomationException;

public class MsoLayerEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public enum MsoLayerEventType {
		SELECTED_EVENT,
		DESELECTED_EVENT
	}

	private boolean isFinal;
	private MsoLayerEventType eventType;
	private List<IMsoFeatureLayer> msoLayers;

	public MsoLayerEvent(Object source, MsoLayerEventType type) {
		super(source);
		this.isFinal = true;
		this.eventType = type;
		this.msoLayers = new ArrayList<IMsoFeatureLayer>();
	}

	public boolean isFinal() {
		return isFinal;
	}

	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	public MsoLayerEventType getEventType() {
		return eventType;
	}

	public List<IMsoFeatureLayer> getList() {
		return msoLayers;
	}

	public boolean add(IMsoFeatureLayer layer) {
		if(layer!=null) {
			if(!msoLayers.contains(layer))
					return msoLayers.add(layer);
		}
		return false;
	}

	public boolean contains(IMsoFeatureLayer layer) {
		return msoLayers.contains(layer);
	}

	public boolean contains(LayerCode code) {
		for(IMsoFeatureLayer it: msoLayers) {
			if(it.getLayerCode().equals(code)) return true;
		}
		return false;
	}

	public boolean remove(IMsoFeatureLayer layer) {
		if(layer!=null) {
			if(msoLayers.contains(layer))
					return msoLayers.remove(layer);
		}
		return false;
	}

	public List<IMsoFeature> getSelected() throws AutomationException, IOException {
		List<IMsoFeature> list = null;
		// loop over all layers
		for(IMsoFeatureLayer it: msoLayers) {
			// initialize?
			if(list==null)
				list = it.getSelectedFeatures();
			else
				list.addAll(it.getSelectedFeatures());
		}
		// finished
		return list;
	}

	public List<IMsoObjectIf> getSelectedMsoObjects() throws AutomationException, IOException {
		List<IMsoObjectIf> list = null;
		// loop over all layers
		for(IMsoFeatureLayer it: msoLayers) {
			// initialize?
			if(list==null)
				list = (List<IMsoObjectIf>)it.getSelectedMsoObjects();
			else
				list.addAll(it.getSelectedMsoObjects());
		}
		// finished
		return list;

	}

}
