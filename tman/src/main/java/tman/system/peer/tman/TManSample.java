package tman.system.peer.tman;

import se.sics.kompics.Event;

/**
 * A sample given from TMan to everyone interested.
 * In our case the ResourceManager is listening.
 * 
 * @author kristian
 *
 */
public class TManSample extends Event {
	
	private Gradient gradient;

	public TManSample(Gradient newGradient) {
		this.gradient = newGradient;
	}

	public Gradient getSample() {
		return this.gradient;
	}
}