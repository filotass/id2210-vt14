package common.simulation;

import java.io.Serializable;

import se.sics.kompics.Event;

/**
 * We assume there are only jobs and that they are not broken down into tasks.
 * 
 * @author filotas
 *
 */
public final class Job extends Event implements Serializable{
    

	private static final long serialVersionUID = 8582787369387642620L;
	private final long id;
    private final int numCpus;
    private final int memoryInMbs;
    private final int timeToHoldResource;

    public Job(long id, int numCpus, int memoryInMbs, int timeToHoldResource) {
        this.id = id;
        this.numCpus = numCpus;
        this.memoryInMbs = memoryInMbs;
        this.timeToHoldResource = timeToHoldResource;
    }

    public long getId() {
        return id;
    }

    public int getTimeToHoldResource() {
        return timeToHoldResource;
    }

    public int getMemoryInMbs() {
        return memoryInMbs;
    }

    public int getNumCpus() {
        return numCpus;
    }

}
