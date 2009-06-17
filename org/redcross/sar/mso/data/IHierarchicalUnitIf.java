package org.redcross.sar.mso.data;

import java.util.List;

import org.redcross.sar.data.IData;


/**
 *
 */
public interface IHierarchicalUnitIf extends IMsoObjectIf
{
    /**
     * Define superior IHierarchicalUnitIf
     *
     * @param aSuperior The new superior unit
     * @return False if error (circular reference), otherwise True
     */
    public boolean setSuperiorUnit(IHierarchicalUnitIf aSuperior);

    /**
     * Get superior IHierarchicalUnitIf
     *
     * @return The superior unit
     */
    public IHierarchicalUnitIf getSuperiorUnit();

    public IData.DataOrigin getSuperiorUnitState();

    public IMsoRelationIf<IHierarchicalUnitIf> getSuperiorUnitAttribute();

    /**
     * Generate list of subordinates
     *
     * @return The list
     */
    public List<IHierarchicalUnitIf> getSubOrdinates();

}
