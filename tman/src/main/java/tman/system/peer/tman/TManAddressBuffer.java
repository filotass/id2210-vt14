package tman.system.peer.tman;


import java.io.Serializable;

import java.util.List;

import cyclon.system.peer.cyclon.PeerDescriptor;
import se.sics.kompics.address.Address;
import tman.system.peer.tman.gradient.Gradient;


public class TManAddressBuffer implements Serializable {

	private static final long serialVersionUID = 7555581949994578697L;
	private final Address from;
	private final Gradient gradient;
	



	public TManAddressBuffer(Address from,Gradient gradient) {
		super();
		this.from = from;
		this.gradient = gradient;
	}


	public Address getFrom() {
		return from;
	}


	public int getSize() {
		return gradient.getEntried().size();
	}
	
	public Gradient getGradient(){
		return gradient;
	}


	public List<PeerDescriptor> getAddresses() {
		return gradient.getEntried();
	}
	

}
