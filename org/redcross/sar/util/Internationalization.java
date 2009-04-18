package org.redcross.sar.util;

import no.cmr.tools.Log;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: vinjar
 * Date: 08.aug.2007
 */

/**
 * Class for internationalization of texts (and icons in the future).
 */
public class Internationalization
{
    private static Map<Class<?>, ResourceBundle> bundles = new LinkedHashMap<Class<?>, ResourceBundle>();

    /**
     * Get international text from a given java.util.ResourceBundle.
     *
     * @param aBundle The ResourceBundle to use
     * @param aKey    Lookup value
     * @return The found value, or <code>null</code> if not found.
     */
    public static String getText(ResourceBundle aBundle, String aKey)
    {
        if (aBundle == null || aKey == null || aKey.isEmpty()) return null;

        try {
            return aBundle.getString(aKey);
        }
        catch (Exception e) { return null; }
    }

    /**
     * Get international text from first occurrence in
     * an installed java.util.ResourceBundle.
     *
     * @param aBundle The ResourceBundle to use
     * @param aKey    Lookup value
     * @return The found value, or <code>null</code> if not found.
     */
    public static String getText(String aKey)
    {
        if (aKey == null || aKey.isEmpty()) return null;

        Collection<ResourceBundle> list = new Vector<ResourceBundle>(bundles.values());
    	for(ResourceBundle bundle : list) {
    		String text = getText(bundle,aKey);
    		if(text!=null) return text;
    	}
    	// not found
    	return null;
    }

    /**
     * Get international text from a given java.util.ResourceBundle.
     *
     * @param aBundle The ResourceBundle to use
     * @param aKey    Lookup value
     * @return The found value, or aKey if not found.
     */
    public static String getString(ResourceBundle aBundle, String aKey)
    {
        String retVal = getText(aBundle, aKey);
        return retVal != null ? retVal : aKey;
    }

    /**
     * Get international text from first occurence in
     * an installed java.util.ResourceBundle.
     *
     * @param aBundle The ResourceBundle to use
     * @param aKey    Lookup value
     * @return The found value, or aKey if not found.
     */
    public static String getString(String aKey)
    {
        if (aKey == null || aKey.isEmpty()) return null;

    	for(ResourceBundle bundle : bundles.values()) {
    		String text = getText(bundle,aKey);
    		if(text!=null) return text;
    	}
    	// not found, return key
    	return aKey;
    }

    /**
     * Get international Enum text from a given java.util.ResourceBundle.
     *
     * @param aBundle The ResourceBundle to use
     * @param anEnum  Lookup value
     * @return The found value, or Enum.name() if not found.
     */
    public static String getEnumText(ResourceBundle aBundle, Enum<?> anEnum)
    {
        String retVal = getText(aBundle, anEnum.getClass().getSimpleName() + "." + anEnum.name() + ".text");
        return retVal != null ? retVal : anEnum.name();
    }


    /**
     * Translate international text from a given java.util.ResourceBundle.
     * <p/>
     * The method shall give the same type of results as {@link org.redcross.sar.util.Utils#translate(Object)}.
     *
     * @param aBundle The ResourceBundle to use
     * @param obj     Lookup value
     * @return Same as {@link #getEnumText(java.util.ResourceBundle,Enum)} or {@link #getText(java.util.ResourceBundle,String)}, depending on type of obj.
     */
    public static String translate(ResourceBundle aBundle, Object obj)
    {
        if (obj == null)
        {
            return "";
        }
        if (obj instanceof Enum)
        {
            return getEnumText(aBundle, (Enum<?>) obj);
        }
        return getText(aBundle, obj.toString()+ ".text");
    }


    /**
     * Get a java.util.ResourceBundle for a given class.
     *
     * @param aClass A class or interface that has a defined properties-file. The name of the file (including path) is defined in the public final static field <code>bundleName</code> in the class or interface.
     * @return The ResourceBundle if defined, otherwise <code>null</code>.
     *
     * The loaded bundles are stored in a static Map for later use.
     */
    public static ResourceBundle getBundle(Class<?> aClass)
    {
        if (aClass == null)
        {
            return null;
        }
        ResourceBundle bundle = bundles.get(aClass);
        if (bundle == null)
        {
            String bundleName = "";
            try
            {
                Field f = aClass.getField("bundleName");
                bundleName = (String)f.get(null);
                bundle = ResourceBundle.getBundle(bundleName);
            }
            catch (NoSuchFieldException e)
            {
                Log.warning("getBundle: Field 'bundleName' not defined for class " + aClass);
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
            	// put locally
                bundles.put(aClass,bundle);
            }
        }
        return bundle;
    }

    /**
     * Get a java.util.ResourceBundle for a given Enum.
     *
     * @param anEnum An enum.
     * @return The ResourceBundle if defined, otherwise <code>null</code>.
     *
     * It is assumed  that the enum is defined in declaring class or interface that has a defined properties-file. See {@link #getBundle(Class)}.
     */
    public static ResourceBundle getBundle(Enum<?> anEnum)
    {
        return getBundle(anEnum.getClass().getDeclaringClass());
    }

    /**
     * Translate international text for an Enum
     * <p/>
     * The method shall give the same type of results as {@link org.redcross.sar.util.Utils#translate(Object)}.
     *
     * @param anEnum Lookup value
     * @return Same as {@link #getEnumText(java.util.ResourceBundle,Enum)} or {@link #getText(java.util.ResourceBundle,String)}, depending on type of obj.
     */

    public static String translate(Enum<?> anEnum)
    {
        ResourceBundle bundle = getBundle(anEnum);
        return translate(bundle, anEnum);
    }
}
