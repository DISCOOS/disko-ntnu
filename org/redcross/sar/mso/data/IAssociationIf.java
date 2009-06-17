package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;


public interface IAssociationIf extends IMsoObjectIf {

    public void setOrganization(String organization);

	public String getOrganization();

	public IData.DataOrigin getOrganizationState();

    public IMsoAttributeIf.IMsoStringIf getOrganizationAttribute();

    
    public void setDivision(String aDepartment);

    public String getDivision();

    public IData.DataOrigin getDivisionState();

    public IMsoAttributeIf.IMsoStringIf getDivisionAttribute();
    
    
	public void setDepartment(String department);

	public String getDepartment();

	public IData.DataOrigin getDepartmentState();

    public IMsoAttributeIf.IMsoStringIf getDepartmentAttribute();	
	
}
