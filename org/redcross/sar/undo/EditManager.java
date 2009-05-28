package org.redcross.sar.undo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.redcross.sar.mso.IChangeRecordIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.work.event.FlowEvent;
import org.redcross.sar.wp.IDiskoWpModule;

/**
 * This class manages a flow of edits 
 * across all work processes and all IMsoModelIf instances
 * 
 * @author kenneth
 *
 */

public class EditManager {

	private static final long serialVersionUID = 1L;

    private final Map<IDiskoWpModule,List<FlowEvent>> 
    	m_stacks = new HashMap<IDiskoWpModule,List<FlowEvent>>();
    
    /* ======================================================
     * Public methods 
     * ====================================================== */
    
    public boolean install(IDiskoWpModule wp) {
    	if(!m_stacks.containsKey(wp)) {
    		m_stacks.put(wp, new ArrayList<FlowEvent>());
    		return true;
    	}
    	return false;
    }
    
    public boolean uninstall(IDiskoWpModule wp) {
    	if(m_stacks.containsKey(wp)) {
    		m_stacks.remove(wp);
    		return true;
    	}
    	return false;
    }
    
    public Map<IMsoModelIf,List<IChangeRecordIf>> getUncomittedChanges(IDiskoWpModule wp) {
    	return getUncomittedChanges(m_stacks.get(wp));
    }
    
	public void onFlowPerformed(IDiskoWpModule wp, FlowEvent e) {
		// get stack
		List<FlowEvent> stack = m_stacks.get(wp);
		// has stack?
		if(stack!=null) {
	    	// update change stack?
			if(e.isChange() || e.isFinish()) 
			{
				stack.add(e);
			}
			else if(e.isCommit() || e.isRollback()) 
			{
				stack.clear();
			}			
		}
	}
    
    public boolean isChanged(IDiskoWpModule wp)
    {
    	List<FlowEvent> stack = m_stacks.get(wp);
    	if(stack!=null) {
    		return getChangeCount(stack)>0;
    	}
        return false;
    }
    
    public void clearAll() {
    	m_stacks.clear();
    }
    
    public void clearAll(IMsoModelIf model) {
    	m_stacks.remove(model);
    }
    
    /* ======================================================
     * Private methods 
     * ====================================================== */
    
    private int getChangeCount(List<FlowEvent> stack) {
    	// get uncommitted changes
    	Map<IMsoModelIf,List<IChangeRecordIf>> changes = getUncomittedChanges(stack);
    	// has changes?
    	return changes!=null?changes.size():0;
    }
    
    private Map<IMsoModelIf,List<IChangeRecordIf>> getUncomittedChanges(List<FlowEvent> stack) {
		// has stack?
		if(stack!=null) {
			// initialize map between changes 
	    	Map<IMsoModelIf,List<IChangeRecordIf>> 
	    		changes = new HashMap<IMsoModelIf,List<IChangeRecordIf>>();
	    	// loop over all events in stack
			for(FlowEvent it : stack) 
			{
				// get uncommitted changes from flow event
				Map<IMsoModelIf,List<IChangeRecordIf>> map = it.getUncommittedChanges();
				// append uncommitted changes gathered from flow event to map 
				for(IMsoModelIf model : map.keySet()) {
					List<IChangeRecordIf> list = changes.get(model);
					if(list==null) {
						list = new ArrayList<IChangeRecordIf>();
						changes.put(model,list);
					}
					list.addAll(map.get(model));
				}
			}
	    	return changes;    	
		}
		// failure
		return null;
    }
            
}
