package main;

import simulator.core.DataCenterSimulationMain;
import simulator.snapshot.Snapshot;
import common.configuration.Configuration;
import common.simulation.scenarios.ConfFileHandler;
import common.simulation.scenarios.Experiment;
import common.simulation.scenarios.Scenario;
import common.simulation.scenarios.Scenario1;

public class Main {
	
	
    public static void main(String[] args) throws Throwable {

        Experiment experiment = ConfFileHandler.fileToExperiments(args[0]);
        ConfFileHandler.deleteFile(Snapshot.FOLDER+experiment.getValue(Experiment.OUTFILE));
        
        // TODO - change the random seed, have the user pass it in.
        long seed = System.currentTimeMillis();
        new Configuration(seed, experiment);
    	
    	Scenario scenario = new Scenario1();
        scenario.setSeed(seed);
        scenario.getScenario().simulate(DataCenterSimulationMain.class);
        //NOTHING RUNS AFTER THIS POINT - BELOW IS THE BLACK HOLE

  
  }
}
