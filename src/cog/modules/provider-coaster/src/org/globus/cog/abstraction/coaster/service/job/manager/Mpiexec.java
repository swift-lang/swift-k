package org.globus.cog.abstraction.coaster.service.job.manager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.util.StreamProcessor;
import org.globus.cog.util.Streamer;
import org.globus.cog.util.StringUtil;

/**
 * Construct MPICH/Hydra proxies and submit them back to sleeping Cpus
 * @author wozniak
 */
public class Mpiexec {

    Logger logger = Logger.getLogger(Mpiexec.class);

    List<Job> proxies;

    /**
       The path to mpiexec
     */
    public String MPIEXEC = "mpiexec";

    /**
       The user job
     */
    Job job;

    /**
       The Cpu that pulled this job
     */
    Cpu cpu;

    /**
       The output from mpiexec
     */
    String output;

    /**
       The error from mpiexec
     */
    String error;

    Mpiexec(Cpu cpu, Job job) {
        this.cpu = cpu;
        this.job = job;
        proxies = new ArrayList<Job>(job.cpus);
    }

    boolean launch() {
        try {
            boolean result = runMpiexec();
            if (!result)
                return result;
        }
        catch (IOException e) {
            return false;
        }
        logger.debug("Output from Hydra: \n" + output);
        if (error.length() > 0)
            logger.error("Errors from Hydra: " + error);
        List<String[]> lines = getProxyLines();

        for (int i = 0; i < job.cpus; i++) {
            Job proxy = getProxyJob(job, lines.get(i));
            proxies.add(proxy);
        }

        launchProxies(proxies);
        return true;
    }

    private boolean runMpiexec() throws IOException {
        JobSpecification spec =
            (JobSpecification) job.getTask().getSpecification();
        String[] cmd = commandLine(spec);
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
                                "HYDRA_NONE_DONE:");
        boolean result = waitForHydra(sprocessor, object);
        output = ibytes.toString();
        error  = ebytes.toString();
        return result;
    }

    private String[] commandLine(JobSpecification spec) {
        List<String> cmdl = new ArrayList<String>();

        String hosts = getHydraHostList(job.cpus);

        cmdl.add(MPIEXEC);
        cmdl.add("-bootstrap");
        cmdl.add("manual");
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

    private String getHydraHostList(int cpus) {
        StringBuilder sb = new StringBuilder(cpus*5);
        for (int i = 0; i < cpus; i++) {
            sb.append(i);
            if (i < cpus-1)
                sb.append(",");
        }
        return sb.toString();
    }

    /**
       Wait until Hydra has reported the proxy command lines
       The process does not exit until the proxies exit
     */
    private boolean waitForHydra(StreamProcessor sprocessor,
                                 Object object) {
        synchronized (object) {
            try {
                sprocessor.start();
                while (!sprocessor.matched())
                    object.wait(100000);
            }
            catch (InterruptedException e) {
                logger.error(e.getStackTrace());
                return false;
            }
        }
        return true;
    }

    /**
       Break output into lines and then into words, returning
       only the significant words
     */
    private List<String[]> getProxyLines() {
        List<String[]> result = new ArrayList<String[]>();

        String[] tokens = output.split("\\n");
        for (String token : tokens)
            if (token.startsWith("HYDRA_NONE_LINE:")) {
                String[] args = token.split("\\s");
                String[] line = StringUtil.subset(args, 2);
                result.add(line);
            }

        return result;
    }

    /**
       Replace the user job with a Hydra proxy job
     */
    private Job getProxyJob(Job job, String[] line) {
        Task clone = (Task) job.getTask().clone();

        JobSpecification spec =
            (JobSpecification) clone.getSpecification();

        /*
        List<String> args = spec.getArgumentsAsList();
        if (args.get(0).contains("_swiftwrap"))
            setSwiftWrapArguments(spec, line);
        else  */
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
       Set up the _swiftwrap command line in this JobSpecification
     */
    /*
    private void setSwiftWrapArguments(JobSpecification spec,
                                       String[] line) {
        List<String> tokens = spec.getArgumentsAsList();
        int index;
        index = tokens.indexOf("-e");
        tokens.set(index+1, line[0]);
        index = tokens.indexOf("-a");
        List<String> args = tokens.subList(0, index+1);
        StringUtil.addSome(args, line, 1);
        spec.setArguments(args);
    }
    */


    /**
       This job is not the first proxy sent to this Node
       Modify its command line to prevent file staging operations
     */
    /*
    private void setSwiftWrapSecondary(JobSpecification spec) {
        List<String> tokens = spec.getArgumentsAsList();
        List<String> args = new ArrayList<String>(tokens.size());
        boolean b = true;
        for (String token : tokens)
        {
            if (token.equals("-if") || token.equals("-of"))
            {
                // Copy token and suppress copies of file names
                args.add(token);
                b = false;
            }
            else if (token.startsWith("-"))
            {
                // Resume copies of tokens
                args.add(token);
                b = true;
            }
            else if (b)
            {
                // This is not a file name: copy it
                args.add(token);
            }
        }
        spec.setArguments(args);
    }
    */

    /**
       Launch proxies on this Cpu and several others
       (The LinkedList sleeping should already have enough Cpus)
     */
    private void launchProxies(List<Job> proxies) {
        Block block = cpu.getBlock();
        Set<Node> used = new HashSet<Node>(proxies.size());
        PullThread pthread = Cpu.getPullThread(block);
        int i;
        for (i = 0; i < proxies.size()-1; i++) {
            Job proxy = proxies.get(i);
            Cpu sleeper = pthread.getSleeper();
            submitToCpu(sleeper, proxy, used, i);
        }
        submitToCpu(cpu, proxies.get(i), used, i);
    }

    private void submitToCpu(Cpu cpu, Job proxy, Set<Node> used,
                             int i) {
        Node node = cpu.getNode();
        JobSpecification spec =
            (JobSpecification) proxy.getTask().getSpecification();
        if (! used.add(node))
        {
            // setSwiftWrapSecondary((JobSpecification) spec);
        }
        spec.addEnvironmentVariable("MPI_RANK", i);
        cpu.launch(proxy);
    }
}
