package org.redcross.sar.map.element;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.gui.factory.DiskoIconFactory;

import com.esri.arcgis.carto.FrameElement;
import com.esri.arcgis.carto.GroupElement;
import com.esri.arcgis.carto.IActiveView;
import com.esri.arcgis.carto.IElement;
import com.esri.arcgis.carto.IGraphicsContainer;
import com.esri.arcgis.carto.IGraphicsContainerSelect;
import com.esri.arcgis.carto.IGraphicsLayer;
import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.carto.SymbolBackground;
import com.esri.arcgis.carto.TextElement;
import com.esri.arcgis.display.IDisplay;
import com.esri.arcgis.display.IScreenDisplay;
import com.esri.arcgis.display.esriScreenCache;
import com.esri.arcgis.display.esriSimpleLineStyle;
import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.interop.AutomationException;

public class DrawFrame {

	private static final long serialVersionUID = 1L;

	private GroupElement groupElement = null;
	private TextElement textElement = null;
	private FrameElement textBoxElement = null;
	private FrameElement iconBoxElement = null;
	private FrameElement frameElement = null;
	
	private String selectedIcon = null;
	
	private Map<String,IconElement> nameIcons = null;
	private List<IconElement> orderIcons = null;
	//private Map<IconElement,Boolean> visibleIcons = null;
	
	private IActiveView activeView = null;
	private IGraphicsContainer container = null;
	
	private boolean isActive = false;
	private boolean isIconBoxActive = false;
	
	private IEnvelope dirtyArea = null;
	
	public DrawFrame(IActiveView activeView) throws IOException, UnknownHostException {

		// forward
		super();
		
		// prepare
		this.nameIcons = new HashMap<String, IconElement>();
		this.orderIcons = new ArrayList<IconElement>();
		//this.visibleIcons = new HashMap<IconElement,Boolean>();
		
		// forward
		setActiveView(activeView);
		
		// initialize elements
		initialize();
		
	}
	
	private void initialize() throws UnknownHostException, IOException {
		
		// add elements to group element
		getGroupElement().addElement(getFrameElement());
		getGroupElement().addElement(getTextBoxElement());
		getGroupElement().addElement(getTextElement());
		getGroupElement().addElement(addIcon("cancel",DiskoIconFactory.getPath("GENERAL.CANCEL", "24x24", null)));
		getGroupElement().addElement(addIcon("finish",DiskoIconFactory.getPath("GENERAL.FINISH", "24x24", null)));
		getGroupElement().addElement(addIcon("replace",DiskoIconFactory.getPath("GENERAL.EQUAL", "24x24", null)));
		getGroupElement().addElement(addIcon("continue",DiskoIconFactory.getPath("GENERAL.CONTINUE", "24x24", null)));
		getGroupElement().addElement(addIcon("append",DiskoIconFactory.getPath("GENERAL.PLUS", "24x24", null)));
		//getGroupElement().addElement(addIcon("delete",DiskoIconFactory.getIconPath("GENERAL.DELETE", "24x24", null)));
		getGroupElement().addElement(getIconBoxElement());				
		
	}
	
	public void setActiveView(IActiveView activeView) 
		throws IllegalArgumentException, AutomationException, IOException {
		
		// forward
		deactivate();
		
		// IMPORTANT: Make sure that the graphics container 
		// belongs to the focus map. ActiveView() only returns the 
		// focus map graphics container if a map in the document is activated
		if(activeView.isMapActivated()) {
			// forward
			setContainer(activeView);
		}
		else {
			// activate map
			activeView.setIsMapActivated(true);
			// forward
			setContainer(activeView);
			// deactivate map
			activeView.setIsMapActivated(false);
		}
		
		
		// update hook
		this.activeView = activeView;
		
	}
	
	private void setContainer(IActiveView activeView) 
		throws IllegalArgumentException, AutomationException, IOException {
		// remove from old?
		if(container!=null) {
			// remove element from current container?
			container.deleteElement(getGroupElement());
			container.deleteElement(getFrameElement());
			container.deleteElement(getTextBoxElement());
			container.deleteElement(getTextElement());
			for(IconElement it : nameIcons.values()) {
				container.deleteElement(it);
			}
			container.deleteElement(getIconBoxElement());
		}
		// was activated?
		if(activeView.isMapActivated()) {
			// create draw frame layer and get hook to IGraphicsContainer
			container = (IGraphicsContainer)MapUtil.createCompositeGraphicsLayer(activeView.getFocusMap(), "DrawFrame");
			// add elements to graphics container
			container.addElement(getGroupElement(),0);			
			container.addElement(getFrameElement(),0);
			container.addElement(getTextBoxElement(), 0);
			container.addElement(getTextElement(), 0);			
			for(IconElement it : nameIcons.values()) {
				container.addElement(it,0);
			}			
			container.addElement(getIconBoxElement(),0);
		}
		else throw new IllegalArgumentException("IActiveView must have an focus map");		
	}
	
	private TextElement getTextElement() throws UnknownHostException, IOException  {
		if(textElement==null) {
			// create text element
			textElement = new TextElement();
			// initialize geometry
			textElement.setGeometry(MapUtil.createPoint());
		}
		return textElement;
	}

	private FrameElement getFrameElement() throws UnknownHostException, IOException  {
		if(frameElement==null) {
			// create frame element
			frameElement = new FrameElement();
			// set name
			frameElement.setName("DRAWFRAME.FRAME");
			// set frame border
			frameElement.setBorder(MapUtil.getSymbolBorder());
			frameElement.setBackground(null);
			// initialize geometry
			frameElement.setGeometry(MapUtil.createEnvelope());
		}
		return frameElement;
	}
	
	private FrameElement getTextBoxElement() throws UnknownHostException, IOException  {
		if(textBoxElement==null) {
			// create frame element
			textBoxElement = new FrameElement();
			// set name
			textBoxElement.setName("DRAWFRAME.TEXTBOX");
			// set frame border
			textBoxElement.setBorder(null);
			textBoxElement.setBackground(MapUtil.getSymbolBackground(5,1));
			// initialize geometry
			textBoxElement.setGeometry(MapUtil.createEnvelope());
		}
		return textBoxElement;
	}
	
	private FrameElement getIconBoxElement() throws UnknownHostException, IOException  {
		if(iconBoxElement==null) {
			// create frame element
			iconBoxElement = new FrameElement();
			// set name
			iconBoxElement.setName("DRAWFRAME.ICONBOX");
			// set frame border
			iconBoxElement.setBorder(MapUtil.getSymbolBorder(0,255,255,esriSimpleLineStyle.esriSLSSolid));
			iconBoxElement.setBackground(null);
			// initialize geometry
			iconBoxElement.setGeometry(MapUtil.createEnvelope());
		}
		return iconBoxElement;
	}
	
	private GroupElement getGroupElement() throws UnknownHostException, IOException  {
		if(groupElement==null) {
			// create group element
			groupElement = new GroupElement();
			// set name
			groupElement.setName("DRAWFRAME.GROUP");
			// initialize geometry
			groupElement.setGeometry(MapUtil.createEnvelope());
		}
		return groupElement;
	}

	public String getText() throws AutomationException, UnknownHostException, IOException {
		return getTextElement().getText();		
	}
	
	public void setText(String text) throws AutomationException, UnknownHostException, IOException {
		// set dirty union
		setDirtyRegion(getTextBoxElement());
		// prepare
		getTextElement().setText(text);
		// forward?
		if(display()!=null) {
			update(getFrame());		
		}
		// set dirty
		getTextElement().isDirty();
		getTextBoxElement().isDirty();
		// set dirty union?
		setDirtyRegion(getTextBoxElement());
	}
	
	public void setFrame(IEnvelope e) throws AutomationException, UnknownHostException, IOException {
		// forward
		update(e,getText());
	}
	
	public IEnvelope getExtent() throws AutomationException, UnknownHostException, IOException {
		// set frame size
		IGeometry g = getGroupElement().getGeometry();
		return (g!=null) ? g.getEnvelope() : null;
	}	
	
	public IEnvelope getFrame() throws AutomationException, UnknownHostException, IOException {
		// set frame size
		IGeometry g = getFrameElement().getGeometry();
		return (g!=null) ? g.getEnvelope() : null;
	}
	
	public void update(IEnvelope frame, String text) throws AutomationException, UnknownHostException, IOException {
		// set dirty unions
		setDirtyRegion(getTextBoxElement());
		setDirtyRegion(getFrameElement());		
		// add icons to dirty area
		for(IconElement it : orderIcons) {
			if(it.isVisible())
				setDirtyRegion(it);
		}
		// set frame size
		getFrameElement().setGeometry(frame);
		// update
		getTextElement().setText(text);
		// forward?
		if(display()!=null) {
			update(frame.getEnvelope());		
		}		
		// set dirty
		getTextElement().isDirty();		
		getFrameElement().isDirty();		
		getTextBoxElement().isDirty();
		// set dirty unions
		setDirtyRegion(getTextBoxElement());
		setDirtyRegion(getFrameElement());		
		// add icons to dirty area
		for(IconElement it : orderIcons) {
			if(it.isVisible())
				setDirtyRegion(it);
		}
	}
	
	public void draw() throws AutomationException, IOException {
		// reset area
		dirtyArea = null;
		// get current screen display 
		IDisplay display = display();
		// start drawing operation
		display.startDrawing(display.getHDC(),(short) esriScreenCache.esriNoScreenCache);
		// draw this
		groupElement.draw(display, null);
		// finished
		display.finishDrawing();
	}
	
	public boolean isDirtyAreaDisjointWith(IEnvelope e) throws AutomationException, IOException {
		if(isDirty()) {
			if(dirtyArea instanceof Envelope) {
				return ((Envelope)dirtyArea).disjoint(e);
			}
		}
		// is disjoint
		return true;
	}
	
	public boolean isDirty() {
		return (dirtyArea!=null);
	}
	
	public void refresh() {
		// refresh group?
		if(isDirty()) {
			Runnable r = new Runnable() {
				public void run() {
					try {
						// only refresh this group inside the dirty area
						activeView.partialRefresh(8, container, dirtyArea);
						// reset area
						dirtyArea = null;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			SwingUtilities.invokeLater(r);
		}
	}
	
	private void update(IEnvelope e) throws AutomationException, UnknownHostException, IOException {
		// get screen extent
		IDisplay display = display();
		// get lower right point
		IPoint o = MapUtil.createPoint();
		// get upper left point
		IPoint p = e.getUpperLeft();
		// forward
		moveCaption(p);
		// get upper right point
		p = MapUtil.createPoint(e.getUpperRight());
		// get bounds of first icon
		IEnvelope b = display!=null ? MapUtil.getPictureBounds(display(), nameIcons.get("cancel")) : null;		
		// get first offset
		p.setY((b!=null && !b.isEmpty()) ? p.getY()+b.getHeight()/2 : p.getY());
		// set outside extent
		o.setY((b!=null && !b.isEmpty()) ? o.getY()+b.getHeight() : o.getY());
		// move icons
		for(IconElement it : orderIcons) {
			if(it.isVisible())
				p.setY(moveIcon(p,it));
			else
				moveIcon(o,it);
		}
		// move icon box?
		if((isIconBoxActive) && selectedIcon!=null) {
			// get icon
			IconElement icon = nameIcons.get(selectedIcon);
			// update icon box position
			moveIconBox(icon.getGeometry().getEnvelope());			
		}
		else {
			moveIconBox(MapUtil.createEnvelope());
		}
	}
	
	private void moveIconBox(IEnvelope e) throws AutomationException, UnknownHostException, IOException {
		// get icon box
		FrameElement box = getIconBoxElement();
		// set geometry
		box.setGeometry(e);
		// ensure box is on top
		IGraphicsContainerSelect selector = (IGraphicsContainerSelect)container;
		selector.selectElement(box);
		container.bringToFront(selector.getSelectedElements());
		selector.unselectElement(box);			
		// set dirty
		box.isDirty();
		// forward
		setDirtyRegion(box);
		
	}
	
	private IconElement addIcon(String name, String file) throws UnknownHostException, IOException {
		// does not exist?
		if(!nameIcons.containsKey(name)) {
			// create icon
			IconElement icon = new IconElement();
			// set name
			icon.setName("DRAWFRAME.ICON."+name.toUpperCase());			
			// prepare
			icon.setBorder(MapUtil.getSymbolBorder());
			icon.setBackground(null);
			// import picture
			File iconFile = new File(file);
			if(iconFile.exists()) {
				// load picture
				icon.importPictureFromFile(iconFile.getAbsolutePath());
			}
			// initialize geometry
			icon.setGeometry(MapUtil.getPictureBounds(display(),icon));
			// update collections
			orderIcons.add(icon);
			nameIcons.put(name,icon);
			// finished
			return icon;
		}
		// failed
		return null;
	}
	
	private double moveCaption(IPoint p) throws UnknownHostException, IOException {
		// get display
		IDisplay display = display();
		// get size
		IEnvelope b = display!=null ? MapUtil.getElementBounds(display, getTextElement()) : null;
		// get symbol background
		SymbolBackground bg = (SymbolBackground)getTextBoxElement().getBackground();
		// get vertical spacing
		double d = display.getDisplayTransformation().fromPoints(bg.getVerticalSpacing());
		// get y-offset
		double y = (b!=null && !b.isEmpty()) ? p.getY()+b.getHeight()/4 + d*0: p.getY();
		// get horizontal spacing
		d = display.getDisplayTransformation().fromPoints(bg.getHorizontalSpacing());
		// get x-offset
		double x = (b!=null && !b.isEmpty()) ? p.getX()+b.getWidth()/2 + d: p.getX();
		// offset this point
		p.setX(x);
		p.setY(y);
		// move text element
		getTextElement().setGeometry(p);
		// get text boundery envelope again
		b = display()!=null ? MapUtil.getElementBounds(display, getTextElement()) : null;
		// move text box element
		getTextBoxElement().setGeometry(b);
		// finished
		return y;
	}
	
	private double moveIcon(IPoint p, IconElement icon) throws UnknownHostException, IOException {
		// create new from old
		p = MapUtil.createPoint(p);		
		// get size
		IEnvelope b = display()!=null ? MapUtil.getPictureBounds(display(), icon) : null;
		// get offsets
		double y = (b!=null && !b.isEmpty()) ? p.getY()-b.getHeight() : p.getY();
		double x = (b!=null && !b.isEmpty()) ? p.getX()+b.getWidth()/2 : p.getX();
		// offset this point
		p.setX(x);
		p.setY(y);
		// get icon envelope
		IEnvelope e = (b!=null) ? b : MapUtil.createEnvelope(p);
		// center at point
		e.centerAt(p);
		// move icon 
		icon.setGeometry(e);	
		//System.out.println("y:="+y);
		// finished
		return y;		
	}
	
	public boolean isActive() {
		return isActive;
	}
	
	public boolean activate() throws AutomationException, IOException{
		// any change?
		if(!isActive) {
			// remove element from current container?
			if(container!=null) {
				// set flag
				isActive = true;
				// reselect icon?
				if(selectedIcon!=null) setIconBorder(selectedIcon, true);
				// show layer
				((IGraphicsLayer)container).activate(display());				
				((ILayer)container).setVisible(true);
				// set dirty
				setDirtyRegion(getGroupElement());				
				// success
				return true;
			}
		}
		// no change
		return false;
	}
	
	public boolean deactivate() throws AutomationException, IOException{
		// any change?
		if(isActive) {
			if(container!=null) {
				// set dirty
				setDirtyRegion(getGroupElement());
				// reselect icon?
				if(selectedIcon!=null) setIconBorder(selectedIcon, false);
				// reset flag
				isActive = false;			
				// hide layer
				((IGraphicsLayer)container).deactivate();				
				((ILayer)container).setVisible(false);				
				// success
				return true;
			}
		}
		// no change
		return false;
	}
	
	public String hitIcon(double x, double y, double tolerance) throws AutomationException, IOException {
		Object[] names = nameIcons.keySet().toArray();
		for(int i=0; i< names.length; i++) {
			String name = names[i].toString();
			if(hitIcon(name, x, y, tolerance))
				return name;
		}
		return null;
	}
	
	public boolean hitIcon(String name, double x, double y, double tolerance) throws AutomationException, IOException {
		IconElement icon = nameIcons.get(name);
		if(icon!=null)  
			return icon.hitTest(x, y, tolerance);
		else
			return false;
	}
	
	public IScreenDisplay display() throws AutomationException, IOException {
		return activeView.getScreenDisplay();
	}
	
	public boolean setIconVisible(String name, boolean isVisible) throws AutomationException, IOException {
		// initialize dirty falg
		boolean isDirty = false;
		// get icon?
		if(nameIcons.containsKey(name)) {
			IconElement icon = nameIcons.get(name);
			// make visible?
			if(!icon.isVisible() && isVisible) {
				// is dirty
				isDirty = true;
				// set flag
				icon.setVisible(true);
				// forward?
				if(display()!=null) {
					update(getFrame());		
				}
				// forward
				setDirtyRegion(icon);				
			}
			// make invisible?
			if(icon.isVisible() && !isVisible) {
				// is dirty
				isDirty = true;
				// set flag
				icon.setVisible(false);
				// forward?
				if(display()!=null) {
					update(getFrame());		
				}				
				// forward
				setDirtyRegion(icon);				
			}
		}
		// state
		return isDirty;
	}
	
	public String getSelectedIcon() {
		return selectedIcon;
	}

	public boolean clearSelectedIcon() throws AutomationException, IOException {
		// deselect old?
		if(selectedIcon!=null) {
			// hide icon box border
			setIconBorder(selectedIcon,false);
			// reset selected name
			selectedIcon =null;
			// success
			return true;
		}		
		// failure
		return false;
	}
	
	public boolean setSelectedIcon(String name, boolean isSelected) throws AutomationException, IOException {
		//34386265
		if(nameIcons.containsKey(name)) {
			// deselect old?
			if(selectedIcon!=null && !selectedIcon.equalsIgnoreCase(name)) {
				// reset selection
				setIconBorder(selectedIcon,false);				
			}
			// update selection
			setIconBorder(name,isSelected);				
			// update selected icon name
			selectedIcon = (isSelected ? name : null);
			// changed
			return true;
		}
		// no change
		return false;
	}
	
	private void setIconBorder(String name, boolean isSelected) throws AutomationException, UnknownHostException, IOException {
		// get icon box
		FrameElement iconBox = getIconBoxElement();
		// move to top?
		if(isSelected && !isIconBoxActive) {
			// get the icon
			IconElement icon = nameIcons.get(name);
			// set active flag
			isIconBoxActive = true;
			// update icon box position
			moveIconBox(icon.getGeometry().getEnvelope());			
		}
		else if(!isSelected && isIconBoxActive) {
			// forward
			setDirtyRegion(iconBox);
			// reset flag
			isIconBoxActive = false;
		}
	}

	private void setDirtyRegion(IElement element) throws AutomationException, IOException {
		// get geometry
		IGeometry g = element.getGeometry();
		// add to dirty area?
		if(dirtyArea!=null)
			dirtyArea.union(MapUtil.expand(1.25,g.getEnvelope()));
		else
			dirtyArea = MapUtil.expand(1.25,g.getEnvelope());
	}
}