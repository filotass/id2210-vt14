package common.simulation;

import java.io.Serializable;

import se.sics.kompics.Event;

public class SuperJob extends Event implements Serializable{


	private static final long serialVersionUID = -2676264713697416246L;
	private long id;
    private final int numOfTasks;
    private final int numCpus;
    private final int memoryInMbs;
    private final int timeToHoldResource;

	public SuperJob(Long id, int numOfTasks, int numCpus, int memoryInMbs, int timeToHoldResource){
		this.id= id;
		this.numOfTasks = numOfTasks;
		this.numCpus = numCpus;
		this.memoryInMbs = memoryInMbs;
		this.timeToHoldResource = timeToHoldResource;
	}
	
	public long getId(){
		return id;
	}
	
	public boolean isSingular(){
		return (numOfTasks==1);
	}
	
	public int getNumOfTasks(){
		return numOfTasks;
	}
	
	public int getNumCpus(){
		return numCpus;
	}
	
	public int getMemoryInMbs(){
		return memoryInMbs;
	}
	
	public int getTimeToHoldResource(){
		return 	timeToHoldResource;
	}
	

	

}
