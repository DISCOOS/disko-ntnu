package org.redcross.sar.map.layer;

import java.io.IOException;

import com.esri.arcgis.carto.IDataLayer;
import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.carto.IWMSGroupLayer;
import com.esri.arcgis.carto.IWMSLayer;
import com.esri.arcgis.carto.WMSMapLayer;
import com.esri.arcgis.gisclient.IWMSLayerDescription;
import com.esri.arcgis.gisclient.IWMSServiceDescription;
import com.esri.arcgis.gisclient.WMSConnectionName;
import com.esri.arcgis.system.PropertySet;



public class DiskoWMSLayer {
	
	public DiskoWMSLayer(){
		
	}
	
	public WMSMapLayer createWMSLayer() throws IOException{
		System.out.println("createWMSLayer");
		
		// Create an WMSMapLayer Instance - this will be added to the map later
		//IWMSGroupLayer wmsMapLayer;
		
		WMSMapLayer wmsMapLayer = new WMSMapLayer();
		
		// create and configure wms connection name
	    // this is used to store the connection properties
		WMSConnectionName connName = new WMSConnectionName();
		
		PropertySet propSet = new PropertySet();
		propSet.setProperty("URL", "http://dnweb5.dirnat.no/wmsconnector/com.esri.wms.Esrimap/WMS_NB_Friluftsliv?");
		//propSet.setProperty("URL", "http://www.geographynetwork.com/ogc/com.esri.ogc.wms.WMSServlet?ServiceName=ESRI_Pop&");
		//propSet.setProperty("URL", "http://www.ngu.no/wmsconnector/com.esri.wms.Esrimap/SnoSteinSkredfareOmrWMS?");
		//propSet.setProperty("URL", "http://wms.geonorge.no/skwms1/wms.kartdata?");
		//propSet.setProperty("URL", "http://wms.geonorge.no/skwms1/wms.norgeibilder?");
		System.out.println("setter opp connection");
		connName.setConnectionProperties(propSet);
		//uses the name information to connect to the service
		IDataLayer dataLayer = (IDataLayer) wmsMapLayer;
		dataLayer.connect(connName);
		
		//get service description out of the layer
		//the service description contains inforamtion about the wms categories
		//and layers supported by the service
		IWMSServiceDescription serviceDesc = (IWMSServiceDescription) wmsMapLayer.getWMSServiceDescription();
		//serviceDesc.getWMSVersion(); 
		System.out.println("connecta, antall layere = "+ serviceDesc.getLayerDescriptionCount());
		System.out.println("abstract: " + serviceDesc.getWMSAbstract() +", navn: "+serviceDesc.getWMSName()+",  title: "+serviceDesc.getWMSTitle());
		
		System.out.println("ant koordsys: " + serviceDesc.getSRSCount());
		for(int i=0; i < serviceDesc.getSRSCount(); i++){
			System.out.println(serviceDesc.getSRS(i));
		}
		
		//for each wms layer either add the stand-alone layer or
		//group layer to the WMSMapLayer which will be added to ArcMap
		System.out.println("looper..." + serviceDesc.getLayerDescriptionCount() +": wmsversjon: "+ serviceDesc.getWMSVersion());
		for(int i = 0; i < serviceDesc.getLayerDescriptionCount(); i++){
			IWMSLayerDescription layerDesc = (IWMSLayerDescription) serviceDesc.getLayerDescription(i);
			
			ILayer newLayer = null;
			
			if(layerDesc.getLayerDescriptionCount() == 0){	
				System.out.println("null layercount");
				IWMSLayer newWMSLayer = (IWMSLayer) wmsMapLayer.createWMSLayer(layerDesc);	
				newLayer = (ILayer) newWMSLayer;
			}
			else {
				System.out.println("ikke layercount");
				IWMSGroupLayer grpLayer = (IWMSGroupLayer) wmsMapLayer.createWMSGroupLayers(layerDesc);
				newLayer = (ILayer) grpLayer;
			}
			wmsMapLayer.insertLayer(newLayer, 0);//legger til øverst
		}
		
		//Configure the layer before adding it to the map
		ILayer layer = (ILayer) wmsMapLayer;
		layer.setName(serviceDesc.getWMSTitle());
		
		/*
		    '' add layer to Map
		    Dim mxDoc As IMxDocument
		    Set mxDoc = Document
		    mxDoc.FocusMap.AddLayer wmsMapLayer
		    
		    'refresh
		    Dim activeView As IActiveView
		    Set activeView = mxDoc.FocusMap
		    activeView.Refresh
		
		*/
		System.out.println("return createWMSLayer, ant lag: " + wmsMapLayer.getCount());
		System.out.println("WMS title = "+layer.getName());
		return wmsMapLayer;		
		
	}
	
	
}
