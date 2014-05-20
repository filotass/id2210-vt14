package common.peer;

import se.sics.kompics.address.Address;

/**
 * 
 * @author filotas
 *
 */
public class TManPeer {

	
	private Address address;
	private AvailableResources availableResources;
	
	public TManPeer(Address address, AvailableResources availableResources){
		this.address = address;
		this.availableResources = availableResources;
	}

	public Address getAddress() {
		return address;
	}

	public AvailableResources getAvailableResources() {
		return availableResources;
	}
	
	
	
}
