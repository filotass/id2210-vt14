package system.peer;

import java.util.ArrayList;

import common.configuration.RmConfiguration;
import common.configuration.CyclonConfiguration;
import common.configuration.TManConfiguration;
import common.peer.AvailableResources;
import common.simulation.SuperJob;
import cyclon.system.peer.cyclon.PeerDescriptor;
import se.sics.kompics.Init;
import se.sics.kompics.address.Address;
import se.sics.kompics.p2p.bootstrap.BootstrapConfiguration;

public final class PeerInit extends Init {

    private final Address peerSelf;
    private final BootstrapConfiguration bootstrapConfiguration;
    private final CyclonConfiguration cyclonConfiguration;
    private final RmConfiguration applicationConfiguration;
    private final AvailableResources availableResources;
    private final TManConfiguration tmanConfiguration;
	private final ArrayList<SuperJob> queueJobs;

    public PeerInit(Address peerSelf, BootstrapConfiguration bootstrapConfiguration,
            CyclonConfiguration cyclonConfiguration, RmConfiguration applicationConfiguration, TManConfiguration tManConfiguration,
            AvailableResources availableResources, ArrayList<SuperJob> queueJobs) {
        super();
        this.peerSelf = peerSelf;
        this.bootstrapConfiguration = bootstrapConfiguration;
        this.cyclonConfiguration = cyclonConfiguration;
        this.applicationConfiguration = applicationConfiguration;
        this.availableResources = availableResources;
        this.tmanConfiguration = tManConfiguration;
        this.queueJobs = queueJobs;

    }

    public AvailableResources getAvailableResources() {
        return availableResources;
    }

    
    public Address getPeerSelf() {
        return this.peerSelf;
    }

    public BootstrapConfiguration getBootstrapConfiguration() {
        return this.bootstrapConfiguration;
    }

    public CyclonConfiguration getCyclonConfiguration() {
        return this.cyclonConfiguration;
    }

    public RmConfiguration getApplicationConfiguration() {
        return this.applicationConfiguration;
    }
    
    public TManConfiguration getTManConfiguration() {
        return this.tmanConfiguration;
    }
    
    public ArrayList<SuperJob> getQueueJobs(){
    	return this.queueJobs;
    }
    
    

}
