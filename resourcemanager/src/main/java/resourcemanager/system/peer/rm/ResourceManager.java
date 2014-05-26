package resourcemanager.system.peer.rm;

import common.configuration.RmConfiguration;
import common.peer.AvailableResources;
import common.simulation.SuperJob;

import cyclon.system.peer.cyclon.CyclonSamplePort;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.web.Web;
import simulator.snapshot.Snapshot;
import system.peer.RmPort;
import tman.system.peer.tman.Gradient;
import tman.system.peer.tman.TManSample;
import tman.system.peer.tman.TManSamplePort;

/**
 * Resource Manager has 2 main independent roles.
 * Role 1: Acts as a Scheduler that listens for Jobs from Client Apps, probes the peer network and assigns jobs to peers.
 * Role 2: Acts as a Worker that executes the Jobs.
 *
 * @author jdowling, filotas, kristian
 */
public final class ResourceManager extends ComponentDefinition {
    
    /**
     * Port for Receiving Incoming Jobs from Clients
     */
    Negative<Web> webPort = negative(Web.class);
	
    Positive<RmPort> indexPort = positive(RmPort.class);
    Positive<Network> networkPort = positive(Network.class);
    Positive<Timer> timerPort = positive(Timer.class);
    Positive<CyclonSamplePort> cyclonSamplePort = positive(CyclonSamplePort.class);
    Positive<TManSamplePort> tmanPort = positive(TManSamplePort.class);
    
    /**
     * Partial View of Peer Network
     */
    private Address self;
    private RmConfiguration configuration;

    
    /**
     * Used for role of Worker. Queues Jobs that are assigned from Schedulers.
     */
    private List<SuperJob> queuedJobs;
    
    private List<SuperJob> runningJobs = new ArrayList<SuperJob>();

    /**
     * Used for role of Scheduler. Holds the Jobs to be assigned to Workers.
     */
    private Map<Long,SuperJob> jobsFromClients = new HashMap<Long,SuperJob>();
 
    /**
     * Used for role of Worker. 
     */
    private AvailableResources availableResources;
    
    /**
     * Task 2: Gradients received from TMan
     */
    private Gradient gradientCPU;
    private Gradient gradientMEM;
    private Gradient gradientCombo;

    public ResourceManager() {

        subscribe(handleInit, control);
        subscribe(handleTManSample, tmanPort);
        subscribe(handleRequestResource, indexPort);
        subscribe(handleJobFinishedTimeout, timerPort);
        subscribe(handleIncomingJob, networkPort);

    }
	
    Handler<RmInit> handleInit = new Handler<RmInit>() {
        @Override
        public void handle(RmInit init) {
            self = init.getSelf();
            configuration = init.getConfiguration();
            availableResources = init.getAvailableResources();
            queuedJobs = init.getQueueJobs();
            long period = configuration.getPeriod();
            SchedulePeriodicTimeout rst = new SchedulePeriodicTimeout(period, period);
            rst.setTimeoutEvent(new UpdateTimeout(rst));
            trigger(rst, timerPort);
        }
    };
    
    /**
     * We are listening to this peer's TMan layer. 
     */
    Handler<TManSample> handleTManSample = new Handler<TManSample>() {
    	
        @Override
        public void handle(TManSample event) {
        	
        	Gradient newGradient = event.getSample();
        	
        	// Determine the type of this received gradient... 
        	if(newGradient.getType() == Gradient.TYPE_CPU) {
        		gradientCPU = newGradient;
        	} else if(newGradient.getType() == Gradient.TYPE_MEM) {
        		gradientMEM = newGradient;
        		
        	} else if(newGradient.getType() == Gradient.TYPE_COMBO) {
        		gradientCombo = newGradient;
        		
        	}else {
        		System.err.println("ERROR UNKNOWN GRADIENT TYPE");
        		System.exit(1);
        	}
        }
    };

    /**
     *  Role of Scheduler. Handle incoming scheduling jobs from client apps and send probes to worker peers.
     */
    Handler<SuperJob> handleRequestResource = new Handler<SuperJob>() {
        @Override
        public void handle(SuperJob event) {
        	Snapshot.report(Snapshot.INI + Snapshot.S + event.getId() + Snapshot.S + System.currentTimeMillis());
        	 

            
            // remember the job and then probe the peer network
            jobsFromClients.put(event.getId(), event);

            // Define what gradient to use for finding the available resources...
            Gradient gradientToUse = null;
            
            // If MBS == 0, we only care about finding CPU... Let's use the CPU only gradient 
            if(event.getMemoryInMbs() == 0) {
            	
            	gradientToUse = gradientCPU;
            
            // If CPU == 0, we only care about finding MEM... Let's use the MEM only gradient
            } else if(event.getNumCpus() == 0) {
            	
            	gradientToUse = gradientMEM;
            
            // Else, if we need both memory and cpu, lets use the combined gradient
            // which uses multiplication of the normalized values of both CPU and MEM. 
            } else {
            	gradientToUse = gradientCombo;
            }
            int rndIndex = (int) Math.random() * gradientToUse.getEntries().size();
          
            RequestResources.ScheduleJob schJob = new RequestResources.ScheduleJob(self, gradientToUse.getEntries().get(rndIndex).getAddress(),event);
            trigger(schJob, networkPort);
            

        }
    };
    
    /**
     * Role of worker: Listens for jobs that are assigned to this worker from schedulers. 
     * 
     * If there are no resources, place the job in the queue
     * 
     * Else, we allocate the needed resources and start executing the job.
     */
    Handler<RequestResources.ScheduleJob> handleIncomingJob = new Handler<RequestResources.ScheduleJob>() {
        @Override
        public void handle(RequestResources.ScheduleJob event) {
        	SuperJob job = event.getJob();
        	Snapshot.report(Snapshot.ASN + Snapshot.S + job.getId() + Snapshot.S + System.currentTimeMillis());

        	System.out.println("HANDLE INCOMING JOB, MY RESOURCES ARE: " + availableResources+ " Queue size="+queuedJobs.size());
        	
        	if(!scheduleJob(job)){
        		queuedJobs.add(job);
        	}
        }
    };
    
    private boolean scheduleJob(SuperJob job){
    	boolean success = availableResources.allocate(job.getNumCpus(), job.getMemoryInMbs());
    	if(success){
    		Snapshot.report(Snapshot.SCH + Snapshot.S + job.getId() + Snapshot.S + System.currentTimeMillis());
    		runningJobs.add(job);
    		ScheduleTimeout st = new ScheduleTimeout(job.getTimeToHoldResource());
    		st.setTimeoutEvent(new JobFinishedTimeout(st,job.getId()));
    		trigger(st, timerPort);
    	}
    	return success;
    }
    
    /**
     * Role of Worker. Listens for a JobFinishedTimeout event in order to indicate that a Job
     * has successfully been executed in order to release resources.
     */
    Handler<JobFinishedTimeout> handleJobFinishedTimeout = new Handler<JobFinishedTimeout>() {
        @Override
        public void handle(JobFinishedTimeout event) {
        	for(SuperJob job: runningJobs){
        		if(job.getId()== event.getJobID()){
        			availableResources.release(job.getNumCpus(), job.getMemoryInMbs());
        			runningJobs.remove(job);
        			if(queuedJobs.size()>0){
        				SuperJob nextJob = queuedJobs.get(0);
        				if(scheduleJob(nextJob)){
        					queuedJobs.remove(nextJob);
        				}
        			}
        			break;
        		}
        	}
        }
    };


}