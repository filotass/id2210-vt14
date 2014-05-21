package tman.system.peer.tman;

import common.configuration.TManConfiguration;
import common.peer.AvailableResources;

import common.simulation.scenarios.Experiment;
import cyclon.system.peer.cyclon.PeerDescriptor;

import java.util.ArrayList;

import cyclon.system.peer.cyclon.CyclonSample;
import cyclon.system.peer.cyclon.CyclonSamplePort;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
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
    private ArrayList<PeerDescriptor>  tmanPartners;
    private TManConfiguration   tmanConfiguration;
    private Random              r;
    private AvailableResources  availableResources;
    
    
    private ArrayList<PeerDescriptor> gradientCPU;
    private ArrayList<PeerDescriptor> gradientMem;
    private ArrayList<PeerDescriptor> gradientAvR;
    
    



    public class TManSchedule extends Timeout {

        public TManSchedule(SchedulePeriodicTimeout request) {
            super(request);
        }

        public TManSchedule(ScheduleTimeout request) {
            super(request);
        }
    }

    public TMan() {
        tmanPartners = new ArrayList<PeerDescriptor>();

        subscribe(handleInit, control);
        subscribe(handleRound, timerPort);
        subscribe(handleCyclonSample, cyclonSamplePort);
        subscribe(handleTManPartnersResponse, networkPort);
        subscribe(handleTManPartnersRequest, networkPort);

    }

    
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
            Snapshot.updateTManPartners(new PeerDescriptor(self,availableResources), tmanPartners);

            // Publish sample to connected components
            trigger(new TManSample(tmanPartners), tmanPort);
        }
    };

    Handler<CyclonSample> handleCyclonSample = new Handler<CyclonSample>() {
    	
    	/*Timed event every T time units*/

        @Override
        public void handle(CyclonSample event) {
            List<PeerDescriptor> cyclonPartners = event.getSample();

            tmanPartners.clear();
            tmanPartners.addAll(cyclonPartners);
            
    		/* handle 	q =	gradientView.selectPeer()
        	myDescriptor = (myAddress, 	myProfile)
        	buf = merge(gradientView, myDescriptor)
        	buf = merge(buf,rnd.view)
        	send buf to q
        	recv bufq from q
        	buf = merge(bufq,gradientView)
        	gradientView = selectView(buf)*/
            

            PeerDescriptor selfPeerDescriptor = new PeerDescriptor(self, availableResources);
            constructGradientAndGossip(gradientCPU, new ComparatorByCPU(selfPeerDescriptor));
            constructGradientAndGossip(gradientMem, new ComparatorByMem(selfPeerDescriptor));
            constructGradientAndGossip(gradientAvR, new ComparatorByAvailableResources(selfPeerDescriptor));
            

        }
    };
    
    
    private void constructGradientAndGossip(ArrayList<PeerDescriptor> gradient, Comparator<? super PeerDescriptor> comparator){
    	//WHO TO GOSHIP. Randomly selected from cyclon sample.
    	int index = (int) Math.round(Math.random()*(tmanPartners.size()-1));

        // We call X times the softmax method where X we define as half of the number of neighbours
        gradient = new ArrayList<PeerDescriptor>();
        for(int i=0; i< tmanPartners.size()/2; i++){
        	gradient.add(getSoftMaxAddress(tmanPartners, comparator));
        }
        TManAddressBuffer tmanBuffer = new TManAddressBuffer(self, gradient); 
        
        //WHAT TO GOSHIP. We send a list of Descriptors of peers from calling the softmax
        new ExchangeMsg.Request(UUID.randomUUID(), tmanBuffer, self,tmanPartners.get(index).getAddress());
    }

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
        
        	ArrayList<PeerDescriptor> temp = new ArrayList<PeerDescriptor>();
        	temp.addAll(tmanPartners);
        	temp.add(new PeerDescriptor(self, availableResources));
        	TManAddressBuffer buf = new TManAddressBuffer(self, temp);
    
        	//Get a Random View ==> It is a random sample of nodes from the network using CYCLON
        	ExchangeMsg.Response responseMsg = new ExchangeMsg.Response(event.getRequestId(), buf, self, event.getSource());
        	trigger(responseMsg, tmanPort);
        
        	temp.clear();
        	temp.addAll(tmanPartners);
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
    public PeerDescriptor getSoftMaxAddress(List<PeerDescriptor> entries, Comparator<? super PeerDescriptor> comparator) {
        Collections.sort(entries, comparator);

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
            // normalize the probability for this entry
            double normalisedUtility = values[i] / total;
            if (normalisedUtility >= rnd) {
                return entries.get(i);
            }
        }
        return entries.get(entries.size() - 1);
    }

}
