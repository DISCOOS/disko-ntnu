/**
 * 
 */
package org.redcross.sar.ds.ete;

import java.lang.Math;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

/**
 *  Route Cost property class
 *  
 * @author kennetgu
 *
 */

public class RouteCostProp {
	
	protected ResourceBundle m_res;	  	
  	
	private int m_count;
	private int[] m_dims;
	private double m_min;
	private double m_max;
	private double m_weight;
	private double m_alfa;
	private double m_alfa_inv;
	private boolean m_constant;
	private String m_key;
	private String m_name;
  	private HashMap m_keys;
  	private String[] m_captions;
  	private Object[] m_params;
	
	/**
	 *  Constructor initialize the maximum index value
	 *  
	 */		
	public RouteCostProp(ResourceBundle res, String key) {

		// save resource bundle handle
		m_res = res;
		
		// save property key
		m_key = key;
		
		// get property name
		m_name = m_res.getString(key);
		
		// forward
		fill();
	}

	/**
	 *  Fills property from resource file
	 *  
	 */		
	@SuppressWarnings("unchecked")
	public void fill() {
		
		// get attribute count
		m_count = Integer.valueOf(getProp(m_key + ".COUNT")).intValue();
		
		// get min and max value
		m_min = Double.valueOf(getProp(m_key + ".MIN")).doubleValue();
		m_max = Double.valueOf(getProp(m_key + ".MAX")).doubleValue();
		
		// calculate alfa
		m_alfa = (m_max - m_min) / (m_count - 1);
		
		// calculate inverted alfa
		m_alfa_inv = 1 / m_alfa;
		
		// get constant flag
		m_constant = Boolean.valueOf(getProp(m_key + ".CONSTANT")).booleanValue();
		
		// create key-to-index map
		m_keys = new HashMap(m_count);
		
		// allocate memory
		m_captions = new String[m_count];
					
		// get all keys and captions
		for(int i=0;i<m_count;i++){
			// put key i
			m_keys.put(getProp(m_key + ".KEY." + i),i);
			// put caption i
			m_captions[i] = getProp(m_key + ".CAPTION." + i);				
		}

		// get parameters
		if (m_constant) {
			// get weight
			m_weight = Double.valueOf(getProp(m_key + ".WEIGHT")).doubleValue();
		}
		else {
			// apply default weight
			m_weight = 1;			
			// get parameter dimension count
			int dim = Integer.valueOf(getProp(m_key + ".PARAM.DIM.COUNT")).intValue();
			// allocate dimensions array
			m_dims = new int[dim];
			// loop over the next dimensions
			for(int i=0;i<dim;i++){
				// get getProp(namecount
				int upper = Integer.valueOf(getProp(m_key + ".PARAM.DIM." + i)).intValue();
				// save in dimension size array
				m_dims[i] = upper;
				// add dimension
				addDimension(m_params, upper + 1);
			}
			// get all parameters
			getParams(m_params,m_key + ".PARAM");
		}					
	}
	
	/**
	 *  Gets property resource bundle key
	 *  
	 */		
	public String getKey() {
		return m_key;
	}
	
	/**
	 *  Gets property variable name
	 *  
	 */		
	public String getName() {
		return m_name;
	}
	
	/**
	 *  Gets minimum property variable value
	 *  
	 */		
	public double getMin() {
		return m_min;
	}
	
	/**
	 *  Gets maxmum property property value
	 *  
	 */		
	public double getMax() {
		return m_max;
	}
	
	/**
	 *  Gets if property is a constant. If false, then the property have parameters
	 *  
	 */		
	public boolean isConstant() {
		return !m_constant;
	}
	
	/**
	 *  Gets minimum property variable value
	 *  
	 */		
	public double getWeight() {
		return m_weight;
	}
	
	/**
	 *  Gets property index count
	 *  
	 */		
	public int getCount() {
		return m_count;
	}
	
	/**
	 *  Gets property from index from variable value
	 *  
	 */		
	public int getIndex(double variable) {
		// apply min-max on variable
		variable = Math.max(Math.min(variable,m_max),m_min);
		// index = (count - 1)/(max - min) * (variable - min)
		return (int)(m_alfa_inv * (variable - m_min));
	}

	/**
	 *  Gets property value at index with specified arguments
	 *  
	 *  @param index variable index
	 *  @param args cost property arguments
	 *  @param param parameter index
	 *  @param method calculation method</p>
	 *  method:=0 -> cost=params[param];</br>
	 *  method:=1 -> cost=params[param]*variable;</br>
	 *  @return Returns value for given variable index, property arguments, and calculation method
	 */
	public double getValue(int index, int[] args, int param, int method) {
		// get parameters
		double params[] = getParams(args);
		// get method
		switch(method) {
		case 0: return params[param];
		case 1: return params[param]*getVariable(index);
		default: return 0.0;
		}
	}
	
	/**
	 *  Gets weighted property variable
	 *  
	 */		
	public double getWeightedVariable(double variable) {
		// apply min-max on variable
		variable = Math.max(Math.min(variable,m_max),m_min);
		// value = weight * Variable
		return m_weight*variable;
	}
	
	/**
	 *  Limit property variable value
	 *  
	 */		
	public double limitVariable(double variable) {
		// apply min-max on variable
		return Math.max(Math.min(variable,m_max),m_min);
	}
	
	/**
	 *  Gets property variable value from index
	 *  
	 */		
	public double getVariable(int index) {
		// variable = (max - min)/(count - 1) * index + min
		return m_alfa * index + m_min;
	}
	
	/**
	 *  Gets property value caption from index
	 *  
	 */		
	public String getCaption(int index) {
		return m_captions[index];
	}
	
	/**
	 *  Gets parameter argument count
	 *  
	 */		
	public int getArgumentCount() {
		return m_dims.length;
	}
	
	/**
	 *  Gets maximum argument value from index
	 *  
	 */		
	public int getArgumentMax(int index) {
		return m_dims[index];
	}
	
	/**
	 *  Gets property parameter value from argument array
	 *  
	 */		
	public double[] getParams(int[] args) {
		// start to recurse on first argument
		return getParams(m_params,args,0);
	}
	
	
	/**
	 *  Allocates a new array to all cells in array
	 *  
	 */		
	private void addDimension(Object[] arr, int size) {
		// is empy?
		if (arr == null){
			// create dimension array
			m_params = new Object[size];			
		}
		else {
			for(int i=0;i<arr.length;i++){
				// get object
				Object o = arr[i];
				// has value?
				if(o != null){
					// is array?
					if(arr[i].getClass().isArray()){
						// cast and recurse
						addDimension((Object[])arr[i],size);
					}
				}
				// allocate
				arr[i]= new Object[size];
			}
		}
	}
	
	/**
	 *  get all parameters in current dimension
	 *  
	 */		
	private void getParams(Object[] arr, String key) {
		for(int i=0;i<arr.length;i++){
			// initialize
			boolean bfill = true;
			// get object
			Object o = arr[i];
			// get key
			String nextKey = new String(key + "." + i);
			// has value?
			if(o != null){
				// is array?
				if(arr[i].getClass().isArray()){
					// cast and recurse
					getParams((Object[])arr[i],nextKey);
					// do not fill this cell...
					bfill = false;
				}
			}
			// get property value?
			if (bfill){
				// try to get property value
				try
				{
					// get parameter values
					String[] splits = m_res.getString(nextKey).split("#");
					double params[] = new double[2];					
					params[0] = Double.valueOf(splits[0]);
					params[1] = Double.valueOf(splits[1]);
					// save parameters
					arr[i]= params;
				}
				catch(MissingResourceException e)
				{
					System.err.println("Unable to get property " + nextKey + " in RouteCostParams");
				}
			}
		}
	}
	
	/**
	 *  get parameter value as function of arguments = {i1,i2,...,in}
	 *  
	 */		
	private double[] getParams(Object[] arr, int[] ids, int i) {
		int j = ids[i];
		// cast and recurse?
		if (arr[j] instanceof double[]) {
			// get parameters 
			double[] params = ((double[])arr[j]);
			// return parameters
			return params;
		} 
		else {
			// recurse...
			return getParams((Object[])arr[j],ids,i+1);				
		}				
	}
	
	/**
	 *  get property from resource bundle
	 *  
	 */		
	private String getProp(String name) {
		String value = null;
		// try to get properties
		try
		{
			value = m_res.getString(name);
		}
		catch(MissingResourceException e)
		{
			System.err.println("Unable to get property " + name + " in RouteCostParams");
		}
		return value;		
	}
}
