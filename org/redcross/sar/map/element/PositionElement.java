package org.redcross.sar.map.element;

import java.io.IOException;
import java.net.UnknownHostException;

import org.redcross.sar.map.MapUtil;

import com.esri.arcgis.carto.CircleElement;
import com.esri.arcgis.carto.MarkerElement;
import com.esri.arcgis.display.esriSimpleFillStyle;
import com.esri.arcgis.display.esriSimpleLineStyle;
import com.esri.arcgis.display.esriSimpleMarkerStyle;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.interop.AutomationException;

public class PositionElement extends AbstractMapFeature {

	private static final long serialVersionUID = 1L;

	private int style;
	private double radius = 50.0; 			// in meters

	private IPoint point = null;

	private MarkerElement markerElement;
	private CircleElement circleElement;

	public PositionElement() throws IOException, UnknownHostException {

		// forward
		this(esriSimpleMarkerStyle.esriSMSDiamond);

	}

	public PositionElement(int style) throws IOException, UnknownHostException {

		// forward
		super();

		// prepare
		this.style = style;

		// initialize elements
		initialize();

	}

	private void initialize() throws UnknownHostException, IOException {

		// initialize point
		point = MapUtil.createPoint();

		// add elements to group element
		addElement(getMarkerElement());
		addElement(getCircleElement());

	}

	private CircleElement getCircleElement() throws UnknownHostException, IOException  {
		if(circleElement==null) {
			// create frame element
			circleElement = new CircleElement();
			// set symbol
			circleElement.setSymbol(MapUtil.getFillSymbol(esriSimpleFillStyle.esriSFSHollow, esriSimpleLineStyle.esriSLSSolid));
			// initialize geometry
			circleElement.setGeometry(MapUtil.createCircle(point,radius));
		}
		return circleElement;
	}

	private MarkerElement getMarkerElement() throws UnknownHostException, IOException  {
		if(markerElement==null) {
			// create frame element
			markerElement = new MarkerElement();
			// set symbol
			markerElement.setSymbol(MapUtil.getMarkerSymbol(0,255,0,style,false));
			// initialize geometry
			markerElement.setGeometry(point);
		}
		return markerElement;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double r) {
		radius = r;
		try {
			getCircleElement().setGeometry(MapUtil.createCircle(point, radius));
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}