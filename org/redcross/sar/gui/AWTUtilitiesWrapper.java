package org.redcross.sar.gui;

import java.awt.GraphicsConfiguration;
import java.awt.Shape;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Anthony Petrov
 */
public class AWTUtilitiesWrapper {

    private static Class<?> m_awtUtilitiesClass;
    private static Class<?> m_translucencyClass;

    private static Method m_isTranslucencySupported;
    private static Method m_isTranslucencyCapable;
    private static Method m_setWindowShape;
    private static Method m_setWindowOpacity;
    private static Method m_setWindowOpaque;

    public static Object PERPIXEL_TRANSPARENT;
    public static Object TRANSLUCENT;
    public static Object PERPIXEL_TRANSLUCENT;

    /**
     * Static constructor
     */
    static {
        init();
    }

    /**
     * Initialize static class
     */
    static void init() {
        try {
            m_awtUtilitiesClass = Class.forName("com.sun.awt.AWTUtilities");
            m_translucencyClass = Class.forName("com.sun.awt.AWTUtilities$Translucency");
            if (m_translucencyClass.isEnum()) {
                Object[] kinds = m_translucencyClass.getEnumConstants();
                if (kinds != null) {
                    PERPIXEL_TRANSPARENT = kinds[0];
                    TRANSLUCENT = kinds[1];
                    PERPIXEL_TRANSLUCENT = kinds[2];
                }
            }
            m_isTranslucencySupported = m_awtUtilitiesClass.getMethod("isTranslucencySupported", m_translucencyClass);
            m_isTranslucencyCapable = m_awtUtilitiesClass.getMethod("isTranslucencyCapable", GraphicsConfiguration.class);
            m_setWindowShape = m_awtUtilitiesClass.getMethod("setWindowShape", Window.class, Shape.class);
            m_setWindowOpacity = m_awtUtilitiesClass.getMethod("setWindowOpacity", Window.class, float.class);
            m_setWindowOpaque = m_awtUtilitiesClass.getMethod("setWindowOpaque", Window.class, boolean.class);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(AWTUtilitiesWrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(AWTUtilitiesWrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AWTUtilitiesWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static boolean isSupported(Method method, Object kind) {
        if (m_awtUtilitiesClass == null ||
                method == null)
        {
            return false;
        }
        try {
            Object ret = method.invoke(null, kind);
            if (ret instanceof Boolean) {
                return ((Boolean)ret).booleanValue();
            }
        } catch (IllegalAccessException ex) {
            Logger.getLogger(AWTUtilitiesWrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(AWTUtilitiesWrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(AWTUtilitiesWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static boolean isTranslucencySupported(Object kind) {
        if (m_translucencyClass == null) {
            return false;
        }
        return isSupported(m_isTranslucencySupported, kind);
    }

    public static boolean isTranslucencyCapable(GraphicsConfiguration gc) {
        return isSupported(m_isTranslucencyCapable, gc);
    }

    private static void set(Method method, Window window, Object value) {
        if (m_awtUtilitiesClass == null ||
                method == null)
        {
            return;
        }
        try {
            method.invoke(null, window, value);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(AWTUtilitiesWrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(AWTUtilitiesWrapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(AWTUtilitiesWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void setWindowShape(Window window, Shape shape) {
        set(m_setWindowShape, window, shape);
    }

    public static void setWindowOpacity(Window window, float opacity) {
        set(m_setWindowOpacity, window, Float.valueOf(opacity));
    }

    public static void setWindowOpaque(Window window, boolean opaque) {
        set(m_setWindowOpaque, window, Boolean.valueOf(opaque));
    }
}
