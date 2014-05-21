package common.simulation.scenarios;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Experiment implements Serializable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -7290526136887511211L;
	public static final String OUTFILE = "OUTFILE";
	public static final String NUM_OF_PROBES = "NUMBER_OF_PROBES";
	public static final String NUM_OF_NODES = "NUMBER_OF_NODES";
	public static final String NUM_OF_JOBS = "NUMBER_OF_JOBS";
	public static final String NUMBER_OF_CPUS_PER_NODE = "NUMBER_OF_CPUS_PER_NODE";
	public static final String NUMBER_OF_MBS_PER_NODE = "NUMBER_OF_MBS_PER_NODE";
	public static final String NUMBER_OF_CPUS_PER_JOB = "NUMBER_OF_CPUS_PER_JOB";
	public static final String NUMBER_OF_MBS_PER_JOB = "NUMBER_OF_MBS_PER_JOB";
	public static final String JOB_DURATION = "JOB_DURATION";
	public static final String NUMBER_OF_TASKS_PER_JOB = "NUMBER_OF_TASKS_PER_JOB";
	public static final String TMAN_C = "TMAN_C";


	
	private Map<String,String> parameters = new HashMap<String,String>();
	
	public void setProperty(String variableName, String value){
		parameters.put(variableName, value);
	}
	
	public String getValue(String variableName){
		return parameters.get(variableName);
	}
	
	@Override
	public String toString(){
		String temp = OUTFILE + ":" + getValue(OUTFILE) + "\n" +
					  NUM_OF_PROBES + ":"+getValue(NUM_OF_PROBES) + "\n" +
					  NUM_OF_NODES + ":"+getValue(NUM_OF_NODES)+"\n"+ 
					  NUM_OF_JOBS + ":"+getValue(NUM_OF_JOBS)+"\n";
		return temp;
	
	}
}
