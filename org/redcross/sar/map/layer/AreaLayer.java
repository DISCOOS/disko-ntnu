package org.redcross.sar.map.layer;

import com.esri.arcgis.display.*;
import com.esri.arcgis.geometry.GeometryBag;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.IPolygon;
import com.esri.arcgis.geometry.IPolyline;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.esriGeometryType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.ITrackCancel;

import org.redcross.sar.map.IDiskoMapManager;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.event.MsoLayerEventStack;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.AreaFeature;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.ISearchIf.SearchSubType;
import org.redcross.sar.mso.util.MsoUtils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;

public class AreaLayer extends AbstractMsoFeatureLayer {

	private static final long serialVersionUID = 1L;
	private static final double fontSize = 12;
	private static final double fontOffset = 5;
	private static final double lineWidth = 1.5;
	private static final double referenceScale = 50000;

	private RgbColor disabledColor = null;
	private RgbColor selectionColor = null;
	private RgbColor plannedColor = null;
	private RgbColor executingColor = null;
	private RgbColor finishedColor = null;
	private Hashtable<SearchSubType, SimpleLineSymbol> symbols = null;
	private SimpleLineSymbol defaultLineSymbol = null;
	private TextSymbol textSymbol = null;

 	public AreaLayer(ISpatialReference srs,
 			MsoLayerEventStack eventStack, IDiskoMapManager manager) {
 		super(esriGeometryType.esriGeometryBag,
 				MsoClassCode.CLASSCODE_AREA,
 				EnumSet.of(MsoClassCode.CLASSCODE_ROUTE,MsoClassCode.CLASSCODE_ASSIGNMENT),
 				LayerCode.AREA_LAYER, srs, eventStack, manager);
 		// prepare
 		symbols = new Hashtable<SearchSubType, SimpleLineSymbol>();
 		// forward
 		createSymbols();
	}

 	protected IMsoFeature createMsoFeature(IMsoObjectIf msoObject)
 			throws IOException, AutomationException {
 		IMsoFeature msoFeature = new AreaFeature();
 		msoFeature.setSpatialReference(srs);
 		msoFeature.setMsoObject(msoObject);
 		System.out.println("Created AreaFeature " + msoObject);
 		return msoFeature;
 	}

 	@Override
	public List<IMsoObjectIf> getGeodataMsoObjects(IMsoObjectIf msoObj) {
		List<IMsoObjectIf> objects = new ArrayList<IMsoObjectIf>(1);
		if (msoObj instanceof IAssignmentIf) {
			IAssignmentIf assignment = (IAssignmentIf)msoObj;
			IAreaIf area = assignment.getPlannedArea();
			if(area!=null)
				objects.add(area);
			area = assignment.getReportedArea();
			if(area!=null)
				objects.add(area);
		}
		if (msoObj instanceof IRouteIf) {
			// get owning area
			IAreaIf area = MsoUtils.getOwningArea(msoObj);
			if(area!=null)
				objects.add(area);
		}
		else if (msoObj instanceof IAreaIf){
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
				if(select(feature) && feature.isVisible()){
					GeometryBag geomBag = (GeometryBag)feature.getShape();
					if (geomBag != null) {
						// initialize
						RgbColor color = plannedColor;
						// get status
						Enum<?> status = MsoUtils.getStatus(feature.getMsoObject());
						// replace color?
						if(AssignmentStatus.EXECUTING.equals(status)) {
							color = executingColor;
						}
						else if(AssignmentStatus.FINISHED.equals(status)){
							color = finishedColor;
						}
						IAreaIf area = (IAreaIf)feature.getMsoObject();
						ISearchIf search = (ISearchIf)area.getOwningAssignment();
						String text = null;
						SimpleLineSymbol lineSymbol = null;
						if (search != null) {
							lineSymbol = (SimpleLineSymbol)symbols.get(search.getSubType());
							text = feature.getCaption();
						} else {
							lineSymbol = defaultLineSymbol;
						}
						lineSymbol.setWidth(zoomLineWidth);
						IColor saveLineColor = lineSymbol.getColor();
						IColor saveTextColor = textSymbol.getColor();

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

						for (int j = 0; j < geomBag.getGeometryCount(); j++) {
							IGeometry geom = geomBag.getGeometry(j);
							if (geom instanceof IPolyline) {
								display.setSymbol(lineSymbol);
								display.drawPolyline(geom);
								if (text != null && isTextShown) {
									display.setSymbol(textSymbol);
									display.drawText(geom, text);
								}
							}
							else if (geom instanceof IPolygon) {
								display.setSymbol(lineSymbol);
								display.drawPolygon(geom);
								if (text != null && isTextShown) {
									display.setSymbol(textSymbol);
									display.drawText(MapUtil.getCenter(geom.getEnvelope()), text);
								}
							}

						}

						lineSymbol.setColor(saveLineColor);
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

}
