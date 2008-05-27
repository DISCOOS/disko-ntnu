package org.redcross.sar.map;

import java.io.IOException;

import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.gisclient.IWMSLayerDescription;
import com.esri.arcgis.gisclient.IWMSLayerStyleDescription;
import com.esri.arcgis.interop.AutomationException;

public class WmsLayerDescription implements IWMSLayerDescription {

	private static final long serialVersionUID = 1L;

	public String getAbstract() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public void getBoundingBox(int arg0, IEnvelope[] arg1, String[] arg2)
			throws IOException, AutomationException {
		// TODO Auto-generated method stub

	}

	public int getBoundingBoxCount() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getExceptionFormat(int arg0) throws IOException,
			AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getExceptionFormatCount() throws IOException,
			AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getFeatureInfoFormat(int arg0) throws IOException,
			AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getFeatureInfoFormatCount() throws IOException,
			AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getFixedHeight() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getFixedWidth() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getImageFormat(int arg0) throws IOException,
			AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getImageFormatCount() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getIsCascaded() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public IEnvelope getLatLongBoundingBox() throws IOException,
			AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public IWMSLayerDescription getLayerDescription(int arg0)
			throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getLayerDescriptionCount() throws IOException,
			AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getName() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSRS(int arg0) throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getSRSCount() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getScaleHintMax() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getScaleHintMin() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public IWMSLayerStyleDescription getStyleDescription(int arg0)
			throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getStyleDescriptionCount() throws IOException,
			AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getTitle() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getWMSVersion() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isOpaque() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isQueryable() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSubsettable() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return false;
	}

}
