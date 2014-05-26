package tman.system.peer.tman.comparators;

import cyclon.system.peer.cyclon.PeerDescriptor;

public class ComparatorByCPU extends PeerComparator{

	
    public ComparatorByCPU(PeerDescriptor self) {
        super(self);
    }
	
	@Override
	public int compare(PeerDescriptor a, PeerDescriptor b) {
		int aCPU = a.getAvailableResources().getNumFreeCpus();
		int bCPU = b.getAvailableResources().getNumFreeCpus();

		if(bCPU - aCPU!=0){
			return bCPU - aCPU;
		}
		
		return a.getQueueSize() - b.getQueueSize();
	}



}
