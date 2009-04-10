package org.redcross.sar.map.layer;

import com.esri.arcgis.display.*;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.esriGeometryType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.ITrackCancel;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoSymbolFactory;
import org.redcross.sar.map.IDiskoMapManager;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.event.MsoLayerEventStack;
import org.redcross.sar.map.feature.UnitFeature;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.data.IUnitIf.UnitType;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.EnumSet;
import java.util.Hashtable;

public class UnitLayer extends AbstractMsoFeatureLayer {

	private static final long serialVersionUID = 1L;
	private static final double fontSize = 8;
	private static final double fontOffset = 24;
	private static final double pointSize = 32;
	private static final double referenceScale = 50000;

	private RgbColor disabledColor = null;
	private RgbColor selectionColor = null;
	private Hashtable<UnitType, PictureMarkerSymbol> symbols = null;
	private TextSymbol textSymbol = null;
	private BalloonCallout textBackground = null;

 	public UnitLayer(ISpatialReference srs, MsoLayerEventStack eventStack, IDiskoMapManager manager) {
 		super(esriGeometryType.esriGeometryPoint, MsoClassCode.CLASSCODE_UNIT,
 				EnumSet.noneOf(MsoClassCode.class), LayerCode.UNIT_LAYER,
 				srs, eventStack, manager);
 		symbols = new Hashtable<UnitType, PictureMarkerSymbol>();
 		createSymbols();
	}

 	protected UnitFeature createMsoFeature(IMsoObjectIf msoObject)
 			throws IOException, AutomationException {
 		UnitFeature msoFeature = new UnitFeature();
 		msoFeature.setSpatialReference(srs);
 		msoFeature.setMsoObject(msoObject);
 		System.out.println("Created UnitFeature " + msoObject);
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

			// get point zoom size
			double zoomPointSize = java.lang.Math.min(pointSize, pointSize*zoomRatio);

			// update
			textSymbol.setSize(zoomFontSize);
			textSymbol.setXOffset(-zoomFontOffset);
			textSymbol.setYOffset(-zoomFontOffset);

			for (int i = 0; i < featureClass.featureCount(null); i++) {
				UnitFeature feature = (UnitFeature)featureClass.getFeature(i);
				IUnitIf unit = (IUnitIf)feature.getMsoObject();
				UnitStatus status = unit.getStatus();
				boolean exists = !(UnitStatus.EMPTY.equals(status) || UnitStatus.RELEASED.equals(status));
				if(select(feature) && feature.isVisible() && exists){
					IGeometry geom = feature.getShape();
					if (geom != null) {

						// get unit
						IUnitIf msoUnit = (IUnitIf)feature.getMsoObject();

						// get picture symbol
						PictureMarkerSymbol pictureSymbol = symbols.get(msoUnit.getType());

						// initialise
						//IColor savePictureColor = pictureSymbol.getColor();
						IColor saveTextColor = textSymbol.getColor();

						// set symbol size
						pictureSymbol.setSize(zoomPointSize);

						// get unit text
						String text = feature.getCaption();

						// is enabled?
						if(isEnabled) {
							// is selected
	 	 					if (feature.isSelected()){
	 	 						//pictureSymbol.setColor(selectionColor);
								textSymbol.setColor(selectionColor);
	 	 					}
						}
						else {
							// disable all features
							//pictureSymbol.setColor(disabledColor);
							textSymbol.setColor(disabledColor);
						}

						// draw on display
						if (geom instanceof Point) {
							Point p = (Point)geom;
							// is not empty?
							if(!p.isEmpty()) {
								display.setSymbol(pictureSymbol);
								display.drawPoint(p);
								if (text != null && isTextShown) {
									/*IPoint point = p.getCentroidEx();
									point.setY(p.getY()-pointSize);*/
									display.setSymbol(textSymbol);
									display.drawText(p, text);
								}
							}
						}
						// restore state
						//pictureSymbol.setColor(savePictureColor);
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

			RgbColor redColor = new RgbColor();
			redColor.setRed(255);

			UnitType types[] = UnitType.values();

			for(int i=0;i<types.length;i++) {
		 		try {
		 			PictureMarkerSymbol pictureSymbol = new PictureMarkerSymbol();
		 			String path = DiskoSymbolFactory.getPath(DiskoEnumFactory.getSymbol(types[i]));
		 			File file = new File(path);
		 			if(file.exists()) {
			 			pictureSymbol.createMarkerSymbolFromFile(
			 					esriIPictureType.esriIPictureBitmap, file.getAbsolutePath());
		 			}
					symbols.put(types[i], pictureSymbol);
		 		}
		 		catch(Exception e) {
		 			e.printStackTrace();
		 		}
			}

			textBackground = new BalloonCallout();
			textBackground.setBottomMargin(1);
			textBackground.setTopMargin(1);
			textBackground.setLeftMargin(1);
			textBackground.setRightMargin(1);
			textBackground.setStyle(esriBalloonCalloutStyle.esriBCSRoundedRectangle);
			textBackground.setSymbolByRef(MapUtil.getFillSymbol(esriSimpleFillStyle.esriSFSSolid, esriSimpleLineStyle.esriSLSSolid));

			textSymbol = new TextSymbol();
			textSymbol.setXOffset(pointSize);
			textSymbol.setHorizontalAlignment(esriTextHorizontalAlignment.esriTHALeft);
			textSymbol.setVerticalAlignment(esriTextHorizontalAlignment.esriTHACenter);
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
