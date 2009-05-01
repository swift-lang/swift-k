//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 13, 2008
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.LocalTCPService;
import org.globus.cog.abstraction.coaster.service.job.manager.Worker.ShutdownCallback;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.impl.common.task.ExecutionServiceImpl;
import org.globus.cog.abstraction.impl.common.task.ExecutionTaskHandler;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;

public class WorkerManager extends Thread {
    public static final Logger logger = Logger.getLogger(WorkerManager.class);

    /**
     * We allow for at least one minute of extra time compared to the requested
     * walltime
     */
    public static final Seconds TIME_RESERVE = new Seconds(60);

    public static final File scriptDir =
            new File(System.getProperty("user.home") + File.separator + ".globus" + File.separator
                    + "coasters");

    public static final String SCRIPT = "worker.pl";

    public static final int OVERALLOCATION_FACTOR = 10;

    public static final int MAX_WORKERS = 2048;
    public static final int MAX_STARTING_WORKERS = 32;

    public static final List coasterAttributes =
            Arrays.asList(new String[] { "coasterspernode", "coasterinternalip",
                    "coasterworkermaxwalltime", "coastermaxjobs" });

    private SortedMap ready;
    private Map ids;
    private Set busy;
    private Set startingTasks;
    private Map requested;
    private boolean shutdownFlag;
    private LinkedList allocationRequests;
    private File script;
    private IDGenerator sr;
    private URI callbackURI;
    private LocalTCPService localService;
    private TaskHandler handler;
    private int currentWorkers, maxJobs;
    private Set workerTasks;

    public WorkerManager(LocalTCPService localService) throws IOException {
        super("Worker Manager");
        ready = new TreeMap();
        busy = new HashSet();
        ids = new HashMap();
        startingTasks = new HashSet();
        requested = new HashMap();
        allocationRequests = new LinkedList();
        this.localService = localService;
        this.callbackURI = localService.getContact();
        writeScript();
        sr = new IDGenerator();
        handler = new ExecutionTaskHandler();
        workerTasks = new HashSet();
    }

    private void writeScript() throws IOException {
        scriptDir.mkdirs();
        if (!scriptDir.exists()) {
            throw new IOException("Failed to create script dir (" + scriptDir + ")");
        }
        script = File.createTempFile("cscript", ".pl", scriptDir);
        script.deleteOnExit();
        InputStream is = WorkerManager.class.getClassLoader().getResourceAsStream(SCRIPT);
        if (is == null) {
            throw new IOException("Could not find resource in class path: " + SCRIPT);
        }
        FileOutputStream fos = new FileOutputStream(script);
        byte[] buf = new byte[1024];
        int len = is.read(buf);
        while (len != -1) {
            fos.write(buf, 0, len);
            len = is.read(buf);
        }
        fos.close();
        is.close();
    }

    public void run() {
        try {
            if (logger.isInfoEnabled()) {
                startInfoThread();
            }
            AllocationRequest req;
            while (!shutdownFlag) {
                synchronized (allocationRequests) {
                    while (allocationRequests.isEmpty()) {
                        allocationRequests.wait();
                    }
                    req = (AllocationRequest) allocationRequests.removeFirst();
                    if (logger.isInfoEnabled()) {
                        logger.info("Got allocation request: " + req);
                    }
                }

                try {
                    startWorker(new Seconds(req.maxWallTime.getSeconds()).multiply(
                        OVERALLOCATION_FACTOR).add(TIME_RESERVE), req.prototype);
                }
                catch (NoClassDefFoundError e) {
                    req.prototype.setStatus(new StatusImpl(Status.FAILED, e.getMessage(),
                        new TaskSubmissionException(e)));
                }
                catch (Exception e) {
                    req.prototype.setStatus(new StatusImpl(Status.FAILED, e.getMessage(), e));
                }
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (Error e) {
            e.printStackTrace();
            System.exit(126);
        }
    }

    private void startWorker(Seconds maxWallTime, Task prototype)
            throws InvalidServiceContactException, InvalidProviderException,
            ProviderMethodException {

        int numWorkers = this.getCoastersPerNode(prototype);

        String workerMaxwalltimeString =
                (String) ((JobSpecification) prototype.getSpecification()).getAttribute("coasterWorkerMaxwalltime");

        if (workerMaxwalltimeString != null) {
            // override the computed maxwalltime
            maxWallTime = new Seconds(WallTime.timeToSeconds(workerMaxwalltimeString));
            int taskSeconds = AssociatedTask.getMaxWallTime(prototype).getSeconds();
            if (TIME_RESERVE.add(taskSeconds).isGreaterThan(maxWallTime)) {
                prototype.setStatus(new StatusImpl(Status.FAILED,
                    "Job cannot be run with the given max walltime worker constraint (task: "
                            + taskSeconds + ", maxwalltime: " + maxWallTime + ")", null));
                return;
            }
            logger.debug("Overridden worker maxwalltime is " + maxWallTime);
        }

        logger.info("Starting new worker set with " + numWorkers + " workers");

        Task t = new TaskImpl();
        t.setType(Task.JOB_SUBMISSION);
        t.setSpecification(buildSpecification(prototype));
        copyAttributes(t, prototype, maxWallTime);
        t.setRequiredService(1);
        t.setService(0, buildService(prototype));
        synchronized (this) {
            if (!startingTasks.contains(prototype)) {
                return;
            }
        }

        Map newlyRequested = new HashMap();
        for (int n = 0; n < numWorkers; n++) {
            int id = sr.nextInt();
            if (logger.isInfoEnabled()) {
                logger.info("Starting worker with id=" + id + " and maxwalltime=" + maxWallTime);
            }
            String sid = String.valueOf(id);

            ((JobSpecification) t.getSpecification()).addArgument(sid);

            try {
                Worker wr = new Worker(this, sid, maxWallTime, t, prototype);
                newlyRequested.put(sid, wr);
            }
            catch (Exception e) {
                prototype.setStatus(new StatusImpl(Status.FAILED, e.getMessage(), e));
            }
        }
        try {
            handler.submit(t);
        }
        catch (Exception e) {
            prototype.setStatus(new StatusImpl(Status.FAILED, e.getMessage(), e));
        }
        synchronized (this) {
            workerTasks.add(t);
            requested.putAll(newlyRequested);
        }
    }

    private JobSpecification buildSpecification(Task prototype) {
        JobSpecification ps = (JobSpecification) prototype.getSpecification();
        JobSpecification js = new JobSpecificationImpl();
        js.setExecutable("/usr/bin/perl");
        js.addArgument(script.getAbsolutePath());

        String internalHostname = (String) ps.getAttribute("coasterInternalIP");

        if (internalHostname != null) { // override automatically determined
            // hostname
            // TODO detect if we've done this already for a different
            // value? (same non-determinism as for coastersPerWorker and
            // walltime handling that jobs may come in with different
            // values and we can only use one)
            try {
                logger.warn("original callback URI is " + callbackURI.toString());
                callbackURI =
                        new URI(callbackURI.getScheme(), callbackURI.getUserInfo(),
                            internalHostname, callbackURI.getPort(), callbackURI.getPath(),
                            callbackURI.getQuery(), callbackURI.getFragment());
                logger.warn("callback URI has been overridden to " + callbackURI.toString());
            }
            catch (URISyntaxException use) {
                throw new RuntimeException(use);
            }
            // TODO nasty exception in the line above
        }
        js.addArgument(callbackURI.toString());

        // js.addArgument(id);
        return js;
    }

    public static ExecutionService buildService(Task prototype)
            throws InvalidServiceContactException, InvalidProviderException,
            ProviderMethodException {
        ExecutionService s = new ExecutionServiceImpl();
        s.setServiceContact(prototype.getService(0).getServiceContact());
        ExecutionService p = (ExecutionService) prototype.getService(0);
        String jm = p.getJobManager();
        int colon = jm.indexOf(':');
        // remove provider used to bootstrap coasters
        jm = jm.substring(colon + 1);
        colon = jm.indexOf(':');
        if (colon == -1) {
            s.setProvider(jm);
        }
        else {
            s.setJobManager(jm.substring(colon + 1));
            s.setProvider(jm.substring(0, colon));
        }
        if (p.getSecurityContext() != null) {
            s.setSecurityContext(p.getSecurityContext());
        }
        else {
            s.setSecurityContext(AbstractionFactory.newSecurityContext(s.getProvider()));
        }
        return s;
    }

    private void copyAttributes(Task t, Task prototype, Seconds maxWallTime) {
        JobSpecification pspec = (JobSpecification) prototype.getSpecification();
        JobSpecification tspec = (JobSpecification) t.getSpecification();
        Iterator i = pspec.getAttributeNames().iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            if (!coasterAttributes.contains(name)) {
                tspec.setAttribute(name, pspec.getAttribute(name));
            }
        }
        tspec.setAttribute("maxwalltime", new WallTime((int) maxWallTime.getSeconds()).format());
    }

    private int k;
    private long last;

    public Worker request(WallTime maxWallTime, Task prototype) throws InterruptedException {
        WorkerKey key =
                new WorkerKey(new Seconds(maxWallTime.getSeconds()).add(TIME_RESERVE).add(
                    Seconds.now()));
        Worker w = null;
        synchronized (this) {
            if (logger.isInfoEnabled()) {
                logger.info("Looking for worker for key " + key);
                logger.info("Ready: " + ready);
            }
            Collection tm = ready.tailMap(key).values();
            Iterator i = tm.iterator();

            if (i.hasNext()) {
                w = (Worker) i.next();
                i.remove();
                if (!w.isFailed()) {
                    busy.add(w);
                    startingTasks.remove(prototype);
                }
                else {
                    removeWorker(w);
                }
            }
        }

        if (w != null) {
            if (k == 0) {
                last = System.currentTimeMillis();
            }
            if (++k % 100 == 0) {
                long crt = System.currentTimeMillis();
                int js = 0;
                if (last != 0) {
                    js = (int) (80000 / (crt - last));
                }
                last = crt;
                System.err.println(" " + k / 80 + "; " + js + " J/s");
            }
            logger.info("Using worker " + w + " for task " + prototype);
            w.setRunning(prototype);
            return w;
        }
        else {
            synchronized (this) {
                maxJobs = getMaxJobs(prototype);
                synchronized (allocationRequests) {
                    if (currentWorkers + allocationRequests.size() >= MAX_WORKERS) {
                        this.wait(250);
                        return null;
                    }
                    if (workerTasks.size() + allocationRequests.size() + 1 >= maxJobs) {
                        logger.info("Maximum worker jobs reached (" + maxJobs + ")");
                        this.wait(250);
                        return null;
                    }
                    boolean alreadyThere;
                    alreadyThere = !startingTasks.add(prototype);

                    if (!alreadyThere) {
                        if (logger.isInfoEnabled()) {
                            logger.info("No suitable worker found. Attempting to start a new one.");
                        }

                        if (allocationRequests.size() < MAX_STARTING_WORKERS) {
                            allocationRequests.add(new AllocationRequest(maxWallTime, prototype));
                            allocationRequests.notify();
                        }
                        else {
                            this.wait(250);
                            return null;
                        }
                    }
                }
            }
            return null;
        }
    }

    public void workerTerminated(Worker worker) {
        if (logger.isInfoEnabled()) {
            logger.info("Worker terminated: " + worker);
        }
        Status s = worker.getStatus();
        Task running = worker.getRunning();
        synchronized (this) {
            workerTasks.remove(worker.getWorkerTask());
            if (s.getStatusCode() == Status.FAILED || requested.containsKey(worker.getId())) {
                if (logger.isInfoEnabled()) {
                    logger.info("Failed or starting: " + worker);
                }
                worker.setFailed(true);
                if (s.getStatusCode() != Status.FAILED) {
                    worker.setStatus(new StatusImpl(Status.FAILED, "Worker ended prematurely", null));
                }
                running.setStatus(new StatusImpl(Status.FAILED, "Failed to start worker: "
                        + s.getMessage(), s.getException()));
            }
            else if (running != null) {
                running.setStatus(new StatusImpl(Status.FAILED,
                    "Worker terminated while job was running", null));
            }
            removeWorker(worker);
        }
    }

    public void registrationReceived(String id, String url, ChannelContext cc) {
        Worker wr;
        synchronized (this) {
            wr = (Worker) requested.remove(id);
        }
        if (wr == null) {
            logger.warn("Received unrequested registration (id = " + id + ", url = " + url);
            throw new IllegalArgumentException("Invalid worker id (" + id
                    + "). This worker manager instance does not "
                    + "recall requesting a worker with such an id.");
        }
        wr.setScheduledTerminationTime(Seconds.now().add(wr.getMaxWallTime()));
        wr.setChannelContext(cc);
        if (logger.isInfoEnabled()) {
            logger.info("Worker registration received: " + wr);
        }
        synchronized (this) {
            startingTasks.remove(wr.getRunning());
            ready.put(new WorkerKey(wr), wr);
            ids.put(id, wr);
            currentWorkers++;
            wr.workerRegistered();
        }
    }

    public void removeWorker(Worker worker) {
        synchronized (this) {
            if (busy.remove(worker)) {
                if (logger.isInfoEnabled()) {
                    logger.info(worker + " was busy");
                }
            }
            if (ready.remove(new WorkerKey(worker)) != null) {
                if (logger.isInfoEnabled()) {
                    logger.info(worker + " was ready");
                }
            }
            startingTasks.remove(worker.getRunning());
            requested.remove(worker.getId());
            if (ids.remove(worker.getId()) != null) {
                currentWorkers--;
            }
        }
    }

    public void workerTaskDone(Worker wr) {
        synchronized (this) {
            // only add to ready if it was not removed previously
            // as this method may be called after the worker task is done
            if (busy.remove(wr)) {
                ready.put(new WorkerKey(wr), wr);
            }
            notifyAll();
            wr.setRunning(null);
        }
    }

    public int availableWorkers() {
        synchronized (this) {
            return ready.size();
        }
    }

    public ChannelContext getChannelContext(String id) {
        Worker wr = (Worker) ids.get(id);
        if (wr == null) {
            throw new IllegalArgumentException("No worker with id=" + id);
        }
        else {
            return wr.getChannelContext();
        }
    }

    protected TaskHandler getTaskHandler() {
        return handler;
    }

    protected int getCoastersPerNode(Task t) {
        String numWorkersString =
                (String) ((JobSpecification) t.getSpecification()).getAttribute("coastersPerNode");

        if (numWorkersString == null) {
            return 1;
        }
        else {
            return Integer.parseInt(numWorkersString);
        }
    }
    
    protected int getMaxJobs(Task t) {
        String s = (String) ((JobSpecification) t.getSpecification()).getAttribute("coasterMaxJobs");
        if (s == null) {
            return Integer.MAX_VALUE;
        }
        else {
            return Integer.parseInt(s);
        }
    }

    private static class AllocationRequest {
        public WallTime maxWallTime;
        public Task prototype;

        public AllocationRequest(WallTime maxWallTime, Task prototype) {
            this.maxWallTime = maxWallTime;
            this.prototype = prototype;
        }
    }

    public void shutdown() {
        try {
            synchronized (this) {
                Iterator i;
                List callbacks = new ArrayList();
                // wr.shutdown removes the worker from this manager, which
                // messes
                // up with the iteration
                i = new ArrayList(ready.values()).iterator();
                while (i.hasNext()) {
                    Worker wr = (Worker) i.next();
                    callbacks.add(wr.shutdown());
                }
                i = callbacks.iterator();
                while (i.hasNext()) {
                    ShutdownCallback cb = (ShutdownCallback) i.next();
                    if (cb != null) {
                        cb.waitFor();
                    }
                }
                i = new ArrayList(requested.values()).iterator();
                while (i.hasNext()) {
                    Worker wr = (Worker) i.next();
                    try {
                        handler.cancel(wr.getWorkerTask());
                    }
                    catch (Exception e) {
                        logger.warn("Failed to cancel queued worker task " + wr.getWorkerTask(), e);
                    }
                }
            }
        }
        catch (InterruptedException e) {
            logger.warn("Interrupted", e);
        }
    }

    private void startInfoThread() {
        new Thread() {
            {
                setDaemon(true);
            }

            public void run() {
                while (true) {
                    try {
                        Thread.sleep(20000);
                        synchronized (WorkerManager.this) {
                            logger.info("Current workers: " + currentWorkers);
                            logger.info("Ready: " + ready);
                            logger.info("Busy: " + busy);
                            logger.info("Requested: " + requested);
                            logger.info("Starting: " + startingTasks);
                            logger.info("Ids: " + ids);
                        }
                        synchronized (allocationRequests) {
                            logger.info("AllocationR: " + allocationRequests);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}
