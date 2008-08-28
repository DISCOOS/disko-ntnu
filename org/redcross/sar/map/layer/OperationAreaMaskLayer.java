package org.redcross.sar.map.layer;

import java.io.IOException;
import java.net.UnknownHostException;

import org.redcross.sar.map.event.MsoLayerEventStack;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.OperationAreaMaskFeature;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.ICmdPostIf;
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
	
	public OperationAreaMaskLayer(IMsoModelIf msoModel, ISpatialReference srs, MsoLayerEventStack eventStack) {
		super(IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATIONAREA,
				LayerCode.OPERATION_AREA_MASK_LAYER, msoModel, srs,
				esriGeometryType.esriGeometryPolygon, eventStack);
		createSymbols();
		if(msoModel.getMsoManager().operationExists()) {
			ICmdPostIf cmdPost = msoModel.getMsoManager().getCmdPost();
			loadObjects(cmdPost.getOperationAreaListItems().toArray());
		}
		try {
			setVisible(false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected IMsoFeature createMsoFeature(IMsoObjectIf msoObject) 
			throws IOException, AutomationException {
		IMsoFeature msoFeature = new OperationAreaMaskFeature();
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
 				IMsoFeature feature = (IMsoFeature)featureClass.getFeature(i);
 				if(select(feature) && feature.isVisible()){
	 				Polygon polygon = (Polygon)feature.getShape();
	 				if (polygon != null) {
	 					display.setFilterByRef(filter);
	 					display.setSymbol(fill);
	 					display.drawPolygon(polygon);
	 					display.setFilterByRef(null);
	 				}
					feature.setDirty(false);
 				}
 			}
 			isDirty = false;
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
