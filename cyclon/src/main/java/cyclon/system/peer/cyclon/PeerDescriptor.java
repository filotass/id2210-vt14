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
	private long timestamp;

	public PeerDescriptor(Address peerAddress,AvailableResources avr, int queueSize, long timeStamp) {
		this.peerAddress = peerAddress;
		this.age = 0;
		this.avr = avr;
		this.queueSize =queueSize;
		this.timestamp = timeStamp;
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
	
	public void setTimeStampAndQueueSize(long timestamp, int size){
		this.timestamp = timestamp;
		this.queueSize = size;
	}
	
	public long getTimeStamp(){
		return this.timestamp;
	}

	@Override
	public int compareTo(PeerDescriptor other) {
		if (this.timestamp > other.timestamp)
			return 1;
		if (this.timestamp < other.timestamp)
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
