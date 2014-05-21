package tman.system.peer.tman.gradient;

import java.util.Comparator;
import java.util.List;

import cyclon.system.peer.cyclon.PeerDescriptor;

/**
 * Gradient for the combination of CPU and MEM
 * 
 * @author kristian
 *
 */
public class GradientCOMBO extends Gradient {

	private static final long serialVersionUID = -1737545730105842953L;

	public GradientCOMBO(List<PeerDescriptor> entries,
			Comparator<? super PeerDescriptor> comparator) {
		super(entries, comparator);
	}
}