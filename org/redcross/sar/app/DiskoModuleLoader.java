package org.redcross.sar.app;

import java.io.File;
import java.io.FileInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.redcross.sar.gui.factory.DiskoStringFactory;
import org.redcross.sar.thread.DiskoProgressMonitor;
import org.redcross.sar.wp.IDiskoWpModule;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class loads and instanciate modules that implements the DiskoAp interface. 
 * Modules are grouped by roles (DiskoRolle). Roles an modules are read from a xml-file.
 * 
 * @author geira
 *
 */

public class DiskoModuleLoader {

	private IDiskoApplication app = null;
	private Document doc = null;
	private String[] rolleNames = null;
	private ClassLoader classLoader = null;
	
	/**
	 * Constructs a DiskoModuleLoader
	 * @param app A reference to the Disko application.
	 * @param file A xml file containing roles and modules.
	 */
	public DiskoModuleLoader(IDiskoApplication app, File file) throws Exception {
		this.app = app;
		this.classLoader = ClassLoader.getSystemClassLoader();
		
		FileInputStream instream = new FileInputStream(file);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setValidating(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		doc = db.parse(instream);
	}
	
	/**
	 * Get an array containing all role titles defined in the xml-file.
	 * @return An array of role titles
	 * @throws Exception
	 */
	public String[] getRoleTitles() throws Exception {
		if (rolleNames == null) {
			NodeList elems = doc.getElementsByTagName("DiskoRolle");
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
	public IDiskoRole parseRole(String name) throws Exception {
		NodeList elems = doc.getElementsByTagName("DiskoRolle");
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

	private IDiskoRole parseDiskoRole(Element elem) throws Exception {
		String name = elem.getAttribute("name");
		String title = elem.getAttribute("title");
		String description = elem.getAttribute("description");
		DiskoRoleImpl rolle = new DiskoRoleImpl(app, name, title, description);

		NodeList children = elem.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child == null || child.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if (child.getNodeName().equalsIgnoreCase("DiskoModule")) {
				String className = ((Element) child).getAttribute("classname");
				Boolean isDefault = false;
				if(((Element) child).hasAttribute("isDefault")) {
					isDefault = Boolean.valueOf(((Element) child).getAttribute("isDefault"));
				}
				Class cls = classLoader.loadClass(className);
				String message = DiskoStringFactory.getText(Utils.getPackageName(cls)+".name");
				message = String.format(DiskoStringFactory.getText("PROGRESS.STARTUP.CLASS.text"),message);
				DiskoProgressMonitor.getInstance().setNote(message);
				Object[] args = {rolle};
				Object obj = cls.getConstructors()[0].newInstance(args);
				if (obj instanceof IDiskoWpModule) {
					IDiskoWpModule module = (IDiskoWpModule)obj;
					rolle.addDiskoWpModule(module,isDefault);
				}
				else {
					throw new Exception("Unsupported class was found");
				}
			}
		}
		return rolle;
	}
}
