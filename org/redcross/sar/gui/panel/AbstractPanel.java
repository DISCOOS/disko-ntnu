package org.redcross.sar.gui.panel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.apache.log4j.Logger;
import org.redcross.sar.gui.IMsoHolder;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.event.MsoLayerEvent;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.layer.IMsoFeatureLayer.LayerCode;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.work.event.IFlowListener;
import org.redcross.sar.work.event.FlowEvent;

public abstract class AbstractPanel extends JPanel implements IPanel, IPanelManager, IMsoHolder {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AbstractPanel.class);

	private int m_isMarked = 0;

	private int m_consumeCount = 0;
	private int m_loopCount = 0;

	private boolean m_isDirty = false;
	private boolean m_requestHideOnFinish = true;
	private boolean m_requestHideOnCancel = true;

	protected IMsoModelIf m_msoModel;
	protected IMsoObjectIf m_msoObject;

	protected EnumSet<?> m_msoLayers;
	protected EnumSet<MsoClassCode> m_msoInterests;

	protected PanelManager m_manager = new PanelManager(null,this);

	/* ===========================================
	 * Constructors
	 * ===========================================
	 */

	public AbstractPanel() {
		this("");
	}

	public AbstractPanel(String caption) {
		// prepare
        m_msoLayers =  EnumSet.noneOf(LayerCode.class);
		m_msoInterests = EnumSet.noneOf(MsoClassCode.class);
	}

	/* ===========================================
	 * Public methods
	 * =========================================== */

	public boolean isLoop() {
		return (m_loopCount>0);
	}

	public void setLoop(boolean isLoop) {
		if(isLoop)
			m_loopCount++;
		else if(m_loopCount>0)
			m_loopCount--;
	}

	public EnumSet<?> getMsoLayers() {
		return m_msoLayers;
	}

	public void setMsoLayers(IDiskoMap map, EnumSet<LayerCode> layers) {
		// unregister?
		if(this.m_msoLayers!=null) {
			// loop over all layers
			for(LayerCode it: layers) {
				IMsoFeatureLayer l = map.getMsoLayer(it);
				if(l!=null) l.removeMsoLayerEventListener(this);
			}
		}
		this.m_msoLayers = layers!=null ? layers : EnumSet.noneOf(LayerCode.class);
		// register?
		if(layers!=null) {
			// loop over all layers
			for(LayerCode it: layers) {
				IMsoFeatureLayer l = map.getMsoLayer(it);
				if(l!=null) l.addMsoLayerEventListener(this);
			}
		}
	}

	public void connect(IMsoModelIf model, EnumSet<MsoClassCode> interests) {
		// unregister
		disconnect();
		// initialize
		m_msoModel = model;
		m_msoInterests = EnumSet.noneOf(MsoClassCode.class);
		// add listener?
		if(model!=null) {
			m_msoInterests = interests;
			m_msoModel.getEventManager().addClientUpdateListener(this);
		}
	}
	
	public void disconnect() {
		// unregister?
		if(m_msoModel!=null) {
			m_msoModel.getEventManager().removeClientUpdateListener(this);
			m_msoModel = null;
		}
	}

	public boolean setSelectedMsoFeature(IDiskoMap map) {

		// initialize
		IMsoObjectIf msoObj = null;

		// catch exceptions
		try {
	        // get selected object
	        List<IMsoFeature> features = map.getMsoSelection();
	        if(features!=null && features.size()>0) {
        		// cast first item to IMsoFeature and get mso object
        		msoObj = features.get(0).getMsoObject();
	        }
		}
		catch(Exception e) {
			logger.error("Failed to select MSO feature",e);
		}

		// forward
		setMsoObject(msoObj);

		// finished
		return (m_msoObject!=null);

	}

	/* ===========================================
	 * Abstract methods
	 * =========================================== */

	public abstract void update();

	public abstract boolean doAction(String command);

	public abstract void addActionListener(ActionListener listener);
	public abstract void removeActionListener(ActionListener listener);

	public abstract void addFlowListener(IFlowListener listener);
	public abstract void removeFlowListener(IFlowListener listener);

    public abstract Container getContainer();
    public abstract void setContainer(Container container);

	/* ===========================================
	 * IPanel implementation
	 * =========================================== */

	public IMsoObjectIf getMsoObject() {
		return m_msoObject;
	}

	public void setMsoObject(IMsoObjectIf msoObj) {
		// prepare
		m_msoObject = msoObj;
		// forward
		update();
	}

	public void reset() {
		// consume change events
		setChangeable(false);
		// reapply mso object
		setMsoObject(getMsoObject());
		// reset flag?
		if(m_isDirty) setDirty(false);
		// resume change events
		setChangeable(true);
	}

	public boolean finish() {
		// get dirty flag
		boolean bFlag = isDirty();
		// consume?
		if(!isChangeable()) return false;
		// consume change events
		setChangeable(false);
		// suspend for faster update?
		if(m_msoModel!=null) m_msoModel.suspendClientUpdate();
		// request action
		bFlag = beforeFinish();
		// resume updates?
		if(m_msoModel!=null) m_msoModel.resumeClientUpdate(true);
		// finish?
		if(bFlag) {
			// request action
			afterFinish();
			// reset dirty flag
			setDirty(false);
		}
		// resume change events
		setChangeable(true);
		// finished
		return bFlag;
	}

	public boolean cancel() {
		// consume?
		if(!isChangeable()) return false;
		// consume change events
		setChangeable(false);
		// request action
		boolean bFlag = beforeCancel();
		// cancel?
		 if(bFlag) {
			// forward
			reset();
			// hide manager
			afterCancel();
			// reset dirty flag
			setDirty(false);
		 }
		// resume change events
		setChangeable(true);
		// finished
		return bFlag;
	}

	public boolean isDirty() {
		return m_isDirty;
	}

	public void setDirty(boolean isDirty) {
		setDirty(isDirty,true);
	}

	public int isMarked() {
		return m_isMarked;
	}

	public void setMarked(int isMarked) {
		if(m_isMarked != isMarked) {
			m_isMarked = isMarked;
		}
	}

	public boolean isRequestHideOnFinish() {
		return m_requestHideOnFinish;
	}

	public void setRequestHideOnFinish(boolean isEnabled) {
		m_requestHideOnFinish = isEnabled;
	}

	public boolean isRequestHideOnCancel() {
		return m_requestHideOnCancel;
	}

	public void setRequestHideOnCancel(boolean isEnabled) {
		m_requestHideOnCancel = isEnabled;
	}

	public boolean isChangeable() {
		return (m_consumeCount==0);
	}

	public void setChangeable(boolean isChangeable) {
		if(!isChangeable)
			m_consumeCount++;
		else if(m_consumeCount>0)
			m_consumeCount--;
	}

	public int clearChangeableCount() {
		int count = m_consumeCount;
		m_consumeCount = 0;
		return count;
	}
	
    public Dimension getPreferredContainerSize() {
        return (getContainer()!=null ? getContainer().getPreferredSize() : new Dimension(0,0));
    }

    public void setPreferredContainerSize(Dimension size) {
        if(getContainer()!=null)
            getContainer().setPreferredSize(size);
    }

    public Dimension getMinimumContainerSize() {
        return (getContainer()!=null ? getContainer().getMinimumSize() : new Dimension(0,0));
    }

    public void setMinimumContainerSize(Dimension size) {
        if(getContainer()!=null)
            getContainer().setMinimumSize(size);
    }

    public Dimension getMaximumContainerSize() {
        return (getContainer()!=null ? getContainer().getMaximumSize() : new Dimension(0,0));
    }

    public void setMaximumContainerSize(Dimension size) {
        if(getContainer()!=null)
            getContainer().setMaximumSize(size);
    }

    public LayoutManager getContainerLayout() {
        return (getContainer()!=null ? getContainer().getLayout() : null);
    }

    public void setContainerLayout(LayoutManager manager) {
        if(getContainer()!=null) getContainer().setLayout(manager);
    }

    public void setContainerBorder(Border border) {
        if(getContainer() instanceof JComponent)
            ((JComponent)getContainer()).setBorder(border);
    }

    public Component addToContainer(Component c) {
        if(getContainer()!=null) {
        	// register this panel manager?
    		if(!(getContainer() instanceof IPanelManager))
    			install(c,true);
    		// add to container
        	return getContainer().add(c);
        }
        return null;
    }

    public Component addToContainer(Component c, int index) {
    	if(getContainer()!=null) {
        	// register this panel manager?
    		if(!(getContainer() instanceof IPanelManager))
    			install(c,true);
    		// add to container
        	return getContainer().add(c,index);
        }
    	return null;
    }

	public Component addToContainer(String name, Component c) {
    	if(getContainer()!=null) {
        	// register this panel manager?
    		if(!(getContainer() instanceof IPanelManager))
    			install(c,true);
    		// add to container
    		return getContainer().add(name,c);
    	}
    	return null;
	}

    public void addToContainer(Component c, Object constraints) {
    	if(getContainer()!=null) {
        	// register this panel manager?
    		if(!(getContainer() instanceof IPanelManager))
    			install(c,true);
    		// add to container
        	getContainer().add(c,constraints);
        }
    }

    public void addToContainer(Component c, Object constraints, int index) {
    	if(getContainer()!=null) {
        	// register this panel manager?
    		if(!(getContainer() instanceof IPanelManager))
    			install(c,true);
    		// add to container
    		getContainer().add(c,constraints,index);
    	}
    }

	public void removeFromContainer(int index) {
    	if(getContainer()!=null) {
    		// get component
    		Component c = getComponent(index);
        	// register this panel manager?
    		if(!(getContainer() instanceof IPanelManager))
    			install(c,false);
    		// remove from container
    		getContainer().remove(index);
    	}
	}

	public void removeFromContainer(Component c) {
    	if(getContainer()!=null) {
        	// register this panel manager?
    		if(!(getContainer() instanceof IPanelManager))
    			install(c,false);
    		// remove from container
    		getContainer().remove(c);
    	}
	}

	public void removeAllFromToContainer() {
    	if(getContainer()!=null) {
    		for(Component it : getContainer().getComponents())
    			removeFromContainer(it);
    	}
	}

	public boolean isContainerEnabled() {
        return getContainer().isEnabled();
    }

    public void setContainerEnabled(Boolean isEnabled) {
        getContainer().setEnabled(isEnabled);
    }

	public Dimension fitContainerToMinimumLayoutSize() {
    	Dimension d = null;
		Container c = getContainer();
    	if(c!=null && c.getLayout()!=null) {
    		d = c.getLayout().minimumLayoutSize(c);
    		if(d!=null) c.setSize(new Dimension(d.width,d.height));
    	}
    	return d;
    }

	public Dimension fitContainerToPreferredLayoutSize() {
    	Dimension d = null;
		Container c = getContainer();
		if(c!=null && c.getLayout()!=null) {
    		d = c.getLayout().preferredLayoutSize(c);
    		if(d!=null) c.setSize(new Dimension(d.width,d.height));
    	}
    	return d;
    }

	public Dimension fitContainerToMaximumLayoutSize() {
    	Dimension d = null;
		Container c = getContainer();
    	if(c!=null && c.getLayout() instanceof LayoutManager2) {
        	LayoutManager2 lm2 = (LayoutManager2)c.getLayout();
    		d = lm2.maximumLayoutSize(c);
    		if(d!=null) c.setSize(new Dimension(d.width,d.height));
    	}
    	return d;
    }

    public Dimension fitThisToMinimumContainerSize() {
    	Dimension d = null;
        if(getLayout()!=null) {
        	d = getLayout().minimumLayoutSize(this);
        	if(d!=null) setSize(new Dimension(d.width,d.height));
        }
        return d;
    }

    public Dimension fitThisToPreferredContainerSize() {
    	Dimension d = null;
        if(getLayout()!=null) {
        	//validateTree();
        	d = getLayout().preferredLayoutSize(this);
        	if(d!=null) setSize(new Dimension(d.width,d.height));
        }
        return d;
    }

    public Dimension fitThisToMaximumContainerSize() {
    	Dimension d = null;
        if(getLayout() instanceof LayoutManager2) {
        	LayoutManager2 lm2 = (LayoutManager2)getLayout();
    		d = lm2.maximumLayoutSize(this);
    		if(d!=null) setSize(new Dimension(d.width,d.height));
        }
        return d;
    }

	public IPanelManager getManager() {
		return m_manager;
	}

	public void setParentManager(IPanelManager parent, boolean requestMoveTo, boolean setAll) {
		// set parent manager
		this.m_manager.setParentManager(parent);
    	// forward to all descendants of container?
		if(setAll) setParentManager(getContainer(),parent,requestMoveTo);
	}

    /* ==========================================================
     *  IPanelManager interface implementation
     * ========================================================== */

    public boolean isRootManager() {
    	return m_manager.isRootManager();
    }

	public IPanelManager getParentManager() {
		return m_manager.getParentManager();
	}

	public IPanelManager setParentManager(IPanelManager parent) {
		// set parent manager
		IPanelManager old = this.m_manager.setParentManager(parent);
    	// forward to all descendants of container
        setParentManager(getContainer(),parent,false);
        // finished
        return old;
	}

	public boolean requestMoveTo(int x, int y, boolean isRelative) {
		return getManager()!=null ? getManager().requestMoveTo(x,y,isRelative) : false;
    }

    public boolean requestResize(int w, int h, boolean isRelative) {
		return getManager()!=null ? getManager().requestResize(w,h,isRelative) : false;
    }

	public boolean requestFitToMinimumContentSize(boolean pack) {
		return (getManager()!=null ? getManager().requestFitToMinimumContentSize(false) : false);
	}

	public boolean requestFitToPreferredContentSize(boolean pack) {
		return (getManager()!=null ? getManager().requestFitToPreferredContentSize(false) : false);
	}

	public boolean requestFitToMaximumContentSize(boolean pack) {
		return (getManager()!=null ? getManager().requestFitToMaximumContentSize(false) : false);
	}

	public boolean requestShow() {
		return (getManager()!=null ? getManager().requestShow() : false);
    }

    public boolean requestHide() {
		return (getManager()!=null ? getManager().requestShow() : false);
    }

	/* ===========================================
	 * IMsoUpdateListenerIf implementation
	 * =========================================== */

	public EnumSet<MsoClassCode> getInterests() {
		return m_msoInterests;
	}

	public void handleMsoUpdateEvent(MsoEvent.UpdateList events) {

		// consume?
		if(!isChangeable()) return;

		// loop over all events
		for(MsoEvent.Update e : events.getEvents(m_msoInterests)) {

			// get mask
			int mask = e.getEventTypeMask();

	        // get mso object
	        IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();

			// is loopback update?
			if(e.isLoopbackMode()) {
				
				// forward
				msoObjectLoopback(msoObj,mask);
				
			} else {
				
		        // get flag
		        boolean clearAll = (mask & MsoEvent.MsoEventType.CLEAR_ALL_EVENT.maskValue()) != 0;

		        // clear all?
		        if(clearAll) {
		        	msoObjectClearAll(this.m_msoObject,mask);
		        }
		        else {
		        	// get flags
			        boolean createdObject  = (mask & MsoEvent.MsoEventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
			        boolean deletedObject  = (mask & MsoEvent.MsoEventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
			        boolean modifiedObject = (mask & MsoEvent.MsoEventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
			        boolean addedReference = (mask & MsoEvent.MsoEventType.ADDED_REFERENCE_EVENT.maskValue()) != 0;
			        boolean removedReference = (mask & MsoEvent.MsoEventType.REMOVED_REFERENCE_EVENT.maskValue()) != 0;

			        // add object?
					if (createdObject) {
						msoObjectCreated(msoObj,mask);
					}
					// is object modified?
					if ( (addedReference || removedReference || modifiedObject)) {
						msoObjectChanged(msoObj,mask);
					}
					// delete object?
					if (deletedObject) {
						msoObjectDeleted(msoObj,mask);
					}
		        }

	        }
		}
	}

	/* ===========================================
	 * IMsoLayerEventListener implementation
	 * =========================================== */

	public void onSelectionChanged(MsoLayerEvent e) {
		if (!e.isFinal()) return;
		try {
			// initialize
			IMsoObjectIf msoObj = null;
			List<IMsoObjectIf> selection = e.getSelectedMsoObjects();
			// select new?
			if (selection != null && selection.size() > 0) {
				// get MSO object
				msoObj = selection.get(0);
			}
			// forward
			setMsoObject(msoObj);
		} catch (Exception ex) {
			logger.error("Failed to get selected MSO objects",ex);
		}
	}

	/* ===========================================
	 * ActionListener implementation
	 * =========================================== */

	public abstract void actionPerformed(ActionEvent e);

	/* ===========================================
	 * IWorkListener implementation
	 * =========================================== */

	public void onFlowPerformed(FlowEvent e) {
		fireOnWorkPerformed(e);
	}

	/* ===========================================
	 * Overridden methods
	 * =========================================== */

	@Override
	public Component add(Component comp, int index) {
		install(comp,true);
		return super.add(comp, index);
	}

	@Override
	public void add(Component comp, Object constraints, int index) {
		install(comp,true);
		super.add(comp, constraints, index);
	}

	@Override
	public void add(Component comp, Object constraints) {
		install(comp,true);
		super.add(comp, constraints);
	}

	@Override
	public Component add(Component comp) {
		install(comp,true);
		return super.add(comp);
	}

	@Override
	public Component add(String name, Component comp) {
		install(comp,true);
		return super.add(name, comp);
	}

	@Override
	public void remove(Component comp) {
		install(comp,false);
		super.remove(comp);
	}

	@Override
	public void remove(int index) {
		install(getComponent(index),false);
		super.remove(index);
	}

	@Override
	public void removeAll() {
		// loop over all components
		for(Component it : getComponents()) {
			install(it,false);
		}
		// TODO Auto-generated method stub
		super.removeAll();
	}

	/* ===========================================
	 * Protected methods
	 * =========================================== */

	protected abstract void fireActionEvent(ActionEvent e, boolean validate);

	protected abstract void fireOnWorkFinish(Object source, Object data);

	protected abstract void fireOnWorkCancel(Object source, Object data);

	protected abstract void fireOnWorkChange(Object source, Object data);

	protected abstract void fireOnWorkPerformed(FlowEvent e);

	protected void setDirty(boolean isDirty, boolean update) {
		this.m_isDirty = isDirty;
		if(update) {
			setChangeable(false);
			update();
			setChangeable(true);
		}
	}

	protected boolean beforeFinish() {
		return true;
	}

	protected boolean beforeCancel() {
		return true;
	}

	protected void afterFinish() {
		fireOnWorkFinish(this,m_msoObject);
		if(getManager()!=null && m_requestHideOnFinish)
			getManager().requestHide();
	}

	protected void afterCancel() {
		fireOnWorkCancel(this,m_msoObject);
		if(getManager()!=null && m_requestHideOnCancel)
			getManager().requestHide();
	}

	protected void msoObjectCreated(IMsoObjectIf msoObj, int mask) { /*NOP*/ }

	protected void msoObjectChanged(IMsoObjectIf msoObj, int mask) {
		// is same as selected?
		if(msoObj == this.m_msoObject) {
			setMsoObject(m_msoObject);
		}
	}

	protected void msoObjectDeleted(IMsoObjectIf msoObj, int mask) {
		// is same as selected?
		if(msoObj == this.m_msoObject) {
			// forward
			setMsoObject(null);
		}
	}

	protected void msoObjectClearAll(IMsoObjectIf msoObj, int mask) {
		// is same as selected?
		if(msoObj == this.m_msoObject) {
			// forward
			setMsoObject(null);
		}
	}

	protected void msoObjectLoopback(IMsoObjectIf msoObj, int mask) {
		// is same as selected?
		if(msoObj == this.m_msoObject) {
			// forward
			setMsoObject(m_msoObject);
		}
	}
	
	protected void setParentManager(Container container, IPanelManager parent, boolean requestMoveTo) {
        for(Component it : container.getComponents()) {
            if(it instanceof IPanel) {
                ((IPanel)it).setParentManager(parent, false, false);
            }
            else if(it instanceof Container){
            	setParentManager((Container)it,parent,requestMoveTo);
            }
        }
    }

	protected boolean install(Component c, boolean set) {
		// initialize flag
		boolean bFlag = false;
		// instance of IPanel?
		if(c instanceof IPanel) {
			// cast to IPanel
			IPanel panel = (IPanel)c;
			// set or reset?
			if(set) {
				panel.setParentManager(getManager(), false, false);
				panel.addActionListener(m_listener);
				bFlag = true;
			}
			else {
				// is managed by this manager?
				if(panel.getParentManager()==getManager()) {
					panel.setParentManager(null, false, false);
					bFlag = true;
				}
				panel.removeActionListener(m_listener);
			}
		}
		return bFlag;
	}

	/* ===========================================
	 * Anonymous classes
	 * =========================================== */

	private final ActionListener m_listener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			fireActionEvent(e, false);
		}

	};

	/**
	 * The main method.
	 *
	 * @param args
	 */
	/*
	public static void main(String[] args)
	{

		UIFactory.initLookAndFeel();

		// initialize GUI on new thread
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{

				if(false) {

					final JDialog dialog = new JDialog();

					final JPanel content = new JPanel(new BorderLayout());

					content.setLayout(new BorderLayout());
					content.add(new JPanel(),BorderLayout.NORTH);
					content.add(new JPanel(),BorderLayout.CENTER);


				}
			}
		});
	}
	*/
}
