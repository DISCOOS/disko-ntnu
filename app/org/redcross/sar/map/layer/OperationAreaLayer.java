package org.redcross.sar.map.layer;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.EnumSet;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.event.MsoLayerEventStack;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.OperationAreaFeature;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;

import com.esri.arcgis.display.IDisplay;
import com.esri.arcgis.display.ILineSymbol;
import com.esri.arcgis.display.RgbColor;
import com.esri.arcgis.display.SimpleFillSymbol;
import com.esri.arcgis.display.esriSimpleFillStyle;
import com.esri.arcgis.display.esriSimpleLineStyle;
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

 	public OperationAreaLayer(ISpatialReference srs, MsoLayerEventStack eventStack) {
 		super(esriGeometryType.esriGeometryPolygon, MsoClassCode.CLASSCODE_OPERATIONAREA,
 				EnumSet.noneOf(MsoClassCode.class), LayerCode.OPERATION_AREA_LAYER,
 				srs, eventStack);
 		createSymbols();
	}

 	protected OperationAreaFeature createMsoFeature(IMsoObjectIf msoObject)
 			throws IOException, AutomationException {
 		OperationAreaFeature msoFeature = new OperationAreaFeature();
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

 			for (int i = 0; i < featureClass.featureCount(null); i++) {
 				IMsoFeature feature = getFeature(i);
 				if(select(feature) && feature.isVisible()){
 					Polygon polygon = (Polygon)feature.getShape();
 	 				if (polygon != null) {
						// is enabled?
						if(isEnabled) {
							// is selected?
	 	 					if (feature.isSelected()) {
		 	 					// update
								ILineSymbol line = selectionSymbol.getOutline();
								line.setWidth(zoomLineWidth);
								selectionSymbol.setOutline(line);
								// select feature
								display.setSymbol(selectionSymbol);
							}
	 	 					else {
		 	 					// update
								ILineSymbol line = defaultSymbol.getOutline();
								line.setWidth(zoomLineWidth);
								defaultSymbol.setOutline(line);
								// do no select feature
								display.setSymbol(defaultSymbol);
	 	 					}
						}
						else {
							// update
							ILineSymbol line = disabledSymbol.getOutline();
							line.setWidth(zoomLineWidth);
							disabledSymbol.setOutline(line);
							// disable all other features
							display.setSymbol(disabledSymbol);
						}
 	 					display.drawPolygon(polygon);
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

 			// create default symbol
			RgbColor c = new RgbColor();
			c.setRed(255);
			c.setBlue(255);
			defaultSymbol = (SimpleFillSymbol)MapUtil.getFillSymbol(c, esriSimpleFillStyle.esriSFSNull, esriSimpleLineStyle.esriSLSSolid);
			defaultSymbol.getOutline().setWidth(lineWidth);

			// create disabled symbol
			c = new RgbColor();
			c.setBlue(110);
			c.setGreen(110);
			c.setRed(110);
			disabledSymbol = (SimpleFillSymbol)MapUtil.getFillSymbol(c, esriSimpleFillStyle.esriSFSNull, esriSimpleLineStyle.esriSLSSolid);
			disabledSymbol.getOutline().setWidth(lineWidth);

			// create selection symbol
			c = new RgbColor();
			c.setBlue(255);
			c.setGreen(255);
			selectionSymbol = (SimpleFillSymbol)MapUtil.getFillSymbol(c, esriSimpleFillStyle.esriSFSNull, esriSimpleLineStyle.esriSLSSolid);
			selectionSymbol.getOutline().setWidth(lineWidth);

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
