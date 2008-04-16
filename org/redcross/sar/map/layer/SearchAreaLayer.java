package org.redcross.sar.map.layer;

import java.io.IOException;
import java.net.UnknownHostException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.SearchAreaFeature;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchAreaIf;
import org.redcross.sar.mso.util.MsoUtils;

import com.esri.arcgis.display.IColor;
import com.esri.arcgis.display.IDisplay;
import com.esri.arcgis.display.RgbColor;
import com.esri.arcgis.display.SimpleFillSymbol;
import com.esri.arcgis.display.SimpleLineSymbol;
import com.esri.arcgis.display.TextSymbol;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.Polygon;
import com.esri.arcgis.geometry.esriGeometryType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.ITrackCancel;

public class SearchAreaLayer extends AbstractMsoFeatureLayer {

	private static final long serialVersionUID = 1L;
	private static final double fontSize = 14;
	private static final double lineWidth = 1.5;
	private static final double referenceScale = 50000;
	
 	private SimpleFillSymbol defaultSymbol = null;
 	private SimpleFillSymbol disabledSymbol = null; 	
	private SimpleFillSymbol selectionSymbol = null;
	private TextSymbol textSymbol = null;
 	
 	public SearchAreaLayer(IMsoModelIf msoModel, ISpatialReference srs) {
 		super(IMsoManagerIf.MsoClassCode.CLASSCODE_SEARCHAREA, 
 				LayerCode.SEARCH_AREA_LAYER, msoModel, srs, 
 				esriGeometryType.esriGeometryPolygon);
 		createSymbols();
 		ICmdPostIf cmdPost = msoModel.getMsoManager().getCmdPost();
		loadObjects(cmdPost.getSearchAreaListItems().toArray());
	}
 	
 	protected IMsoFeature createMsoFeature(IMsoObjectIf msoObject) 
 			throws IOException, AutomationException {
 		IMsoFeature msoFeature = new SearchAreaFeature();
 		msoFeature.setSpatialReference(srs);
 		msoFeature.setMsoObject(msoObject);
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
			
			// get text zoom size
			double zoomFontSize = java.lang.Math.min(fontSize, fontSize*zoomRatio);
			
			// get line zoom width
			double zoomLineWidth = java.lang.Math.min(lineWidth, lineWidth*zoomRatio);
			
			// update
			textSymbol.setSize(zoomFontSize);
			defaultSymbol.getOutline().setWidth(zoomLineWidth);
			disabledSymbol.getOutline().setWidth(zoomLineWidth);
			selectionSymbol.getOutline().setWidth(zoomLineWidth);
			
			for (int i = 0; i < featureClass.featureCount(null); i++) {
				IMsoFeature feature = (IMsoFeature)featureClass.getFeature(i);
 				if(select(feature) && feature.isVisible()){
					ISearchAreaIf searchArea = (ISearchAreaIf)feature.getMsoObject();
					Polygon polygon = (Polygon)feature.getShape();
					if (polygon != null) {
						IColor saveTextColor = textSymbol.getColor();
						/*
						// is layer in edit mode?
						if (isEditing) {
							// is editing feature?
							if(feature.isEditing()) {
								// is feature selected?
								if (feature.isSelected()) {
		 	 						display.setSymbol(selectionSymbol);
									textSymbol.setColor(selectionSymbol.getOutline().getColor());	 	 						
								}
								else {
									// do not select feature
									display.setSymbol(defaultSymbol);
									textSymbol.setColor(saveTextColor);										
								}
							}
							else {
								// disable all other features
								display.setSymbol(disabledSymbol);							
								textSymbol.setColor(disabledSymbol.getOutline().getColor());
							}								
						}
						else {
						*/
							// is enabled?
							if(isEnabled) {
								// is selected
		 	 					if (feature.isSelected()){
		 	 						// select feature
		 	 						display.setSymbol(selectionSymbol);
									textSymbol.setColor(selectionSymbol.getOutline().getColor());	 	 						
		 	 					}
		 	 					else {
		 	 						display.setSymbol(defaultSymbol);
									textSymbol.setColor(saveTextColor);	 	 						
		 	 					}
							}
							else {
								// disable all features
								display.setSymbol(disabledSymbol);							
								textSymbol.setColor(disabledSymbol.getOutline().getColor());
							}
						//}
						
						// draw 
						display.drawPolygon(polygon);
						display.setSymbol(textSymbol);	
						if( isTextShown) {
							display.drawText(polygon.getCentroid(), MsoUtils.getSearchAreaName(searchArea));
						}
						
						// restore
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
			
			// create default symbol
			defaultSymbol = new SimpleFillSymbol();
			defaultSymbol.setStyle(com.esri.arcgis.display.esriSimpleFillStyle.esriSFSNull);

			RgbColor color = new RgbColor();
			color.setBlue(255);

			SimpleLineSymbol outlineSymbol = new SimpleLineSymbol();	
			outlineSymbol.setWidth(lineWidth);
			outlineSymbol.setColor(color);
			defaultSymbol.setOutline(outlineSymbol);

			// create disabled symbol
			disabledSymbol = new SimpleFillSymbol();
			disabledSymbol.setStyle(com.esri.arcgis.display.esriSimpleFillStyle.esriSFSNull);
			RgbColor disabledColor = new RgbColor();
			disabledColor.setBlue(110);
			disabledColor.setGreen(110);
			disabledColor.setGreen(110);

			SimpleLineSymbol disabledOutlineSymbol = new SimpleLineSymbol();	
			disabledOutlineSymbol.setWidth(lineWidth);
			disabledOutlineSymbol.setColor(disabledColor);
			disabledSymbol.setOutline(disabledOutlineSymbol);
			
			// create selection symbol
			selectionSymbol = new SimpleFillSymbol();
			selectionSymbol.setStyle(com.esri.arcgis.display.esriSimpleFillStyle.esriSFSNull);
			RgbColor selectionColor = new RgbColor();
			selectionColor.setBlue(255);
			selectionColor.setGreen(255);

			SimpleLineSymbol selectedOutlineSymbol = new SimpleLineSymbol();	
			selectedOutlineSymbol.setWidth(lineWidth);
			selectedOutlineSymbol.setColor(selectionColor);
			selectionSymbol.setOutline(selectedOutlineSymbol);

			// create text symbol
			textSymbol = new TextSymbol();
			textSymbol.setSize(fontSize);
			
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
