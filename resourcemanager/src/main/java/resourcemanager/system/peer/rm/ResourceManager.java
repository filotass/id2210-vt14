package resourcemanager.system.peer.rm;

import common.configuration.RmConfiguration;
import common.peer.AvailableResources;
import common.simulation.Job;
import common.simulation.SuperJob;
import common.simulation.scenarios.Experiment;
import cyclon.system.peer.cyclon.CyclonSample;
import cyclon.system.peer.cyclon.CyclonSamplePort;
import cyclon.system.peer.cyclon.PeerDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);
    
    /**
     * Port for Receiving Incoming Jobs from Clients
     */
    Positive<RmPort> indexPort = positive(RmPort.class);
    
    Positive<Network> networkPort = positive(Network.class);
    Positive<Timer> timerPort = positive(Timer.class);
    Negative<Web> webPort = negative(Web.class);
    
    Positive<CyclonSamplePort> cyclonSamplePort = positive(CyclonSamplePort.class);
    Positive<TManSamplePort> tmanPort = positive(TManSamplePort.class);
    
    /**
     * Partial View of Peer Network
     */
    ArrayList<Address> neighbours = new ArrayList<Address>();
    private Address self;
    private RmConfiguration configuration;
    Random random;
    
    /**
     * Used for role of Worker. Queues Jobs that are assigned from Schedulers.
     */
    private List<SuperJob> queuedJobs = new ArrayList<SuperJob>();
    
    private List<SuperJob> runningJobs = new ArrayList<SuperJob>();
    
    /**
     * Used for role of Scheduler. Holds the Probe Responses per Job while probing the Peer Network.
     */
    private Map<Long,List<RequestResources.Response>> probesReceived = new HashMap<Long, List<RequestResources.Response>>();
    
    
    /**
     * Used for role of Scheduler. Holds the Number of probes per Job.
     */
    private Map<Long,Integer> numProbesPerJob = new HashMap<Long,Integer>();
    
    /**
     * Used for role of Scheduler. Holds the Jobs to be assigned to Workers.
     */
    private Map<Long,SuperJob> jobsFromClients = new HashMap<Long,SuperJob>();
    
    /**
     * Number of Probes sent for each Job. Increasing the number of probes may have better accuracy but it increases the 
     * risk that the last probe will have greater latency (tail-sensitive).
     */
    private static int NUM_PROBES;
    
    /**
     * Used for role of Worker. 
     */
    private AvailableResources availableResources;
    
    
    Comparator<PeerDescriptor> peerAgeComparator = new Comparator<PeerDescriptor>() {
        @Override
        public int compare(PeerDescriptor t, PeerDescriptor t1) {
            if (t.getAge() > t1.getAge()) {
                return 1;
            } else {
                return -1;
            }
        }
    };

    public ResourceManager() {

        subscribe(handleInit, control);
        subscribe(handleCyclonSample, cyclonSamplePort);
        subscribe(handleRequestResource, indexPort);
        subscribe(handleUpdateTimeout, timerPort);
        subscribe(handleJobFinishedTimeout, timerPort);
        subscribe(handleResourceAllocationRequest, networkPort);
        subscribe(handleResourceAllocationResponse, networkPort);
        subscribe(handleIncomingJob, networkPort);
        subscribe(handleTManSample, tmanPort);
    }
	
    Handler<RmInit> handleInit = new Handler<RmInit>() {
        @Override
        public void handle(RmInit init) {
            self = init.getSelf();
            configuration = init.getConfiguration();
            NUM_PROBES = Integer.parseInt(System.getProperty(Experiment.NUM_OF_PROBES));
            random = new Random(init.getConfiguration().getSeed());
            availableResources = init.getAvailableResources();
            long period = configuration.getPeriod();
            SchedulePeriodicTimeout rst = new SchedulePeriodicTimeout(period, period);
            rst.setTimeoutEvent(new UpdateTimeout(rst));
            trigger(rst, timerPort);
        }
    };

    Handler<UpdateTimeout> handleUpdateTimeout = new Handler<UpdateTimeout>() {
        @Override
        public void handle(UpdateTimeout event) {

            // pick a random neighbour to ask for index updates from. 
            // You can change this policy if you want to.
            // Maybe a gradient neighbour who is closer to the leader?
            if (neighbours.isEmpty()) {
                return;
            }
            // TODO: Implement
            //Address dest = neighbours.get(random.nextInt(neighbours.size()));


        }
    };
    
    /**
     * Role of peer: Get neighbour samples form cyclon.
     */
    Handler<CyclonSample> handleCyclonSample = new Handler<CyclonSample>() {
        @Override
        public void handle(CyclonSample event) {
           // System.out.println("Received samples: " + event.getSample().size());
            
            // receive a new list of neighbours
            neighbours.clear();
            neighbours.addAll(event.getSample());
        }
    };
    
    /**
     *  Role of Scheduler. Handle incoming scheduling jobs from client apps and send probes to worker peers.
     */
    Handler<SuperJob> handleRequestResource = new Handler<SuperJob>() {
        @Override
        public void handle(SuperJob event) {
            
            //System.out.println("Client wants to allocate resources: " + event.getNumCpus() + " + " + event.getMemoryInMbs());

            List<Address> copyNeighbourList = new ArrayList<Address>();
            copyNeighbourList.addAll(neighbours);
            
            // remember the job and then probe the peer network
            jobsFromClients.put(event.getId(), event);
            
            //If it is a single job fine. If it has many subJobs then the num of subJobs should not be greater than the number of neighbouring nodes.
            if(event.isSingular() || event.getNumJobs() <= neighbours.size()){
	            numProbesPerJob.put(event.getId(), Math.min(NUM_PROBES*event.getNumJobs(), neighbours.size()));
	            
	            if(numProbesPerJob.get(event.getId()) != 0){
	            	Snapshot.report(Snapshot.INI + Snapshot.S + event.getId() + Snapshot.S + System.currentTimeMillis());
	            }
	            for(int i=0; i< numProbesPerJob.get(event.getId()); i++){
	            	
	            	int index = (int) Math.round(Math.random()*(copyNeighbourList.size()-1));
	            	RequestResources.Request req = new RequestResources.Request(self, copyNeighbourList.get(index), event.getId(), event.getNumCpus(), event.getMemoryInMbs());
	            	copyNeighbourList.remove(index);
	            	trigger(req, networkPort);     	
	            }
            }
        }
    };
    
    
    
    /**
     *  Role of Worker. Listening incoming ResourceAllocationRequests from Schedulers that probe this Worker.
     */
    Handler<RequestResources.Request> handleResourceAllocationRequest = new Handler<RequestResources.Request>() {
        @Override
        public void handle(RequestResources.Request event) {
        	//System.out.println("Request incoming for job with id = "+ event.getJobID());
        	boolean eval = (availableResources.getFreeMemInMbs()>= event.getAmountMemInMb()) && 
        				   (availableResources.getNumFreeCpus() >= event.getNumCpus());
        	trigger(new RequestResources.Response(self, event.getSource(),event.getJobID(), eval,queuedJobs.size()),networkPort);
        }
    };
    
    /**
     *  Role of Scheduler. Listening for the Probes that were previously sent. When all probes for a Job have arrived then 
     *  the scheduling decision should be made by the scheduler to the least loaded node according to the size of the queue.
     *  
     */
    Handler<RequestResources.Response> handleResourceAllocationResponse = new Handler<RequestResources.Response>() {
        @Override
        public void handle(RequestResources.Response event) {
            
        	//System.out.println("Response incoming for job with id = "+ event.getJobID() + " was " + event.isSuccessful());
            
        	List<RequestResources.Response>  list =  probesReceived.get(event.getJobID());
            
        	if(list==null){
            	list = new ArrayList<RequestResources.Response>();
            	probesReceived.put(event.getJobID(), list);
            }
            list.add(event);
                        
            if(list.size()== numProbesPerJob.get(event.getJobID())){
            	SuperJob superJob = jobsFromClients.get(event.getJobID());
            	
            	// When removing events from the list, we are guaranteed to have enough elements
            	// to not exhaust the list in the loop. Because we reject scheduling requests from apps
            	// when they request more than the available neighbors.
            	for(int i=0; i<superJob.getNumJobs(); i++){
            	   	RequestResources.Response minLoadResponse = Collections.min(list);
            	   	list.remove(minLoadResponse);
                	Address selectedPeer = minLoadResponse.getSource();
                	RequestResources.ScheduleJob schJob = new RequestResources.ScheduleJob(self, selectedPeer, jobsFromClients.get(event.getJobID()));
                	trigger(schJob,networkPort);
                	jobsFromClients.remove(event.getJobID());
            	}
         
            }
            
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

        	Snapshot.report(Snapshot.PRB + Snapshot.S + job.getId() + Snapshot.S + System.currentTimeMillis());
        	if(!scheduleJob(job)){
        		queuedJobs.add(job);
        	}
        }
    };
    
    private boolean scheduleJob(SuperJob job){
    	boolean success = availableResources.allocate(job.getNumCpus(), job.getMemoryInMbs());
    	if(success){
    		runningJobs.add(job);
    		Snapshot.report(Snapshot.SCH + Snapshot.S + job.getId() + Snapshot.S + System.currentTimeMillis());
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
        			Snapshot.report(Snapshot.TER + Snapshot.S + event.getJobID() + Snapshot.S + System.currentTimeMillis());
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

    Handler<TManSample> handleTManSample = new Handler<TManSample>() {
        @Override
        public void handle(TManSample event) {
           System.err.println("It fucking works. Fuck yeah!");
        }
    };
}