/**
 * 
 */
package org.redcross.sar.ds.ete;

import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;

import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.carto.FeatureLayer;
import com.esri.arcgis.carto.RasterLayer;

/**
 *  Route Cost property class
 *  
 * @author kennetgu
 *
 */

public class RouteCostFeatureMap {
	
	protected ResourceBundle m_res;	  	
  	
	private int m_layerCount;
	private int m_mappingCount;
	private int m_default;
	private boolean m_raster;
	private String m_key;
	private String m_name;
  	private HashMap<String,String> m_fields;
  	private HashMap<String,ILayer> m_layers;
  	private HashMap<String,Integer> m_mapping;
  	private IDiskoMap m_map;
	
	/**
	 *  Constructor initialize the maximum index value
	 *  
	 */		
	public RouteCostFeatureMap(RouteCostProps parent, String key) {

		// save resource bundle handle
		m_res = parent.getResource();
		
		// save map handle
		m_map = parent.getMap();
		
		// save property key
		m_key = key;
		
		// get property name
		m_name = m_res.getString(key);
		
		// forward
		fill();
	}

	/**
	 *  Fills map from resource file
	 *  
	 */		
	@SuppressWarnings("unchecked")
	public void fill() {
		
		// create array lists
		m_fields = new HashMap<String,String>();
		m_layers = new HashMap<String,ILayer>();
		m_mapping = new HashMap<String,Integer>();
		
		// initialize
		m_default = 0;
		
		try {
			// get layer count
			m_layerCount = Integer.valueOf(getProp(m_key + ".LAYER.COUNT")).intValue();
			
			// loop over all layers
			for(int i=0;i<m_layerCount;i++){
				// get flag
				m_raster = Boolean.valueOf(getProp(m_key + ".LAYER.RASTER." + i)).booleanValue();
				// raster layer?
				if (m_raster) {
					// get layer key
					String key = getProp(m_key + ".LAYER.KEY." + i);
					// put empty field name (raster has no field name)
					m_fields.put(key,"");
					// create raster layer
					RasterLayer layer = new RasterLayer();
					layer.createFromFilePath(getProp(m_key + ".LAYER.FILE." + i));
					// add layer
					m_layers.put(key, layer);
				}
				else {
					// get layer key
					String key = getProp(m_key + ".LAYER.KEY." + i);
					// get field name
					m_fields.put(key,getProp(m_key + ".LAYER.FIELD." + i));
					// add layer
					m_layers.put(key, (ILayer)m_map.getFeatureLayer(getProp(m_key + ".LAYER.NAME." + i)));
				}
			}
			// has feature class?
			if (Boolean.valueOf(getProp(m_key + ".MAPPING")).booleanValue()){
				// get mapping count
				m_mappingCount = Integer.valueOf(getProp(m_key + ".MAPPING.COUNT")).intValue();
				// get mapping
				for(int i=0;i<m_mappingCount;i++){
					String map = getProp(m_key + ".MAPPING." + i);
					if (map.length()>0) {
						String[] splits = map.split("#");
						for(int j=0; j<splits.length;j++){
							m_mapping.put(splits[j], i);
						}
					}
				}						
				// get default map index
				m_default = Integer.valueOf(getProp(m_key + ".MAPPING.DEFAULT"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *  Gets property resource bundle key
	 *  
	 */		
	public String getKey() {
		return m_key;
	}
	
	/**
	 *  Gets property variable name
	 *  
	 */		
	public String getName() {
		return m_name;
	}
	
	/**
	 *  Gets is property is a constant. If false, then the property have parameters
	 *  
	 */		
	public boolean isRaster() {
		return m_raster;
	}
	
	/**
	 *  Gets layer count
	 *  
	 */		
	public int getLayerCount() {
		return m_layerCount;
	}
	
	/**
	 *  Gets mapping count
	 *  
	 */		
	public int geMappingCount() {
		return m_mappingCount;
	}
	
	/**
	 *  Gets property index from map value
	 *  
	 */		
	public int getIndexFromMap(int value) {
		// initialize
		int index = m_default;
		String key = String.valueOf(value);
		// Has map?
		if (m_mapping.containsKey(key)) {
			// get map index
			index = m_mapping.get(key);
		} 
		// return index
		return index;
	}
	
	/**
	 *  Gets layer
	 *  
	 */		
	public ILayer getLayer(String name) {
		return m_layers.get(name);
	}
	
	/**
	 *  Gets field name
	 *  
	 */		
	public String getField(String name) {
		if (getLayer(name) instanceof FeatureLayer)
			return m_fields.get(name);
		else
			return null;
	}

	/**
	 *  Gets field index
	 *  
	 */		
	public int getFieldIndex(String name) {
		// initialize
		int index = -1;
		// get layer
		ILayer layer = getLayer(name);
		// get index
		if (layer instanceof FeatureLayer) {
			
			try {
				// get field index
				index = ((FeatureLayer)layer).getFields().findField(m_fields.get(name));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return index;
	}
	
	/**
	 *  get property from resourse bundle
	 *  
	 */		
	private String getProp(String name) {
		String value = null;
		// try to get properties
		try
		{
			value = m_res.getString(name);
		}
		catch(MissingResourceException e)
		{
			System.err.println("Unable to get property " + name + " in RouteCostParams");
		}
		return value;		
	}
}
