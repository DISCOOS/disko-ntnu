package org.redcross.sar.map.layer;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.redcross.sar.ds.ete.RouteCost;
import org.redcross.sar.ds.ete.RouteCostEstimator;
import org.redcross.sar.ds.event.IDsUpdateListenerIf;
import org.redcross.sar.ds.event.DsEvent.Update;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.element.PositionElement;
import org.redcross.sar.util.mso.GeoPos;

import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.interop.AutomationException;

public class EstimatedPositionLayer extends AbstractDiskoLayer implements IDsUpdateListenerIf {
	
	private static final long serialVersionUID = 1L;
	
	private RouteCostEstimator estimator = null;
	private Map<RouteCost,PositionElement> costs = null;

	public EstimatedPositionLayer(ISpatialReference srs) throws UnknownHostException, IOException {

		// forward
		super("ESTIMATEDPOSITIONLAYER",
				LayerCode.ESTIMATED_POSITION_LAYER,srs);
		
		// prepare
		costs = new HashMap<RouteCost, PositionElement>();
		setMinimumScale(50000);
		setRefreshRate(10000);
		
	}

	public void handleDsUpdateEvent(Update e) {		
		try {
			Object[] data;
			IEnvelope extent = null;
			switch(e.getType()) {
			case ADDED_EVENT:
				data = e.getData();
				for(int i=0;i<data.length;i++) {
					IEnvelope added = addCost((RouteCost)data[i]);
					if(added!=null && !added.isEmpty()) {
						if(extent==null)
							extent = added;
						else
							extent.union(added);
					}
				}
				break;
			case MODIFIED_EVENT:
				data = e.getData();
				for(int i=0;i<data.length;i++) {
					IEnvelope updated = update((RouteCost)data[i]);
					if(updated!=null && !updated.isEmpty()) {
						if(extent==null)
							extent = updated;
						else
							extent.union(updated);
					}
				}
				
				break;
			case REMOVED_EVENT:
				data = e.getData();
				for(int i=0;i<data.length;i++) {
					IEnvelope removed = removeCost((RouteCost)data[i]);
					if(removed!=null && !removed.isEmpty()) {
						if(extent==null)
							extent = removed;
						else
							extent.union(removed);
					}
				}
				break;
			}
			// forward
			refresh(extent);
			
		} catch (AutomationException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}
	
	public void setEstimator(RouteCostEstimator estimator) {
		clear();
		if(this.estimator!=null)
			this.estimator.removeUpdateListener(this);
		this.estimator = estimator;
		if(estimator!=null) {
			estimator.addUpdateListener(this);
			for(RouteCost it : estimator.getCosts().values()) 
				addCost(it);
		}
	}
	
	private void clear() {
		try {
			deleteAllElements();
			features.clear();
			costs.clear();
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private PositionElement getFeature(RouteCost cost) {
		return costs.get(cost);
	}
	
	private IEnvelope addCost(RouteCost cost) {
		try {
			PositionElement p = new PositionElement();
			addFeature(p);
			setPosition(p, cost);
			costs.put(cost, p);
			return getExtent(p);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private IEnvelope setPosition(PositionElement p, RouteCost cost) {
		GeoPos ecp = cost.ecp();
		if(ecp!=null) {
			try {
				IEnvelope extent = getExtent(p);
				p.setPoint(MapUtil.getEsriPoint(ecp.getPosition(), srs));
				extent.union(getExtent(p));
				return extent;
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private IEnvelope update(RouteCost cost) {
		PositionElement p = getFeature(cost);
		if(p!=null) {
			return setPosition(p,cost);
			
		}
		return null;
	}
	
	private IEnvelope removeCost(RouteCost cost) {
		PositionElement p = getFeature(cost);
		IEnvelope e = getExtent(p);
		removeFeature(p);
		costs.remove(cost);
		return e;
	}

}