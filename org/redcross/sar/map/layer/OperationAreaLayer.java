package org.redcross.sar.map.layer;

import java.io.IOException;
import java.net.UnknownHostException;

import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.OperationAreaFeature;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;

import com.esri.arcgis.display.IDisplay;
import com.esri.arcgis.display.RgbColor;
import com.esri.arcgis.display.SimpleFillSymbol;
import com.esri.arcgis.display.SimpleLineSymbol;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.Polygon;
import com.esri.arcgis.geometry.esriGeometryType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.ITrackCancel;

public class OperationAreaLayer extends AbstractMsoFeatureLayer {

	private static final long serialVersionUID = 1L;
	private static final double lineWidth = 1.5;
	private static final double referenceScale = 50000;

	private SimpleFillSymbol defaultSymbol = null;
	private SimpleFillSymbol disabledSymbol = null;
	private SimpleFillSymbol selectionSymbol = null;
 	
 	public OperationAreaLayer(IMsoModelIf msoModel, ISpatialReference srs) {
 		super(IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATIONAREA, 
 				LayerCode.OPERATION_AREA_LAYER, msoModel, srs, 
 				esriGeometryType.esriGeometryPolygon);
 		createSymbols();
 		ICmdPostIf cmdPost = msoModel.getMsoManager().getCmdPost();
		loadObjects(cmdPost.getOperationAreaListItems().toArray());
	}
 	
 	protected IMsoFeature createMsoFeature(IMsoObjectIf msoObject) 
 			throws IOException, AutomationException {
 		IMsoFeature msoFeature = new OperationAreaFeature();
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
			
			// get line zoom width
			double zoomLineWidth = java.lang.Math.min(lineWidth, lineWidth*zoomRatio);
			
			// update
			defaultSymbol.getOutline().setWidth(zoomLineWidth);
			disabledSymbol.getOutline().setWidth(zoomLineWidth);
			selectionSymbol.getOutline().setWidth(zoomLineWidth);
			
 			for (int i = 0; i < featureClass.featureCount(null); i++) {
 				IMsoFeature feature = (IMsoFeature)featureClass.getFeature(i);
 				if(!isFiltered(feature) && feature.isVisible()){
 					Polygon polygon = (Polygon)feature.getShape();
 	 				if (polygon != null) {	 		
 	 					/*
						// is layer in edit mode?
						if (isEditing) {
							// is editing feature?
							if(feature.isEditing()) {
								// is feature selected?
								if (feature.isSelected())
									// select feature 
									display.setSymbol(selectionSymbol);							
								else
									// do no select feature
									display.setSymbol(defaultSymbol);
							}
							else
								// disable all other features
								display.setSymbol(disabledSymbol);							
						}
						else {
						*/
							// is enabled?
							if(isEnabled) {
								// is selected?
		 	 					if (feature.isSelected())
									// select feature 
									display.setSymbol(selectionSymbol);							
		 	 					else
									// do no select feature
									display.setSymbol(defaultSymbol);									
							}
							else {
								// disable all other features
								display.setSymbol(disabledSymbol);							
							}
						//}
 	 					display.drawPolygon(polygon);
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

			RgbColor c = new RgbColor();
			c.setRed(255);
			c.setBlue(255);

			SimpleLineSymbol outlineSymbol = new SimpleLineSymbol();	
			outlineSymbol.setWidth(lineWidth);
			outlineSymbol.setColor(c);
			defaultSymbol.setOutline(outlineSymbol);

			// create disabled symbol
			disabledSymbol = new SimpleFillSymbol();
			disabledSymbol.setStyle(com.esri.arcgis.display.esriSimpleFillStyle.esriSFSNull);
			c = new RgbColor();
			c.setBlue(110);
			c.setGreen(110);
			c.setRed(110);

			SimpleLineSymbol disabledOutlineSymbol = new SimpleLineSymbol();	
			disabledOutlineSymbol.setWidth(lineWidth);
			disabledOutlineSymbol.setColor(c);
			disabledSymbol.setOutline(disabledOutlineSymbol);
			
			// create selection symbol
			selectionSymbol = new SimpleFillSymbol();
			selectionSymbol.setStyle(com.esri.arcgis.display.esriSimpleFillStyle.esriSFSNull);
			c = new RgbColor();
			c.setBlue(255);
			c.setGreen(255);

			SimpleLineSymbol selectedOutlineSymbol = new SimpleLineSymbol();	
			selectedOutlineSymbol.setWidth(lineWidth);
			selectedOutlineSymbol.setColor(c);
			selectionSymbol.setOutline(selectedOutlineSymbol);
			
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
