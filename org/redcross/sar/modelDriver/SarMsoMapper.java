package org.redcross.sar.modelDriver;

import no.cmr.geo.PositionOccurrence;
import no.cmr.tools.Log;
import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.util.mso.*;
import org.rescuenorway.saraccess.model.*;
import org.rescuenorway.saraccess.model.TimePos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Helper class used to map between MSO object attributes and Sara facts
 * User: Stian
 * Date: 08.mai.2007
 * Time: 13:54:03
 * To change this template use File | Settings | File Templates.
 */
public class SarMsoMapper {

    public static void mapMsoAttrToSarFact(SarFact sarFact, IAttributeIf msoAttr, boolean distribute) {
        try
        {
        if (msoAttr instanceof AttributeImpl.MsoBoolean) {
            AttributeImpl.MsoBoolean lAttr = (AttributeImpl.MsoBoolean) msoAttr;
            ((SarFactNumerical) sarFact).setNumValue(lAttr.booleanValue() ? 1 : 0, distribute);
        }
        if (msoAttr instanceof AttributeImpl.MsoInteger) {
            AttributeImpl.MsoInteger lAttr = (AttributeImpl.MsoInteger) msoAttr;
            ((SarFactNumerical) sarFact).setNumValue(lAttr.intValue(), distribute);
        }
        if (msoAttr instanceof AttributeImpl.MsoDouble) {
            AttributeImpl.MsoDouble lAttr = (AttributeImpl.MsoDouble) msoAttr;
            ((SarFactNumerical) sarFact).setNumValue(lAttr.doubleValue(), distribute);
        }
        if (msoAttr instanceof AttributeImpl.MsoString) {
            AttributeImpl.MsoString lAttr = (AttributeImpl.MsoString) msoAttr;
            ((SarFactString) sarFact).setStringValue(lAttr.getString(), distribute);
        }
        if (msoAttr instanceof AttributeImpl.MsoCalendar) {
            AttributeImpl.MsoCalendar lAttr = (AttributeImpl.MsoCalendar) msoAttr;
            ((SarFactDate) sarFact).setDate(lAttr.getCalendar(), distribute);
        }
        if (msoAttr instanceof AttributeImpl.MsoPosition) {

            AttributeImpl.MsoPosition lAttr = (AttributeImpl.MsoPosition) msoAttr;
            if(lAttr.getPosition()!=null)
            {

            ((SarFactLocation) sarFact).updateLocation( lAttr.getPosition().getPosition().getY(),
                     lAttr.getPosition().getPosition().getX(), distribute);
            }

        }
        if (msoAttr instanceof AttributeImpl.MsoTimePos) {
            AttributeImpl.MsoTimePos lAttr = (AttributeImpl.MsoTimePos) msoAttr;
            if(lAttr.getTimePos()!=null && lAttr.getTimePos().getPosition()!=null && lAttr.getTimePos().getTime()!=null)
            {
                ((SarFactLocation) sarFact).updateLocation( lAttr.getTimePos().getPosition().getY(),
                     lAttr.getTimePos().getPosition().getX(), lAttr.getTimePos().getTime(), distribute);
            }
        }
        if (msoAttr instanceof AttributeImpl.MsoPolygon) {
            AttributeImpl.MsoPolygon lAttr = (AttributeImpl.MsoPolygon) msoAttr;
            List<TimePos> posList = mapGeoPosListToSaraTimePos(lAttr.getPolygon().getVertices());
            ((SarFactArea) sarFact).setArea(posList, lAttr.getPolygon().getName(), lAttr.getPolygon().getLayout(), distribute);//TODO gjennomsøkt eller ei??
        }
        if (msoAttr instanceof AttributeImpl.MsoRoute) {
            AttributeImpl.MsoRoute lAttr = (AttributeImpl.MsoRoute) msoAttr;
            List<TimePos> posList = mapGeoPosListToSaraTimePos(lAttr.getRoute().getItems());
            ((SarFactTrack) sarFact).setTrack(posList, lAttr.getRoute().getName(), lAttr.getRoute().getLayout(), distribute);
        }
        if (msoAttr instanceof AttributeImpl.MsoTrack) {
            AttributeImpl.MsoTrack lAttr = (AttributeImpl.MsoTrack) msoAttr;
            List<TimePos> posList = mapTimePosListToSaraTimePos(lAttr.getTrack().getItems());
            ((SarFactTrack) sarFact).setTrack(posList, lAttr.getTrack().getName(), lAttr.getTrack().getLayout(), distribute);
        }
// todo remove        if (msoAttr instanceof AttributeImpl.MsoGeoList) {
// todo remove            throw new ClassCastException("MsoGeoList not supported by mapper");
// todo remove        }
        if (msoAttr instanceof AttributeImpl.MsoEnum) {
            AttributeImpl.MsoEnum lAttr = (AttributeImpl.MsoEnum) msoAttr;
            ((SarFactString) sarFact).setStringValue(lAttr.getValueName(), distribute);
        }
        }
        catch(Exception e)
        {
            Log.printStackTrace(e);
           if(!sarFact.getLabel().equalsIgnoreCase("Objektnavn"))
            Log.warning("Unable to map msoattr "+msoAttr.getName()+" to fact"+ sarFact.getLabel());
        }
    }

    public static void mapSarFactToMsoAttr(Object msoAttr, SarFact sarFact) {
        if (msoAttr instanceof AttributeImpl.MsoBoolean) {
            AttributeImpl.MsoBoolean lAttr = (AttributeImpl.MsoBoolean) msoAttr;
            int factVal = (int) ((SarFactNumerical) sarFact).getIntegerValue();
            lAttr.setValue(factVal == 1 ? true : false);
        } else if (msoAttr instanceof AttributeImpl.MsoInteger) {
            AttributeImpl.MsoInteger lAttr = (AttributeImpl.MsoInteger) msoAttr;
            Integer factVal = ((SarFactNumerical) sarFact).getIntegerValue();
            if(factVal!=null)
            {
                lAttr.setValue(factVal);
            }

        } else if (msoAttr instanceof AttributeImpl.MsoDouble) {
            AttributeImpl.MsoDouble lAttr = (AttributeImpl.MsoDouble) msoAttr;
            double factVal = ((SarFactNumerical) sarFact).getDoubleValue();
            lAttr.setValue(factVal);

        } else if (msoAttr instanceof AttributeImpl.MsoString) {
            AttributeImpl.MsoString lAttr = (AttributeImpl.MsoString) msoAttr;
            lAttr.setValue(((SarFactString) sarFact).getStringValue());
        } else if (msoAttr instanceof AttributeImpl.MsoCalendar) {
            AttributeImpl.MsoCalendar lAttr = (AttributeImpl.MsoCalendar) msoAttr;
            lAttr.setValue(((SarFactDate) sarFact).getDate());
        } else if (msoAttr instanceof AttributeImpl.MsoPosition) {
            AttributeImpl.MsoPosition lAttr = (AttributeImpl.MsoPosition) msoAttr;
            SarFactLocation lFact = (SarFactLocation) sarFact;    //2Sjekk med vinjar
//            lAttr.setValue(new Position(lFact.getID(), lFact.getLongValue(), lFact.getLatValue()));   todo er denne feil?
            lAttr.setValue(new Position(null, lFact.getLongValue(), lFact.getLatValue())); // todo sjekk at dette er rett!!!
        } else if (msoAttr instanceof AttributeImpl.MsoTimePos) {
            AttributeImpl.MsoTimePos lAttr = (AttributeImpl.MsoTimePos) msoAttr;
            SarFactLocation lFact = (SarFactLocation) sarFact;
            lAttr.setValue(new org.redcross.sar.util.mso.TimePos(lFact.getLongValue(), lFact.getLatValue(), lFact.getTimeAtPos()));
        } else if (msoAttr instanceof AttributeImpl.MsoPolygon) {
            AttributeImpl.MsoPolygon lAttr = (AttributeImpl.MsoPolygon) msoAttr;

            SarFactArea aFact = (SarFactArea) sarFact;
            //Polygon lPoly = new Polygon(lAttr.getPolygon().getId(), aFact.getName());
            Polygon lPoly = new Polygon(aFact.getID(), aFact.getName());
            lPoly.setLayout(aFact.getStyle());
            for (TimePos tp : aFact.getArea()) {
                lPoly.add(tp.getLongitude(), tp.getLatitude());
            }
            lAttr.setValue(lPoly);
        } else if (msoAttr instanceof AttributeImpl.MsoRoute) {
            AttributeImpl.MsoRoute lAttr = (AttributeImpl.MsoRoute) msoAttr;
            SarFactTrack aFact = (SarFactTrack) sarFact;

            //Route lPoly = new Route(lAttr.getRoute().getId(), aFact.getName());//TODO avsjekk
            Route lPoly = new Route(aFact.getID(), aFact.getName());//TODO avsjekk
            lPoly.setLayout(aFact.getStyle());
            for (TimePos tp : aFact.getTrack()) {
                lPoly.add(tp.getLongitude(), tp.getLatitude());
            }
            lAttr.setValue(lPoly);


        } else if (msoAttr instanceof AttributeImpl.MsoTrack) {

            AttributeImpl.MsoTrack lAttr = (AttributeImpl.MsoTrack) msoAttr;
            SarFactTrack aFact = (SarFactTrack) sarFact;
            //Track lPoly = new Track(lAttr.getTrack().getId(), aFact.getName());
            Track lPoly = new Track(aFact.getID(), aFact.getName());

            lPoly.setLayout(aFact.getStyle());
            for (TimePos tp : aFact.getTrack()) {
                lPoly.add(tp.getLongitude(), tp.getLatitude(), tp.getTimeInPos());
            }
            lAttr.setValue(lPoly);

// todo remove        } else if (msoAttr instanceof AttributeImpl.MsoGeoList) {
// todo remove            throw new ClassCastException("MsoGeoList not supported by mapper");
        } else if (msoAttr instanceof AttributeImpl.MsoEnum) {
            AttributeImpl.MsoEnum lAttr = (AttributeImpl.MsoEnum) msoAttr;
            if(((SarFactString) sarFact).getStringValue().length()>0)
            {
            lAttr.setValue(((SarFactString) sarFact).getStringValue());
            }
        } else {
            Log.warning("Unknown mapping type" + msoAttr.getClass().getName());
        }

    }


    private static List<TimePos> mapGeoPosListToSaraTimePos(Collection<GeoPos> route) {
        List<TimePos> list = new ArrayList();
        for (GeoPos geo : route) {
            TimePos lPos = new PositionOccurrence((float) geo.getPosition().getY(), (float) geo.getPosition().getX(), "");
            list.add(lPos);
        }
        return list;
    }

    private static List<TimePos> mapTimePosListToSaraTimePos(Collection<org.redcross.sar.util.mso.TimePos> track) {
        List<TimePos> list = new ArrayList();
        for (org.redcross.sar.util.mso.TimePos geo : track) {
            TimePos lPos = new PositionOccurrence((float) geo.getPosition().getY(), (float) geo.getPosition().getX(), geo.getTime().getTimeInMillis(), "");
            list.add(lPos);
        }
        return list;
    }

}
