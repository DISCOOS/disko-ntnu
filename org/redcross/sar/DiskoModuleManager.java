package org.redcross.sar;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.redcross.sar.gui.factory.DiskoStringFactory;
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.ProgressMonitor;
import org.redcross.sar.wp.IDiskoWpModule;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * This class manages modules that implement the IDiskoWpModule
 * interface. Modules shared resources between roles, each role will
 * can have one or more of these roles available.
 *
 * <p>
 * Roles an modules are read from a xml-file.
 *
 * @author geira
 *
 */

public class DiskoModuleManager {

	private final IApplication app;
	private final Document doc;
	private String[] rolleNames;
	private final ClassLoader classLoader;
	private final Map<String,IDiskoWpModule> modules;

	/**
	 * Constructs a DiskoModuleManager
	 * @param app A reference to the Disko application.
	 * @param file A xml file containing roles and modules.
	 */
	public DiskoModuleManager(IApplication app, File file) throws Exception {

		// prepare
		this.app = app;
		this.classLoader = ClassLoader.getSystemClassLoader();
		this.modules = new HashMap<String,IDiskoWpModule>();

		// load xml document
		FileInputStream instream = new FileInputStream(file);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setValidating(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		this.doc = db.parse(instream);

	}

	/**
	 * Get an array containing all role titles defined in the xml-file.
	 * @return An array of role titles
	 * @throws Exception
	 */
	public String[] getRoleTitles(boolean refresh) throws Exception {
		if (rolleNames == null || refresh) {
			NodeList elems = doc.getElementsByTagName("DiskoRole");
			rolleNames = new String[elems.getLength()];
			for (int i = 0; i < elems.getLength(); i++) {
				Element elem = (Element)elems.item(i);
				rolleNames[i] = elem.getAttribute("title");
			}
		}
		return rolleNames;
	}

	/**
	 * Parse and load a role with the given name. New instances of
	 * DiskoWP defined under the given role is parsed.
	 * @param name The name of the role to parse
	 * @return A new IDiskoRole
	 * @throws Exception
	 */
	public IDiskoRole parseRole(String name)  {
		NodeList elems = doc.getElementsByTagName("DiskoRole");
		for (int i = 0; i < elems.getLength(); i++) {
			Element elem = (Element)elems.item(i);
			String title = elem.getAttribute("title");
			if (title.equals(name)) {
				// parse this rolle
				return parseDiskoRole(elem);
			}
		}
		return null;
	}

	private IDiskoRole parseDiskoRole(Element elem) {

		// get role information
		String name = elem.getAttribute("name");
		String title = elem.getAttribute("title");
		String description = elem.getAttribute("description");
		DiskoRoleImpl role = new DiskoRoleImpl(app, name, title, description);

		// enumerate all modules
		NodeList children = elem.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child == null || child.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if (child.getNodeName().equalsIgnoreCase("DiskoModule")) {
				// get class name
				String className = ((Element) child).getAttribute("classname");
				// initialize
				IDiskoWpModule module = null;
				Boolean isDefault = false;
				if(((Element) child).hasAttribute("isDefault")) {
					isDefault = Boolean.valueOf(((Element) child).getAttribute("isDefault"));
				}
				// is module loaded already?
				if(modules.containsKey(className)) {
					module = modules.get(className);
				}
				else {
					// load module
					try {
						Class<?> cls = classLoader.loadClass(className);
						String message = DiskoStringFactory.getText(Utils.getPackageName(cls));
						message = String.format(DiskoStringFactory.getText("PROGRESS_LOADING_CLASS"),message);
						ProgressMonitor.getInstance().setNote(message);
						Object obj = cls.getConstructors()[0].newInstance();
						if (obj instanceof IDiskoWpModule) {
							module = (IDiskoWpModule)obj;
							modules.put(className,module);
						}
						else throw new Exception("Unsupported class was found");
					} catch (Exception e) { /* NOP */
						// TODO: Handle failed load of work modules
						Utils.showError("Feil i arbeidsprosess", "Følgende feil funnet i "+ className + ":",e);
						System.out.println("Error"+e.getMessage());
						e.printStackTrace();
					}
				}
				// add module?
				if(module!=null)
					role.addDiskoWpModule(module,isDefault);
			}
		}
		// return role
		return role;
	}
}
