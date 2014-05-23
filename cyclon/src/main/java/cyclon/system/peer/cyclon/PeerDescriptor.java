package cyclon.system.peer.cyclon;

import java.io.Serializable;

import common.peer.AvailableResources;

import se.sics.kompics.address.Address;


public class PeerDescriptor implements Comparable<PeerDescriptor>, Serializable {
	private static final long serialVersionUID = 1906679375438244117L;
	private final Address peerAddress;
	private int age;
	private AvailableResources avr;
	private int queueSize;

	public PeerDescriptor(Address peerAddress,AvailableResources avr, int queueSize) {
		this.peerAddress = peerAddress;
		this.age = 0;
		this.avr = avr;
		this.queueSize =queueSize;
	}

	public int incrementAndGetAge() {
		age++;
		return age;
	}

	public int getAge() {
		return age;
	}


	public Address getAddress() {
		return peerAddress;
	}
	
	public AvailableResources getAvailableResources() {
		return avr;
	}

	public int getQueueSize() {
		return queueSize;
	}

	@Override
	public int compareTo(PeerDescriptor that) {
		if (this.age > that.age)
			return 1;
		if (this.age < that.age)
			return -1;
		return 0;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((peerAddress == null) ? 0 : peerAddress.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PeerDescriptor other = (PeerDescriptor) obj;
		if (peerAddress == null) {
			if (other.peerAddress != null)
				return false;
		} else if (!peerAddress.equals(other.peerAddress))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return peerAddress + "";
	}
	
}
