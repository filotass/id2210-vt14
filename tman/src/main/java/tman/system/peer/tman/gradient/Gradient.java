package tman.system.peer.tman.gradient;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import cyclon.system.peer.cyclon.PeerDescriptor;

/**
 * This class works as a superclass to different gradient types.
 * 
 * The subclasses are intended to be both flags and containers
 * of entries and a comparator for each specific gradient.
 * 
 * @author kristian
 *
 */
public abstract class Gradient implements Serializable {
	
	private static final long serialVersionUID = -5489515024758468494L;
	private List<PeerDescriptor> entries;
	private Comparator<? super PeerDescriptor> comparator;
	
	public Gradient(List<PeerDescriptor> entries, Comparator<? super PeerDescriptor> comparator) {
		
		this.entries = entries;
		this.comparator = comparator;
	}
	
	public List<PeerDescriptor> getEntried() {
		return entries;
	}
	
	public void setEntries(List<PeerDescriptor> entries){
		this.entries = entries;
	}
	
	public int getSize(){
		return entries.size();
	}
	
	public boolean isEmpty(){
		return (entries.size()==0);
	}
	
	public Comparator<? super PeerDescriptor> getComparator() {
		return comparator;
	}
	
	public boolean isCPUbased(){
		return false;
	}
	
	public boolean isMEMbased(){
		return false;
	}
	
	public boolean isCOMBObased(){
		return false;
	}
	
	
}