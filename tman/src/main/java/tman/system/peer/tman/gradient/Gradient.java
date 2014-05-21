package tman.system.peer.tman.gradient;

import java.io.Serializable;
import java.util.Collection;
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
public class Gradient implements Serializable {
	
	private static final long serialVersionUID = -5489515024758468494L;
	private List<PeerDescriptor> entries;
	private Comparator<? super PeerDescriptor> comparator;
	private int type;
	
	public static final int TYPE_CPU=0;
	public static final int TYPE_MEM=1;
	public static final int TYPE_COMBO=2;
	
	public Gradient(List<PeerDescriptor> entries, Comparator<? super PeerDescriptor> comparator, int type) {
		this.type = type;
		this.entries = entries;
		this.comparator = comparator;
	}
	
	public List<PeerDescriptor> getEntries() {
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
	
	public void addEntry(PeerDescriptor p){
		entries.add(p);
	}
	
	public void addEntry(Collection<PeerDescriptor> p){
		entries.addAll(p);
	}
	
	public Comparator<? super PeerDescriptor> getComparator() {
		return comparator;
	}
	
	public int getType() {
		return type;
	}
	
	
}