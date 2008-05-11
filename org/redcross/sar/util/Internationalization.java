package org.redcross.sar.util;

import no.cmr.tools.Log;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * User: vinjar
 * Date: 08.aug.2007
 */

/**
 * Class for internationational texts (and icons in the future).
 */
public class Internationalization
{
    private static Map<Class, ResourceBundle> resourceBundles = new LinkedHashMap<Class, ResourceBundle>();

    /**
     * Get international text from a given java.util.ResourceBundle.
     *
     * @param aBundle The ResourceBundle to use
     * @param aKey    Lookup value
     * @return The found value, or <code>null</code> if not found.
     */
    public static String getBundleText(ResourceBundle aBundle, String aKey)
    {
        if (aBundle == null)
        {
            return null;
        }
        try
        {
            return aBundle.getString(aKey);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Get international text from a given java.util.ResourceBundle.
     *
     * @param aBundle The ResourceBundle to use
     * @param aKey    Lookup value
     * @return The found value, or aKey if not found.
     */
    public static String getFullBundleText(ResourceBundle aBundle, String aKey)
    {
        String retVal = getBundleText(aBundle, aKey);
        return retVal != null ? retVal : aKey;
    }

    /**
     * Get international Enum text from a given java.util.ResourceBundle.
     *
     * @param aBundle The ResourceBundle to use
     * @param anEnum  Lookup value
     * @return The found value, or Enum.name() if not found.
     */
    public static String getEnumText(ResourceBundle aBundle, Enum anEnum)
    {
        String retVal = getBundleText(aBundle, anEnum.getClass().getSimpleName() + "." + anEnum.name() + ".text");
        return retVal != null ? retVal : anEnum.name();
    }


    /**
     * Translate international text from a given java.util.ResourceBundle.
     * <p/>
     * The method shall give the same type of results as {@link org.redcross.sar.app.Utils#translate(Object)}.
     *
     * @param aBundle The ResourceBundle to use
     * @param obj     Lookup value
     * @return Same as {@link #getEnumText(java.util.ResourceBundle,Enum)} or {@link #getBundleText(java.util.ResourceBundle,String)}, depending on type of obj.
     */
    public static String translate(ResourceBundle aBundle, Object obj)
    {
        if (obj == null)
        {
            return "";
        }
        if (obj instanceof Enum)
        {
            return getEnumText(aBundle, (Enum) obj);
        }
        return getBundleText(aBundle, obj.toString()+ ".text");
    }


    /**
     * Get a java.util.ResourceBundle for a given class.
     *
     * @param aClass A class or interface that has a defined properties-file. The name of the file (including path) is defined in the public final static field <code>bundleName</code> in the class or interface.
     * @return The ResourceBundle if defined, otherwise <code>null</code>.
     *
     * The loaded bundles are stored in a static Map for later use.
     */
    public static ResourceBundle getBundle(Class aClass)
    {
        if (aClass == null)
        {
            return null;
        }
        ResourceBundle retVal = resourceBundles.get(aClass);
        if (retVal == null)
        {
            String bundleName = "";
            try
            {
                Field f = aClass.getField("bundleName");
                bundleName = (String)f.get(null);
                retVal = ResourceBundle.getBundle(bundleName);
            }
            catch (NoSuchFieldException e)
            {
                Log.error("getBundle: Field 'bundleName' not defined for class " + aClass);
            }
            catch (IllegalAccessException e)
            {
                Log.error("getBundle: IllegalAccessException " + e + " for " + aClass);
            }
            catch (ClassCastException e)
            {
                Log.error("getBundle: Field 'bundleName' in class " + aClass + " cannot be cast to String");
            }
            catch (Exception e)
            {
                Log.error("getBundle: properties-file" + bundleName+ " for " + aClass + " not found or erroneous, error: " + e.getMessage());
            }
            finally
            {
                resourceBundles.put(aClass,retVal);
            }
        }
        return retVal;
    }

    /**
     * Get a java.util.ResourceBundle for a given Enum.
     *
     * @param anEnum An enum.
     * @return The ResourceBundle if defined, otherwise <code>null</code>.
     *
     * It is assumed  that the enum is defined in declaring class or interface that has a defined properties-file. See {@link #getBundle(Class)}.
     */
    private static ResourceBundle getBundle(Enum anEnum)
    {
        return getBundle(anEnum.getClass().getDeclaringClass());
    }

    /**
     * Translate international text for an Enum
     * <p/>
     * The method shall give the same type of results as {@link org.redcross.sar.app.Utils#translate(Object)}.
     *
     * @param anEnum Lookup value
     * @return Same as {@link #getEnumText(java.util.ResourceBundle,Enum)} or {@link #getBundleText(java.util.ResourceBundle,String)}, depending on type of obj.
     */

    public static String translate(Enum anEnum)
    {
        ResourceBundle bundle = getBundle(anEnum);
        return translate(bundle, anEnum);
    }
}
