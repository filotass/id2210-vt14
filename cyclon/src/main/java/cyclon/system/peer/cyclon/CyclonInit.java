package cyclon.system.peer.cyclon;

import java.util.ArrayList;

import common.configuration.CyclonConfiguration;
import common.peer.AvailableResources;
import common.simulation.SuperJob;
import se.sics.kompics.Init;

public final class CyclonInit extends Init {

    private final CyclonConfiguration configuration;
    private final AvailableResources availableResources;
	private final ArrayList<SuperJob> queueJobs;

    public CyclonInit(CyclonConfiguration configuration,
            AvailableResources availableResources, ArrayList<SuperJob> queueJobs) {
        super();
        this.configuration = configuration;
        this.availableResources = availableResources;
        this.queueJobs = queueJobs;
    }

    public AvailableResources getAvailableResources() {
        return availableResources;
    }

    public CyclonConfiguration getConfiguration() {
        return configuration;
    }
    
    public  ArrayList<SuperJob> getQueueJobs(){
    	return queueJobs;
    }
}
