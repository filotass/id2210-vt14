package tman.system.peer.tman;

import java.util.Comparator;

import cyclon.system.peer.cyclon.PeerDescriptor;

public class ComparatorByMem implements Comparator<PeerDescriptor>{

	@Override
	public int compare(PeerDescriptor a, PeerDescriptor b) {
		int aMem = a.getAvailableResources().getFreeMemInMbs();
		int bMem = b.getAvailableResources().getFreeMemInMbs();
		
		if(bMem - aMem != 0){
			return bMem - aMem;
		}

		return a.getQueueSize() - b.getQueueSize();
	}
}
