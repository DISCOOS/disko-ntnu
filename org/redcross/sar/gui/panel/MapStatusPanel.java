/**
 * 
 */
package org.redcross.sar.gui.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.util.MsoUtils;

import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.Point;

/**
 * @author kennetgu
 *
 */
public class MapStatusPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public enum BarItemType {
		BAR_ITEM_CLICKPOINT,
		BAR_ITEM_MOUSEOVERPOINT,
		BAR_ITEM_SCALE,
		BAR_ITEM_SELECTED,
		BAR_ITEM_USERDEFINED
	}
	
	private BarItem clickLabel = null;
	private BarItem mouseOverLabel = null;
	private BarItem selectedLabel = null;	
	private BarItem scaleLabel = null;	
	
	private HashMap<String,BarItem> bars = null;

	private int rows;
	
	/**
	 * Constructor 
	 */
	public MapStatusPanel() {
		
		// initialize
		bars = new HashMap<String,BarItem>(BarItemType.values().length);
		
		FlowLayout fl = new FlowLayout();
		fl.setVgap(0);
		fl.setHgap(10);
		fl.setAlignment(FlowLayout.LEFT);
		setLayout(fl);
		setFocusable(false);
		setRows(1);
		setBackground(Color.WHITE);
		add(getClickLabel());
		add(getMouseOverLabel());
		add(getScaleLabel());
		add(getSelectedLabel());
		
		// listen for show event
		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentShown(ComponentEvent e) {
				// forward
				fitHightToRows();
			}

			@Override
			public void componentResized(ComponentEvent e) {
				// forward
				fitHightToRows();
			}
			
		});
		
		// is not focusable
		setFocusable(false);
		
	}
	
	public int getRows() {
		return rows;
	}
	
	public void setRows(int rows) {
		rows = Math.max(1,rows);
		int h = 20*rows;
		setMinimumSize(new Dimension(20,h));
		setPreferredSize(new Dimension(20,h));
		setMaximumSize(new Dimension(Integer.MAX_VALUE,h));
		this.rows = rows;
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
	
	public void onMouseClick(IPoint p) {
		try {
			if(p!=null && !p.isEmpty())
				clickLabel.setValue(MapUtil.formatMGRS(MapUtil.getMGRSfromPoint((Point)p,5),5,3,true));		
			else {
				mouseOverLabel.setEmpty();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			clickLabel.setEmpty();
		}
	}
	
	public void onMouseMove(IPoint p) {
		try {
			if(p!=null && !p.isEmpty()) {
				String text = MapUtil.formatMGRS(MapUtil.getMGRSfromPoint((Point)p,5),5,3,true);
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
	
	public void onSelectionChanged(List<IMsoFeature> list) {
		// update status panel?
		if(isVisible()) {
			IMsoObjectIf msoObj = list.size()>0 ? list.get(0).getMsoObject() :null;
			if(msoObj!=null) {
				selectedLabel.setValue(MsoUtils.getCompleteMsoObjectName(msoObj,1)); 									
			}
			else {
				selectedLabel.setEmpty(); 
			}
		}		
	}

	public void setScale(double scale) {
		// update status panel?
		if(isVisible()) {
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
		MapStatusPanel.this.remove(item);
	}
	
	public void reset() {
		Collection<BarItem>  c = bars.values();
		for(BarItem it : c) {
			it.setEmpty();
		}
	}
	
	private void fitHightToRows() {
		if(isShowing()) {
			int rows = calculateRowCount();
			if(getRows()!=rows) setRows(rows);
		}
	}
	
	private int calculateRowCount() {
		int y = -1;
		int rows = 0;
		int count = getComponentCount();
		for(int i=0;i<count;i++) {
			if(getComponent(i) instanceof BarItem) {
				BarItem item = (BarItem)getComponent(i);
				if(item.getY()!=y) {
					y = item.getY();
					rows++;
				}
			}
		}
		return rows;
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
			MapStatusPanel.this.add(this);
		}
		
		BarItem(String label, String prefix, String postfix, 
				String empty, boolean isHtml, MapStatusPanel.BarItemType type) {
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
			MapStatusPanel.this.add(this);
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
			// forward
			fitHightToRows();
		}
		
		public void setEmpty() {
			setValue(empty);
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
	
	@SuppressWarnings("serial")
	public class ItemNotExistException extends Exception {};
	
	@SuppressWarnings("serial")
	public class CanNotRemoveItemException extends Exception {};	
	
}
