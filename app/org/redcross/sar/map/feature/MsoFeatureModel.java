package org.redcross.sar.map.feature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.redcross.sar.mso.data.IMsoObjectIf;

import com.esri.arcgis.geodatabase.IEnumRelationshipClass;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureBuffer;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geodatabase.IFeatureDataset;
import com.esri.arcgis.geodatabase.IField;
import com.esri.arcgis.geodatabase.IFields;
import com.esri.arcgis.geodatabase.IIndex;
import com.esri.arcgis.geodatabase.IIndexes;
import com.esri.arcgis.geodatabase.IQueryFilter;
import com.esri.arcgis.geodatabase.ISelectionSet;
import com.esri.arcgis.geodatabase.ISpatialFilter;
import com.esri.arcgis.geodatabase.IWorkspace;
import com.esri.arcgis.geodatabase.esriSpatialRelEnum;
import com.esri.arcgis.geometry.GeometryBag;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.IGeometryCollection;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.IRelationalOperator;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.IPropertySet;
import com.esri.arcgis.system.IUID;

/**
 *
 * @author geira, kennetgu
 *
 */

public class MsoFeatureModel implements IMsoFeatureModel, IFeatureClass {

	private static final long serialVersionUID = 1L;

	protected final int m_shape; // usually esriGeometryType.esriGeometryAny;
	protected final List<IMsoFeature> m_data = new ArrayList<IMsoFeature>();

	/* ==========================================================
	 * Constructors
	 * ========================================================== */

	public MsoFeatureModel(int shape) {

		// prepare
		m_shape = shape;

	}

	/* ==========================================================
	 * Public methods
	 * ========================================================== */

	public void setSpatialReferenceByRef(ISpatialReference srs) {
		// forward to data
		for(IMsoFeature it : m_data) {
			try {
				it.setSpatialReference(srs);
			} catch (AutomationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/* ==========================================================
	 * IMsoFeatureClass implementation
	 * ========================================================== */

	public IMsoFeature getFeature(String id) {
		for (int i = 0; i < m_data.size(); i++) {
			IMsoFeature feature = (IMsoFeature)m_data.get(i);
			if (feature.getID().equals(id)) {
				return feature;
			}
		}
		return null;
	}

	public IMsoFeature getFeature(IMsoObjectIf msoObj)  {
		return getFeature(msoObj.getObjectId());
	}

	public boolean addFeature(IMsoFeature feature) {
		if(!m_data.contains(feature))
			return m_data.add(feature);
		return false;
	}

	public boolean removeFeature(IMsoFeature feature) {
		try {
			feature.setShapeByRef(null);
			feature.setMsoObject(null);
			return m_data.remove(feature);
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public boolean removeAll() {
		try {
			int count = m_data.size();
			if(count>0) {
				// loop over all features and remove them
				for(int i = 0; i < count; i++) {
					m_data.get(i).setShapeByRef(null);
					m_data.get(i).setMsoObject(null);
				}
				m_data.clear();
				return true;
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


	/* ==========================================================
	 * IFeatureClass implementation
	 * ========================================================== */

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
		return m_data.size();
	}

	public IField getAreaField() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public IFeature getFeature(int index) throws IOException,
			AutomationException {
		return (IFeature)m_data.get(index);
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
		return m_shape;
	}

	public IFeatureCursor search(IQueryFilter filter, boolean arg1)
			throws IOException, AutomationException {
		MsoFeatureCursor cursor = new MsoFeatureCursor();
		if (filter instanceof ISpatialFilter) {
			ISpatialFilter spatialFilter = (ISpatialFilter)filter;
			IRelationalOperator filterGeom = (IRelationalOperator)spatialFilter.getGeometry();
			int relation = spatialFilter.getSpatialRel();
			for (int i = 0; i < m_data.size(); i++) {
				IFeature feature = (IFeature)m_data.get(i);
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

	/* ==========================================================
	 * Helper methods
	 * ========================================================== */

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


}
