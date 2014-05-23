package tman.system.peer.tman;

import java.util.Comparator;

import common.simulation.scenarios.Experiment;

import cyclon.system.peer.cyclon.PeerDescriptor;

public class ComparatorByAvailableResources implements Comparator<PeerDescriptor>{

	@Override
	public int compare(PeerDescriptor a, PeerDescriptor b) {
		
		double aCPU = a.getAvailableResources().getNumFreeCpus();
		double bCPU = b.getAvailableResources().getNumFreeCpus();

		double aMem = a.getAvailableResources().getFreeMemInMbs();
		double bMem = b.getAvailableResources().getFreeMemInMbs();

		
		double totalCPUs = Integer.parseInt(System.getProperty(Experiment.NUMBER_OF_CPUS_PER_NODE));
		double totalMem  = Integer.parseInt(System.getProperty(Experiment.NUMBER_OF_MBS_PER_NODE));
		
		double aNRM_CPU = aCPU/totalCPUs;
		double aNRM_MEM = aMem/totalMem;
		double bNRM_CPU = bCPU/totalCPUs;
		double bNRM_MEM = bMem/totalMem;

		
		double aUtility = aNRM_CPU*aNRM_MEM;
		double bUtility = bNRM_CPU*bNRM_MEM;

		if(aUtility > bUtility){
			return -1;
		}else if(aUtility < bUtility){
			return 1;
		}
		return 0;

	}
}
