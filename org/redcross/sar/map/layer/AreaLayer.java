package org.redcross.sar.map.layer;

import com.esri.arcgis.display.*;
import com.esri.arcgis.geometry.GeometryBag;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.IPolyline;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.esriGeometryType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.ITrackCancel;

import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.AreaFeature;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.data.ISearchIf.SearchSubType;
import org.redcross.sar.mso.util.MsoUtils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Hashtable;

public class AreaLayer extends AbstractMsoFeatureLayer {

	private static final long serialVersionUID = 1L;
	private static final double fontSize = 12;
	private static final double fontOffset = 5;
	private static final double lineWidth = 1.5;
	private static final double referenceScale = 50000;
	
	private RgbColor disabledColor = null;
	private RgbColor selectionColor = null;
	private RgbColor plannedColor = null;
	private RgbColor finishedColor = null;
	private Hashtable<SearchSubType, SimpleLineSymbol> symbols = null;
	private SimpleLineSymbol defaultLineSymbol = null;
	private TextSymbol textSymbol = null;

 	public AreaLayer(IMsoModelIf msoModel, ISpatialReference srs) {
 		super(IMsoManagerIf.MsoClassCode.CLASSCODE_AREA,
 				LayerCode.AREA_LAYER, msoModel, srs, 
 				esriGeometryType.esriGeometryBag);
 		symbols = new Hashtable<SearchSubType, SimpleLineSymbol>();
 		createSymbols();
 		ICmdPostIf cmdPost = msoModel.getMsoManager().getCmdPost();
		loadObjects(cmdPost.getAreaListItems().toArray());
	}

 	protected IMsoFeature createMsoFeature(IMsoObjectIf msoObject)
 			throws IOException, AutomationException {
 		IMsoFeature msoFeature = new AreaFeature(msoModel);
 		msoFeature.setSpatialReference(srs);
 		msoFeature.setMsoObject(msoObject);
 		System.out.println("Created AreaFeature " + msoObject);
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
				IMsoFeature feature = (IMsoFeature)featureClass.getFeature(i);
				if(!isFiltered(feature) && feature.isVisible()){
					GeometryBag geomBag = (GeometryBag)feature.getShape();
					if (geomBag != null) {
						// get color
						RgbColor color = IAssignmentIf.AssignmentStatus.FINISHED.equals(
							MsoUtils.getStatus(feature.getMsoObject())) ? finishedColor : plannedColor;
						IAreaIf area = (IAreaIf)feature.getMsoObject();
						ISearchIf search = (ISearchIf)area.getOwningAssignment();
						String text = null;
						SimpleLineSymbol lineSymbol = null;
						if (search != null) {
							lineSymbol = (SimpleLineSymbol)symbols.get(search.getSubType());
							text = MsoUtils.getAssignmentName(search,2);
						} else {
							lineSymbol = defaultLineSymbol;
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
		 	 					else
		 	 						lineSymbol.setColor(color);
							}
							else {
								// disable all features
								lineSymbol.setColor(disabledColor);
								textSymbol.setColor(disabledColor);
							}
						//}
												
						for (int j = 0; j < geomBag.getGeometryCount(); j++) {
							IGeometry geom = geomBag.getGeometry(j);
							if (geom instanceof IPolyline) {
								display.setSymbol(lineSymbol);
								display.drawPolyline((IPolyline)geom);
								if (text != null && isTextShown) {
									display.setSymbol(textSymbol);
									display.drawText(geom, text);
								}
							}
						}
						
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

			finishedColor = new RgbColor();
			finishedColor.setGreen(155);

			SimpleLineSymbol lineSymbol = new SimpleLineSymbol();
			lineSymbol.setStyle(esriSimpleLineStyle.esriSLSDash);
			lineSymbol.setWidth(lineWidth);
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

			defaultLineSymbol = new SimpleLineSymbol();
			defaultLineSymbol.setWidth(lineWidth);
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
		if(msoObj instanceof IAreaIf) {
			// cast to area
			IAreaIf area = (IAreaIf)msoObj;
			// get collection
			java.util.Collection<IPOIIf> c = area.getAreaPOIsItems();
			// get poi's
			poiList = new ArrayList<IPOIIf>(c);				
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
		if(msoObj instanceof IAreaIf) {
			// cast to area
			IAreaIf area = (IAreaIf)msoObj;
			// get poi's
			poiList = new ArrayList<IPOIIf>(area.getAreaPOIsItems());
		}
		// return poi list
		return poiList;
	}
	*/			
}
