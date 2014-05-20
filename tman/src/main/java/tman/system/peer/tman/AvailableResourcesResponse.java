package tman.system.peer.tman;

import java.io.Serializable;

import common.peer.AvailableResources;
import se.sics.kompics.address.Address;
import se.sics.kompics.network.Message;

public class AvailableResourcesResponse extends Message implements Serializable {

	private static final long serialVersionUID = 6980229644368313388L;
	private AvailableResources avr;
	
	protected AvailableResourcesResponse(Address source, Address destination,AvailableResources avr) {
		
		super(source, destination);
		
		this.avr = avr;
	}
	
	public AvailableResources getAvailableResources() {
		
		return avr;
	}
}