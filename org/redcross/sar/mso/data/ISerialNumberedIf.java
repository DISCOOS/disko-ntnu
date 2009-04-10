package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;

/**
 * Interface for objects with serial numbers
 */
public interface ISerialNumberedIf extends IMsoObjectIf
{
    /**
     * Set serial number
     *
     * @param aNumber The serial number
     */
    public void setNumber(int aNumber);

    /**
     * Get serial number
     *
     * @return The serial number
     */
    public int getNumber();

    /**
     * Get serial number {@link org.redcross.sar.mso.IMsoModelIf.ModificationState Modification state}
     *
     * @return The serial number Modification state.
     */
    public IMsoModelIf.ModificationState getNumberState();

    /**
     * Get serial number attribute
     *
     * @return The serial number attribute
     */
    public IAttributeIf.IMsoIntegerIf getNumberAttribute();
}
