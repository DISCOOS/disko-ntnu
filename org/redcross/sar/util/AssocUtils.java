package org.redcross.sar.util;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AssocUtils {

	private static final Logger m_logger = Logger.getLogger(AssocUtils.class);
    
    private static Document m_associations;
    
    static {
    	try {
			m_associations = Utils.getXmlDoc("Associations.xml");
		} catch (Exception e) {
			m_logger.error("Failed to load Associations.xml",e);
		}
    }
    
    public static Association[] getAssociations() {
    	return getAssociations(-1);
    }
    
    public static Association[] getAssociations(int level) {
    	return getAssociations(level,null);
    }
    
    public static Association[] getAssociations(int level,String pattern) {
    	List<Association> list = new Vector<Association>();
    	for(Association org : getOrganizations(pattern)) {
    		if(level<2)list.add(org);
    		for(Association div : getDivisions(org,pattern)) {
    			if(level==-1||level==2)list.add(div);
        		for(Association dep : getDepartments(div,pattern)) {
        			if(level==-1||level==3)list.add(dep);
        		}
    		}
    	}
    	return list.size()>0?list.toArray(new Association[list.size()]):null;
    }
    
	public static Association[] getOrganizations() {
		return getOrganizations(null);
	}
	
	public static Association[] getOrganizations(String pattern) {
		String[] names = getOrganizationNames();
		int size = names.length;
		Association[] list = new Association[size];
		for(int i=0;i<size;i++) {
			list[i] = new Association(names[i],"",null,1,pattern);
		}
		return list;
	}
	
	public static Association getOrganization(String name) {
		Integer[] idx = findOrganization(name, true, false);
		if(idx!=null) {
			String[] names = getOrganizationNames();
			return new Association(names[idx[0]],"",null,1,null);
		}
		return new Association(name,"",null,0,null);
	}
		
	public static String[] getOrganizationNames() {
		return getOrganizationAttrs("name","");		
	}
	
	public static String[] getOrganizationAttrs(String attrName, String suffix) {
		return Utils.getXmlAttrs(m_associations.getDocumentElement(),"Organization",attrName,suffix);		
	}
	
	public static String getOrganizationAttr(String org, String attrName) {
		Element[] list = Utils.getXmlNode(m_associations.getDocumentElement(), "Organization", "name", org, true, false);
		if(list!=null) {
			return list[0].getAttribute(attrName);		
			
		}
		return null;
	}
	
	public static Integer[] findOrganization(String org, boolean isStrict, boolean isCaseSensitive) {
		return findOrganization(getOrganizationNames(),org,isStrict,isCaseSensitive);
	}
	
	public static Integer[] findOrganization(String[] orgs, String org, boolean isStrict, boolean isCaseSensitive) {
		List<Integer> list = new Vector<Integer>();
		String pattern = (isStrict ? org : org + ".*");
		pattern = isCaseSensitive?pattern:pattern.toLowerCase();
		int count = orgs.length;
		for(int i=0;i<count;i++) {
			String match = isCaseSensitive?orgs[i] : orgs[i].toLowerCase();
			if(match.matches(pattern)) list.add(i);
		}
		// finished
		return (list.size()>0?list.toArray(new Integer[list.size()]) : null);
	}
	
	public static Integer[] findOrganization(String[] orgs, String department, String suffix, boolean isStrict, boolean isCaseSensitive) {
		List<Integer> list = new Vector<Integer>();
		int iCount = orgs.length;
		for(int i=0;i<iCount;i++) {
			String org = orgs[i];
			String[] divs = getDivisionNames(org,false);
			String[] sufs = getDivisionAttrs(org, "suffix", "");
			int jCount = divs.length;
			for(int j=0;j<jCount;j++) {
				String div = divs[j];
				String[] deps = getDepartmentNames(org, div);
				int kCount = deps.length;
				for(int k=0;k<kCount;k++) {
					String search;
					String dep = deps[k];
					String suf = sufs[j];
					String pattern = (isStrict ?  department + "(\\s)*" + suffix : department + ".*" + suffix + ".*");
					if(dep!=null) {
						search = dep;
						if(suf!=null) {
							search = search.concat(" " + suf);
						}
						pattern = isCaseSensitive? pattern : pattern.toLowerCase();
						search = isCaseSensitive? search : search.toLowerCase();
						if(search.matches(pattern)) list.add(i);
					}
				}
			}
		}
		// finished
		return (list.size()>0?list.toArray(new Integer[list.size()]) : null);
	}
	
	public static Association[] getDivisions() {
		return getAssociations(2);
	}	
	
	public static Association[] getDivisions(String pattern) {
		return getAssociations(2,pattern);
	}	
	
	public static Association getDivision(Association organization, String name) {
		Association[] orgs = getDivisions(organization);
		if(orgs!=null) {
			for(Association it : orgs) {
				if(name.equalsIgnoreCase(it.getName())) return it;
			}
		}
		return new Association(name,"",organization,2,null);
	}
	
	public static String[] getDivisionNames(boolean suffix) {
		if(suffix) {
			List<String> list = new Vector<String>();
			for(String org : getOrganizationNames()) {
				for(String div : getDivisionNames(org,suffix)) {
					list.add(div);
				}
			}
			return (list.size()>0 ? list.toArray(new String[list.size()]) : null);			
		} else {
			return Utils.getXmlAttrs(m_associations.getDocumentElement(),"Division","name");
		}
	}
		
	public static Association[] getDivisions(Association organization) {
		return getDivisions(organization,null);
	}
	
	public static Association[] getDivisions(Association organization,String pattern) {
		String[] names = getDivisionNames(organization.getName(),false);
		Association[] list = new Association[0];
		if(names!=null&&names.length>0) {
			String suffix = getOrganizationAttr(organization.getName(),"suffix");
			int size = names.length;
			list = new Association[size];
			for(int i=0;i<size;i++) {
				list[i] = new Association(names[i],suffix,organization,2,pattern);
			}
		}
		return list;
	}	
	
	public static String[] getDivisionNames(String organization) {
		return getDivisionNames(false);
	}
	
	public static String[] getDivisionNames(String organization, boolean suffix) {
		String s = suffix?" " + getOrganizationAttr(organization, "suffix"):"";
		return getDivisionAttrs(organization,"name",s);
	}
	
	public static String[] getDivisionAttrs(String organization, String attrName, String suffix) {
		
		// load map source data
		NodeList elems = m_associations.getElementsByTagName("Organization");
		// get size
		int size = elems.getLength();
		// locate and update
		for (int i = 0; i < size; i++) {
			// get element
			Element e = (Element)elems.item(i); 
			// search for organization name
			if(e.getAttribute("name").compareTo(organization)==0) {
				// get divisions
				return Utils.getXmlAttrs(e,"Division",attrName,suffix);
			}
		}
		// failure
		return null;
	}
	
	public static String getDivisionAttr(String organization, String division, String attrName) {
		Element[] list = Utils.getXmlNode(m_associations.getDocumentElement(),"Organization","name",organization,true,false);
		if(list!=null) {
			list = Utils.getXmlNode(list[0],"Division","name",division,true,false);
			if(list!=null) {
				return list[0].getAttribute(attrName);		
			}
		}
		return null;
	}
	
	public static Integer[] findDivision(String organization, String[] divs, String department, String suffix, boolean isStrict, boolean isCaseSensitive) {
		List<Integer> list = new Vector<Integer>();
		String[] sufs = Utils.getXmlAttrs(m_associations.getDocumentElement(),"Division","suffix");
		int iCount = divs.length;
		for(int i=0;i<iCount;i++) {
			String div = divs[i];			
			String[] deps = getDepartmentNames(organization,div);
			int jCount = deps.length;
			for(int j=0;j<jCount;j++) {
				String search;
				String dep = deps[j];
				String suf = sufs[i];
				String pattern = (isStrict ? department + "(\\s)*" + suffix : department + ".*" + suffix + ".*" );
				pattern = isCaseSensitive? pattern : pattern.toLowerCase();
				if(dep!=null) {
					search = dep;
					if(suf!=null) {
						search = search.concat(" " + suf);
					}
					search = isCaseSensitive? search : search.toLowerCase();
					if(search.matches(pattern)) list.add(i);
				}
			}
		}
		// finished
		return (list.size()>0?list.toArray(new Integer[list.size()]) : null);
	}
	
	public static Association[] getDepartments() {
		return getAssociations(3);		
	}
	
	public static Association getDepartment(Association division, String name) {
		Association[] divs = getDepartments(division);
		if(divs!=null) {
			for(Association it : divs) {
				if(name.equalsIgnoreCase(it.getName())) return it;
			}
		}
		return new Association(name,"",division,3,null);
	}
	
	public static String[] getDepartmentNames(boolean suffix) {
		if(suffix) {
			List<String> list = new Vector<String>();
			for(String org : getOrganizationNames()) {
				for(String div : getDivisionNames(org,false)) {
					for(String dep : getDepartmentNames(org, div, suffix)) {
						list.add(dep);
					}
				}
			}
			return (list.size()>0 ? list.toArray(new String[list.size()]) : null);			
		} else {
			return Utils.getXmlAttrs(m_associations.getDocumentElement(),"Department","name");
		}
	}	
	
	public static Association[] getDepartments(Association division) {
		return getDepartments(division,null);
	}
	
	public static Association[] getDepartments(Association division,String pattern) {
		String[] names = getDepartmentNames(division.getName(1),division.getName(),false);
		Association[] list = new Association[0];
		if(names!=null&&names.length>0) {
			String suffix = getDivisionAttr(division.getName(1),division.getName(),"suffix");
			int size = names.length;
			list = new Association[size];
			for(int i=0;i<size;i++) {
				list[i] = new Association(names[i],suffix,division,3,pattern);
			}
		}
		return list;
	}	
	
	
	public static String[] getDepartmentNames(String organization, String division) {
		return getDepartmentNames(organization, division, false);
	}
	
	public static String[] getDepartmentNames(String organization, String division, boolean suffix) {
		String s = suffix?" " + getDivisionAttr(organization, division, "suffix"):"";
		return getDepartmentAttrs(organization, division, "name", s);
	}
		
	public static String[] getDepartmentAttrs(String organization, String division, String attrName, String suffix) {
		
		// get organization list
		NodeList elems = m_associations.getElementsByTagName("Organization");
		// get size
		int size = elems.getLength();
		// locate and update
		for (int i = 0; i < size; i++) {
			// get element
			Element e = (Element)elems.item(i); 
			// search for organization name
			if(e.getAttribute("name").compareTo(organization)==0) {
				// get division list 
				elems = e.getElementsByTagName("Division");
				// get size
				size = elems.getLength();
				// locate and update
				for (int j = 0; j < size; j++) {
					// get element
					e = (Element)elems.item(j); 
					// search for organization name
					if(e.getAttribute("name").compareTo(division)==0) {
						// get department attributes
						return Utils.getXmlAttrs(e,"Department",attrName,suffix);
					}
				}
			}
		}
		// failure
		return null;
	}
	
	public static String getDepartmentAttr(String organization, String division, String department, String attrName) {
		Element[] list = Utils.getXmlNode(m_associations.getDocumentElement(),"Organization","name",organization,true,false);
		if(list!=null) {
			list = Utils.getXmlNode(list[0],"Division","name",division,true,false);
			if(list!=null) {
				list = Utils.getXmlNode(list[0], "Department", "name", department, true, false);
				if(list!=null) {
					return list[0].getAttribute(attrName);							
				}				
			}
		}
		return null;
	}
	
	/**
	 * Parse an association string into an organization, division and department array
	 * @param association - the association string. <br>
	 * Format: department [suffix] | division [suffix] | organization
	 * @return Association[] - array of associations matching the association string </p>
	 * If several organizations or divisions are found to match the association string, the first occurrences are returned
	 */
	public static Association[] parse(String association, boolean isStrict, boolean isCaseSensitive) {
		List<Association> list = new Vector<Association>(); 
		String pattern = (isStrict ? association : association + ".*");
		pattern = isCaseSensitive? pattern : pattern.toLowerCase();
		Association[] items = getAssociations();
		// loop over all associations
		for(Association it : items) {
			String s = isCaseSensitive?it.format():it.format().toLowerCase();
			if(s.matches(pattern)) {
				list.add(it);
			}
		}		
		// finished
		return list.size()>0?list.toArray(new Association[list.size()]):null;
	}	
	
	/**
	 * Get formatted array of strings
	 * 
	 * @param pattern - the format pattern. </p>
	 * The following fields are available: <br>
	 * '{x:n}' - association name<br>
	 * '{x:s}' - association suffix<br>
	 * where 'x' is the level. If 'x'='l', <code>getLevel()</code> is used. For 
	 * example, '{1:n}' will print the name at level 1. Likewise, '{l:n}' will 
	 * print the name of the invoked association.<br>
	 * '\' is the escape character for '{' and '}'</p>
	 * @param associations - the association to format as strings
	 * @return String[] - array of formatted string 
	 */	
	public static String[] format(String pattern, Association[] associations) {
		String[] formatted = new String[associations.length];
		int count = associations.length;
		for(int i=0;i<count;i++) {
			formatted[i] = associations[i].format(pattern);
		}
		return formatted;
	}
	
	public static class Association  {
		
		private int level;
		private String name;
		private String suffix;
		private Association parent;
		private String pattern;
		
		public Association(String name, String suffix, Association parent, int level, String pattern) {		
			this.name = name;
			this.suffix = suffix;
			this.level = parent!=null?parent.getLevel()+1 : level;
			this.parent = parent;
			this.pattern = pattern;
		}
		
		public Association(String name, String suffix) {
			this.name = name;
			this.suffix = suffix;
			this.level = 0;
		}
		
		public String getName() {
			return getName(level);
		}
		
		public String getName(int level) {
			String text = "";
			if(level<this.level && parent!=null) { 
				return parent.getName(level);
			} else if(level==this.level) {
				text = name;
			}
			return text;
		}
		
		public String getSuffix() {
			return getSuffix(level);
		}
		
		public String getSuffix(int level) {
			String text = "";
			if(level<this.level && parent!=null) { 
				return parent.getSuffix(level);
			} else if(level==this.level) {
				text = suffix;
			}
			return text;
		}
		
		public String getText() {
			return getText(level); 
		}
		
		public String getText(int level) {
			String text = "";
			if(level<this.level && parent!=null) { 
				text = parent.getText(level);
			} else if(level==this.level) {
				String name = getName();
				String suffix = getSuffix();
				text = name + (suffix!=null&&!suffix.isEmpty()?" "+suffix:"");
			}
			return text;
		}		
		
		public boolean isUnknown() {
			return (level==0);
		}
		
		public int getLevel() {
			return level;
		}
		
		public Association getParent() {
			return parent;
		}
		
		public String getPattern() {
			return pattern;
		}

		public void setPattern(String pattern) {
			this.pattern = pattern;
		}
		
		/**
		 * Get association as formatted string
		 * 
		 * @param pattern - the format pattern. </p>
		 * The following fields are available: <br>
		 * '{x:n}' - association name<br>
		 * '{x:s}' - association suffix<br>
		 * where 'x' is the level. If 'x'='l', <code>getLevel()</code> is used. For 
		 * example, '{1:n}' will print the name at level 1. Likewise, '{l:n}' will 
		 * print the name of this association.<br>
		 * '\' is the escape character for '{' and '}'</p>
		 * @return String - association string 
		 */	
		public String format(String pattern) {
			int pos = 0;
			int end = 0;
			String text = pattern;
			while((pos=pattern.indexOf("{", pos))!=-1) {
				if(pos==0 || pos>0&&pattern.charAt(pos-1)!='\\') {
					end = pattern.indexOf("}", pos);
					if(end>0&&pattern.charAt(end-1)!='\\') {
						String field = pattern.substring(pos,end+1);
						String[] split = field.split(":");
						if(split.length>1) {
							String s = split[0].replaceAll("\\{", "");
							int level = s.equalsIgnoreCase("l")?this.level:Integer.valueOf(s);
							String attr = split[1].replaceAll("\\}", "");
							field = "\\{"+s+":"+attr+"\\}";
							if(attr.length()==1) {
								switch(attr.charAt(0)) {
								case 'n': 
									text = text.replaceAll(field, getName(level));
									break;
								case 's': 
									text = text.replaceAll(field, getSuffix(level));
									break;
								} 								
							}							
						}
					}
					pos = end+1;
				}					
			}
			return text;
		}
		
		public String format() {
			return pattern!=null&&pattern.length()>0?format(pattern):format(level);
		}
		
		public String format(int level) {
			level = Math.min(this.level, level);
			String text = getText();
			for(int i=level-1;i>0;i--) {
				text = text.length()>0?text.concat(", " + getText(i)):getText();
			}
			return text;
		}
		
		@Override
		public String toString() {
			return format();
		}
		
	}
	
	public static void main(String[] args) {
		Association[] items = parse("Oslo",false,false);
		for(String it : format("Org:{3:n},{2:n},{1:n}",items)) {
			System.out.println(it);
		}		
	}
	
}
