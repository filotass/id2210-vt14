package tman.system.peer.tman;

import java.util.ArrayList;

import common.configuration.TManConfiguration;
import common.peer.AvailableResources;
import common.simulation.SuperJob;
import se.sics.kompics.Init;
import se.sics.kompics.address.Address;

public final class TManInit extends Init {

    private final Address peerSelf;
    private final TManConfiguration configuration;
    private final AvailableResources availableResources;
	private final ArrayList<SuperJob> queueJobs;

    public TManInit(Address peerSelf, TManConfiguration configuration,
            AvailableResources availableResources, ArrayList<SuperJob> queueJobs) {
        super();
        this.peerSelf = peerSelf;
        this.configuration = configuration;
        this.availableResources = availableResources;
        this.queueJobs = queueJobs;
    }

    public AvailableResources getAvailableResources() {
        return availableResources;
    }

    public Address getSelf() {
        return this.peerSelf;
    }

    public TManConfiguration getConfiguration() {
        return this.configuration;
    }

	public ArrayList<SuperJob> getQueueJobs() {
		return queueJobs;
	}
    
    
}
