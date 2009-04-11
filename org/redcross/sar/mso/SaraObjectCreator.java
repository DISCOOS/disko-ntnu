package org.redcross.sar.mso;

import no.cmr.hrs.sar.model.*;
import no.cmr.tools.FileUtils;
import no.cmr.tools.Log;
import org.redcross.sar.mso.data.AttributeImpl;
import org.rescuenorway.saraccess.model.SarBaseObject;
import org.rescuenorway.saraccess.model.SarFact;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;


/**
 * Created by IntelliJ IDEA.
 * User: Stian
 * Date: 11.mai.2007
 * Time: 13:27:44
 * To change this template use File | Settings | File Templates.
 */
public class SaraObjectCreator
{
   public static final int LOC_POI = 0;
   public static final int LOC_POS = 1;
   public static final int LOC_POS_MANUEL = 2;
   public static final int LOC_POS_AUTO = 3;
   public static final int LOC_POS_GPS = 4;

   static final NumberFormat millisforma = new DecimalFormat("00000000000");
   //private HashMap<String, SarBaseObject> facts = new HashMap();
   private HashMap<String, SarObjectImpl> objs = new LinkedHashMap<String, SarObjectImpl>();
   private HashMap<String, String> objNameIdMap = new LinkedHashMap<String, String>();
   private HashMap<String, String> idObjNameMap = new LinkedHashMap<String, String>();


   private int counter = 0;
   int result = 0;
   private ArrayList notcreated = new ArrayList();
   private String PACKAGENAME = "org.redcross.sar.mso.data";

   public int generateSARAObjects(String classDir, String aOutDir)
   {
      result = 0;
      File f = new File(classDir);
      if (f.isDirectory())
      {
         File outDir = new File(aOutDir);
         if (outDir.exists() && outDir.isDirectory())
         {
            FileUtils.deleteFilesInDir(outDir);
         }
         else
         {
            outDir.mkdir();
         }
         for (File fil : f.listFiles())
         {
            if (fil.isFile() && fil.getName().endsWith("Impl.java")
                  && !fil.getName().endsWith("ListImpl.java")
                  )
            {

               try
               {
                  if (generateObject(fil, outDir, false))
                  {
                     result += 1;
                  }
                  else
                  {
                     notcreated.add(fil);
                  }
               }
               catch (ClassNotFoundException e)
               {
                  //Log.warning("Unable to create map object for "+ fil.getName());
                  System.out.println("Unable to create map object for " + fil.getName());
               }
            }
         }

         boolean done = false;
         while (notcreated.size() > 0 && !done)
         {
            ArrayList notCr = new ArrayList();
            int startresult = notcreated.size();
            ListIterator it = (ListIterator) notcreated.listIterator();
            System.out.println("---- Remaining objects not created" + notcreated.size() + "----");
            while (it.hasNext())
            {
               File fil = (File) it.next();
               it.remove();
               try
               {
                  if (generateObject(fil, outDir, true))
                  {

                     result += 1;
                  }
                  else
                  {
                     notCr.add(fil);
                  }
               }
               catch (ClassNotFoundException e)
               {
                  Log.warning("Unable to create mapping for " + fil.getName());
               }

            }
            if (startresult== notCr.size())
            {
               done = true;
            }
            else
            {
               notcreated.clear();
               notcreated.addAll(notCr);
            }
         }
      }
      return result;
   }

   private boolean generateObject(File fil, File outDir, boolean isIF) throws ClassNotFoundException
   {

      String objId = getId();
      String objName =fil.getName().substring(0, fil.getName().indexOf("Impl.java"));

      SarObjectImpl so = new SarObjectImpl(objId, objId, Calendar.getInstance().getTimeInMillis(), objName, "test", result);
      Class cla = Class.forName(PACKAGENAME + "." +objName+"Impl");
      List<String[]> fields = getSetters(cla);
      for (String[] field : fields)
      {
         //Ignore DTG, type Long and references to other mso objects handled by named reference
         if (field[0].equals("")||field[0].equals("DTG") && field[1].equals("java.lang.Long")||
               (field[1].startsWith("org.redcross.sar.mso.data.I")&& field[1].endsWith("If"))||
                 field[1].endsWith("Enum"))
         {
            continue;
         }
         else
         {

            SarBaseObject fact = createFact(objId, field[0], field[1]);
            if (fact != null)
            {
               if (fact instanceof SarFact)
               {
                  so.addFact((SarFact) fact);
//                  facts.put(((SarFact) fact).getLabel(), fact);
               }
               else
               {
                  so.addSarObjectImpl((SarObjectImpl) fact);
               }


            }
            else
            {
               Log.println(field[0] + " type" + field[1] + " not created for" + cla.getName());
               System.out.println(field[0] + " type" + field[1] + " not created for" + cla.getName());
               return false;
            }
         }

      }

      objs.put(so.getName(), (SarObjectImpl) so);
      writeObject(outDir, so);
      return true;
   }

   private void writeObject(File dir, SarObjectImpl so)
   {
      DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();
      factory.setIgnoringElementContentWhitespace(true);
      factory.setValidating(false);
      DocumentBuilder builder = null;
      try
      {
         builder = factory.newDocumentBuilder();
         org.w3c.dom.Document llDoc = builder.newDocument();
         llDoc.appendChild(so.getAsXmlElement(llDoc));
         String name = so.getName();

         FileUtils.xmlDocToFile(llDoc, dir.getPath() + File.separator + name + ".xml", false);
         idObjNameMap.put(so.getBaseIdentitifer(), name);
         objNameIdMap.put(name, so.getBaseIdentitifer());
      }
      catch (ParserConfigurationException e)
      {
         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }

   }

   private SarBaseObject createFact(String objId, String name, String type)
   {

      SarFact sf = null;
      if (type.equalsIgnoreCase("int") || type.equalsIgnoreCase("Integer"))
      {
         return createIntFact(objId, name);
      }
      else if (type.equalsIgnoreCase("boolean"))
      {
         return createBoolFact(objId, name);
      }
      else if (type.equalsIgnoreCase("String") || type.equals("java.lang.String")

            )
      {
         return createStringFact(objId, name);
      }

      else if (type.equalsIgnoreCase("java.util.Calendar"))
      {
         return createDateFact(objId, name);
      }

      else if (type.equalsIgnoreCase("org.redcross.sar.util.mso.Position"))
      {
         return createPositionFact(objId, name);
      }

      else if (type.equalsIgnoreCase("org.redcross.sar.util.mso.Polygon") ||
            type.equalsIgnoreCase("org.redcross.sar.util.mso.GeoList"))
      {
         return createAreaFact(objId, name);
      }
      else if (type.equalsIgnoreCase("org.redcross.sar.util.mso.Route"))
      {
         return createTrackFact(objId, name);
      }
       else if (type.equalsIgnoreCase("org.redcross.sar.util.mso.Track"))
      {
         return createTrackFact(objId, name);
      }

      else if (type.equalsIgnoreCase("double"))
      {
         return createDoubleFact(objId, name);
      }
      return null;
   }

   private SarFact createTrackFact(String objId, String name)
   {
      String lId = getId();
      FactTrack fn = new FactTrack("Bus", objId + "." + lId, 999, 999, name, false, objId, true, lId, 1, new ArrayList(), FactTrack.TYPE_DEFAULT, "");
      return fn;

   }

   private SarFact createIntFact(String objId, String name)
   {
      String lId = getId();
      FactNumerical fn = new FactNumerical("Bus", objId + "." + lId, 999, 999, name, false, objId, true, lId, 1, 0, 100000000, 0);
      return fn;
   }

   private SarFact createBoolFact(String objId, String name)
   {
      String lId = getId();
      FactNumerical fn = new FactNumerical("Bus", objId + "." + lId, 999, 999, name, false, objId, true, lId, 1, 0, 1, 0);
      return fn;
   }

   private SarFact createDoubleFact(String objId, String name)
   {
      String lId = getId();
      FactNumerical fn = new FactNumerical("Bus", objId + "." + lId, 999, 999, name, false, objId, true, lId, 1, -1000000000, 1000000000, 12);
      return fn;
   }

   private SarFact createStringFact(String objId, String name)
   {
      String lId = getId();
      FactString fn = new FactString("Bus", objId + "." + lId, 999, 999, name, false, objId, true, lId, 1);
      return fn;
   }

   private SarFact createDateFact(String objId, String name)
   {
      String lId = getId();
      FactDate fn = new FactDate("Bus", objId + "." + lId, 999, 999, name, false, objId, true, lId, 1);
      return fn;
   }

   private SarFact createPOIFact(String objId, String name)
   {
      String lId = getId();
      FactLocation fn = new FactLocation("Bus", objId + "." + lId, 999, 999, name, false, objId, true, lId, 1, LOC_POI);
      return fn;
   }

   private SarFact createPositionFact(String objId, String name)
   {
      String lId = getId();
      FactLocation fn = new FactLocation("Bus", objId + "." + lId, 999, 999, name, false, objId, true, lId, 1, LOC_POS);
      return fn;
   }

   private SarFact createAreaFact(String objId, String name)
   {
      String lId = getId();
      FactArea fn = new FactArea("Bus", objId + "." + lId, 999, 999, name, false, objId, true, lId, 1, new ArrayList(), "", FactArea.STATE_DEFAULT, "");
      return fn;
   }

   private String getId()
   {
      return millisforma.format(counter += 1);
   }

   public static List<String[]> getSetters(Class cls)
   {
      List<String[]> list = new ArrayList<String[]>();
      if (cls.getName().indexOf("AbstractMsoObject") >= 0)
      {
         return list;
      }
      Method fieldlist[] = cls.getDeclaredMethods();
      if (cls.getSuperclass() != null && cls.getSuperclass().getPackage().getName().equals(cls.getPackage().getName()))
      {

         list.addAll(getSetters(cls.getSuperclass()));
      }

      for (int i = 0; i < fieldlist.length; i++)
      {

         Method fld = fieldlist[i];
         Class[] params = fld.getParameterTypes();
         if (fld.getName().startsWith("set") && Modifier.isPublic(fld.getModifiers()) && params.length == 1 &&
                 !params[0].isAssignableFrom(AttributeImpl.MsoEnum.class) && !params[0].isEnum())
         {
            String name = fld.getName().substring(3);
            list.add(new String[]{name, params[0].getName()});

//            System.out.println("name = " + fld.getName());
//            System.out.println("decl class = " +  fld.getDeclaringClass());
//            System.out.println("parameters= " + params[0].getClass());

            //System.out.println("-----");
         }
      }
      return list;
   }


   public static void main(String args[])
   {
      Log.init("MsoTestGen");
      SaraObjectCreator mso = new SaraObjectCreator();
      System.out.print("Genererte " +
            mso.generateSARAObjects(".\\org\\redcross\\sar\\mso\\data",
                  ".\\mapping\\adm\\objects") + " objekter");

//          try {
//                      Class cls = Class.forName("org.redcross.sar.mso.data.IBoatIf");
//                  SaraObjectCreator.getSetters(cls);
//                    }
//                    catch (Throwable e)
//                    {
//                       System.err.println(e);
//                    }
//
   }

}
