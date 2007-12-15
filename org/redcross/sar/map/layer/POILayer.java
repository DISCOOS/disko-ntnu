package org.redcross.sar.map.layer;

import com.esri.arcgis.display.*;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.esriGeometryType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.ITrackCancel;
import org.redcross.sar.app.Utils;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.POIFeature;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.mso.util.MsoUtils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Hashtable;

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
	private Hashtable<POIType, IDisplayName> symbols = null;

 	public POILayer(IMsoModelIf msoModel, ISpatialReference srs) {
 		super(IMsoManagerIf.MsoClassCode.CLASSCODE_POI,
 				LayerCode.POI_LAYER, msoModel, srs, 
 				esriGeometryType.esriGeometryPoint);
 		symbols = new Hashtable<POIType, IDisplayName>();
 		createSymbols();
 		ICmdPostIf cmdPost = msoModel.getMsoManager().getCmdPost();
		loadObjects(cmdPost.getPOIListItems().toArray());
	}

 	protected IMsoFeature createMsoFeature(IMsoObjectIf msoObject)
 			throws IOException, AutomationException {
 		IMsoFeature msoFeature = new POIFeature();
 		msoFeature.setSpatialReference(srs);
 		msoFeature.setMsoObject(msoObject);
 		System.out.println("Created POIFeature " + msoObject);
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
			
			// get point zoom size
			double zoomPointSize = java.lang.Math.min(pointSize, pointSize*zoomRatio);
			
			// get text zoom size and offset
			double zoomFontSize = java.lang.Math.min(fontSize, fontSize*zoomRatio);
			double zoomFontOffset = java.lang.Math.min(pointSize, zoomPointSize*zoomRatio);
			
			// update
			textSymbol.setSize(zoomFontSize);
			textSymbol.setYOffset(zoomFontOffset);
			
			for (int i = 0; i < featureClass.featureCount(null); i++) {
				IMsoFeature feature = (IMsoFeature)featureClass.getFeature(i);
 				if(!isFiltered(feature) && feature.isVisible()){
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
	 					if(POIType.START.equals(type) || POIType.VIA.equals(type) 
	 							|| POIType.STOP.equals(type)) {
							// initialize
							RgbColor color = plannedColor;
							// get status
							Enum status = MsoUtils.getStatus(MsoUtils.getOwningArea(poi));
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
	 					}
	 						
						
						/*
						// is layer in edit mode?
						if (isEditing) {
							// is editing feature?
							if(feature.isEditing()) {
								// is feature selected?
								if (feature.isSelected()) {
									markerSymbol.setColor(selectionColor);
									textSymbol.setColor(selectionColor);
								}
							}
							else {
								// disable all other features
								markerSymbol.setColor(disabledColor);
								textSymbol.setColor(disabledColor);
							}								
						}
						else {
						*/
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
						//}
						
						markerSymbol.setSize(size);
						display.setSymbol((ISymbol)markerSymbol);
						display.drawPoint(point);
						
						// draw label?
						if(isTextShown) {
							display.setSymbol(textSymbol);
							String text = Utils.translate(poi.getType());
							String remark = poi.getRemarks();
							// replace with comment?
							if (remark != null) {
								if(remark.length() > 0) {
									text = remark;
								}
							}
							display.drawText(point, text);
						}
						
						// restore
						markerSymbol.setColor(savePointColor);
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

			textSymbol = new TextSymbol();
			textSymbol.setHorizontalAlignment(esriTextHorizontalAlignment.esriTHALeft);
			textSymbol.setVerticalAlignment(esriTextHorizontalAlignment.esriTHACenter);
			textSymbol.setXOffset(redRoundSymbol.getSize());
			
			
			
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
