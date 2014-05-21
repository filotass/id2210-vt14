package tman.system.peer.tman;

import java.io.ObjectInputStream.GetField;
import java.io.Serializable;
import java.util.ArrayList;

import cyclon.system.peer.cyclon.PeerDescriptor;
import se.sics.kompics.address.Address;


public class TManAddressBuffer implements Serializable {

	private static final long serialVersionUID = 7555581949994578697L;
	private final Address from;
	private final ArrayList<PeerDescriptor> addresses;
	private int typeOfGradient;
	
	public static int MEMORY = 0;
	public static int CPU = 1;
	public static int AVR = 2;


	public TManAddressBuffer(Address from,ArrayList<PeerDescriptor> addresses, int typeOfGradient) {
		super();
		this.from = from;
		this.addresses = addresses;
		this.typeOfGradient = typeOfGradient;
	}


	public Address getFrom() {
		return from;
	}


	public int getSize() {
		return addresses.size();
	}


	public ArrayList<PeerDescriptor> getAddresses() {
		return addresses;
	}
	
	public int getGradientType(){
		return typeOfGradient;
	}
}
