package org.redcross.sar.map.layer;

import java.util.StringTokenizer;

import com.esri.arcgis.display.*;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.Polyline;
import com.esri.arcgis.geometry.esriGeometryType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.ITrackCancel;

import org.redcross.sar.event.MsoLayerEventStack;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.RouteFeature;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
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
	private Hashtable<SearchSubType, SimpleLineSymbol> symbols = null;
	private SimpleLineSymbol defaultLineSymbol = null;
	private TextSymbol textSymbol = null;

 	public RouteLayer(IMsoModelIf msoModel, ISpatialReference srs, MsoLayerEventStack eventStack) {
 		super(IMsoManagerIf.MsoClassCode.CLASSCODE_ROUTE,
 				LayerCode.ROUTE_LAYER, msoModel, srs, 
 				esriGeometryType.esriGeometryPolyline, eventStack);
 		addInterestIn(MsoClassCode.CLASSCODE_ASSIGNMENT);
 		symbols = new Hashtable<SearchSubType, SimpleLineSymbol>();
 		createSymbols();
 		ICmdPostIf cmdPost = msoModel.getMsoManager().getCmdPost();
		loadObjects(cmdPost.getRouteListItems().toArray());
	}

 	protected IMsoFeature createMsoFeature(IMsoObjectIf msoObject)
 			throws IOException, AutomationException {
 		IMsoFeature msoFeature = new RouteFeature(msoModel);
 		msoFeature.setSpatialReference(srs);
 		msoFeature.setMsoObject(msoObject);
 		System.out.println("Created RouteFeature " + msoObject);
 		return msoFeature;
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
						
						// initialise
						String text = null;
						SimpleLineSymbol lineSymbol = defaultLineSymbol;
						
						// update symbols
						lineSymbol.setWidth(zoomLineWidth);
						
						// save current colors
						IColor saveLineColor = lineSymbol.getColor();
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
								Enum status = MsoUtils.getStatus(assignment);
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
								lineSymbol = (SimpleLineSymbol)symbols.get(search.getSubType());
								text = feature.getCaption();
								// update line color
								lineSymbol.setColor(color);
							} 
						}
												
						// is enabled?
						if(isEnabled) {
							// is selected
	 	 					if (feature.isSelected()){
								lineSymbol.setColor(selectionColor);
								textSymbol.setColor(selectionColor);
	 	 					}
	 	 					
						}
						else {
							// disable all features
							lineSymbol.setColor(disabledColor);
							textSymbol.setColor(disabledColor);
						}
						
						// draw on display
						if (geom instanceof Polyline) {
							String layout = ((IRouteIf)feature.getMsoObject()).getGeodata().getLayout();
							Hashtable params = getParams(layout);
							boolean isPolygon = Boolean.valueOf((String)params.get("isPolygon"));
							Polyline polyline = (Polyline)((Polyline)geom).esri_clone();
							if(isPolygon) {
								// closing
								polyline.addPoint(polyline.getFromPoint(), null, null);
							}
							display.setSymbol(lineSymbol);
							display.drawPolyline(polyline);
							if (text != null && isTextShown) {
								display.setSymbol(textSymbol);
								if(isPolygon)
									display.drawText(MapUtil.getPolygon(polyline).getCentroid(), text);
								else
									display.drawText(geom, text);									
							}
						}
						
						// restore state
						lineSymbol.setColor(saveLineColor);
						textSymbol.setColor(saveTextColor);
					}
					feature.setDirty(false);
				}
			}
			isDirty = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Hashtable getParams(String paramString) {
		Hashtable<String, String> params = new Hashtable<String, String>();
		StringTokenizer st1 = new StringTokenizer(paramString, "&");
		while(st1.hasMoreTokens()) {
			StringTokenizer st2 = new StringTokenizer(st1.nextToken(), "=");
			String name  = st2.nextToken();
			String value =  st2.nextToken();
			params.put(name, value);
		}
		return params;
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
			
			SimpleLineSymbol lineSymbol = new SimpleLineSymbol();
			lineSymbol.setStyle(esriSimpleLineStyle.esriSLSDash);
			lineSymbol.setWidth(LINE_WIDTH);
			lineSymbol.setColor(plannedColor);

			symbols.put(ISearchIf.SearchSubType.LINE, lineSymbol);
			symbols.put(ISearchIf.SearchSubType.PATROL, lineSymbol);
			symbols.put(ISearchIf.SearchSubType.URBAN, lineSymbol);
			symbols.put(ISearchIf.SearchSubType.SHORELINE, lineSymbol);
			symbols.put(ISearchIf.SearchSubType.MARINE, lineSymbol);
			symbols.put(ISearchIf.SearchSubType.AIR, lineSymbol);
			symbols.put(ISearchIf.SearchSubType.DOG, lineSymbol);

			textSymbol = new TextSymbol();
			textSymbol.setYOffset(5);
			textSymbol.getFont().setName("Arial");

			defaultLineSymbol = new SimpleLineSymbol();
			defaultLineSymbol.setWidth(LINE_WIDTH);
			defaultLineSymbol.setColor(plannedColor);
			
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
	
	/**
	 * Override to ensure that area poi's also are marked as editing
	 */
	/*
	public List startEdit(IMsoObjectIf msoObj) throws IOException, AutomationException {
		List poiList = null;
		// do default operation
		super.startEdit(msoObj);
		// is area?
		if(msoObj instanceof IRouteIf) {
			// get owning area
			IAreaIf area = MsoUtils.getOwningArea((IRouteIf)msoObj);
			// has owning area?
			if(area!=null) {
				// get collection
				java.util.Collection<IPOIIf> c = area.getAreaPOIsItems();
				// get poi's
				poiList = new ArrayList<IPOIIf>(c);
			}					
		}		
		// return poi list
		return poiList;
	}
	*/
	
	/**
	 * Override to ensure that area poi's also are marked as editing
	 */
	/*
	public List stopEdit(IMsoObjectIf msoObj) throws IOException, AutomationException {
		List poiList = null;
		// do default operation
		super.stopEdit(msoObj);
		// is area?
		if(msoObj instanceof IRouteIf) {
			// get owning area
			IAreaIf area = MsoUtils.getOwningArea((IRouteIf)msoObj);
			// has owning area?
			if(area!=null) {
				// get collection
				java.util.Collection<IPOIIf> c = area.getAreaPOIsItems();
				// get poi's
				poiList = new ArrayList<IPOIIf>(c);
			}					
		}
		// return poi list
		return poiList;
	}		
	*/	
}
