package resourcemanager.system.peer.rm;

import java.util.HashMap;
import java.util.Map;

public class Experiment {
	
	public static final String OUTFILE = "OUTFILE";
	public static final String NUM_OF_PROBES = "NUM_OF_PROBES";
	public static final String NUM_OF_NODES = "NUM_OF_NODES";
	public static final String NUM_OF_JOBS = "NUM_OF_JOBS";

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
