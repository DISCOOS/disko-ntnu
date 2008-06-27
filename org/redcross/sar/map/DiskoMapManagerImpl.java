package org.redcross.sar.map;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.thread.AbstractDiskoWork;
import org.redcross.sar.thread.DiskoWorkPool;
import org.redcross.sar.util.MapInfoComparator;
import org.redcross.sar.util.mso.Position;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.esri.arcgis.carto.GroupLayer;
import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.controls.MapControl;
import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

public class DiskoMapManagerImpl implements IDiskoMapManager {

	private IDiskoApplication app = null;
	private IDiskoMap printMap = null;
	private MapControl tmpMap = null;
	private Document xmlDoc = null;
	private File xmlFile = null;
	
	private String mxdDoc = null;
	private int isWorking = 0;
	private boolean keepTmpMap = false;
	
	private final List<IDiskoMap> maps = new ArrayList<IDiskoMap>();	
	private final Map<String,MapSourceInfo> mxdDocs = new HashMap<String,MapSourceInfo>();
	
	public DiskoMapManagerImpl(IDiskoApplication app, File file)  throws Exception {
		// prepare
		this.app = app;
		this.xmlFile = file;
		// initialize
		if(!loadXmlFile() || getMxdDocCount()==0) installMxdDocs();
	}

	private EnumSet<IMsoFeatureLayer.LayerCode> getPrintMapLayers() {	
		EnumSet<IMsoFeatureLayer.LayerCode> myLayers;
		myLayers = EnumSet.of(IMsoFeatureLayer.LayerCode.OPERATION_AREA_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.SEARCH_AREA_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.POI_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.AREA_LAYER);
	    return myLayers;
	}
	
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
	
	public boolean loadXmlFile(){
		try {
			
			// notify
			System.out.println("loadXmlFile()::started");
			
			// clear current
			mxdDocs.clear();
			
			// keep temporary map between uses
			setKeepTmpMap(true);
			
			// load xml document
			FileInputStream instream = new FileInputStream(xmlFile);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			dbf.setValidating(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			this.xmlDoc = db.parse(instream);			
			// load map source data
			NodeList elems = xmlDoc.getElementsByTagName("MxdDoc");
			// locate and update
			for (int i = 0; i < elems.getLength(); i++) {
				// create info from xml element
				MapSourceInfo info = createMapSourceInfo((Element)elems.item(i));
				// created?
				if(info!=null)
					mxdDocs.put(info.getMxdDoc(),info);
			}				

			// anything loaded?
			if(mxdDocs.size()>0) {
				
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
						// try all installed mxd documents
						for(String name: mxdDocs.keySet()) {
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
			}
			
			// clear temporary map 
			setKeepTmpMap(false);
			
			// notify
			System.out.println("loadXmlFile()::finished, antall mxd'er installert: " +  mxdDocs.size());
			
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
		
	}
	
	public boolean storeXmlFile() {
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
			Source src = new DOMSource(xmlDoc);
			Result dest = new StreamResult(xmlFile);
			transformer.transform(src, dest);
			return true;
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}	
	
	public IDiskoMap getCurrentMap() {
		return app.getCurrentMap();
	}
	
	public int getMxdDocCount() {
		return mxdDocs.size();
	}
	
	public boolean isMxdDocInstalled(String mxddoc) {
		NodeList elems = xmlDoc.getElementsByTagName("MxdDoc");
		for (int i = 0; i < elems.getLength(); i++) {
			Element elem = (Element)elems.item(i);
			if (mxddoc.equals(elem.getAttribute("name"))) return true;
		}		
		return false;
	}
	
	/**
	 * Reads all mxddoc paths from properties.
	 *
	 */
	public int installMxdDocs() {

		// notify
		System.out.println("installMxdDocs()::started");
		
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
						installMxdDoc(path + "\\" + filename);
					}
				}catch (Exception e){
					e.printStackTrace();
				}
			}			
		}
		
		// update xml file?
		if(mxdDocs.size()>0) storeXmlFile();
		
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
				// try all installed mxd documents
				for(String name: mxdDocs.keySet()) {
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
		System.out.println("installMxdDocs()::started, antall mxd'er installert: " +  mxdDocs.size());
		
		// finished
		return mxdDocs.size();
	}
	
	public boolean installMxdDoc(String mxddoc) {
		// not already installed?
		if(!isMxdDocInstalled(mxddoc)) {
			// check if document is a valid mxd document
			if(checkMxdDoc(mxddoc,false)) {
				// create map source info
				MapSourceInfo info = createMapSourceInfo(mxddoc);
				// created?
				if(info!=null) {
					// update xml file
					addMapSourceInfo(info);
					// install
					mxdDocs.put(mxddoc,info);
					// finished
					return true;
				}
			}
		}
		// failed!
		return false;
	}
	
	private void addMapSourceInfo(MapSourceInfo info) {
		
		// initialize
		Element it = null;
		String mxddoc = info.getMxdDoc();
		NodeList elems = xmlDoc.getElementsByTagName("MxdDoc");
		// locate and update
		for (int i = 0; i < elems.getLength(); i++) {
			Element e = (Element)elems.item(i);
			if (mxddoc.equals(e.getAttribute("name"))) { 
				it = e; break;
			}
		}	
		
		// create?
		if(it==null) {
			// root node
			elems = xmlDoc.getElementsByTagName("DiskoApplication");
			// found?
			if(elems.getLength()>0) {
				// get first element
				Element disko = (Element)elems.item(0); 
				it = xmlDoc.createElement("MxdDoc");
				disko.appendChild(it);
			}
		}		
		// update
		it.setAttribute("name", info.getMxdDoc());
		// get MapBase children nodes
		elems = it.getElementsByTagName("MapBase");
		// delete all
		for (int i = 0; i < elems.getLength(); i++)
			it.removeChild(elems.item(i));
		// add all
		for (int i = 1; i <= info.getMapBaseCount(); i++) {
			// add
			Element e = xmlDoc.createElement("MapBase");
			e.setAttribute("name", info.getMapBase(i));
			it.appendChild(e);
		}
		// get extent node
		Element rc = null;
		elems = it.getElementsByTagName("Extent");
		// not found?
		if(elems.getLength()==0) {
			rc = xmlDoc.createElement("Extent");
			it.appendChild(rc);
		}
		// update extent
		rc.setAttribute("XMin", String.valueOf(info.getXMin()));
		rc.setAttribute("XMax", String.valueOf(info.getXMax()));
		rc.setAttribute("YMin", String.valueOf(info.getYMin()));
		rc.setAttribute("YMax", String.valueOf(info.getYMax()));
				
	}
	

	/**
	 * Uninstalls all mxd documents.
	 *
	 */
	public int uninstallMxdDocs(){
		
		// initialize
		int count = 0;
		
		// loop over all maps an hide them
		for(IDiskoMap map: maps)
			map.setVisible(false);
		
		// loop over all mxd documents
		for(MapSourceInfo it : mxdDocs.values()) {
			if(uninstallMxdDoc(it.getMxdDoc())) count++;
		}
		
		// finished
		return count;
 
	}

	public boolean uninstallMxdDoc(String mxddoc) {
		if(mxdDocs.containsKey(mxddoc)) {
			mxdDocs.remove(mxddoc);
			return true;
		}
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
		if(mxdDocs.containsKey((mxdDoc)))
			return mxdDocs.get(mxdDoc).getMapBaseCount();
		return 0;
	}
	
	/** 
	 * Return name of base layer
	 * 
	 * @param index 1-based base layer index
	 */
	
	public String getMapBase(int index) {
		if(mxdDocs.containsKey((mxdDoc)))
			return mxdDocs.get(mxdDoc).getMapBase(index);
		return null;
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
		// any change?
		if(mxddoc!=null && !mxddoc.equalsIgnoreCase(this.mxdDoc)) {
			// valid file?
			if(isMxdDocInstalled(mxddoc)) {
				// prepare
				this.mxdDoc = mxddoc;					
				// update properties
				app.setProperty("MxdDocument.path", mxddoc);
				// changed
				return true;
			}
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
	
	public MapSourceInfo getMapInfo(String mxdDoc) {
		if(isMxdDocInstalled(mxdDoc)) {
			return mxdDocs.get(mxdDoc);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<MapSourceInfo> getMapInfoList() {
		
		// initialize
		ArrayList<MapSourceInfo> maps = new ArrayList<MapSourceInfo>();
		
		// gather information
		for (MapSourceInfo it : mxdDocs.values()){
			it.setCurrent(it.getMxdDoc().equalsIgnoreCase(this.mxdDoc));
			maps.add(it);
		}	
		
		// sort list		
		Collections.sort(maps, new MapInfoComparator()); 
		
		// return info list
		return maps;
	}
	
	private MapSourceInfo createMapSourceInfo(String mxddoc) {
		try {
			// valid file?
			if(checkMxdDoc(mxddoc,false)) {
				
				// load document to read layers
				MapControl map = getTmpMap(mxddoc,false);
				
				// has map?
				if(map!=null) {
					// get full extent
					IEnvelope extent = map.getFullExtent();
					// create object
					MapSourceInfo info = new MapSourceInfo();
					// initialize
					info.setMxdDoc(mxddoc);
					info.setCurrent(false);
					info.setType("DISKO Standard");
					info.setStatus("OK");
					Position p = MapUtil.getMsoPosistion((Point)extent.getUpperLeft());
					info.setXMin(p.getPosition().x);
					info.setYMax(p.getPosition().y);
					p = MapUtil.getMsoPosistion((Point)extent.getLowerRight());
					info.setXMax(p.getPosition().x);
					info.setYMin(p.getPosition().y);
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
							if(name.startsWith("MAP_")) info.addMapBase(name);
						}
					}
					return info;
				}
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failed
		return null;
	}
	
	private MapSourceInfo createMapSourceInfo(Element e) {
		try {
			// create object
			MapSourceInfo info = new MapSourceInfo();
			// initialize
			info.setCurrent(false);
			info.setMxdDoc(e.getAttribute("name"));
			NodeList extents = e.getElementsByTagName("Extent");
			if(extents.getLength()>0) {
				Element extent = (Element)extents.item(0);
				info.setXMin(Double.valueOf(extent.getAttribute("XMin")));
				info.setXMax(Double.valueOf(extent.getAttribute("XMax")));
				info.setYMin(Double.valueOf(extent.getAttribute("YMin")));
				info.setYMax(Double.valueOf(extent.getAttribute("YMax")));
			}
			info.setType("DISKO Standard");
			info.setStatus("OK");			
			// get MapBase children nodes
			NodeList bases = e.getElementsByTagName("MapBase");
			// add all
			for (int i = 0; i < bases.getLength(); i++) {
				// add
				Element base = (Element)bases.item(i);
				info.addMapBase(base.getAttribute("name"));
			}	
			return info;
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		// failed
		return null;
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
	
	/**
	 * This method matches the active operation with a suitable map. <p>   
	 * It is automatically called after the active operation is changed, by 
	 * IDiskoApplication.<p>
	 * @param autoselect - if <code>true</code> the map that covers the active
	 * operation the most is selected, else an MapOptionDialog is shown allowing
	 * the user to select a map manually.
	 * @param useWorkPool - if <code>true</code> the work is done on the Disko Work
	 * Pool and the method will not block, otherwise the method will block until
	 * the selection processes is done.
	 * 
	 */
	public void selectMap(boolean autoselect, boolean useWorkPool) {

		try {
			// create work
			SelectMapWork work = new SelectMapWork(autoselect);
			// forward to work pool?
			if(useWorkPool)
				DiskoWorkPool.getInstance().schedule(work);
			else {
				work.doWork();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	public boolean isMap(Component c) {
		return maps.contains(c);
	}
	
	/* ===============================================================================
	 * Internal classes
	 * =============================================================================== */
	
	class SelectMapWork extends AbstractDiskoWork<Boolean> {

		private boolean m_autoSelect = false;

		/**
		 * Constructor
		 * 
		 * @param autoselect
		 */
		SelectMapWork(boolean autoselect) throws Exception {
			// forward
			super(false,true,WorkOnThreadType.WORK_ON_SAFE,
					"Analyserer installerte kart",0,true,false,false,0);
			// prepare
			m_autoSelect = autoselect;
		}

		/**
		 * Worker
		 * 
		 */	   
		@Override
		public Boolean doWork() {
			
			try {
				// has maps?
				if(mxdDocs.size()>0) {
					// initialize
					Map<String,MapSourceInfo> maps = new HashMap<String,MapSourceInfo>();
					// get map with mso data
					IDiskoMap map = getPrintMap();
					// get extent of operation 
					Envelope extent = (Envelope)MapUtil.getOperationExtent(map);
					// has no extent?
					if(extent==null) {
						// loop over all map and reset coverage index to unknown
						for(MapSourceInfo it: mxdDocs.values()) {
							it.setCoverage(0);
							maps.put(it.getMxdDoc(),it);
						}
					}
					else {
						// get a geographic coordinate system spatial reference
						ISpatialReference srs = map.getSpatialReference();
						// loop over all map and select intersecting maps
						for(MapSourceInfo it: mxdDocs.values()) {
							// get full extent of map
							IEnvelope e = (Envelope)it.getExtent(srs);
							// get name
							String name = it.getMxdDoc();
							// set current selection
							it.setCurrent(name.equalsIgnoreCase(mxdDoc));
							// is disjoint?
							if(extent.disjoint(e)) it.setCoverage(1);
							else {
								// set coverage index
								it.setCoverage(extent.within(e) ? 3 : 2);
								// add to available maps
								maps.put(it.getMxdDoc(),it);
							}
						}
					}
					// is current map within this list?
					if(!maps.containsKey(mxdDoc)) {
						// automatic selection?
						if(m_autoSelect && maps.size()>0) {
							// try first full coverage
							for(MapSourceInfo it: maps.values()) {
								if(it.getCoverage()==2) {
									String name = it.getMxdDoc();
									if(setMxdDoc(name));
										return true;
								}
							}
							// full coverage map not found, use first partial coverage map
							String name = maps.values().iterator().next().getMxdDoc();
							// finished
							return setMxdDoc(name);
							
						}
						else {
							// allow user to select a map
							return true;
						}
					}
					// use current (does coverage operation area)
					return isMxdDocInstalled(mxdDoc);
				}
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}

		/**
		 * done 
		 * 
		 * Executed on the Event Dispatch Thread.
		 */
		@Override
		public void afterDone() {

			// get result
			if(get() && m_autoSelect) {
				// allow user to select map from list
				app.getUIFactory().getMapOptionDialog()
					.selectMap("Velg kart for operasjonsområdet", 
							new ArrayList<MapSourceInfo>(mxdDocs.values()));
			}
	
		}		
		
	}	
	
}
