package resourcemanager.system.peer.rm;

import java.util.ArrayList;

import common.configuration.RmConfiguration;
import common.peer.AvailableResources;
import common.simulation.SuperJob;
import se.sics.kompics.Init;
import se.sics.kompics.address.Address;

public final class RmInit extends Init {

    private final Address peerSelf;
    private final RmConfiguration configuration;
    private final AvailableResources availableResources;
	private final ArrayList<SuperJob> queueJobs;
    

    public RmInit(Address peerSelf, RmConfiguration configuration,
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

    public RmConfiguration getConfiguration() {
        return this.configuration;
    }

	public ArrayList<SuperJob> getQueueJobs() {
		return queueJobs;
	}
    
    
}
