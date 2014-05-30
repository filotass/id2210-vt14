package tman.system.peer.tman.comparators;

import cyclon.system.peer.cyclon.PeerDescriptor;

public class ComparatorByCPU extends PeerComparator{

	
    public ComparatorByCPU(PeerDescriptor self) {
        super(self);
    }
	
	@Override
	public int compare(PeerDescriptor a, PeerDescriptor b) {
		
		double aCPU = a.getAvailableResources().getNumFreeCpus();
		double bCPU = b.getAvailableResources().getNumFreeCpus();


		
		if(a.getQueueSize() - b.getQueueSize()!=0){
			return a.getQueueSize() - b.getQueueSize();
		}
		
		if(bCPU > aCPU){
			return 1;
		}else if(bCPU < aCPU){
			return -1;
		}	    
		return 0;
	}



}
