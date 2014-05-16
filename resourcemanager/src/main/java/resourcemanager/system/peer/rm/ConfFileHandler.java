package resourcemanager.system.peer.rm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ConfFileHandler {

	
	public static ArrayList<Experiment> fileToExperiments(String configurationFilePath, int numOfVariables){

		ArrayList<Experiment> experiments = new ArrayList<Experiment>();
		
		BufferedReader	br = null;
		try {
			br = new BufferedReader(new FileReader(configurationFilePath));
			String line = br.readLine();
			int num_of_line=0;
			Experiment ex = new Experiment();
			while (line!=null){
				
				if(num_of_line%numOfVariables==0){
					ex = new Experiment();
				}
				StringTokenizer st=new StringTokenizer(line,":");
				String variableName = st.nextToken();
				String value = st.nextToken();
				ex.setProperty(variableName, value);

		    	line = br.readLine();
		    	
		    	if(num_of_line%numOfVariables == numOfVariables-1){
		    		experiments.add(ex);
		    	}
		    	num_of_line++;
			}			
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}finally{
			if(br!=null){
				try {
					br.close();
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}	
			}
		}
		
		return experiments;
	}
	


	
}
