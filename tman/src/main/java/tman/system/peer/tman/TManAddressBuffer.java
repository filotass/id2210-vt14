package tman.system.peer.tman;

import java.io.Serializable;
import java.util.ArrayList;

import se.sics.kompics.address.Address;


public class TManAddressBuffer implements Serializable {

	private static final long serialVersionUID = 7555581949994578697L;
	private final Address from;
	private final ArrayList<Address> addresses;


	public TManAddressBuffer(Address from,ArrayList<Address> addresses) {
		super();
		this.from = from;
		this.addresses = addresses;
	}


	public Address getFrom() {
		return from;
	}


	public int getSize() {
		return addresses.size();
	}


	public ArrayList<Address> getAddresses() {
		return addresses;
	}
}
