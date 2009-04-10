package org.redcross.sar.map.layer;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.redcross.sar.ds.ete.RouteCost;
import org.redcross.sar.ds.ete.RouteCostEstimator;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.element.PositionElement;
import org.redcross.sar.util.mso.GeoPos;

import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.interop.AutomationException;

public class EstimatedPositionLayer extends AbstractDsLayer<RouteCost> {

	private static final long serialVersionUID = 1L;

	/**
	 * Costs
	 */
	private final Map<RouteCost,PositionElement> costs
		= new HashMap<RouteCost, PositionElement>();

	/* =======================================================
	 * Constructor
	 * ======================================================= */

	public EstimatedPositionLayer(ISpatialReference srs) throws Exception {

		// forward
		super(RouteCostEstimator.class,"ESTIMATEDPOSITIONLAYER",LayerCode.ESTIMATED_POSITION_LAYER,srs);

		// prepare
		setMinimumScale(50000);
		setRefreshRate(10000);

	}

	/* =======================================================
	 * Required methods
	 * ======================================================= */

	protected void clear() {
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

	protected IEnvelope add(RouteCost cost) {
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

	protected IEnvelope update(RouteCost cost) {
		PositionElement p = getFeature(cost);
		if(p!=null) {
			return setPosition(p,cost);

		}
		return null;
	}

	protected IEnvelope remove(RouteCost cost) {
		PositionElement p = getFeature(cost);
		IEnvelope e = getExtent(p);
		removeFeature(p);
		costs.remove(cost);
		return e;
	}

	/* =======================================================
	 * Helper methods
	 * ======================================================= */

	private PositionElement getFeature(RouteCost cost) {
		return costs.get(cost);
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


}