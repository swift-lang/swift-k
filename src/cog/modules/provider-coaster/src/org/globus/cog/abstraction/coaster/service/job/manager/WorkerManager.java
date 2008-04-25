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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.LocalTCPService;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.ExecutionServiceImpl;
import org.globus.cog.abstraction.impl.common.task.ExecutionTaskHandler;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.FileLocation;
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
    public static final int TIME_RESERVE = 60;

    public static final File scriptDir = new File(System
            .getProperty("user.home")
            + File.separator + ".globus" + File.separator + "coasters");

    public static final String SCRIPT = "worker.pl";

    public static final int OVERALLOCATION_FACTOR = 10;

    public static final int MAX_WORKERS = 256;
    public static final int MAX_STARTING_WORKERS = 32;

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
    private int currentWorkers;

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
    }

    private void writeScript() throws IOException {
        scriptDir.mkdirs();
        if (!scriptDir.exists()) {
            throw new IOException("Failed to create script dir (" + scriptDir
                    + ")");
        }
        script = File.createTempFile("cscript", ".pl", scriptDir);
        script.deleteOnExit();
        InputStream is = WorkerManager.class.getClassLoader()
                .getResourceAsStream(SCRIPT);
        if (is == null) {
            throw new IOException("Could not find resource in class path: "
                    + SCRIPT);
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
            AllocationRequest req;
            while (!shutdownFlag) {
                synchronized (allocationRequests) {
                    while (allocationRequests.isEmpty()) {
                        allocationRequests.wait();
                    }
                    req = (AllocationRequest) allocationRequests
                            .removeFirst();
                    if (logger.isInfoEnabled()) {
                        logger.info("Got allocation request: " + req);
                    }
                }
                try {
                    startWorker(req.maxWallTime.getSeconds()
                            * OVERALLOCATION_FACTOR + TIME_RESERVE,
                            req.prototype);
                }
                catch (Exception e) {
                    req.prototype.setStatus(new StatusImpl(Status.FAILED, e
                            .getMessage(), e));
                }
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startWorker(int maxWallTime, Task prototype)
            throws InvalidServiceContactException {
        int id = sr.nextInt();
        if (logger.isInfoEnabled()) {
            logger.info("Starting worker with id=" + id + " and maxwalltime=" + maxWallTime + "s");
        }
        String sid = String.valueOf(id);
        Task t = new TaskImpl();
        t.setType(Task.JOB_SUBMISSION);
        t.setSpecification(buildSpecification(sid, maxWallTime, prototype));
        copyAttributes(t, prototype);
        t.setRequiredService(1);
        t.setService(0, buildService(prototype));
        try {
            Worker wr = new Worker(this, sid, maxWallTime, t, prototype);
            handler.submit(t);
            synchronized (requested) {
                requested.put(sid, wr);
            }
        }
        catch (Exception e) {
            prototype.setStatus(new StatusImpl(Status.FAILED, e.getMessage(),
                    e));
        }
    }

    private JobSpecification buildSpecification(String id, int maxWallTime,
            Task prototype) {
        JobSpecification js = new JobSpecificationImpl();
        js.setExecutable("/usr/bin/perl");
        js.addArgument(script.getAbsolutePath());
        js.addArgument(id);
        js.addArgument(callbackURI.toString());
        js.setStdOutputLocation(FileLocation.MEMORY);
        js.setStdErrorLocation(FileLocation.MEMORY);
        js.setAttribute("maxwalltime", new WallTime(maxWallTime)
                .getSpecInMinutes());
        return js;
    }

    private ExecutionService buildService(Task prototype)
            throws InvalidServiceContactException {
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
        System.out.println("Worker start provider: " + s.getProvider());
        System.out.println("Worker start JM: " + s.getJobManager());
        return s;
    }

    private void copyAttributes(Task t, Task prototype) {
        Iterator i = prototype.getAttributeNames().iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            t.setAttribute(name, prototype.getAttribute(name));
        }
    }

    private int k;
    private long last;

    public Worker request(WallTime maxWallTime, Task prototype)
            throws InterruptedException {
        WorkerKey key = new WorkerKey(maxWallTime.getSeconds() + TIME_RESERVE
                + now());
        Worker w = null;
        synchronized (this) {
            Collection tm = ready.tailMap(key).values();
            Iterator i = tm.iterator();

            if (i.hasNext()) {
                w = (Worker) i.next();
                i.remove();
                busy.add(w);
                startingTasks.remove(prototype);
            }
        }

        if (w != null) {
            System.err.print(".");
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
            return w;
        }
        else {
            if (currentWorkers >= MAX_WORKERS) {
                synchronized (this) {
                    this.wait(250);
                }
                return null;
            }
            boolean alreadyThere;
            synchronized (this) {
                alreadyThere = !startingTasks.add(prototype);
            }
            if (!alreadyThere) {
                currentWorkers++;
                if (logger.isInfoEnabled()) {
                    logger
                            .info("No suitable worker found. Attempting to start a new one.");
                }
                synchronized (allocationRequests) {
                    if (allocationRequests.size() < MAX_STARTING_WORKERS) {
                        allocationRequests.add(new AllocationRequest(
                                maxWallTime, prototype));
                        allocationRequests.notify();
                    }
                    else {
                        synchronized (this) {
                            this.wait(250);
                        }
                        return null;
                    }
                }
            }

            return null;
        }
    }

    private long now() {
        return System.currentTimeMillis() / 1000;
    }

    public void workerTerminated(Worker worker) {
        Status s = worker.getStatus();
        if (s.getStatusCode() == Status.FAILED) {
            synchronized (this) {
                requested.remove(worker.getId());
                startingTasks.remove(worker.getRunning());
                ready.put(new WorkerKey(worker), worker);
            }
        }
    }

    public void registrationReceived(String id, String url, ChannelContext cc) {
        Worker wr;
        synchronized (this) {
            wr = (Worker) requested.remove(id);
        }
        if (wr == null) {
            logger.warn("Received unrequested registration (id = " + id
                    + ", url = " + url);
            throw new IllegalArgumentException("Invalid worker id (" + id
                    + "). This worker manager instance does not "
                    + "recall requesting a worker with such an id.");
        }
        wr.workerRegistered();
        wr.setScheduledTerminationTime(now() + wr.getMaxWallTime()
                - TIME_RESERVE);
        wr.setChannelContext(cc);
        synchronized (this) {
            startingTasks.remove(wr.getRunning());
            ready.put(new WorkerKey(wr), wr);
            ids.put(id, wr);
        }
        System.err.println("RR. ready: " + ready.size() + ", busy: "
                + busy.size());
    }

    public void removeWorker(Worker worker) {
        synchronized (this) {
            ready.remove(new WorkerKey(worker));
            currentWorkers--;
            busy.remove(worker);
            startingTasks.remove(worker.getRunning());
            ids.remove(worker.getId());
        }
    }

    public void workerTaskDone(Worker wr) {
        synchronized (this) {
            busy.remove(wr);
            ready.put(new WorkerKey(wr), wr);
            notifyAll();
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

    private static class AllocationRequest {
        public WallTime maxWallTime;
        public Task prototype;

        public AllocationRequest(WallTime maxWallTime, Task prototype) {
            this.maxWallTime = maxWallTime;
            this.prototype = prototype;
        }
    }

    public void shutdown() {
        synchronized (this) {
            Iterator i;
            i = ready.values().iterator();
            while (i.hasNext()) {
                Worker wr = (Worker) i.next();
                wr.shutdown();
            }
            i = new ArrayList(requested.values()).iterator();
            while (i.hasNext()) {
                Worker wr = (Worker) i.next();
                try {
                    handler.cancel(wr.getWorkerTask());
                }
                catch (Exception e) {
                    logger.warn("Failed to cancel queued worker task "
                            + wr.getWorkerTask(), e);
                }
            }
        }
    }
}
