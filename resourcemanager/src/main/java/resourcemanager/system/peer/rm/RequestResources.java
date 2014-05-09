package resourcemanager.system.peer.rm;

import java.util.List;

import common.simulation.Job;

import se.sics.kompics.address.Address;
import se.sics.kompics.network.Message;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;

/**
 * User: jdowling
 */
public class RequestResources  {

    public static class Request extends Message {

    	private final long jobID;
        private final int numCpus;
        private final int amountMemInMb;

        public Request(Address source, Address destination, long jobID, int numCpus, int amountMemInMb) {
            super(source, destination);
            this.jobID = jobID;
            this.numCpus = numCpus;
            this.amountMemInMb = amountMemInMb;
        }

        public int getAmountMemInMb() {
            return amountMemInMb;
        }

        public int getNumCpus() {
            return numCpus;
        }
        
        public long getJobID(){
        	return jobID;
        }

    }
    
    public static class Response extends Message implements Comparable<Response>{

    	private final int numOfJobsInQueue;
        private final boolean success;
    	private final long jobID;
        
        public Response(Address source, Address destination, long jobID, boolean success, int numOfJobsInQueue) {
            super(source, destination);
            this.jobID = jobID;
            this.success = success;
            this.numOfJobsInQueue = numOfJobsInQueue;
        }
        
        public boolean isSuccessful(){
        	return success;
        }
        
        public long getJobID(){
        	return jobID;
        }
        
        
        public long getNumOfJobsInQueue(){
        	return numOfJobsInQueue;
        }

		@Override
		public int compareTo(Response other) {
			if(this.numOfJobsInQueue == other.numOfJobsInQueue){
				if(success){
					return -1;
				}
				return 1;
			}
			return this.numOfJobsInQueue - other.numOfJobsInQueue;
		}
    }
    
    public static class RequestTimeout extends Timeout {
        private final Address destination;
        RequestTimeout(ScheduleTimeout st, Address destination) {
            super(st);
            this.destination = destination;
        }

        public Address getDestination() {
            return destination;
        }
    }
    
    public static class ScheduleJob extends Message{
    	private final Job job;
    	
		public ScheduleJob(Address source, Address destination, Job job) {
			super(source, destination);
			this.job = job;
		}
		
		public Job getJob(){
			return job;
		}
    	
    }
}
