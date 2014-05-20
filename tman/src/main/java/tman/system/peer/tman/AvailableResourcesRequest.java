package tman.system.peer.tman;

import java.io.Serializable;

import se.sics.kompics.address.Address;
import se.sics.kompics.network.Message;

public class AvailableResourcesRequest extends Message implements Serializable {

	private static final long serialVersionUID = 8311398682226224336L;

	protected AvailableResourcesRequest(Address source, Address destination) {
		
		super(source, destination);
	}
}