package tman.system.peer.tman.gradient;

import java.util.Comparator;
import java.util.List;

import cyclon.system.peer.cyclon.PeerDescriptor;

/**
 * Gradient, dominant resource: CPU
 * 
 * @author kristian
 *
 */
public class GradientCPU extends Gradient {

	private static final long serialVersionUID = -1767116002525493812L;

	public GradientCPU(List<PeerDescriptor> entries,
			Comparator<? super PeerDescriptor> comparator) {
		super(entries, comparator);
	}
}