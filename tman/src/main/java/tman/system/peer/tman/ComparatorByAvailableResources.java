package tman.system.peer.tman;

import java.util.Comparator;

import common.simulation.scenarios.Experiment;

import cyclon.system.peer.cyclon.PeerDescriptor;

public class ComparatorByAvailableResources implements Comparator<PeerDescriptor>{

	private PeerDescriptor self;
	
	public ComparatorByAvailableResources(PeerDescriptor self){
		this.self = self;
	}
	
	@Override
	public int compare(PeerDescriptor a, PeerDescriptor b) {
		
		int aCPU = a.getAvailableResources().getNumFreeCpus();
		int bCPU = b.getAvailableResources().getNumFreeCpus();
		int selfCPU = self.getAvailableResources().getNumFreeCpus();
		int aMem = a.getAvailableResources().getFreeMemInMbs();
		int bMem = b.getAvailableResources().getFreeMemInMbs();
		int selfMem = self.getAvailableResources().getFreeMemInMbs();
		
		int totalCPUs = Integer.parseInt(System.getProperty(Experiment.NUMBER_OF_CPUS_PER_NODE));
		int totalMem  = Integer.parseInt(System.getProperty(Experiment.NUMBER_OF_MBS_PER_NODE));
		
		double aNRM_CPU = aCPU/totalCPUs;
		double aNRM_MEM = aMem/totalMem;
		double bNRM_CPU = bCPU/totalCPUs;
		double bNRM_MEM = bMem/totalMem;
		double selfNRM_CPU = selfCPU/totalCPUs;
		double selfNRM_MEM = selfMem/totalMem;
		
		double aUtility = aNRM_CPU*aNRM_MEM;
		double bUtility = bNRM_CPU*bNRM_MEM;
		double selfUtility = selfNRM_CPU*selfNRM_MEM;
		
		if (aUtility < selfUtility && bUtility > selfUtility) {
            return 1;
        } else if (bUtility < selfUtility && aUtility > selfUtility) {
            return -1;
        } else if (Math.abs(aUtility - selfUtility) < Math.abs(bUtility - selfUtility)) {
            return -1;
        }
        return 1;
	}
}
