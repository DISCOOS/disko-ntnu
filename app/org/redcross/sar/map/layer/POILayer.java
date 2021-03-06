package org.redcross.sar.map.layer;

import com.esri.arcgis.display.*;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.esriGeometryType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.ITrackCancel;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.event.MsoLayerEventStack;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.POIFeature;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.mso.util.MsoUtils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;

public class POILayer extends AbstractMsoFeatureLayer {

	private static final long serialVersionUID = 1L;
	private static final double fontSize = 10;
	private static final double pointSize = 8;
	private static final double referenceScale = 50000;

	private RgbColor disabledColor = null;
	private RgbColor selectionColor = null;
	private RgbColor plannedColor = null;
	private RgbColor executingColor = null;
	private RgbColor finishedColor = null;
	private TextSymbol textSymbol = null;
	private BalloonCallout textBackground = null;
	private Hashtable<POIType, IDisplayName> symbols = null;

 	public POILayer(ISpatialReference srs, MsoLayerEventStack eventStack) {
 		super(esriGeometryType.esriGeometryPoint, MsoClassCode.CLASSCODE_POI,
 				EnumSet.noneOf(MsoClassCode.class), LayerCode.POI_LAYER,
 				srs, eventStack);
 		symbols = new Hashtable<POIType, IDisplayName>();
 		createSymbols();
	}

 	protected POIFeature createMsoFeature(IMsoObjectIf msoObject)
 			throws IOException, AutomationException {
 		POIFeature msoFeature = new POIFeature();
 		msoFeature.setSpatialReference(srs);
 		msoFeature.setMsoObject(msoObject);
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
			// add all POIs
			objects.addAll(area.getAreaPOIsItems());
		}
		else if (msoObj instanceof IPOIIf) {
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

			// get point zoom size
			double zoomPointSize = java.lang.Math.min(pointSize, pointSize*zoomRatio);

			// get text zoom size and offset
			double zoomFontSize = java.lang.Math.min(fontSize, fontSize*zoomRatio);
			double zoomFontOffset = java.lang.Math.min(pointSize, zoomPointSize*zoomRatio);

			// update
			textSymbol.setSize(zoomFontSize);
			textSymbol.setYOffset(zoomFontOffset);

			for (int i = 0; i < featureClass.featureCount(null); i++) {
				IMsoFeature feature = getFeature(i);
 				if(select(feature) && feature.isVisible()){
					Point point = (Point)feature.getShape();
					if (point != null) {
						IPOIIf poi = (IPOIIf)feature.getMsoObject();
						POIType type = POIType.GENERAL;
						if(poi.getType()!=null)
							type = poi.getType();
						IMarkerSymbol markerSymbol = (IMarkerSymbol)symbols.get(type);
						IColor savePointColor = markerSymbol.getColor();
						IColor saveTextColor = textSymbol.getColor();
	 					// get assignment?
	 					if(IPOIIf.AREA_SET.contains(type)) {
							// initialize
							RgbColor color = plannedColor;
							// get status
							Enum<?> status = MsoUtils.getStatus(MsoUtils.getOwningArea(poi));
							// replace color?
							if(AssignmentStatus.EXECUTING.equals(status)) {
								color = executingColor;
							}
							else if(AssignmentStatus.FINISHED.equals(status)){
								color = finishedColor;
							}
							// set color
	 						markerSymbol.setColor(color);
	 					}

	 					// get point size
	 					double size = zoomPointSize;

	 					// increase size?
	 					if(POIType.FINDING.equals(type) || POIType.SILENT_WITNESS.equals(type)
	 							|| POIType.INTELLIGENCE.equals(type)) {
	 						// double the size
	 						size = size*2;
	 						textSymbol.setBackgroundByRef(textBackground);
	 					}

	 					// show text background?
	 					if(!IPOIIf.AREA_SET.contains(type))
	 						textSymbol.setBackgroundByRef(textBackground);
	 					else
	 						textSymbol.setBackgroundByRef(null);

						// is enabled?
						if(isEnabled) {
							// is selected?
	 	 					if (feature.isSelected()){
	 	 						markerSymbol.setColor(selectionColor);
	 	 						textSymbol.setColor(selectionColor);
	 	 					}
						}
						else {
							// disable all features
							markerSymbol.setColor(disabledColor);
							textSymbol.setColor(disabledColor);
						}

						markerSymbol.setSize(size);
						display.setSymbol((ISymbol)markerSymbol);
						display.drawPoint(point);

						// draw label?
						if(isTextShown) {
							display.setSymbol(textSymbol);
							String text = feature.getCaption();
							display.drawText(point, text);
						}

						// restore
						markerSymbol.setColor(savePointColor);
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
		// colors
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

			RgbColor redColor = new RgbColor();
			redColor.setRed(255);
			RgbColor blueColor = new RgbColor();
			blueColor.setBlue(255);
			RgbColor greenColor = new RgbColor();
			greenColor.setGreen(255);
			RgbColor cyanColor = new RgbColor();
			cyanColor.setRed(255);
			cyanColor.setBlue(255);

			SimpleMarkerSymbol redSquareSymbol = new SimpleMarkerSymbol();
			redSquareSymbol.setStyle(esriSimpleMarkerStyle.esriSMSSquare);
			redSquareSymbol.setColor(redColor);
			redSquareSymbol.setSize(pointSize);

			SimpleMarkerSymbol blueSquareSymbol = new SimpleMarkerSymbol();
			blueSquareSymbol.setStyle(esriSimpleMarkerStyle.esriSMSSquare);
			blueSquareSymbol.setColor(blueColor);
			blueSquareSymbol.setSize(pointSize);

			SimpleMarkerSymbol redRoundSymbol = new SimpleMarkerSymbol();
			redRoundSymbol.setColor(redColor);
			redSquareSymbol.setSize(pointSize);

			SimpleMarkerSymbol blueRoundSymbol = new SimpleMarkerSymbol();
			blueRoundSymbol.setColor(blueColor);
			blueRoundSymbol.setSize(pointSize);

			SimpleMarkerSymbol greenRoundSymbol = new SimpleMarkerSymbol();
			greenRoundSymbol.setColor(greenColor);
			greenRoundSymbol.setSize(pointSize);

			SimpleMarkerSymbol blackDiamondSymbol = new SimpleMarkerSymbol();
			blackDiamondSymbol.setStyle(esriSimpleMarkerStyle.esriSMSDiamond);
			blackDiamondSymbol.setSize(pointSize);

			SimpleMarkerSymbol redDiamondSymbol = new SimpleMarkerSymbol();
			redDiamondSymbol.setStyle(esriSimpleMarkerStyle.esriSMSDiamond);
			redDiamondSymbol.setColor(redColor);
			redDiamondSymbol.setSize(pointSize);

			SimpleMarkerSymbol greenDiamondSymbol = new SimpleMarkerSymbol();
			greenDiamondSymbol.setStyle(esriSimpleMarkerStyle.esriSMSDiamond);
			greenDiamondSymbol.setColor(greenColor);
			greenDiamondSymbol.setSize(pointSize);

			SimpleMarkerSymbol cyanDiamondSymbol = new SimpleMarkerSymbol();
			cyanDiamondSymbol.setStyle(esriSimpleMarkerStyle.esriSMSDiamond);
			cyanDiamondSymbol.setColor(cyanColor);
			cyanDiamondSymbol.setSize(pointSize);

			symbols.put(POIType.START, redRoundSymbol);
			symbols.put(POIType.STOP, redRoundSymbol);
			symbols.put(POIType.VIA, blueRoundSymbol);
			symbols.put(POIType.OBSERVATION, blackDiamondSymbol);
			symbols.put(POIType.GENERAL, blueSquareSymbol);
			symbols.put(POIType.FINDING, cyanDiamondSymbol);
			symbols.put(POIType.SILENT_WITNESS, cyanDiamondSymbol);
			symbols.put(POIType.INTELLIGENCE, redDiamondSymbol);

			textBackground = new BalloonCallout();
			textBackground.setBottomMargin(1);
			textBackground.setTopMargin(1);
			textBackground.setLeftMargin(1);
			textBackground.setRightMargin(1);
			textBackground.setStyle(esriBalloonCalloutStyle.esriBCSRoundedRectangle);
			textBackground.setSymbolByRef(MapUtil.getFillSymbol(esriSimpleFillStyle.esriSFSSolid, esriSimpleLineStyle.esriSLSSolid));

			textSymbol = new TextSymbol();
			textSymbol.setHorizontalAlignment(esriTextHorizontalAlignment.esriTHALeft);
			textSymbol.setVerticalAlignment(esriTextHorizontalAlignment.esriTHACenter);
			textSymbol.setXOffset(redRoundSymbol.getSize());
			textSymbol.setBackgroundByRef(textBackground);



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
