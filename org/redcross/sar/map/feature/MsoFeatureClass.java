package org.redcross.sar.map.feature;

import java.io.IOException;
import java.util.ArrayList;

import com.esri.arcgis.geodatabase.IEnumRelationshipClass;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureBuffer;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geodatabase.IFeatureDataset;
import com.esri.arcgis.geodatabase.IField;
import com.esri.arcgis.geodatabase.IFields;
import com.esri.arcgis.geodatabase.IGeoDataset;
import com.esri.arcgis.geodatabase.IIndex;
import com.esri.arcgis.geodatabase.IIndexes;
import com.esri.arcgis.geodatabase.IQueryFilter;
import com.esri.arcgis.geodatabase.ISelectionSet;
import com.esri.arcgis.geodatabase.ISpatialFilter;
import com.esri.arcgis.geodatabase.IWorkspace;
import com.esri.arcgis.geodatabase.esriSpatialRelEnum;
import com.esri.arcgis.geometry.GeometryBag;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.IGeometryCollection;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.IRelationalOperator;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.IPropertySet;
import com.esri.arcgis.system.IUID;

public class MsoFeatureClass implements IFeatureClass, IGeoDataset {

	private static final long serialVersionUID = 1L;
	protected ISpatialReference srs = null;
	protected ArrayList<IMsoFeature> data = null;
	protected int shapeType = 7; // esriGeometryType.esriGeometryAny
	
	public MsoFeatureClass() {
		data = new ArrayList<IMsoFeature>();
	}
	
	public MsoFeatureClass(int shapeType) {
		data = new ArrayList<IMsoFeature>();
		this.shapeType = shapeType;		
	}
	
	public IMsoFeature getFeature(String id) {
		for (int i = 0; i < data.size(); i++) {
			IMsoFeature feature = (IMsoFeature)data.get(i);
			if (feature.getID().equals(id)) {
				return feature;
			}
		}
		return null;
	}
	
	public void addFeature(IMsoFeature feature) {
		data.add(feature);
	}
	
	public void removeFeature(IMsoFeature feature) throws IOException, AutomationException {
		feature.setShapeByRef(null);
		feature.setMsoObject(null);
		data.remove(feature);
	}
	
	public void removeAll() throws AutomationException, IOException {
		int count = data.size();
    	// loop over all features and remove them
    	for(int i = 0; i < count; i++) {
    		data.get(i).setShapeByRef(null);
    		data.get(i).setMsoObject(null);
    	}
    	data.clear();
	}

	public IFeatureCursor IFeatureClass_insert(boolean arg0)
			throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public IFeatureCursor IFeatureClass_update(IQueryFilter arg0, boolean arg1)
			throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public IFeature createFeature() throws IOException, AutomationException {
		return null;
	}

	public IFeatureBuffer createFeatureBuffer() throws IOException,
			AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public int featureCount(IQueryFilter arg0) throws IOException,
			AutomationException {
		return data.size();
	}

	public IField getAreaField() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public IFeature getFeature(int index) throws IOException,
			AutomationException {
		return (IFeature)data.get(index);
	}

	public int getFeatureClassID() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public IFeatureDataset getFeatureDataset() throws IOException,
			AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getFeatureType() throws IOException, AutomationException {
		return com.esri.arcgis.geodatabase.esriFeatureType.esriFTSimple;
	}

	public IFeatureCursor getFeatures(Object arg0, boolean arg1)
			throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public IField getLengthField() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getShapeFieldName() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getShapeType() throws IOException, AutomationException {
		return shapeType;
	}

	public IFeatureCursor search(IQueryFilter filter, boolean arg1)
			throws IOException, AutomationException {
		MsoFeatureCursor cursor = new MsoFeatureCursor();
		if (filter instanceof ISpatialFilter) {
			ISpatialFilter spatialFilter = (ISpatialFilter)filter;
			IRelationalOperator filterGeom = (IRelationalOperator)spatialFilter.getGeometry();
			int relation = spatialFilter.getSpatialRel();
			for (int i = 0; i < data.size(); i++) {
				IFeature feature = (IFeature)data.get(i);
				IGeometry geom = feature.getShape();
				if(geom instanceof GeometryBag) {
					// get bag
					GeometryBag bag = (GeometryBag)geom;
					// only match valid geometries (line, polygon and point)
					for(int j=0;j<bag.getGeometryCount();j++) {
						// get geometry
						geom = bag.getGeometry(j);
						// line, polygon or point?
						if(geom instanceof IPoint || 
								geom instanceof IGeometryCollection) {
							if(matches(filterGeom,geom,relation)){
								cursor.add(feature);
							}							
						}
					}
				}
				else if(matches(filterGeom,geom,relation)){
					cursor.add(feature);
				}

			}
		}
		return cursor;
	}
		
	private boolean matches(IRelationalOperator filter, IGeometry geom, int relation) 
		throws AutomationException, IOException {
		
		if(geom != null) { 
			switch(relation) {
			case esriSpatialRelEnum.esriSpatialRelContains:
				return filter.contains(geom);
			case esriSpatialRelEnum.esriSpatialRelCrosses:
				return filter.crosses(geom);
			case esriSpatialRelEnum.esriSpatialRelOverlaps:
				return filter.overlaps(geom);
			case esriSpatialRelEnum.esriSpatialRelTouches:
				return filter.touches(geom);
			case esriSpatialRelEnum.esriSpatialRelWithin:
				return filter.within(geom);
			default:
				return !filter.disjoint(geom);
			}
		}	
		return false;
	}

	public ISelectionSet select(IQueryFilter arg0, int arg1, int arg2,
			IWorkspace arg3) throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAliasName() throws IOException, AutomationException {
		return null;
	}

	public int getObjectClassID() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public IEnumRelationshipClass getRelationshipClasses(int arg0)
			throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public void addField(IField arg0) throws IOException, AutomationException {
		// TODO Auto-generated method stub

	}

	public void addIndex(IIndex arg0) throws IOException, AutomationException {
		// TODO Auto-generated method stub

	}

	public void deleteField(IField arg0) throws IOException,
			AutomationException {
		// TODO Auto-generated method stub

	}

	public void deleteIndex(IIndex arg0) throws IOException,
			AutomationException {
		// TODO Auto-generated method stub

	}

	public int findField(String arg0) throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public IUID getCLSID() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public IUID getEXTCLSID() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getExtension() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public IPropertySet getExtensionProperties() throws IOException,
			AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public IFields getFields() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public IIndexes getIndexes() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getOIDFieldName() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isHasOID() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return false;
	}

	public IEnvelope getExtent() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setSpatialReference(ISpatialReference srs) throws IOException, AutomationException {
		this.srs = srs;
	}

	public ISpatialReference getSpatialReference() throws IOException, AutomationException {
		return srs;
	}
}
