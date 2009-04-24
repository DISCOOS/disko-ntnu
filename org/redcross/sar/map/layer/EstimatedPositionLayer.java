package org.redcross.sar.map.layer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.redcross.sar.ds.ete.RouteCost;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.element.IGeodataCreator;
import org.redcross.sar.map.element.IMapElement;
import org.redcross.sar.map.element.PositionElement;
import org.redcross.sar.map.event.MapLayerEventStack;
import org.redcross.sar.map.layer.IDsObjectLayer.LayerCode;
import org.redcross.sar.util.mso.GeoPos;

import com.esri.arcgis.carto.IElement;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.ISpatialReference;

public class EstimatedPositionLayer extends AbstractCompositeGraphicsLayer<RouteCost, GeoPos> {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(EstimatedPositionLayer.class);
	
	/* =======================================================
	 * Constructor
	 * ======================================================= */

	public EstimatedPositionLayer(
			MapLayerEventStack eventStack,
			ISpatialReference srs) {

		// forward
		super("ESTIMATEDPOSITIONLAYER",null,LayerCode.ESTIMATED_POSITION_LAYER,srs,eventStack);

		// prepare
		try {
			getLayerImpl().setMinimumScale(50000);
		} catch (Exception e) {
			logger.error("Failed to set minimum scale",e);
		}
		setRefreshRate(10000);

	}

	/* =======================================================
	 * IMapLayer implementation
	 * ======================================================= */

	public List<GeoPos> getGeodataObjects(RouteCost cost) {
		List<GeoPos> geodata = new ArrayList<GeoPos>(1);
		geodata.add(cost.ecp());
		return geodata;
	}	
	
	/* =======================================================
	 * protected methods
	 * ======================================================= */

	protected IMapElement<IElement, RouteCost, GeoPos> createElementImpl(RouteCost dataObj) {		
		return new PositionElement<RouteCost, GeoPos>(dataObj.getId(),dataObj,new GeoPosCreator());
	}

	protected IEnvelope updateElementImpl(IMapElement<IElement, RouteCost, GeoPos> element, List<GeoPos> geodata) {
		if(element instanceof PositionElement) {
			// has geodate data?
			if(geodata.size()==1) {
				try {
					PositionElement<RouteCost, GeoPos> impl = (PositionElement<RouteCost, GeoPos>)element;
					IEnvelope extent = getExtent((IElement)element);
					impl.setPoint(MapUtil.getEsriPoint(geodata.get(0), srs));
					extent.union(getExtent((IElement)element));
					return extent;
				} catch (Exception e) {
					logger.error("Failed to set position",e);
				}
			}
		}
		return null;
	}

	
	
	/* =======================================================
	 * Helper methods
	 * ======================================================= */
	
	private class GeoPosCreator implements IGeodataCreator<RouteCost, GeoPos> {

		RouteCost cost;
		List<GeoPos> geodata;		
		
		public List<GeoPos> create(RouteCost dataObj) {
			cost = dataObj;
			return getGeodataObjects();
		}

		public List<GeoPos> getGeodataObjects() {
			// collect geodata
			List<GeoPos> current = EstimatedPositionLayer.this.getGeodataObjects(cost);
			// update?
			if(!isEqualTo(current)) geodata = current;
			// finished
			return geodata;
		}

		public int getGeodataObjectsCount() {
			return geodata!=null ? geodata.size() : 0;
		}

		public boolean isChanged() {
			// collect geodata
			List<GeoPos> current = EstimatedPositionLayer.this.getGeodataObjects(cost);
			// compare
			return isEqualTo(current);
		}
		
		public boolean isEqualTo(List<GeoPos> list) {
			if(geodata!=null && geodata!=list) {
				if(geodata.size()>0&&geodata.size()==list.size()) {
					return geodata.get(0).equals(list.get(0));
				}
				return false;
			}
			return false;
		}
		
	}



	@Override
	public Collection<IMapElement<IElement, RouteCost, GeoPos>> load(
			Collection<RouteCost> dataObjs) {
		// TODO Auto-generated method stub
		return null;
	}

}