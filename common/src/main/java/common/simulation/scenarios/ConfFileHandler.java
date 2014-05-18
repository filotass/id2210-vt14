package common.simulation.scenarios;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class ConfFileHandler {
	
	public static Experiment fileToExperiments(String configurationFilePath){
		
		Experiment ex = null;
		BufferedReader	br = null;
		try {
			br = new BufferedReader(new FileReader(configurationFilePath));
			String line = br.readLine();
	
			ex = new Experiment();
			while (line!=null){
				StringTokenizer st=new StringTokenizer(line,":");
				String variableName = st.nextToken();
				String value = st.nextToken();
				ex.setProperty(variableName, value);
		    	
		    	line = br.readLine();

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
		
		return ex;
	}
	
	public static void deleteFile(String filename){
		try{
			File file = new File(filename);
			 
			if(file.delete()){
				System.out.println(file.getName() + " is deleted!");
			}else{
				System.out.println("Delete operation is failed.");
			}
	
		}catch(Exception e){
				e.printStackTrace();
		}
	}
	


	
}
