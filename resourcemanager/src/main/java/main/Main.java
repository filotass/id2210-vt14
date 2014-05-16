package main;

import java.util.ArrayList;

import simulator.core.DataCenterSimulationMain;
import common.configuration.Configuration;
import common.simulation.scenarios.ConfFileHandler;
import common.simulation.scenarios.Experiment;
import common.simulation.scenarios.Scenario;
import common.simulation.scenarios.Scenario1;


public class Main {
	
    public static void main(String[] args) throws Throwable {

        Experiment experiment = ConfFileHandler.fileToExperiments(args[0]);

    	System.err.println(experiment);

        // TODO - change the random seed, have the user pass it in.
        long seed = System.currentTimeMillis();
        Configuration configuration = new Configuration(seed);
    	
    	Scenario scenario = new Scenario1(experiment);
        scenario.setSeed(seed);
        scenario.getScenario().simulate(DataCenterSimulationMain.class);
        //NOTHING RUNS AFTER THIS POINT - BELOW IS THE BLACK HOLE
        
        System.err.println("JAVLA SKIT");

  
  }
}
