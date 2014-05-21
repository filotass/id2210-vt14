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
    private ArrayList<PeerDescriptor>  randomView;
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
        randomView = new ArrayList<PeerDescriptor>();

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
            Snapshot.updateTManPartners(new PeerDescriptor(self,availableResources), randomView);

            // Publish sample to connected components
            trigger(new TManSample(randomView), tmanPort);
        }
    };

    Handler<CyclonSample> handleCyclonSample = new Handler<CyclonSample>() {
    	
    	/*Timed event every T time units*/

        @Override
        public void handle(CyclonSample event) {
            List<PeerDescriptor> cyclonPartners = event.getSample();

            randomView.clear();
            randomView.addAll(cyclonPartners);
            
    	
            
            checkGradient(gradientCPU);
            checkGradient(gradientMem);
            checkGradient(gradientAvR);
            

            PeerDescriptor selfPeerDescriptor = new PeerDescriptor(self, availableResources);
            constructGradientAndGossip(gradientCPU, new ComparatorByCPU(selfPeerDescriptor),TManAddressBuffer.CPU);
            constructGradientAndGossip(gradientMem, new ComparatorByMem(selfPeerDescriptor), TManAddressBuffer.MEMORY);
            constructGradientAndGossip(gradientAvR, new ComparatorByAvailableResources(selfPeerDescriptor),TManAddressBuffer.AVR);
            

        }
    };
    
    private void checkGradient(List<PeerDescriptor> gradient){
        if(gradient==null){
        	gradient = new ArrayList<PeerDescriptor>();
        	gradient.addAll(randomView);
        }
    }
    
    
    private void constructGradientAndGossip(ArrayList<PeerDescriptor> gradient, Comparator<? super PeerDescriptor> comparator, int type){
    	
    	//WHO TO GOSHIP. Randomly selected from cyclon sample.
    	PeerDescriptor who = selectPeer(gradient,comparator);
  
    	
    	ArrayList<PeerDescriptor> bufToSend = new ArrayList<PeerDescriptor>();
    	bufToSend.add(new PeerDescriptor(self, availableResources));
    	bufToSend.addAll(gradient);
    	bufToSend.addAll(randomView);
    	Utils.removeDuplicates(bufToSend);

        TManAddressBuffer tmanBuffer = new TManAddressBuffer(self, bufToSend, type); 
        
        //WHAT TO GOSHIP. We send a list of Descriptors of peers from calling the softmax
        new ExchangeMsg.Request(UUID.randomUUID(), tmanBuffer, self,who.getAddress());
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
        	
        	ArrayList<PeerDescriptor> bufReceived = event.getRandomBuffer().getAddresses();
        	int gradientType = event.getRandomBuffer().getGradientType();
        	
        	
        	List<PeerDescriptor> gradient = null;
        	Comparator<? super PeerDescriptor> c = null;
        	if(gradientType==TManAddressBuffer.CPU){
        		gradient = gradientCPU;
        		c = new ComparatorByCPU(new PeerDescriptor(self, availableResources));
        	}else if(gradientType==TManAddressBuffer.MEMORY){
        		gradient = gradientMem;
        		c = new ComparatorByMem(new PeerDescriptor(self, availableResources));
        	}else if(gradientType==TManAddressBuffer.AVR){
        		gradient = gradientAvR;
        		c = new ComparatorByAvailableResources(new PeerDescriptor(self, availableResources));
        	}else{
        		System.err.println("ERROR UNKNOWN GRADIENT TYPE");
        		System.exit(1);
        	}
        	
        	bufReceived.addAll(gradient);
        	
        	gradient = selectView(bufReceived, c, 1);

        	
        }
    };

    /**
     * Get a list of PeerDescriptors (view), and return a random peer from the 
     * highest ranked half of the list.
     * 
     * @param view
     * @param comparator
     * @return
     */
    private PeerDescriptor selectPeer(List<PeerDescriptor> view, Comparator<? super PeerDescriptor> comparator) {
    	
    	List<PeerDescriptor> halfList = selectView(view, comparator, view.size()/2);
    	
    	return halfList.get((int) (Math.random() * halfList.size()) - 1);
    }
    
    /**
     * Get a list of PeerDescriptors (view) which is supposed to be twice the size
     * of a partial view. In this process keep half of the input list (the highest
     * ranked PeerDescriptors)
     * 
     * @return
     */
    private List<PeerDescriptor> selectView(List<PeerDescriptor> view, Comparator<? super PeerDescriptor> comparator, int c) {
    	
    	Collections.sort(view, comparator);
    	
    	List<PeerDescriptor> returnList = new ArrayList<PeerDescriptor>();
    	
    	for(int i = 0; i < c; i ++) {
    		returnList.add(view.get(i));
    	}
    	
    	return returnList;
    }
    
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
