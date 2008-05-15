package org.redcross.sar.map;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.util.MapInfoComparator;

import com.esri.arcgis.carto.GroupLayer;
import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.controls.MapControl;
import com.esri.arcgis.interop.AutomationException;

public class DiskoMapManagerImpl implements IDiskoMapManager {

	private IDiskoApplication app = null;
	private IDiskoMap printMap = null;
	private MapControl tmpMap = null;
	
	private String mxdDoc = null;
	private int isWorking = 0;
	private boolean keepTmpMap = false;
	
	private final List<IDiskoMap> maps = new ArrayList<IDiskoMap>();	
	private final List<String> mapBases = new ArrayList<String>();
	private final List<String> mxdDocs = new ArrayList<String>();
	
	public DiskoMapManagerImpl(IDiskoApplication app) {
		// prepare
		this.app = app;
		// initialize
		installMxdDocs();		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.redcross.sar.map.IDiskoMapManager#getMapInstance()
	 */
	public IDiskoMap createMap(EnumSet<IMsoFeatureLayer.LayerCode> myLayers) {
		DiskoMap map = null;
		try {
			// create new disko map instance
			map = new DiskoMap(this, app.getMsoModel(), myLayers);
			// add to internal collection
			maps.add(map);
			// prepare print map to saves some time later
			getPrintMap();
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
	
	public IDiskoMap getPrintMap(){
		try {
			if (printMap == null){
				printMap = new DiskoMap(this, app.getMsoModel(),getPrintMapLayers());
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return printMap;
	}
	
	public IDiskoMap getCurrentMap() {
		return app.getCurrentMap();
	}
	
	private EnumSet<IMsoFeatureLayer.LayerCode> getPrintMapLayers() {	
		EnumSet<IMsoFeatureLayer.LayerCode> myLayers;
		myLayers = EnumSet.of(IMsoFeatureLayer.LayerCode.OPERATION_AREA_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.SEARCH_AREA_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.POI_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.AREA_LAYER);
	    return myLayers;
	}
	
	public int getInstallMxdDocCount() {
		return mxdDocs.size();
	}
	
	/**
	 * Removes all mxd documents.
	 *
	 */
	public void uninstallMxdDocs(){
		
		// clear current
		mxdDocs.clear();
		mapBases.clear();
		
		// loop over all maps an hide them
		for(IDiskoMap map: maps)
			map.setVisible(false);
		
	}
	/**
	 * Reads all mxddoc paths from properties.
	 *
	 */
	public int installMxdDocs(){

		// uninstall first?
		if(mxdDocs.size()>0) uninstallMxdDocs();
		
		// notify
		System.out.println("installMxdDocs()");
		
		// clear current
		mxdDocs.clear();
		
		// keep temporary map between uses
		setKeepTmpMap(true);
		
		// load available mxd document file paths
		String path = app.getProperty("MxdDocument.catalog.path");	
		File f = FileSystemView.getFileSystemView().createFileObject(path);
		File[] files = FileSystemView.getFileSystemView().getFiles(f, true);
		for (int i=0; i < files.length; i++){			
			String filename = files[i].getName();
			if (filename.contains(".")){
				try{
					if (filename.substring(filename.lastIndexOf(".")).equalsIgnoreCase(".mxd")){
						addMxdDoc(path,filename);
					}
				}catch (Exception e){
					e.printStackTrace();
				}
			}			
		}
		
		// get current mxd document
		String mxddoc = this.mxdDoc;
		
		// get from name from propertes?
		mxddoc = (mxddoc==null) ? app.getProperty("MxdDocument.path") : mxddoc;

		// reset to allow for change
		this.mxdDoc = null;

		// forward
		if(!setMxdDoc(mxddoc)) {
			// retry a last time?
			if(mxdDocs.size()>0) {
				for(String name: mxdDocs) {
					// retry
					if(setMxdDoc(name)) break;
				}
				// failure?
				if(this.mxdDoc==null) {
					// TODO: Should this failure be allowed?
					System.out.println("Ingen mxd'er funnet!");
				}
			}
		}			
		
		// clear temporary map 
		setKeepTmpMap(false);
		
		// notify
		System.out.println("installMxdDocs(), antall mxd'er funnet: " + mxdDocs.size());
		// finished
		return mxdDocs.size();
	}
	
	public boolean addMxdDoc(String path, String mxddoc) {
		// get full url
		String url = path + "\\" + mxddoc;
		// check if document is a valid map document
		if(checkMxdDoc(url,false)) {
			// register as base map
			return mxdDocs.add(mxdDoc);
		}
		// failed!
		return false;
	}

	public boolean checkMxdDoc(String mxddoc, boolean keep) {
		try {
			// prepare map for checking?
			if(tmpMap==null)
				tmpMap = new MapControl();					
			// forward
			boolean isValid = tmpMap.checkMxFile(mxddoc);
			// clear?
			if(!(keep || keepTmpMap)) tmpMap = null;
			// finished
			return isValid;
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failure
		return false;		
	}
	
	public MapControl getTmpMap(String mxddoc, boolean keep) {
		try {
			// get current
			MapControl map = tmpMap;
			// remove reference?
			if(!(keep || keepTmpMap)) tmpMap = null;
			// prepare map for checking?
			if(map==null)
				map = new MapControl();					
			// forward
			map.loadMxFile(mxddoc,null,null);
			// keep?
			if(keep || keepTmpMap) tmpMap = map;
			// finished
			return map;
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failure
		return null;		
	}
	
	private void setKeepTmpMap(boolean keep) {
		// update
		keepTmpMap = keep;
		// clear current?
		if(!keep) tmpMap = null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.redcross.sar.map.IDiskoMapManager#getMaps()
	 */
	public List<IDiskoMap> getMaps() {
		return maps;
	}
	
	public int getMapBaseCount() {
		return mapBases.size();
	}
	
	/** 
	 * Return name of base layer
	 * 
	 * @param index 1-based base layer index
	 */
	
	public String getMapBase(int index) {
		return mapBases.get(index-1);
	}
	
	public int toggleMapBase() {
		
		// not allowed?
		if(isWorking() || getMapBaseCount()==0) return 0;

		try {
			// get current map
			IDiskoMap map = app.getCurrentMap();
			
			// has map?
			if(map!=null) {
				// forward
				int index = map.toggleMapBase();
				// refresh?
				if(index>0) map.refreshMapBase();
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// failed
		return 0;
	}	
	
	
	public String getMxdDoc() {
		return this.mxdDoc;
	}

	public boolean setMxdDoc(String mxddoc) {
		try {
			// any change?
			if(mxddoc!=null && !mxddoc.equalsIgnoreCase(this.mxdDoc)) {
				// valid file?
				if(checkMxdDoc(mxddoc,false)) {
					// load document to read layers
					MapControl map = getTmpMap(mxddoc,false);
					// has map?
					if(map!=null) {
						// get layer count
						int count = map.getLayerCount(); 
						// loop over all layers and look for map base layers
						for(int i=0;i<count;i++) {
							// get map
							ILayer l = map.getLayer(i);
							// is a group layer?
							if(l instanceof GroupLayer) {
								// get name
								String name = l.getName();
								// is name given in disko standard base layer name format?
								if(name.startsWith("MAP_")) {
									mapBases.add(name);
								}
							}
							
						}
						// prepare
						this.mxdDoc = mxddoc;					
						// update properties
						app.setProperty("MxdDocument.path", mxddoc);
						// changed
						return true;
					}
				}
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// not changed
		return false;
	}
	
	public boolean loadMxdDoc() {

		// consume?
		if(isWorking()) return false;

		// prevent reentry
		setIsWorking();
		
		// initialize counter
		int count = 0;
		
		for(IDiskoMap map: maps) {
		
			// forward (only visible maps will be updated)
			if (map.loadMxdDoc()) count++;
			
		}
			
		// Speed up the load process
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// load print map later
				getPrintMap().loadMxdDoc();
			}
		});
		
		// stop working
		setIsNotWorking();
		
		// finished
		return count>0;
		
	}	
	
	@SuppressWarnings("unchecked")
	public List<MapSourceInfo> getMapsInfo() {
		
		// initialize
		ArrayList<MapSourceInfo> maps = new ArrayList<MapSourceInfo>();
		
		// gather information
		for (int i=0; i<mxdDocs.size(); i++){
			MapSourceInfo info = new MapSourceInfo();
			String path = app.getProperty("MxdDocument.catalog.path");
			String mxdDoc = path + "/" + this.mxdDocs.get(i);
			info.setMxdDoc(mxdDoc);
			info.setCurrent(mxdDoc.equalsIgnoreCase(this.mxdDoc));
			info.setMxdPath(app.getProperty("MxdDocument.catalog.path"));			
			info.setType("DISKO Standard");
			info.setStatus("OK");			
			maps.add(info);
		}	
		
		//sort list		
		Collections.sort(maps, new MapInfoComparator()); 
		
		// return info list
		return maps;
	}
	
	/*
	public void initWMSLayers() throws IOException{
		
		// create a
		DiskoWMSLayer wms = new DiskoWMSLayer();
		DiskoMap map = (DiskoMap) app.getCurrentMap();
		IMap focusMap = map.getActiveView().getFocusMap();
		System.out.println(map.getLayerCount());
		try{
			ILayer wmsLayer = (ILayer) wms.createWMSLayer();
			wmsLayer.setVisible(true);
			focusMap.addLayer(wmsLayer);			
			System.out.println("WMS layer " + wmsLayer.getName() + " added");
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		map.getActiveView().refresh();
		
		System.out.println("Updated layer count:" + map.getLayerCount());
		
		for (int i = 0; i < focusMap.getLayerCount(); i++){
			ILayer lay = focusMap.getLayer(i);
			System.out.println(lay.getName());
			
		}
		
	}
	*/
	
	private boolean isWorking() {
		return (isWorking>0);
	}

	private int setIsWorking() {
		isWorking++;
		return isWorking; 
	}
	
	private int setIsNotWorking() {
		if(isWorking>0) {
			isWorking--;
		}
		return isWorking; 
	}	
}
