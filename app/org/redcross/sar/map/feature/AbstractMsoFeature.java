package org.redcross.sar.map.feature;

import java.io.IOException;
import java.util.List;

import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.util.mso.IGeodataIf;

import com.esri.arcgis.geodatabase.IFields;
import com.esri.arcgis.geodatabase.IObjectClass;
import com.esri.arcgis.geodatabase.ITable;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.IClone;

public abstract class AbstractMsoFeature implements IMsoFeature {

	private static final long serialVersionUID = 1L;

	protected IMsoObjectIf msoObject;
	protected IGeometry geometry;
	protected ISpatialReference srs;

	protected String caption = "";

	protected boolean isSelected = false;
	protected boolean isVisible = true;
	protected boolean isEditing = false;
	protected boolean isDirty = false;

	public AbstractMsoFeature() {}

	public Object getID() {
		return msoObject.getObjectId();
	}

	public IMsoObjectIf getMsoObject() {
		return msoObject;
	}

	public void setMsoObject(IMsoObjectIf msoObject) throws IOException, AutomationException {
		if(this.msoObject != msoObject) {
			// update
			this.msoObject = msoObject;
			// forward
			setDirty(true);
		}
	}

	/**
	 * Override this!
	 */

	public boolean isMsoChanged(IMsoObjectIf msoObj) {
		return false;
	}

	public boolean create() throws IOException, AutomationException {
		if(srs!=null && msoObject!=null && !msoObject.isDeleted()) {
			return true;
		}
		return false;
	}

	public void setSelected(boolean isSelected) {
		if(this.isSelected!=isSelected) {
			this.isSelected = isSelected;
			setDirty(true);
		}
	}

	public boolean isSelected() {
		return isSelected;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		if(this.isVisible!=isVisible) {
			this.isVisible=isVisible;
			setDirty(true);
		}
	}

	public Object getGeodata() {
		return null;
	}

	public int getGeodataCount() {
		return 0;
	}

	public IEnvelope getExtent() throws IOException, AutomationException {
		if (geometry != null) {
			return geometry.getEnvelope();
		}
		return null;
	}

	public IGeometry getShape() throws IOException, AutomationException {
		return geometry;
	}

	public IGeometry getShapeCopy() throws IOException, AutomationException {
		return (IGeometry)((IClone)geometry).esri_clone();
	}

	public void setShapeByRef(IGeometry geom) throws IOException, AutomationException {
		setDirty(true);
	}

	public int getFeatureType() throws IOException, AutomationException {
		return com.esri.arcgis.geodatabase.esriFeatureType.esriFTSimple;
	}

	public IObjectClass esri_getClass() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public void delete() throws IOException, AutomationException {
		// TODO: Implement delete
	}

	public int getOID() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public ITable getTable() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isHasOID() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return false;
	}

	public void store() throws IOException, AutomationException {
		// TODO Auto-generated method stub
	}

	public IFields getFields() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getValue(int arg0) throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setValue(int arg0, Object arg1) throws IOException, AutomationException {
		// TODO Auto-generated method stub
	}

	public void setSpatialReference(ISpatialReference srs) throws IOException, AutomationException {
		if(this.srs!=srs) {
			this.srs = srs;
			//setDirty(true);
		}
	}

	public ISpatialReference getSpatialReference() throws IOException, AutomationException {
		return srs;
	}

	public boolean isDirty() {
		return isDirty;
	}

	public void setDirty(boolean isDirty) {
		if(this.isDirty!=isDirty) {
			this.isDirty = isDirty;
			/*
			if(this instanceof UnitFeature)
				System.out.println(isDirty+"#"+this);
			*/
		}
	}

	public String getCaption() {
		return this.caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	protected boolean isGeodataChanged(IGeodataIf geodata, List<Integer> changeList, int index) {
		// get count
		int changeCount = geodata.getChangeCount();
		// changed?
		return (changeList.size()<=changeCount || !changeList.get(index).equals(changeCount));
	}


}


