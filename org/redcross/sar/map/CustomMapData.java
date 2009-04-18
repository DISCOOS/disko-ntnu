package org.redcross.sar.map;

import java.io.File;

import org.redcross.sar.IApplication;

import com.esri.arcgis.carto.RasterLayer;

import com.esri.arcgis.carto.FeatureLayer;
import com.esri.arcgis.carto.WMSLayer;
//import com.esri.arcgis.datasourcesraster.RasterDataset;
//import com.esri.arcgis.datasourcesraster.RasterWorkspace;
//import com.esri.arcgis.datasourcesraster.RasterWorkspaceFactory;

import com.esri.arcgis.system.FileStream;



public class CustomMapData {
	
	
	public void AddCustomData(IApplication app, File f){
		String fname = f.getName();
		String path = f.getAbsolutePath().substring(0,f.getAbsolutePath().length()-fname.length());		
		DiskoMap map = (DiskoMap) app.getCurrentMap();	
		System.out.println("sjekker ext");
		//må sjekke fil type 
		int i = fname.lastIndexOf(".") + 1;
		String ext = fname.substring(i, fname.length());
		System.out.println("sjekker ext: " +  ext);
		
		
		if(ext.equalsIgnoreCase("tif") || ext.equalsIgnoreCase("tiff")){
			System.out.println("raster");
			AddRasterFile(map, f.getAbsolutePath());	
		}
		else if (ext.equalsIgnoreCase("shp")){
			System.out.println("shp");
			addShapeFile(map, fname, path);
		}
						
		//lagre kartet med de nye dataene
		try{
			System.out.println("Ikke lagret");
			FileStream fs = new FileStream();
			//fs.open("C:\\DISKO\\Disko.mxd", com.esri.arcgis.system.esriFilePermission.esriReadWrite);
			//fs.loadFromFile("C:\\DISKO\\Disko.mxd");
			//map.save(fs, 0);
		}catch (Exception e){
			e.printStackTrace();
			System.out.println("FileStream feilet");			
		}	
		//else hvis meldingsboks
		
	}
	
	private boolean addShapeFile(DiskoMap map, String fname, String path){
		try{
			//map
			FeatureLayer fl = new FeatureLayer();
			map.addShapeFile(path, fname);	
			return true;
			//map.save(mxd, false);//må ha tak i mxd dokumentet
			//mapDocument.save(mapDocument.isUsesRelativePaths(), false);
		}
		catch(Exception e){			
			System.out.println("Add shape failed: " + e);
			return false;
		}
	}
		
	
	
	private boolean AddRasterFile(DiskoMap map, String fullpath){
//		RasterDataset rd = openRasterDataset(path, fname);
		try{			
			RasterLayer rl = new RasterLayer();
			rl.createFromFilePath(fullpath);			
			//adds on top
			map.addLayer(rl, 0);
			//adds in bottom
			//map.addLayer(rl, map.getLayerCount());
			
			return true;
			
		}catch(Exception e){
			System.out.println("Error new RasterLayer");
			return false;
		}
	}
	
	public boolean AddWMSLayer(DiskoMap map, String wmsurl){
//		RasterDataset rd = openRasterDataset(path, fname);
		try{	
			System.out.println("Skal legge til WMSLayer");
			WMSLayer wmsl = new WMSLayer(wmsurl);
			//adds on top	
			map.addLayer(wmsl,0);
			
			return true;
			
		}catch(Exception e){
			System.out.println("Error new WMSLayer");
			return false;
		}
	}
	
	/*private RasterDataset openRasterDataset(String directoryName, String fileName) {
		RasterDataset rasterDataset = null;
		try {
			// Create a workspace factory which then opens a RasterWorkspace and
			// finally open the dataset
			RasterWorkspaceFactory rasterWorkspaceFactory = new RasterWorkspaceFactory();
			RasterWorkspace rasterWorkspace = (RasterWorkspace) rasterWorkspaceFactory
					.openFromFile(directoryName, 0);
			rasterDataset = (RasterDataset) rasterWorkspace
					.openRasterDataset(fileName);
		} catch (Exception e) {
			System.out.println("Threw an exception trying to open a rasterdataset");
			e.printStackTrace();
		}

		return rasterDataset;
	}*/
}
