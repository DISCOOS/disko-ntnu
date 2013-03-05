package org.redcross.sar.mso.data;

import org.redcross.sar.mso.data.IMsoObjectIf.IObjectIdIf;

/**
 *
 */
public interface IUnitListIf extends IMsoListIf<IUnitIf>
{
    public IVehicleIf createVehicle(String anIdentifier);

    public IVehicleIf createVehicle(IMsoObjectIf.IObjectIdIf anObjectId);

    public IBoatIf createBoat(String anIdentifier);

    public IBoatIf createBoat(IMsoObjectIf.IObjectIdIf anObjectId);
    
    public IDogIf createDog(String anIdentifier);
    
    public IDogIf createDog(IObjectIdIf objectId);
    
    public IAircraftIf createAircraft(String anIdentifier);

	public IAircraftIf createAircraft(IObjectIdIf objectId);
	
	public ITeamIf createTeam(String anIdentifier);

	public ITeamIf createTeam(IObjectIdIf objectId);

    /**
     * Find a unit with a given unit number.
     * @param aUnitNr The required number
     * @return The actual unit, or <code>null</code> if non-existent.
     */
    public IUnitIf getUnit(int aUnitNr);
}
