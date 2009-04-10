package org.redcross.sar.gui.factory;

import java.util.ResourceBundle;

import javax.swing.Icon;

import org.redcross.sar.util.Internationalization;

public class DiskoAnimationFactory {

	private final static String m_path = "animations";

	private final static ResourceBundle m_default =
		ResourceBundle.getBundle("resources/animations");

	public static Icon getIcon(String animation) {
		return getIcon(animation,null);
	}

	public static Icon getIcon(String animation, Object resource) {
		if (animation != null && !animation.isEmpty()) {
			// forward
			try {
				return DiskoIconFactory.createImageIcon(animation,getPath(animation,resource));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String getPath(String animation) {
		return getPath(animation,null);
	}

	public static String getPath(String animation, Object resource) {

		// is path?
		if (BasicDiskoFactory.fileExist(animation)) {
			return animation;
		}
		else {
			// get key
			String key = (!animation.endsWith(".animation"))
					   ? BasicDiskoFactory.getKey(animation,"animation") : animation;
			// get from passed resource
			String filename = BasicDiskoFactory.getText(key,resource);
			// try default bundle?
			if((filename==null || filename.isEmpty()) && m_default.containsKey(key))
				filename = m_default.getString(key);
			// found filename?
			if((filename==null || filename.isEmpty())) {
				// get from installed resource
				filename = Internationalization.getText(key);
			}
			// found filename?
			if(filename!=null && !filename.isEmpty())
				// use relative path!
				return m_path + "/" + filename;
		}
		// failure
		return null;
	}

}
