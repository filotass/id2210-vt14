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
		int selfCPU = self.getAvailableResources().getNumFreeCpus();
		
		
        if (aCPU < selfCPU && bCPU > selfCPU) {
            return 1;
        } else if (bCPU < selfCPU && aCPU > selfCPU) {
            return -1;
        } else if (Math.abs(aCPU - selfCPU) < Math.abs(bCPU - selfCPU)) {
            return -1;
        }

		return a.getQueueSize() - b.getQueueSize();
	}



}
