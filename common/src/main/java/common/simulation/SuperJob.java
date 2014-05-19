package common.simulation;

import java.io.Serializable;
import java.util.ArrayList;

import se.sics.kompics.Event;

public class SuperJob extends Event implements Serializable{

	private static final long serialVersionUID = 8560340748147990308L;
	private long id;
	private ArrayList<Job> jobs;
	
	public SuperJob(Long id, ArrayList<Job> jobs){
		this.id= id;
		this.jobs = jobs;
	}
	
	public ArrayList<Job> getJobs(){
		return jobs;	
	}
	
	public long getId(){
		return id;
	}
	
	public boolean isSingular(){
		return (jobs.size()==1);
	}
	
	public int getNumJobs(){
		return jobs.size();
	}
	
	public int getNumCpus(){
		return jobs.get(0).getNumCpus();
	}
	
	public int getMemoryInMbs(){
		return jobs.get(0).getMemoryInMbs();
	}
	
	public int getTimeToHoldResource(){
		return 	jobs.get(0).getTimeToHoldResource();
	}
	

	

}
