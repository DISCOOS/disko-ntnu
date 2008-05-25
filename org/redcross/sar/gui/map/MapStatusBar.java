/**
 * 
 */
package org.redcross.sar.gui.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.util.MsoUtils;

import com.esri.arcgis.geometry.Point;

/**
 * @author kennetgu
 *
 */
public class MapStatusBar extends JPanel {

	private static final long serialVersionUID = 1L;

	public enum BarItemType {
		BAR_ITEM_CLICKPOINT,
		BAR_ITEM_MOUSEOVERPOINT,
		BAR_ITEM_SCALE,
		BAR_ITEM_SELECTED,
		BAR_ITEM_USERDEFINED
	}
	
	private JPanel statusPanel = null;
	private BarItem clickLabel = null;
	private BarItem mouseOverLabel = null;
	private BarItem selectedLabel = null;	
	private BarItem scaleLabel = null;	
	
	private HashMap<String,BarItem> bars = null;
	
	/**
	 * Constructor 
	 */
	public MapStatusBar() {
		
		// initialize
		bars = new HashMap<String,BarItem>(BarItemType.values().length);
		
		// add status panel
		setLayout(new BorderLayout());
		add(getStatusPanel(),BorderLayout.NORTH);
		
		// is not focusable
		setFocusable(false);
		
	}

	private JPanel getStatusPanel() {
		if (statusPanel == null) {
			try {
				FlowLayout fl = new FlowLayout();
				fl.setVgap(0);
				fl.setHgap(10);
				fl.setAlignment(FlowLayout.LEFT);
				statusPanel = new JPanel();
				statusPanel.setLayout(fl);
				statusPanel.setFocusable(false);
				statusPanel.setPreferredSize(new Dimension(20,20));
				statusPanel.setBackground(Color.WHITE);
				statusPanel.add(getClickLabel());
				statusPanel.add(getMouseOverLabel());
				statusPanel.add(getScaleLabel());
				statusPanel.add(getSelectedLabel());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return statusPanel;
	}
	
	private JLabel getClickLabel() {
		if (clickLabel == null) {
			clickLabel = new BarItem("Siste", "","",
					"<klikk>", true,
					BarItemType.BAR_ITEM_CLICKPOINT);
		}
		return clickLabel;
	}
	
	private JLabel getMouseOverLabel() {
		if (mouseOverLabel == null) {
			mouseOverLabel = new BarItem("Posisjon","","",
					"<beveg>",true,
					BarItemType.BAR_ITEM_MOUSEOVERPOINT);
		}
		return mouseOverLabel;
	}
	
	private JLabel getSelectedLabel() {
		if (selectedLabel == null) {
			selectedLabel = new BarItem("Objekt","<b>","",
					"<velg>",true,
					BarItemType.BAR_ITEM_SELECTED);
		}
		return selectedLabel;
	}
	
	private JLabel getScaleLabel() {
		if (scaleLabel == null) {
			scaleLabel = new BarItem("Skala","<b>1:","",
					"<velg>",true,BarItemType.BAR_ITEM_SCALE);
		}
		return scaleLabel;
	}
	
	public void onMouseDown(Point p) {
		try {
			if(p!=null && !p.isEmpty())
				clickLabel.setValue(MapUtil.formatMGRS(MapUtil.getMGRSfromPoint(p),3,true));		
			else {
				mouseOverLabel.setEmpty();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			clickLabel.setEmpty();
		}
	}
	
	public void onMouseMove(Point p) {
		try {
			if(p!=null && !p.isEmpty()) {
				String text = MapUtil.formatMGRS(MapUtil.getMGRSfromPoint(p),3,true);
				mouseOverLabel.setValue(text);
				mouseOverLabel.updateUI();
			}
			else {
				mouseOverLabel.setEmpty();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			mouseOverLabel.setEmpty();
		}
	}
	
	public void setSelected(IMsoObjectIf msoObj) {
		// update status panel?
		if(getStatusPanel().isVisible()) {
			if(msoObj!=null) {
				IAreaIf area = MsoUtils.getOwningArea(msoObj);
				if(area!=null && area.getOwningAssignment()!=null) {
					String name = MsoUtils.getAssignmentName(area.getOwningAssignment(), 1);
					name += " - " + MsoUtils.getMsoObjectName(msoObj,1);
					selectedLabel.setValue(name); 									
				}
				else
					selectedLabel.setValue(MsoUtils.getMsoObjectName(msoObj,1)); 				
			}
			else {
				selectedLabel.setEmpty(); 
			}
		}		
	}

	public void setScale(double scale) {
		// update status panel?
		if(getStatusPanel().isVisible()) {
			scaleLabel.setValue(Integer.toString((int)scale)); 				
		}		
	}
	
	public void setBarVisible(String key, boolean isVisible) 
		throws ItemNotExistException {
		if(!bars.containsKey(key))
			throw new ItemNotExistException();
		bars.get(key).setVisible(isVisible);
	}
	
	public boolean isBarVisible(String key) 
		throws ItemNotExistException {
		if(!bars.containsKey(key))
			throw new ItemNotExistException();
		return bars.get(key).isVisible();
	}
	
	public BarItem getItem(String key)
		throws ItemNotExistException {
		if(!bars.containsKey(key))
			throw new ItemNotExistException();
		return bars.get(key);
		
	}
	
	public BarItem addBarItem(String label, String prefix, String postfix, 
			String empty, boolean isHtml, String key) 
		throws ItemAlreadyExistException {
		BarItem item = new BarItem(label,prefix,postfix,empty,isHtml,key);
		return item;
	}
	
	public void removeBarItem(String key) 
		throws ItemNotExistException, CanNotRemoveItemException {
		if(!bars.containsKey(key))
			throw new ItemNotExistException();
		BarItem item = bars.get(key);
		if (item.getType().equals(BarItemType.BAR_ITEM_USERDEFINED))
			throw new CanNotRemoveItemException();
		bars.remove(key);
		getStatusPanel().remove(item);
	}
	
	public void reset() {
		Collection<BarItem>  c = bars.values();
		for(BarItem it : c) {
			it.setEmpty();
		}
	}
	
	public class BarItem extends JLabel {

		private static final long serialVersionUID = 1L;
		private static final String SEMICOLON = ":";
		private static final String SPACE = " ";
		private static final String OPEN_TAG = "<html>";
		private static final String CLOSE_TAG = "</html>";
		
		private String label = null;
		private String prefix = null;
		private String postfix = null;
		private String empty = null;
		private String value = null;
		private boolean isHtml = false;
		private BarItemType type = BarItemType.BAR_ITEM_USERDEFINED;
		
		BarItem(String label, String prefix, String postfix, 
				String empty, boolean isHtml, String key) 
			throws ItemAlreadyExistException {
								
			// validate
			if(bars.containsKey(key)) {
				throw new ItemAlreadyExistException(key);				
			}
			
			this.label = label;
			this.prefix = prefix;
			this.postfix = postfix;
			this.empty = empty;
			this.isHtml = isHtml;
			setOpaque(false);
			setFocusable(false);
			bars.put(key, this);
			setEmpty();
			getStatusPanel().add(this);
		}
		
		BarItem(String label, String prefix, String postfix, 
				String empty, boolean isHtml, MapStatusBar.BarItemType type) {
			this.label = label;
			this.prefix = prefix;
			this.postfix = postfix;
			this.empty = empty;
			this.type = type;
			this.isHtml = isHtml;
			setOpaque(false);
			setFocusable(false);
			bars.put(type.toString(), this);
			setEmpty();
			getStatusPanel().add(this);
		}		
		
		public BarItemType getType () {
			return type;
		}
		
		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label=label;
			setValue();
		}
		
		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix=prefix;
			setValue();
		}
		
		public String getPostfix() {
			return postfix;
		}
		
		public void setPostfix(String postfix) {
			this.postfix=postfix;
			setValue();
		}
		
		public boolean getIsHtml() {
			return isHtml;
		}
		
		public void setIsHtml(boolean isHtml) {
			this.isHtml=isHtml;
			setValue();
		}
		
		public String getValue() {
			return value;
		}
		
		public void setValue() {
			setValue(value);
		}
		
		public void setValue(String value) {
			if(value==null || value.length()==0)				
				this.value = empty;
			else
				this.value = value;
			if(isHtml)
				setText(OPEN_TAG+label+SEMICOLON+SPACE+prefix+this.value+postfix+CLOSE_TAG);
			else
				setText(label+SEMICOLON+SPACE+prefix+this.value+postfix);
			
		}
		
		public void setEmpty() {
			this.value = empty;
			if(isHtml)
				setText(OPEN_TAG+label+SEMICOLON+SPACE+prefix+empty+postfix+CLOSE_TAG);
			else
				setText(label+SEMICOLON+SPACE+prefix+empty+postfix);
		}
	}
	
	public class ItemAlreadyExistException extends Exception {

		private static final long serialVersionUID = 1L;
		
		private String key = null;
		
		public ItemAlreadyExistException(String key) {
			this.key = key;
		}
		public String getKey() {
			return key;
		}
		
		public BarItem getItem() {
			return bars.get(key);
		}		
	}
	
	public class ItemNotExistException extends Exception {};
	
	public class CanNotRemoveItemException extends Exception {};	
	
}
