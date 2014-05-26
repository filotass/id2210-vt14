package tman.system.peer.tman.comparators;
import cyclon.system.peer.cyclon.PeerDescriptor;

public class ComparatorByMEM extends PeerComparator{
	
    public ComparatorByMEM(PeerDescriptor self) {
    	super(self);
    }
    
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
