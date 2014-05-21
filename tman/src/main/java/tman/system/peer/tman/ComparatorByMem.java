package tman.system.peer.tman;

import java.util.Comparator;

import cyclon.system.peer.cyclon.PeerDescriptor;

public class ComparatorByMem implements Comparator<PeerDescriptor>{

	private PeerDescriptor self;
	
	public ComparatorByMem(PeerDescriptor self){
		this.self = self;
	}
	
	@Override
	public int compare(PeerDescriptor a, PeerDescriptor b) {
		int aMem = a.getAvailableResources().getFreeMemInMbs();
		int bMem = b.getAvailableResources().getFreeMemInMbs();
		int selfMem = self.getAvailableResources().getFreeMemInMbs();
		
        if (aMem < selfMem && bMem > selfMem) {
            return 1;
        } else if (bMem < selfMem && aMem > selfMem) {
            return -1;
        } else if (Math.abs(aMem - selfMem) < Math.abs(bMem - selfMem)) {
            return -1;
        }
        return 1;
	}
}
