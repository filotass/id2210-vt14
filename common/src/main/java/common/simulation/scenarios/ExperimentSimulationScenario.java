package common.simulation.scenarios;

import se.sics.kompics.p2p.experiment.dsl.SimulationScenario;

public class ExperimentSimulationScenario  extends SimulationScenario{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -950577208332899636L;
	private static Experiment e;
	
	private StochasticProcess process0;
	private StochasticProcess process1;
	
	// TODO - not used yet
	private StochasticProcess failPeersProcess = new StochasticProcess() {{
		eventInterArrivalTime(constant(100));
		raise(1, Operations.peerFail, uniform(0, Integer.MAX_VALUE));
	}};

	private StochasticProcess terminateProcess = new StochasticProcess() {{
		eventInterArrivalTime(constant(100));
		raise(1, Operations.terminate);
	}};
	
	
	public ExperimentSimulationScenario(){

		process0 = new StochasticProcess() {{
			eventInterArrivalTime(constant(1000));
			raise(Integer.valueOf(System.getProperty(Experiment.NUM_OF_NODES)), Operations.peerJoin(), 
					uniform(0, Integer.MAX_VALUE), 
					constant(8), constant(12000)
					);
		}};
		
		process1 = new StochasticProcess() {{
			eventInterArrivalTime(constant(100));
			raise(Integer.valueOf(System.getProperty(Experiment.NUM_OF_JOBS)), Operations.requestResources(), 
				  uniform(0, Integer.MAX_VALUE),
					constant(2), constant(2000),
					constant(1000*60*1) // 1 minute
					);
		}};
		
		
		
		process0.start();
		process1.startAfterTerminationOf(2000, process0);
		terminateProcess.startAfterTerminationOf(100*1000, process1);
	}
	
	public Experiment getExperiment(){
		return e;
	}


}
