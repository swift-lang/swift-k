
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.impl.execution.coaster.NotificationManager;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.util.ProcessListener;
import org.globus.cog.util.ProcessMonitor;
import org.globus.cog.util.StreamProcessor;
import org.globus.cog.util.Streamer;
import org.globus.cog.util.StringUtil;

/**
 * Construct MPICH/Hydra proxies and submit them back to sleeping Cpu
 * There is one Mpiexec instance for each Job that requires MPI
 *
 * @author wozniak
 */
public class Mpiexec implements ProcessListener, ExtendedStatusListener {

    public static final Logger logger = Logger.getLogger(Mpiexec.class);

    /**
       The path to mpiexec
     */
    public static String MPIEXEC = "mpiexec";

    /**
       The original user Job
     */
    private final Job job;

    /**
       The proxy jobs constructed by Mpiexec to satisfy the user Job
     */
    private List<Job> proxies = null;

    /**
       Map from Status code to count of those codes received
     */
    private final Map<Integer, Integer> statusCount = new HashMap<Integer, Integer>();

    /*
       The Block containing the Cpus that will run this Job TODO: Use
       this to ensure all Cpus are in this Block
     */
    // private final Block block;

    /**
       The Cpus that will run this Job.
       The size of this list is the number of Hydra proxies
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
       The provider on which this task will be run Essentially, there
       is the local SMP mode and the remote host/TCP mode
     */
    private final String provider;

    /**
       If non-empty, override the proxy line with this host
       Motivation: The Hydra proxy line contains the hostname to which
       the proxies connect. On some systems, mpiexec produces a
       hostname that the proxies cannot find. mpiexec.host.subst
       allows the user to override this hostname This is a System
       property, use COG_OPTS to set this.
     */
    static String mpiexecHostSubst = "";

    static boolean verbose = false;

    static {
        String v = System.getProperty("mpiexec.host.subst");
        if (v != null && v.length() > 0) {
            mpiexecHostSubst = v;
            logger.debug("Property mpiexec.host.subst="
                    + mpiexecHostSubst);
        }

        v = System.getProperty("mpiexec.verbose");
        if (v != null && v.length() > 0)
            verbose = Boolean.parseBoolean(v);
    }

    Mpiexec(List<Cpu> cpus, Job job) {
        assert(cpus.size() > 0);
    	this.cpus = cpus;
        this.job = job;
        // this.block = cpus.get(0).getBlock();
        proxies = new ArrayList<Job>();

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

        List<String[]> lines = scanProxyLines();
        for (int i = 0; i < cpus.size(); i++) {
            Job proxy = buildProxyJob(lines.get(i), i);
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
        error = ebytes.toString();
        return result;
    }

    /**
       Build the mpiexec command line for the given Task
     */
    private String[] commandLine(Task task) {
        JobSpecification spec = (JobSpecification) task.getSpecification();
        List<String> cmdl = new ArrayList<String>();

        logger.debug(spec.toString());

        String hosts = buildHydraHostList(task);

        cmdl.add(MPIEXEC);
        // Uncomment this for tons of output:
        if (verbose)
        	cmdl.add("-verbose");
        cmdl.add("-launcher");
        cmdl.add("manual");
        cmdl.add("-disable-hostname-propagation");
        cmdl.add("-n");
        cmdl.add(Integer.toString(job.mpiProcesses));
        cmdl.add("-hosts");
        cmdl.add(hosts);
        cmdl.add(spec.getExecutable());
        cmdl.addAll(spec.getArgumentsAsList());

        String[] result = new String[cmdl.size()];
        cmdl.toArray(result);

        if (logger.isDebugEnabled()) {
            String logline = StringUtil.concat(result);
            logger.debug(logline);
        }

        return result;
    }

    private String buildHydraHostList(Task task) {
        String result = null;

        ExecutionService service = (ExecutionService) task.getService(0);
        String jobManager = service.getJobManager();
        logger.debug("jobmanager: " + jobManager);

        //if (jobManager.equals("local:local"))
        //    result = buildHydraHostListLocal();
        //else
        result = buildHydraHostListHostnames();
        return result;
    }

    private String buildHydraHostListLocal() {
        int count = cpus.size();
        StringBuilder sb = new StringBuilder(count * 4);
        for (int i = 0; i < count; i++) {
            sb.append(i);
            if (i < count - 1)
                sb.append(',');
        }
        String result = sb.toString();
        // logger.trace("getHydraHostListLocal: " + result);
        return result;
    }

    private String buildHydraHostListHostnames() {
        StringBuilder sb = new StringBuilder(cpus.size() * 32);

        for (int i = 0; i < cpus.size(); i++) {
            Cpu cpu = cpus.get(i);
            Node node = cpu.getNode();
            sb.append(node.getHostname());
            if (job.mpiPPN > 1) {
            	sb.append(':');
            	sb.append(job.mpiPPN);
            }
            if (i < cpus.size() - 1)
                sb.append(',');
        }

        return sb.toString();
    }

    /**
       Time to wait for MPICH output; seconds
     */
    private static final int MPICH_TIMEOUT = 3;

    /**
       Wait until Hydra has reported the proxy command lines.
       The mpiexec process does not exit until the proxies exit
     */
    private boolean waitForHydra(StreamProcessor sprocessor,
                                 Object object) {
        int tries = 0;
        boolean result = false;

        synchronized (object) {
            try {
                sprocessor.start();
                while (!sprocessor.matched()
                        && tries++ < MPICH_TIMEOUT)
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
       Break output into lines and then into words, returning only the
       significant words
     */
    private List<String[]> scanProxyLines() {
        List<String[]> result = new ArrayList<String[]>();

        String[] lines = output.split("\\n");
        for (String line : lines)
            if (line.startsWith("HYDRA_LAUNCH:")) {
                line = proxyLineSubst(line);
                String[] tokens = line.split("\\s");
                String[] args = StringUtil.subset(tokens, 1);
                result.add(args);
            }

        return result;
    }

    /**
       Pattern to match on for mpiexec.host.subst
     */
    final Pattern controlPortHost = Pattern.compile("--control-port (.*):");

    /**
       Replacement for mpiexec.host.subst
     */
    final String replacement = "--control-port " + mpiexecHostSubst
            + ":";

    /**
       Perform translations on Hydra proxy line here.
       For now, only does mpiexec.host.subst
     */
    String proxyLineSubst(String input) {

        if (mpiexecHostSubst.length() == 0)
            return input;

        Matcher matcher = controlPortHost.matcher(input);
        if (!matcher.find())
            throw new RuntimeException
            ("Proxy line does not contain --control-port !");
        String result = matcher.replaceFirst(replacement);
        return result;
    }

    /**
       Build and register a Hydra proxy job from the Hydra output
       New Job.Task.Identity is appended with unique integer
     */
    private Job buildProxyJob(String[] line, int i) {
        // Clone original job as proxy job
        Task clone = (Task) job.getTask().clone();

        // Update Task Identity and set Notification
        Identity cloneID = new IdentityImpl(clone.getIdentity());
        String value = cloneID.getValue() + ":" + i;
        cloneID.setValue(value);
        clone.setIdentity(cloneID);
        NotificationManager.getDefault().registerListener(value, clone, this);

        // Update Task Specification
        JobSpecification spec = (JobSpecification) clone.getSpecification();
        spec.setExecutable(line[0]);
        spec.setArguments(StringUtil.concat(line, 1));
        // The clone is not an MPI job: it is a part of the MPI job
        spec.removeAttribute("mpi.processes");
        spec.removeAttribute("mpi.ppn");

        if (logger.isDebugEnabled())
            logger.debug("Proxy job: " + spec.getExecutable() + " "
                    + spec.getArguments());

        Job result = new Job(clone, 1);
        return result;
    }

    /**
     * Set up threads to watch the external process
     *
     * @param process
     */
    private void monitor(Process process) {
        ProcessMonitor monitor = new ProcessMonitor(process, this);
        monitor.start();
        // ProcessKiller killer = new ProcessKiller(process, 10000);
        // killer.start();
    }

    public void callback(ProcessMonitor monitor) {
        logger.debug("mpiexec exitcode: " + monitor.getExitCode());
    }

    /**
       Launch proxies on this Cpu and several others (The LinkedList
       sleeping should already have enough Cpus)
     */
    private void launchProxies() {
        for (int i = 0; i < proxies.size(); i++)
            submitToCpu(cpus.get(i), proxies.get(i), i);
    }

    private void submitToCpu(Cpu sleeper, Job proxy, int i) {
        assert (sleeper != null);
        JobSpecification spec =
        	(JobSpecification) proxy.getTask().getSpecification();
        spec.addEnvironmentVariable("MPI_RANK", i);
        sleeper.launch(proxy);
    }

    /**
     * Multiplex Hydra proxy StatusEvents into the StatusEvents for
     * the original job
     */
    public void statusChanged(Status s, String out, String err) {
        logger.debug(s);
        synchronized (statusCount) {
            int code = s.getStatusCode();
            Integer count = statusCount.get(code);
            if (count == null)
                count = 1;
            else
                count++;
            statusCount.put(code, count);
            if (count == proxies.size())
                propagate(s);
        }
    }

    private void propagate(Status s) {
        logger.debug("propagating: to: " + job + " " + s);
        job.getTask().setStatus(s);
    }
}
