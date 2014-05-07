package resourcemanager.system.peer.rm;

import java.util.List;
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
    
    public static class Response extends Message {

        private final boolean success;
    	private final long jobID;
        
        public Response(Address source, Address destination, long jobID, boolean success) {
            super(source, destination);
            this.jobID = jobID;
            this.success = success;
        }
        
        public boolean isSuccessful(){
        	return success;
        }
        
        public long getJobID(){
        	return jobID;
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
}
