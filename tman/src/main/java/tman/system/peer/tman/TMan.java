package tman.system.peer.tman;

import common.configuration.TManConfiguration;
import common.peer.AvailableResources;
import cyclon.system.peer.cyclon.PeerDescriptor;

import java.util.ArrayList;

import cyclon.system.peer.cyclon.CyclonSample;
import cyclon.system.peer.cyclon.CyclonSamplePort;
import cyclon.system.peer.cyclon.DescriptorBuffer;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

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
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;
import tman.simulator.snapshot.Snapshot;

public final class TMan extends ComponentDefinition {

    private static final Logger logger           = LoggerFactory.getLogger(TMan.class);
    Negative<TManSamplePort>    tmanPort         = negative(TManSamplePort.class);
    Positive<CyclonSamplePort>  cyclonSamplePort = positive(CyclonSamplePort.class);
    Positive<Network>           networkPort      = positive(Network.class);
    Positive<Timer>             timerPort        = positive(Timer.class);
    private long                period;
    private Address             self;
    private ArrayList<Address>  tmanPartners;
    private TManConfiguration   tmanConfiguration;
    private Random              r;
    private AvailableResources  availableResources;
    
    private ArrayList<Address> view;


    public class TManSchedule extends Timeout {

        public TManSchedule(SchedulePeriodicTimeout request) {
            super(request);
        }

        public TManSchedule(ScheduleTimeout request) {
            super(request);
        }
    }

    public TMan() {
        tmanPartners = new ArrayList<Address>();

        subscribe(handleInit, control);
        subscribe(handleRound, timerPort);
        subscribe(handleCyclonSample, cyclonSamplePort);
        subscribe(handleTManPartnersResponse, networkPort);
        subscribe(handleTManPartnersRequest, networkPort);
        
        subscribe(handleAvailableResourcesRequest,  networkPort);
        subscribe(handleAvailableResourcesResponse, networkPort);
    }

    
    /**
     * A fellow TMan peer asks us to see our resources. We should be kind enough to reply.
     */
    Handler <AvailableResourcesRequest> handleAvailableResourcesRequest = new Handler<AvailableResourcesRequest>() {
    	
    	@Override
    	public void handle(AvailableResourcesRequest query) {
    		
    		trigger(new AvailableResourcesResponse(self, query.getSource(), availableResources), networkPort);
    	}
    };
    
    /**
     * A TMan peer has answered my request. Now I should save his information.
     */
    Handler <AvailableResourcesResponse> handleAvailableResourcesResponse = new Handler<AvailableResourcesResponse>() {
    	
    	@Override
    	public void handle(AvailableResourcesResponse query) {
    		
    		// Now we know the available resources of this peer
    		Address peer                     = query.getSource();
    		AvailableResources peerResources = query.getAvailableResources();
    	}
    };
    
    Handler<TManInit> handleInit = new Handler<TManInit>() {
        @Override
        public void handle(TManInit init) {
        	
            self = init.getSelf();
            tmanConfiguration = init.getConfiguration();
            period = tmanConfiguration.getPeriod();
            r = new Random(tmanConfiguration.getSeed());
            availableResources = init.getAvailableResources();
            
            SchedulePeriodicTimeout rst = new SchedulePeriodicTimeout(period, period);
            rst.setTimeoutEvent(new TManSchedule(rst));
            trigger(rst, timerPort);
        }
    };
    
    Handler<TManSchedule> handleRound = new Handler<TManSchedule>() {
        @Override
        public void handle(TManSchedule event) {
            Snapshot.updateTManPartners(self, tmanPartners);

            // Publish sample to connected components
            trigger(new TManSample(tmanPartners), tmanPort);
        }
    };

    Handler<CyclonSample> handleCyclonSample = new Handler<CyclonSample>() {
        @Override
        public void handle(CyclonSample event) {
            List<Address> cyclonPartners = event.getSample();

            System.err.println("It works!");

            tmanPartners.addAll(cyclonPartners);
            Utils.removeDuplicates(tmanPartners);
        }
    };

    /**
     * When handling a request,  
     * 
     * TODO: What is view?
     * 
     * @see https://www.kth.se/social/upload/51647982f276546170461c46/4-gossip.pdf
     */
    Handler<ExchangeMsg.Request> handleTManPartnersRequest = new Handler<ExchangeMsg.Request>() {
        @Override
        public void handle(ExchangeMsg.Request event) {
  
        	TManAddressBuffer buf_p = event.getRandomBuffer();
        
        	ArrayList<Address> temp = new ArrayList<Address>();
        	temp.addAll(view);
        	temp.add(self);
        	TManAddressBuffer buf = new TManAddressBuffer(self, temp);
    
        	//Get a Random View ==> It is a random sample of nodes from the network using CYCLON
        	ExchangeMsg.Response responseMsg = new ExchangeMsg.Response(event.getRequestId(), buf, self, event.getSource());
        	trigger(responseMsg, tmanPort);
        
        	temp.clear();
        	temp.addAll(view);
        	temp.addAll(buf_p.getAddresses());
        	
        	
        	buf = new TManAddressBuffer(self, temp);
        	
        	//TODO call c times getSoftMaxAddress();
        	
        

        	
        	
        }
    };

    /**
     * This node has requested to see availableResources of another resource and
     * now availableResources are returned to us.
     */
    Handler<ExchangeMsg.Response> handleTManPartnersResponse = new Handler<ExchangeMsg.Response>() {
        @Override
        public void handle(ExchangeMsg.Response event) {
        	
      
        }
    };

    // TODO - if you call this method with a list of entries, it will
    // return a single node, weighted towards the 'best' node (as defined by
    // ComparatorById) with the temperature controlling the weighting.
    // A temperature of '1.0' will be greedy and always return the best node.
    // A temperature of '0.000001' will return a random node.
    // A temperature of '0.0' will throw a divide by zero exception :)
    // Reference:
    // http://webdocs.cs.ualberta.ca/~sutton/book/2/node4.html
    public Address getSoftMaxAddress(List<Address> entries) {
        Collections.sort(entries, new ComparatorById(self));

        double rnd = r.nextDouble();
        double total = 0.0d;
        double[] values = new double[entries.size()];
        int j = entries.size() + 1;
        for (int i = 0; i < entries.size(); i++) {
            // get inverse of values - lowest have highest value.
            double val = j;
            j--;
            values[i] = Math.exp(val / tmanConfiguration.getTemperature());
            total += values[i];
        }

        for (int i = 0; i < values.length; i++) {
            if (i != 0) {
                values[i] += values[i - 1];
            }
            // normalise the probability for this entry
            double normalisedUtility = values[i] / total;
            if (normalisedUtility >= rnd) {
                return entries.get(i);
            }
        }
        return entries.get(entries.size() - 1);
    }

}
