package tman.system.peer.tman.comparators;

import java.util.Comparator;

import cyclon.system.peer.cyclon.PeerDescriptor;

public abstract class PeerComparator implements Comparator<PeerDescriptor>{

	protected PeerDescriptor self;
	
	public PeerComparator(PeerDescriptor self){
		this.self = self;
	}
}
