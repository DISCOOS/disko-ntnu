package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;

public interface IAssociationIf extends IMsoObjectIf {

    public void setOrganization(String organization);

	public String getOrganization();

	public IMsoModelIf.ModificationState getOrganizationState();

    public IAttributeIf.IMsoStringIf getOrganizationAttribute();

    
    public void setDivision(String aDepartment);

    public String getDivision();

    public IMsoModelIf.ModificationState getDivisionState();

    public IAttributeIf.IMsoStringIf getDivisionAttribute();
    
    
	public void setDepartment(String department);

	public String getDepartment();

	public IMsoModelIf.ModificationState getDepartmentState();

    public IAttributeIf.IMsoStringIf getDepartmentAttribute();	
	
}
