package org.redcross.sar.map.layer;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.esri.arcgis.display.*;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.Polyline;
import com.esri.arcgis.geometry.esriGeometryType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.ITrackCancel;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.RouteFeature;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoUtils;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.data.ISearchIf.SearchSubType;
import org.redcross.sar.util.mso.Route;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Hashtable;

public class RouteLayer extends AbstractMsoFeatureLayer {

	private static final long serialVersionUID = 1L;
	private static final double fontSize = 12;
	private static final double fontOffset = 5;
	private static final double lineWidth = 1.5;
	private static final double referenceScale = 50000;
	
	private RgbColor disabledColor = null;
	private RgbColor selectionColor = null;
	private Hashtable<SearchSubType, SimpleLineSymbol> symbols = null;
	private SimpleLineSymbol defaultLineSymbol = null;
	private TextSymbol textSymbol = null;

 	public RouteLayer(IMsoModelIf msoModel, ISpatialReference srs) {
 		super(IMsoManagerIf.MsoClassCode.CLASSCODE_ROUTE,
 				LayerCode.ROUTE_LAYER, msoModel, srs, 
 				esriGeometryType.esriGeometryPolyline);
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
			double zoomRatio = java.lang.Math.min(1.0,referenceScale / scale);
			
			// get text zoom size and offset
			double zoomFontSize = java.lang.Math.min(fontSize, fontSize*zoomRatio);
			double zoomFontOffset = java.lang.Math.min(fontOffset, fontOffset*zoomRatio);
			
			// get line zoom width
			double zoomLineWidth = java.lang.Math.min(lineWidth, lineWidth*zoomRatio);
			
			// update
			textSymbol.setSize(zoomFontSize);
			textSymbol.setYOffset(zoomFontOffset);
			
			for (int i = 0; i < featureClass.featureCount(null); i++) {
				RouteFeature feature = (RouteFeature)featureClass.getFeature(i);
				if(!isFiltered(feature) && feature.isVisible()){
					IGeometry geom = feature.getShape();
					if (geom != null) {
						// get owning area
						IAreaIf area = feature.getOwningArea();
						// initialise
						String text = null;
						SimpleLineSymbol lineSymbol = defaultLineSymbol;
						// has owning area?
						if(area!=null) {
							ISearchIf search = (ISearchIf)area.getOwningAssignment();
							if (search != null) {
								lineSymbol = (SimpleLineSymbol)symbols.get(search.getSubType());
								text = MsoUtils.getAssignmentName(search,2);
							} 
						}
						lineSymbol.setWidth(zoomLineWidth);
						IColor saveLineColor = lineSymbol.getColor();
						IColor saveTextColor = textSymbol.getColor();
						
						/*
						// is layer in edit mode?
						if (isEditing) {
							// is editing feature?
							if(feature.isEditing()) {
								// is feature selected?
								if (feature.isSelected()) {
									lineSymbol.setColor(selectionColor);
									textSymbol.setColor(selectionColor);
								}
							}
							else {
								// disable all other features
								lineSymbol.setColor(disabledColor);
								textSymbol.setColor(disabledColor);
							}								
						}
						else {
						*/
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
						//}
						
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

			RgbColor redColor = new RgbColor();
			redColor.setRed(255);

			SimpleLineSymbol lineSymbol = new SimpleLineSymbol();
			lineSymbol.setStyle(esriSimpleLineStyle.esriSLSDash);
			lineSymbol.setWidth(lineWidth);
			lineSymbol.setColor(redColor);

			symbols.put(ISearchIf.SearchSubType.LINE, lineSymbol);
			symbols.put(ISearchIf.SearchSubType.PATROL, lineSymbol);
			symbols.put(ISearchIf.SearchSubType.URBAN, lineSymbol);
			symbols.put(ISearchIf.SearchSubType.SHORELINE, lineSymbol);
			symbols.put(ISearchIf.SearchSubType.MARINE, lineSymbol);
			symbols.put(ISearchIf.SearchSubType.AIR, lineSymbol);
			symbols.put(ISearchIf.SearchSubType.DOG, lineSymbol);

			textSymbol = new TextSymbol();
			textSymbol.setYOffset(5);

			defaultLineSymbol = new SimpleLineSymbol();
			defaultLineSymbol.setWidth(lineWidth);
			defaultLineSymbol.setColor(redColor);
			
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
