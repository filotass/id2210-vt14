package tman.system.peer.tman;

import java.util.Comparator;

import cyclon.system.peer.cyclon.PeerDescriptor;

public class ComparatorByCPU implements Comparator<PeerDescriptor>{

	private PeerDescriptor self;
	
	public ComparatorByCPU(PeerDescriptor self){
		this.self = self;
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
        return 1;
	}



}
