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
			raise(	Integer.valueOf(System.getProperty(Experiment.NUM_OF_NODES)), 
					Operations.peerJoin(), 
					uniform(0, Integer.MAX_VALUE), 
					constant(Integer.valueOf(System.getProperty(Experiment.NUMBER_OF_CPUS_PER_NODE))),
					constant(Integer.valueOf(System.getProperty(Experiment.NUMBER_OF_MBS_PER_NODE)))
					);
		}};
		
		process1 = new StochasticProcess() {{
			eventInterArrivalTime(constant(100));
			raise(Integer.valueOf(System.getProperty(Experiment.NUM_OF_JOBS)), 
					Operations.requestBatchResources(), 
				    uniform(0, Integer.MAX_VALUE),
				    constant(Integer.valueOf(System.getProperty(Experiment.NUMBER_OF_TASKS_PER_JOB))), // 1 = singular, +1 = batch
					constant(Integer.valueOf(System.getProperty(Experiment.NUMBER_OF_CPUS_PER_JOB))),
					constant(Integer.valueOf(System.getProperty(Experiment.NUMBER_OF_MBS_PER_JOB))),
					constant(Integer.valueOf(System.getProperty(Experiment.JOB_DURATION))) // 1 second
					);
		}};
		
		
		
		process0.start();
		process1.startAfterTerminationOf(20000, process0);
		terminateProcess.startAfterTerminationOf(100*1000, process1);
	}
	
	public Experiment getExperiment(){
		return e;
	}


}
