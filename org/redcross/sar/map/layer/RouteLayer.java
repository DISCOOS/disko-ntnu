package org.redcross.sar.map.layer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.esri.arcgis.display.*;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.Polyline;
import com.esri.arcgis.geometry.esriGeometryType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.ITrackCancel;

import org.redcross.sar.map.IDiskoMapManager;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.event.MsoLayerEventStack;
import org.redcross.sar.map.feature.RouteFeature;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.ISearchIf.SearchSubType;
import org.redcross.sar.mso.util.MsoUtils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Hashtable;

public class RouteLayer extends AbstractMsoFeatureLayer {

	private static final long serialVersionUID = 1L;
	private static final double FONT_SIZE = 8;
	private static final double FONT_OFFSET = 5;
	private static final double LINE_WIDTH = 1.5;
	private static final double REFERENCE_SCALE = 50000;

	private RgbColor disabledColor = null;
	private RgbColor selectionColor = null;
	private RgbColor plannedColor = null;
	private RgbColor executingColor = null;
	private RgbColor finishedColor = null;
	private Hashtable<SearchSubType, SimpleFillSymbol> symbols = null;
	private SimpleFillSymbol defaultSymbol = null;
	private TextSymbol textSymbol = null;

 	public RouteLayer(ISpatialReference srs, MsoLayerEventStack eventStack, IDiskoMapManager manager) {
 		super(esriGeometryType.esriGeometryPolyline, MsoClassCode.CLASSCODE_ROUTE,
 				EnumSet.of(MsoClassCode.CLASSCODE_ASSIGNMENT), LayerCode.ROUTE_LAYER,
 				srs, eventStack, manager);
 		symbols = new Hashtable<SearchSubType, SimpleFillSymbol>();
 		createSymbols();
	}

 	protected RouteFeature createMsoFeature(IMsoObjectIf msoObject)
 			throws IOException, AutomationException {
 		RouteFeature msoFeature = new RouteFeature();
 		msoFeature.setSpatialReference(srs);
 		msoFeature.setMsoObject(msoObject);
 		System.out.println("Created RouteFeature " + msoObject);
 		return msoFeature;
 	}

 	@Override
	public List<IMsoObjectIf> getGeodataMsoObjects(IMsoObjectIf msoObj) {
		IAreaIf area = null;
		List<IMsoObjectIf> objects = new ArrayList<IMsoObjectIf>(1);
		if (msoObj instanceof IAssignmentIf) {
			IAssignmentIf assignment = (IAssignmentIf)msoObj;
			area = assignment.getPlannedArea();
		}
		else if(msoObj instanceof IAreaIf) {
			area = (IAreaIf)msoObj;
		}
		if(area!=null) {
			// add all routes
			for(IMsoObjectIf it: area.getAreaGeodataItems()) {
				if(it instanceof IRouteIf)
					objects.add(it);
			}
		}
		else if (msoObj instanceof IRouteIf) {
			objects.add(msoObj);
		}
		return objects;
	}

	public void draw(int drawPhase, IDisplay display, ITrackCancel trackCancel)
			throws IOException, AutomationException {
		try {
			if (display == null || !this.isVisible) {
				return;
			}

			// get scale
			double scale = display.getDisplayTransformation().getScaleRatio();

			// get zoom ratio
			double zoomRatio = java.lang.Math.min(1.0,REFERENCE_SCALE / scale);

			// get text zoom size and offset
			double zoomFontSize = java.lang.Math.min(FONT_SIZE, FONT_SIZE*zoomRatio);
			double zoomFontOffset = java.lang.Math.min(FONT_OFFSET, FONT_OFFSET*zoomRatio);

			// get line zoom width
			double zoomLineWidth = java.lang.Math.min(LINE_WIDTH, LINE_WIDTH*zoomRatio);

			// update
			textSymbol.setSize(zoomFontSize);
			textSymbol.setYOffset(zoomFontOffset);

			for (int i = 0; i < featureClass.featureCount(null); i++) {
				RouteFeature feature = (RouteFeature)featureClass.getFeature(i);
				if(select(feature) && feature.isVisible()){
					// get finished color?
					IGeometry geom = feature.getShape();
					if (geom != null) {

						// initialize
						String text = null;
						SimpleFillSymbol fillSymbol = defaultSymbol;

						// update symbol
						ILineSymbol line = fillSymbol.getOutline();
						line.setWidth(zoomLineWidth);
						fillSymbol.setOutline(line);

						// save current colors
						IColor saveLineColor = fillSymbol.getColor();
						IColor saveTextColor = textSymbol.getColor();

						// get owning area
						IAreaIf area = feature.getOwningArea();

						// has owning area?
						if(area!=null) {
							// get assignment
							IAssignmentIf assignment = (IAssignmentIf)area.getOwningAssignment();
							// is search assignment?
							if (assignment instanceof ISearchIf) {
								// initialize
								RgbColor color = plannedColor;
								// get status
								Enum<?> status = MsoUtils.getStatus(assignment);
								// replace color?
								if(AssignmentStatus.EXECUTING.equals(status)) {
									color = executingColor;
								}
								else if(AssignmentStatus.FINISHED.equals(status)){
									color = finishedColor;
								}
								// cast to ISearchIf
								ISearchIf search = (ISearchIf)assignment;
								// get line symbol
								fillSymbol = symbols.get(search.getSubType());
								// update symbol
								line = fillSymbol.getOutline();
								line.setWidth(zoomLineWidth);
								line.setColor(color);
								fillSymbol.setOutline(line);
								// get text
								text = feature.getCaption();
							}
						}

						// is enabled?
						if(isEnabled) {
							// is selected
	 	 					if (feature.isSelected()){
								line = fillSymbol.getOutline();
								line.setColor(selectionColor);
								fillSymbol.setOutline(line);
								textSymbol.setColor(selectionColor);
	 	 					}

						}
						else {
							// disable all features
							line = fillSymbol.getOutline();
							line.setColor(disabledColor);
							fillSymbol.setOutline(line);
							textSymbol.setColor(disabledColor);
						}

						// draw route
						if (geom instanceof Polyline) {
							display.setSymbol((SimpleLineSymbol)fillSymbol.getOutline());
							display.drawPolyline(geom);
						}
						else {
							display.setSymbol(fillSymbol);
							display.drawPolygon(geom);
						}
						// draw text?
						if (text != null && isTextShown) {
							display.setSymbol(textSymbol);
							if (geom instanceof Polyline)
								display.drawText(geom, text);
							else
								display.drawText(MapUtil.getCenter(geom.getEnvelope()), text);
						}

						// restore states
						line = fillSymbol.getOutline();
						line.setColor(saveLineColor);
						fillSymbol.setOutline(line);
						textSymbol.setColor(saveTextColor);
					}
				}
				feature.setDirty(false);
			}
			setDirty(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createSymbols() {
		try {

			disabledColor = new RgbColor();
			disabledColor.setBlue(110);
			disabledColor.setGreen(110);
			disabledColor.setRed(110);

			selectionColor = new RgbColor();
			selectionColor.setBlue(255);
			selectionColor.setGreen(255);

			plannedColor = new RgbColor();
			plannedColor.setRed(255);

			executingColor = new RgbColor();
			executingColor.setRed(255);
			executingColor.setGreen(230);

			finishedColor = new RgbColor();
			finishedColor.setGreen(155);

			// create default symbol
			defaultSymbol = (SimpleFillSymbol)MapUtil.getFillSymbol(plannedColor, esriSimpleFillStyle.esriSFSNull, esriSimpleLineStyle.esriSLSSolid);
			ILineSymbol line = defaultSymbol.getOutline();
			line.setWidth(LINE_WIDTH);
			defaultSymbol.setOutline(line);

			// create subtype symbol
			SimpleFillSymbol fillSymbol = (SimpleFillSymbol)defaultSymbol.esri_clone();

			// add to symbols
			symbols.put(ISearchIf.SearchSubType.LINE, fillSymbol);
			symbols.put(ISearchIf.SearchSubType.PATROL, fillSymbol);
			symbols.put(ISearchIf.SearchSubType.URBAN, fillSymbol);
			symbols.put(ISearchIf.SearchSubType.SHORELINE, fillSymbol);
			symbols.put(ISearchIf.SearchSubType.MARINE, fillSymbol);
			symbols.put(ISearchIf.SearchSubType.AIR, fillSymbol);
			symbols.put(ISearchIf.SearchSubType.DOG, fillSymbol);

			textSymbol = new TextSymbol();
			textSymbol.setYOffset(5);
			textSymbol.getFont().setName("Arial");

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
	}
}
