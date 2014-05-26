package tman.system.peer.tman;

import common.configuration.TManConfiguration;
import common.peer.AvailableResources;
import common.simulation.SuperJob;
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
import tman.system.peer.tman.comparators.ComparatorByCOMBO;
import tman.system.peer.tman.comparators.ComparatorByCPU;
import tman.system.peer.tman.comparators.ComparatorByMEM;
import tman.system.peer.tman.comparators.PeerComparator;


public final class TMan extends ComponentDefinition {

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
	private ArrayList<SuperJob> queuedJobs;

	private PeerDescriptor selfPeerDescriptor;

	private Gradient gradientCPU;
	private Gradient gradientMEM;
	private Gradient gradientCOMBO;

	private PeerComparator comparatorCPU;
	private PeerComparator comparatorMEM;
	private PeerComparator comparatorCOMBO;


	private int c = Integer.parseInt(System.getProperty(Experiment.TMAN_C));


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
			queuedJobs = init.getQueueJobs();
			selfPeerDescriptor = new PeerDescriptor(self, availableResources, queuedJobs.size());

			gradientCPU = new Gradient(new ArrayList<PeerDescriptor>(),Gradient.TYPE_CPU);
			gradientMEM = new Gradient(new ArrayList<PeerDescriptor>(),Gradient.TYPE_MEM);
			gradientCOMBO = new Gradient(new ArrayList<PeerDescriptor>(),Gradient.TYPE_COMBO);

			comparatorCPU = new ComparatorByCPU(selfPeerDescriptor);
			comparatorMEM = new ComparatorByMEM(selfPeerDescriptor);
			comparatorCOMBO = new ComparatorByCOMBO(selfPeerDescriptor);


			SchedulePeriodicTimeout rst = new SchedulePeriodicTimeout(period, period);
			rst.setTimeoutEvent(new TManSchedule(rst));
			trigger(rst, timerPort);
		}
	};

	Handler<TManSchedule> handleRound = new Handler<TManSchedule>() {
		@Override
		public void handle(TManSchedule event) {
			Snapshot.updateTManPartners(selfPeerDescriptor, randomView);

			constructGradientAndGossip(gradientCPU);
			constructGradientAndGossip(gradientMEM);
			constructGradientAndGossip(gradientCOMBO);
		}
	};

	Handler<CyclonSample> handleCyclonSample = new Handler<CyclonSample>() {

		/*Timed event every T time units*/

		@Override
		public void handle(CyclonSample event) {
			List<PeerDescriptor> cyclonPartners = event.getSample();
			if(cyclonPartners.size()!=0){
				randomView.clear();
				randomView.addAll(cyclonPartners);
			}
		}
	};


	private void constructGradientAndGossip(Gradient gradient){

		if(gradient.isEmpty()){
			gradient.getEntries().addAll(randomView);
			if(gradient.isEmpty()){
				return;
			}
		}


		//WHO TO GOSHIP. Randomly selected from cyclon sample.
		PeerDescriptor who = selectPeer(gradient);


		ArrayList<PeerDescriptor> bufToSend = new ArrayList<PeerDescriptor>();
		bufToSend.add(selfPeerDescriptor);
		bufToSend.addAll(gradient.getEntries());
		bufToSend.addAll(randomView);
		Utils.removeDuplicates(bufToSend);

		Gradient gradientToSend = new Gradient(bufToSend,gradient.getType());


		TManAddressBuffer tmanBuffer = new TManAddressBuffer(self, gradientToSend); 

		//WHAT TO GOSHIP. We send a list of Descriptors of peers from calling the softmax
		trigger(new ExchangeMsg.Request(UUID.randomUUID(), tmanBuffer, self,who.getAddress()),networkPort);
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

			Gradient gradientReceived = event.getRandomBuffer().getGradient();

			Gradient gradientToRespond = new Gradient(new ArrayList<PeerDescriptor>(), gradientReceived.getType());
			gradientToRespond.addEntry(selfPeerDescriptor);
			gradientToRespond.addEntries(randomView);
			Utils.removeDuplicates(gradientToRespond.getEntries());

			TManAddressBuffer tManAddressBuffer = new TManAddressBuffer(self, gradientToRespond);

			trigger(new ExchangeMsg.Response(event.getRequestId(), tManAddressBuffer, self, event.getSource()),networkPort);

			Gradient relatedGradient = getCorrectGradient(gradientReceived);

			gradientReceived.addEntries(relatedGradient.getEntries());
			relatedGradient.setEntries(selectView(gradientReceived,getComparator(gradientReceived.getType()),c));
			trigger(new TManSample(relatedGradient), tmanPort);

		}
	};



	/**
	 * This node has requested to see availableResources of another resource and
	 * now availableResources are returned to us.
	 */
	Handler<ExchangeMsg.Response> handleTManPartnersResponse = new Handler<ExchangeMsg.Response>() {
		@Override
		public void handle(ExchangeMsg.Response event) {
			Gradient receivedGradient = event.getSelectedBuffer().getGradient();
			Gradient view = getCorrectGradient(receivedGradient);
			receivedGradient.addEntries(view.getEntries());
			view.setEntries(selectView(receivedGradient,getComparator(receivedGradient.getType()), c));

			trigger(new TManSample(view), tmanPort);
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
	public PeerDescriptor getSoftMaxAddress(List<PeerDescriptor> entries, PeerComparator comparator) {
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

	private Gradient getCorrectGradient(Gradient gradientReceived){
		Gradient temp = null;

		if(gradientReceived.getType() == Gradient.TYPE_CPU){
			temp = gradientCPU;

		}else if(gradientReceived.getType() == Gradient.TYPE_MEM){
			temp = gradientMEM;

		}else if(gradientReceived.getType() == Gradient.TYPE_COMBO){
			temp = gradientCOMBO;
		}else{
			System.err.println("ERROR UNKNOWN GRADIENT TYPE");
			System.exit(1);
		}

		return temp;
	}
	
	private PeerComparator getComparator(int type){
		PeerComparator temp = null;

		if(type == Gradient.TYPE_CPU){
			temp = comparatorCPU;

		}else if(type == Gradient.TYPE_MEM){
			temp = comparatorMEM;

		}else if(type == Gradient.TYPE_COMBO){
			temp = comparatorCOMBO;
		}else{
			System.err.println("ERROR UNKNOWN GRADIENT TYPE");
			System.exit(1);
		}

		return temp;
	}

	/**
	 * Get a list of PeerDescriptors (view), and return a random peer from the 
	 * highest ranked half of the list.
	 * 
	 * @param view
	 * @param comparator
	 * @return
	 */
	private PeerDescriptor selectPeer(Gradient gradient) {
		if(gradient.getEntries().size()==0){
			System.err.println("No neighbours: List is empty");
			System.exit(1);
		}

		int add = 0;
		if(gradient.getEntries().size() % 2 == 1){
			add = 1;
		}

		List<PeerDescriptor> halfList = selectView(gradient, getComparator(gradient.getType()), gradient.getEntries().size()/2 +add);

		return halfList.get((int) (Math.random() * halfList.size()));
	}

	/**
	 * Get a list of PeerDescriptors (view) which is supposed to be twice the size
	 * of a partial view. In this process keep half of the input list (the highest
	 * ranked PeerDescriptors)
	 * 
	 * @return
	 */
	private List<PeerDescriptor> selectView(Gradient gradient, PeerComparator comparator, int c) {
		Utils.removeDuplicates(gradient.getEntries());
		c = Math.min(gradient.getEntries().size(), c);
		Collections.sort(gradient.getEntries(), comparator);

		String name = comparator.getClass().getName();
		if(name.equals("tman.system.peer.tman.ComparatorByAvailableResources")){
			System.err.println("==============GRADIENT "+ name +" ========================================");
			for(int i = 0; i < gradient.getEntries().size(); i ++) {
				AvailableResources av = gradient.getEntries().get(i).getAvailableResources();
				System.err.println("ID =" +self.getId()+" CPUs = " + av.getNumFreeCpus() +" MEM = "+av.getFreeMemInMbs()+ " Peer ="+ gradient.getEntries().get(i).getAddress().getId()+ " Size: "+ gradient.getEntries().get(i).getQueueSize());
			}
			System.err.println("==============AFTER "+ name +" ========================================");
		}
		List<PeerDescriptor> returnList = new ArrayList<PeerDescriptor>();



		for(int i = 0; i < c; i ++) {
			returnList.add(gradient.getEntries().get(i));
		}



		//Collections.sort(returnList, gradient.getComparator());
		if(name.equals("tman.system.peer.tman.ComparatorByAvailableResources")){
			for(int i = 0; i < returnList.size(); i ++) {
				AvailableResources av = returnList.get(i).getAvailableResources();
				System.err.println("ID =" +self.getId()+" CPUs = " + av.getNumFreeCpus() +"MEM = "+av.getFreeMemInMbs()+ "peer ="+ returnList.get(i).getAddress().getId()+" Size: "+ gradient.getEntries().get(i).getQueueSize());
			}
		}

		return returnList;
	}

}
