package org.globus.cog.abstraction.coaster.service.job.manager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.execution.coaster.NotificationManager;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.util.ProcessKiller;
import org.globus.cog.util.ProcessListener;
import org.globus.cog.util.ProcessMonitor;
import org.globus.cog.util.StreamProcessor;
import org.globus.cog.util.Streamer;
import org.globus.cog.util.StringUtil;

/**
 * Construct MPICH/Hydra proxies and submit them back to sleeping Cpu
 * There is one Mpiexec instance for each Job that requires MPI
 * @author wozniak
 */
public class Mpiexec implements ProcessListener, StatusListener {

    public static final Logger logger =
        Logger.getLogger(Mpiexec.class);

    /**
       The path to mpiexec
     */
    public static String MPIEXEC = "mpiexec";

    private List<Job> proxies = null;

    /**
       The original user job
     */
    private final Job job;

    /**
       Map from Status code to count of those codes received
     */
    private final Map<Integer,Integer> statusCount =
        new HashMap<Integer,Integer>();

    /*
       The Block containing the Cpus that will run this Job
       TODO: Use this to ensure all Cpus are in this Block
     */
    //private final Block block;

    /**
       The Cpus that will run this Job
    */
    private final List<Cpu> cpus;

    /**
       The output from mpiexec
     */
    private String output;

    /**
       The error from mpiexec
     */
    private String error;

    /**
       The provider on which this task will be run
       Essentially, there is the local SMP mode and the remote
       host/TCP mode (currently, only local SMP works)
     */
    private String provider;

    Mpiexec(List<Cpu> cpus, Job job) {
        this.cpus = cpus;
        this.job = job;
        assert(cpus.size() > 0);
        // this.block = cpus.get(0).getBlock();
        proxies = new ArrayList<Job>(job.cpus);

        // Get the provider
        String p = job.getTask().getService(0).getProvider();
        provider = p.toLowerCase();

        logger.debug("start: " + job + " provider: " + provider);
    }

    boolean launch() {
        try {
            boolean result = runMpiexec();
            logger.debug("Output from Hydra: \n" + output);
            if (error.length() > 0)
                logger.error("Errors from Hydra:\n" + error);
            if (!result)
                return result;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        List<String[]> lines = getProxyLines();
        for (int i = 0; i < job.cpus; i++) {
            Job proxy = getProxyJob(lines.get(i), i);
            proxies.add(proxy);
        }

        launchProxies();
        return true;
    }

    private boolean runMpiexec() throws IOException {
        Task task = job.getTask();
        String[] cmd = commandLine(task);
        Process process = Runtime.getRuntime().exec(cmd);
        InputStream istream = process.getInputStream();
        InputStream estream = process.getErrorStream();
        ByteArrayOutputStream ibytes = new ByteArrayOutputStream();
        ByteArrayOutputStream ebytes = new ByteArrayOutputStream();
        Streamer streamer = new Streamer(estream, ebytes);
        streamer.start();
        Object object = new Object();
        StreamProcessor sprocessor =
            new StreamProcessor(istream, ibytes, object,
                                "HYDRA_LAUNCH_END");
        monitor(process);
        boolean result = waitForHydra(sprocessor, object);
        output = ibytes.toString();
        error  = ebytes.toString();
        return result;
    }

    private String[] commandLine(Task task) {
        JobSpecification spec =
            (JobSpecification) task.getSpecification();
        List<String> cmdl = new ArrayList<String>();

        logger.debug(spec.toString());

        String hosts = getHydraHostList(task);

        cmdl.add(MPIEXEC);
        cmdl.add("-bootstrap");
        cmdl.add("none");
        cmdl.add("-disable-hostname-propagation");
        cmdl.add("-n");
        cmdl.add(Integer.toString(job.cpus));
        cmdl.add("-hosts");
        cmdl.add(hosts);
        cmdl.add(spec.getExecutable());
        cmdl.addAll(spec.getArgumentsAsList());

        String[] result = new String[cmdl.size()];
        cmdl.toArray(result);

        if (logger.isDebugEnabled())
        {
            String logline = StringUtil.concat(result);
            logger.debug(logline);
        }

        return result;
    }

    private String getHydraHostList(Task task) {
        String result = null;

        ExecutionService service =
            (ExecutionService) task.getService(0);
        String jobManager = service.getJobManager();
        // logger.debug("jobmanager: " + jobManager);

        if (jobManager.equals("local:local"))
            result = getHydraHostListLocal();
        else
            result = getHydraHostListHostnames();
        return result;
    }

    private String getHydraHostListLocal() {
        int count = cpus.size();
        StringBuilder sb = new StringBuilder(count*4);
        for (int i = 0; i < count; i++) {
            sb.append(i);
            if (i < count-1)
                sb.append(',');
        }
        String result = sb.toString();
        // logger.trace("getHydraHostListLocal: " + result);
        return result;
    }

    private String getHydraHostListHostnames() {
        StringBuilder sb = new StringBuilder(cpus.size()*16);

        for (int i = 0; i < cpus.size(); i++) {
            Cpu cpu = cpus.get(i);
            Node node = cpu.getNode();
            sb.append(node.getHostname());
            if (i < cpus.size()-1)
                sb.append(',');
        }

        return sb.toString();
    }

    /**
       Time to wait for MPICH output; seconds
     */
    private static final int MPICH_TIMEOUT = 3;

    /**
       Wait until Hydra has reported the proxy command lines
       The process does not exit until the proxies exit
     */
    private boolean waitForHydra(StreamProcessor sprocessor,
                                 Object object) {
        int tries = 0;
        boolean result = false;

        synchronized (object) {
            try {
                sprocessor.start();
                while (!sprocessor.matched() &&
                        tries++ < MPICH_TIMEOUT)
                    object.wait(1000);
            }
            catch (InterruptedException e) {
                logger.error(e.getStackTrace());
            }
        }

        result = sprocessor.matched();
        logger.debug("waitForHydra complete: " + result);
        return result;
    }

    /**
       Break output into lines and then into words, returning
       only the significant words
     */
    private List<String[]> getProxyLines() {
        List<String[]> result = new ArrayList<String[]>();

        String[] lines = output.split("\\n");
        for (String line : lines)
            if (line.startsWith("HYDRA_LAUNCH:")) {
                String[] tokens = line.split("\\s");
                String[] args   = StringUtil.subset(tokens, 1);
                result.add(args);
            }

        return result;
    }

    /**
       Replace the user job with a Hydra proxy job
       New Job.Task.Identity is appended with unique integer
     */
    private Job getProxyJob(String[] line, int i) {
        // Set clone to notify this Mpiexec
        Task clone = (Task) job.getTask().clone();
        clone.addStatusListener(this);

        // Update Task Identity and set Notification
        Identity cloneID = new IdentityImpl(clone.getIdentity());
        String value = cloneID.getValue() + ":" + i;
        cloneID.setValue(value);
        clone.setIdentity(cloneID);
        NotificationManager.getDefault().registerTask(value, clone);

        // Update Task Specification
        JobSpecification spec =
            (JobSpecification) clone.getSpecification();
        spec.setExecutable(line[0]);
        spec.setArguments(StringUtil.concat(line, 1));

        if (logger.isDebugEnabled())
            logger.debug("Proxy job: " +
                         spec.getExecutable() + " " +
                         spec.getArguments());

        Job result = new Job(clone, 1);
        return result;
    }

    /**
     * Set up threads to watch the external process
     * @param process
     */
    private void monitor(Process process) {
        ProcessMonitor monitor = new ProcessMonitor(process, this);
        monitor.start();
        ProcessKiller killer = new ProcessKiller(process, 10000);
        killer.start();
    }

    public void callback(ProcessMonitor monitor) {
        logger.debug("mpiexec exitcode: " + monitor.getExitCode());
    }

    /**
       Launch proxies on this Cpu and several others
       (The LinkedList sleeping should already have enough Cpus)
     */
    private void launchProxies() {
        for (int i = 0; i < proxies.size(); i++)
            submitToCpu(cpus.get(i), proxies.get(i), i);
    }

    private void submitToCpu(Cpu sleeper, Job proxy, int i) {
        assert(sleeper != null);
        JobSpecification spec =
            (JobSpecification) proxy.getTask().getSpecification();
        spec.addEnvironmentVariable("MPI_RANK", i);
        sleeper.launch(proxy);
    }

    /**
       Multiplex Hydra proxy StatusEvents into the StatusEvents for
       the original job
     */
    public void statusChanged(StatusEvent event) {
        logger.debug(event);
        synchronized (statusCount) {
            int code = event.getStatus().getStatusCode();
            Integer count = statusCount.get(code);
            if (count == null)
                count = 1;
            else
                count++;
            statusCount.put(code, count);
            if (count == proxies.size())
                propagate(event);
        }
    }

    private void propagate(StatusEvent event) {
        Status s = event.getStatus();
        logger.debug("propagating: to: " + job + " " + s);
        job.getTask().setStatus(s);
    }
}
