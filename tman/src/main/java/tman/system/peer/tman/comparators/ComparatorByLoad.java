package tman.system.peer.tman.comparators;

import common.simulation.scenarios.Experiment;
import cyclon.system.peer.cyclon.PeerDescriptor;

public class ComparatorByLoad extends PeerComparator{

	public ComparatorByLoad(PeerDescriptor self) {
		super(self);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(PeerDescriptor a, PeerDescriptor b) {
		if(a.getQueueSize() - b.getQueueSize() != 0){
			return a.getQueueSize() - b.getQueueSize();
		}
		
		double aCPU = a.getAvailableResources().getNumFreeCpus();
		double bCPU = b.getAvailableResources().getNumFreeCpus();
		double sCPU = self.getAvailableResources().getNumFreeCpus();

		double aMem = a.getAvailableResources().getFreeMemInMbs();
		double bMem = b.getAvailableResources().getFreeMemInMbs();
		double sMem = self.getAvailableResources().getFreeMemInMbs();

		
		double totalCPUs = Integer.parseInt(System.getProperty(Experiment.NUMBER_OF_CPUS_PER_NODE));
		double totalMem  = Integer.parseInt(System.getProperty(Experiment.NUMBER_OF_MBS_PER_NODE));
		
		double aNRM_CPU = aCPU/totalCPUs;
		double aNRM_MEM = aMem/totalMem;
		double bNRM_CPU = bCPU/totalCPUs;
		double bNRM_MEM = bMem/totalMem;
		double sNRM_CPU = sCPU/totalCPUs;
		double sNRM_MEM = sMem/totalMem;

		
		double aUtility = aNRM_CPU*aNRM_MEM;
		double bUtility = bNRM_CPU*bNRM_MEM;
		double sUtility = sNRM_CPU*sNRM_MEM;
		
      	if (aUtility < sUtility && bUtility > sUtility) {
            return 1;
        } else if (bUtility < sUtility && aUtility > sUtility) {
            return -1;
        } else if (Math.abs(aUtility - sUtility) < Math.abs(bUtility - sUtility)) {
            return -1;
        }
		
		
		
		return 0;
	}
	
	

}
