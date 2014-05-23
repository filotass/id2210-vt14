package tman.system.peer.tman;

import java.util.Comparator;

import cyclon.system.peer.cyclon.PeerDescriptor;

public class ComparatorByCPU implements Comparator<PeerDescriptor>{

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
