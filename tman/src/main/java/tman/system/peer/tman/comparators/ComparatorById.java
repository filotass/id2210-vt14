/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tman.system.peer.tman.comparators;


import cyclon.system.peer.cyclon.PeerDescriptor;


/**
 * Make Node with Highest Id Leader in the Gradient
 */
public class ComparatorById extends PeerComparator {

    public ComparatorById(PeerDescriptor self) {
        super(self);
    }

    @Override
    public int compare(PeerDescriptor a, PeerDescriptor b) {

    	int aID = a.getAddress().getId();
    	int bID = b.getAddress().getId();
    	int sID = self.getAddress().getId();
    	
        if (aID< sID && bID > sID) {
            return 1;
        } else if (bID < sID && aID > sID) {
            return -1;
        } else if (Math.abs(aID - sID) < Math.abs(bID - sID)) {
            return -1;
        }
        return 1;
    }
    
}
