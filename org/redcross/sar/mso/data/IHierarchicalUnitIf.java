package org.redcross.sar.mso.data;

import java.util.List;

import org.redcross.sar.mso.IMsoModelIf;

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

    public IMsoModelIf.ModificationState getSuperiorUnitState();

    public IMsoReferenceIf<IHierarchicalUnitIf> getSuperiorUnitAttribute();

    /**
     * Generate list of subordinates
     *
     * @return The list
     */
    public List<IHierarchicalUnitIf> getSubOrdinates();

}
