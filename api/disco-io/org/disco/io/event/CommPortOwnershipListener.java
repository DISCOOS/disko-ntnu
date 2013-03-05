package org.disco.io.event;

public interface CommPortOwnershipListener extends gnu.io.CommPortOwnershipListener {
	
	static final int PORT_OWNED = 1; 
	static final int PORT_OWNERSHIP_REQUESTED = 2; 
    static final int PORT_UNOWNED = 3;
    
    @Override
    public void ownershipChange(int type); 

}
