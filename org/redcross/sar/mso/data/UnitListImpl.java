package org.redcross.sar.mso.data;

import org.redcross.sar.data.Selector;
import org.redcross.sar.mso.data.IMsoObjectIf.IObjectIdIf;

public class UnitListImpl extends MsoListImpl<IUnitIf> implements IUnitListIf
{
    public UnitListImpl(IMsoObjectIf anOwner, String theName, boolean isMain)
    {
        super(IUnitIf.class, anOwner, theName, isMain);
    }

    public UnitListImpl(IMsoObjectIf anOwner, String theName, boolean isMain, int aSize)
    {
        super(IUnitIf.class, anOwner, theName, isMain, 0, aSize);
    }

    private int makeUnitSerialNumber(Class<?> aClass)
    {
        int retVal = 0;
        for (IUnitIf item : getObjects())
        {
            if (item.getClass() == aClass && item.getNumber() > retVal)
            {
                retVal = item.getNumber();
            }
        }
        return retVal + 1;
    }

    /*
    void rearrangeUnitSerialNumber(Class<?> aClass)
    {
        // todo make code
        for (IUnitIf item : getObjects())
        {
        }
    }
	*/
    
    public IVehicleIf createVehicle(String anIdentifier)
    {
        checkCreateOp();
        return (VehicleImpl) createdUniqueItem(new VehicleImpl(getOwner().getModel(), makeUniqueId(), makeUnitSerialNumber(VehicleImpl.class), anIdentifier));
    }

    public IVehicleIf createVehicle(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IVehicleIf retVal = (IVehicleIf) getLoopback(anObjectId);
        return retVal != null ? retVal : (IVehicleIf) createdItem(new VehicleImpl(getOwner().getModel(), anObjectId, -1));
    }

    public IBoatIf createBoat(String anIdentifier)
    {
        checkCreateOp();
        return (BoatImpl) createdUniqueItem(new BoatImpl(getOwner().getModel(), makeUniqueId(), makeUnitSerialNumber(BoatImpl.class), anIdentifier));
    }

    public IBoatIf createBoat(IMsoObjectIf.IObjectIdIf anObjectId)
    {
        checkCreateOp();
        IBoatIf retVal = (IBoatIf) getLoopback(anObjectId);
        return retVal != null ? retVal : (IBoatIf) createdItem(new BoatImpl(getOwner().getModel(), anObjectId, -1));
    }

    public IDogIf createDog(String anIdentifier)
    {
        checkCreateOp();
        return (IDogIf) createdUniqueItem(new DogImpl(getOwner().getModel(), makeUniqueId(), makeUnitSerialNumber(DogImpl.class)));
    }

    public IDogIf createDog(IObjectIdIf objectId)
    {
        checkCreateOp();
        IDogIf retVal = (IDogIf) getLoopback(objectId);
        return retVal != null ? retVal : (IDogIf) createdItem(new DogImpl(getOwner().getModel(), objectId, -1));
    }

    public IAircraftIf createAircraft(String anIdentifier)
    {
        checkCreateOp();
        return (IAircraftIf) createdUniqueItem(new AircraftImpl(getOwner().getModel(), makeUniqueId(), makeUnitSerialNumber(AircraftImpl.class), anIdentifier));
    }

    public IAircraftIf createAircraft(IObjectIdIf objectId)
    {
        checkCreateOp();
        IAircraftIf retVal = (IAircraftIf) getLoopback(objectId);
        return retVal != null ? retVal : (IAircraftIf) createdItem(new AircraftImpl(getOwner().getModel(), objectId, -1));
    }

    public ITeamIf createTeam(String anIdentifier)
    {
        checkCreateOp();
        return (ITeamIf) createdUniqueItem(new TeamImpl(getOwner().getModel(), makeUniqueId(), makeUnitSerialNumber(TeamImpl.class)));
    }

    public ITeamIf createTeam(IObjectIdIf objectId)
    {
        checkCreateOp();
        ITeamIf retVal = (ITeamIf) getLoopback(objectId);
        return retVal != null ? retVal : (ITeamIf) createdItem(new TeamImpl(getOwner().getModel(), objectId, -1));
    }

    public IUnitIf getUnit(int aUnitNr)
    {
        for (IUnitIf unit : getObjects())
        {
            if (unit.getNumber() == aUnitNr)
            {
                return unit;
            }
        }
        return null;
    }

    @Override
    protected Selector<IUnitIf> getRenumberSelector(IUnitIf anItem)
    {
        unitRenumberSelector.setSelectionItem(anItem);
        return unitRenumberSelector;
    }

    private final UnitRenumberSelector unitRenumberSelector = new UnitRenumberSelector();

    protected class UnitRenumberSelector implements Selector<IUnitIf>
    {
        protected IUnitIf m_selectionItem;

        void setSelectionItem(IUnitIf anItem)
        {
            m_selectionItem = anItem;
        }

        public boolean select(IUnitIf anObject)
        {
            if (anObject == m_selectionItem)
            {
                return false;
            }
            if (!anObject.getClass().equals(m_selectionItem.getClass()))
            {
                return false;
            }
            return anObject.getNumber() >= m_selectionItem.getNumber();
        }
    }

}