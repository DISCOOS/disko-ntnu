package org.redcross.sar.map.layer;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.EnumSet;

import org.redcross.sar.map.IDiskoMapManager;
import org.redcross.sar.map.event.MsoLayerEventStack;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.OperationAreaMaskFeature;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;

import com.esri.arcgis.display.IDisplay;
import com.esri.arcgis.display.SimpleFillSymbol;
import com.esri.arcgis.display.TransparencyDisplayFilter;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.Polygon;
import com.esri.arcgis.geometry.esriGeometryType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.ITrackCancel;

public class OperationAreaMaskLayer extends AbstractMsoFeatureLayer {

	private static final long serialVersionUID = 1L;
	private SimpleFillSymbol fill = null;
	private TransparencyDisplayFilter filter = null;

	public OperationAreaMaskLayer(ISpatialReference srs, MsoLayerEventStack eventStack) {
		super(esriGeometryType.esriGeometryPolygon, MsoClassCode.CLASSCODE_OPERATIONAREA,
				EnumSet.noneOf(MsoClassCode.class), LayerCode.OPERATION_AREA_MASK_LAYER,
				srs, eventStack);

		// prepare
		isVisible = false;

		// forward
		createSymbols();

	}

	protected OperationAreaMaskFeature createMsoFeature(IMsoObjectIf msoObject)
			throws IOException, AutomationException {
		OperationAreaMaskFeature msoFeature = new OperationAreaMaskFeature();
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
 			for (int i = 0; i < featureClass.featureCount(null); i++) {
 				IMsoFeature feature = getFeature(i);
 				if(select(feature) && feature.isVisible()){
	 				Polygon polygon = (Polygon)feature.getShape();
	 				if (polygon != null) {
	 					display.setFilterByRef(filter);
	 					display.setSymbol(fill);
	 					display.drawPolygon(polygon);
	 					display.setFilterByRef(null);
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
			filter = new TransparencyDisplayFilter();
			filter.setTransparency((short)75);
			fill = new SimpleFillSymbol();
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
