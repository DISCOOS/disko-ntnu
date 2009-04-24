package org.redcross.sar.map.element;

import org.redcross.sar.data.IData;
import org.redcross.sar.map.MapUtil;

import com.esri.arcgis.carto.CircleElement;
import com.esri.arcgis.carto.MarkerElement;
import com.esri.arcgis.display.esriSimpleFillStyle;
import com.esri.arcgis.display.esriSimpleLineStyle;
import com.esri.arcgis.display.esriSimpleMarkerStyle;
import com.esri.arcgis.geometry.IPoint;

public class PositionElement<D extends IData, G extends IData> extends AbstractGroupElement<D,G> {

	private static final long serialVersionUID = 1L;

	private int style;
	private double radius = 50.0; 			// in meters

	private IPoint point = null;

	private MarkerElement markerElement;
	private CircleElement circleElement;
	
	/* =============================================================
	 * Constructors
	 * ============================================================= */

	public PositionElement(Object id, D dataObj, IGeodataCreator<D,G> creator) {

		// forward
		this(id,dataObj,creator,esriSimpleMarkerStyle.esriSMSDiamond);

	}

	public PositionElement(Object id, D dataObj, IGeodataCreator<D,G> creator, int style) {

		// forward
		super(id,dataObj,creator);

		// prepare
		this.style = style;

		// initialize elements
		initialize();

	}
	
	/* =============================================================
	 * Public methods
	 * ============================================================= */
	
	public double getRadius() {
		return radius;
	}

	public void setRadius(double r) {
		radius = r;
		try {
			getCircleElement().setGeometry(MapUtil.createCircle(point, radius));
		} catch (Exception e) {
			logger.error("Failed to set radius",e);
		}
	}

	public IPoint getPoint() {
		return point;
	}

	public void setPoint(IPoint p) {
		point = p;
		try {
			getMarkerElement().setGeometry(p);
			getCircleElement().setGeometry(MapUtil.createCircle(p, radius));
		} catch (Exception e) {
			logger.error("Failed to set point",e);
		}
	}	

	/* =============================================================
	 * Helper methods
	 * ============================================================= */
	
	private void initialize() {

		// initialize point
		point = MapUtil.createPoint();

		try {
			// add elements to group element
			getElementImpl().addElement(getMarkerElement());
			getElementImpl().addElement(getCircleElement());
		} catch (Exception e) {
			logger.error("Failed to initialize PointElement",e);
		}

	}

	private CircleElement getCircleElement() {
		if(circleElement==null) {
			try {
				// create frame element
				circleElement = new CircleElement();
				// set symbol
				circleElement.setSymbol(MapUtil.getFillSymbol(esriSimpleFillStyle.esriSFSHollow, esriSimpleLineStyle.esriSLSSolid));
				// initialize geometry
				circleElement.setGeometry(MapUtil.createCircle(point,radius));
			} catch (Exception e) {
				logger.error("Failed to create CircleElement",e);
			}
		}
		return circleElement;
	}

	private MarkerElement getMarkerElement() {
		if(markerElement==null) {
			try {
				// create frame element
				markerElement = new MarkerElement();
				// set symbol
				markerElement.setSymbol(MapUtil.getMarkerSymbol(0,255,0,style,false));
				// initialize geometry
				markerElement.setGeometry(point);
			} catch (Exception e) {
				logger.error("Failed to create MarkerElement",e);
			}
		}
		return markerElement;
	}

}