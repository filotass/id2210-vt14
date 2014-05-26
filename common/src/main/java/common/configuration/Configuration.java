package common.configuration;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import common.simulation.scenarios.Experiment;

import se.sics.kompics.address.Address;
import se.sics.kompics.p2p.bootstrap.BootstrapConfiguration;

public class Configuration {

    public static int SNAPSHOT_PERIOD = 1000;
    public static int AVAILABLE_TOPICS = 20;
    public InetAddress ip = null;

    {
        try {
            ip = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
        }
    }
    int webPort = 8080;
    int bootId = Integer.MAX_VALUE;
    int networkPort = 8081;
    Address bootServerAddress = new Address(ip, networkPort, bootId);
    final long seed;
    BootstrapConfiguration bootConfiguration = new BootstrapConfiguration(bootServerAddress, 60000, 4000, 3, 30000, webPort, webPort);
    CyclonConfiguration cyclonConfiguration;
    TManConfiguration tmanConfiguration;
    RmConfiguration searchConfiguration;

    public Configuration(long seed, Experiment e) throws IOException {
        this.seed = seed;
        searchConfiguration = new RmConfiguration(seed);
        tmanConfiguration = new TManConfiguration(seed, 50, 1);
        cyclonConfiguration = new CyclonConfiguration(seed, 5, 10, 400, 500000,(long) (Integer.MAX_VALUE - Integer.MIN_VALUE), 20);
        
        System.setProperty(Experiment.OUTFILE, e.getValue(Experiment.OUTFILE));
        System.setProperty(Experiment.NUM_OF_PROBES, e.getValue(Experiment.NUM_OF_PROBES));
        System.setProperty(Experiment.NUM_OF_JOBS, e.getValue(Experiment.NUM_OF_JOBS));
        System.setProperty(Experiment.NUM_OF_NODES, e.getValue(Experiment.NUM_OF_NODES));
        System.setProperty(Experiment.NUMBER_OF_CPUS_PER_JOB, e.getValue(Experiment.NUMBER_OF_CPUS_PER_JOB));
        System.setProperty(Experiment.NUMBER_OF_CPUS_PER_NODE, e.getValue(Experiment.NUMBER_OF_CPUS_PER_NODE));
        System.setProperty(Experiment.NUMBER_OF_MBS_PER_JOB, e.getValue(Experiment.NUMBER_OF_MBS_PER_JOB));
        System.setProperty(Experiment.NUMBER_OF_MBS_PER_NODE, e.getValue(Experiment.NUMBER_OF_MBS_PER_NODE));
        System.setProperty(Experiment.NUMBER_OF_TASKS_PER_JOB, e.getValue(Experiment.NUMBER_OF_TASKS_PER_JOB));
        System.setProperty(Experiment.JOB_DURATION, e.getValue(Experiment.JOB_DURATION));
        System.setProperty(Experiment.TMAN_C, e.getValue(Experiment.TMAN_C));
        
        String c = File.createTempFile("bootstrap.", ".conf").getAbsolutePath();
        bootConfiguration.store(c);
        System.setProperty("bootstrap.configuration", c);

        c = File.createTempFile("cyclon.", ".conf").getAbsolutePath();
        cyclonConfiguration.store(c);
        System.setProperty("cyclon.configuration", c);

        c = File.createTempFile("tman.", ".conf").getAbsolutePath();
        tmanConfiguration.store(c);
        System.setProperty("tman.configuration", c);

        c = File.createTempFile("rm.", ".conf").getAbsolutePath();
        searchConfiguration.store(c);
        System.setProperty("rm.configuration", c);

    }
}
