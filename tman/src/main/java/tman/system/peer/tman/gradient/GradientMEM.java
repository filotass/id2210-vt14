package tman.system.peer.tman.gradient;

import java.util.Comparator;
import java.util.List;

import cyclon.system.peer.cyclon.PeerDescriptor;

/**
 * Gradient, dominant resource: MEM
 * 
 * @author kristian
 *
 */
public class GradientMEM extends Gradient {

	private static final long serialVersionUID = 8122306219518948771L;

	public GradientMEM(List<PeerDescriptor> entries,
			Comparator<? super PeerDescriptor> comparator) {
		super(entries, comparator);
	}
	
	public boolean isMEMbased(){
		return true;
	}
}