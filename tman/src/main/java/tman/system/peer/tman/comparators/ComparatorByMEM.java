package tman.system.peer.tman.comparators;
import common.simulation.scenarios.Experiment;

import cyclon.system.peer.cyclon.PeerDescriptor;

public class ComparatorByMEM extends PeerComparator{
	
    public ComparatorByMEM(PeerDescriptor self) {
    	super(self);
    }
    
	@Override
	public int compare(PeerDescriptor a, PeerDescriptor b) {
		
		double aMem = a.getAvailableResources().getFreeMemInMbs();
		double bMem = b.getAvailableResources().getFreeMemInMbs();


		
		if(a.getQueueSize() - b.getQueueSize()!=0){
			return a.getQueueSize() - b.getQueueSize();
		}
		
		if(bMem > aMem){
			return 1;
		}else if(bMem < aMem){
			return -1;
		}	    
		return 0;
	}
}
