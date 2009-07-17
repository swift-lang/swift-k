//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 21, 2009
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.CoasterService;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.execution.coaster.SubmitJobCommand;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.commands.Command;
import org.globus.cog.karajan.workflow.service.commands.ShutdownCommand;
import org.globus.cog.karajan.workflow.service.commands.Command.Callback;

public class Cpu implements Comparable, Callback, StatusListener {
    public static final Logger logger = Logger.getLogger(Cpu.class);
    
    private static PullThread pullThread;

    private int id;
    private List done;
    private Job running;
    private Block block;
    private Time starttime, endtime, timelast, donetime;
    private ChannelContext channelContext;
    
    public Cpu() {
        this.done = new ArrayList();
        this.timelast = Time.fromMilliseconds(0);
    }

    public Cpu(int id, Block block) {
        this();
        this.id = id;
        this.block = block;
        timelast = Time.fromSeconds(0);
    }

    public void workerStarted(ChannelContext channelContext) {
        this.channelContext = channelContext;
        block.remove(this);
        starttime = Time.now();
        endtime = starttime.add(block.getWalltime());
        timelast = starttime;
        pullLater();
    }

    public synchronized void jobTerminated() {
        if (logger.isInfoEnabled()) {
            logger.info(block.getId() + ":" + getId() + " jobTerminated");
        }
        block.increaseDoneJobCount();
        block.remove(this);
        donetime = Time.now();
        timelast = donetime;
        done.add(running);
        running = null;
        pullLater();
    }

    private void pullLater() {
        pullLater(this);
    }

    private static synchronized void pullLater(Cpu cpu) {
        if (logger.isInfoEnabled()) {
            logger.info(cpu.block.getId() + ":" + cpu.getId() + " pullLater");
        }
        if (pullThread == null) {
            pullThread = new PullThread(cpu.block);
            pullThread.start();
        }
        pullThread.enqueue(cpu);
    }

    public synchronized void pull() {
        try {
            if (logger.isInfoEnabled()) {
                logger.info(block.getId() + ":" + getId() + " pull");
            }
            if (running == null) {
                running = block.getAllocationProcessor().request(endtime.subtract(Time.now()));
                if (running != null) {
                    running.getTask().addStatusListener(this);
                    running.start();
                    timelast = running.getEndTime();
                    if (timelast == null) {
                        CoasterService.error(20, "Timelast is null", new Throwable());
                    }
                    block.add(this);
                    submit(running);
                }
                else {
                    if (block.getAllocationProcessor().getQueued().size() == 0) {
                        block.getAllocationProcessor().waitForJobs();
                    }
                    pullLater();
                }
            }
            else {
                CoasterService.error(40, "pull called while another job was running", new Throwable());
            }
        }
        catch (Exception e) {
            taskFailed("Failed pull", e);
            CoasterService.error(21, "Failed pull", e);
        }
    }

    protected void submit(Job job) {
        Task task = job.getTask();
        if (logger.isInfoEnabled()) {
            logger.info(block.getId() + ":" + getId() + " submitting " + task.getIdentity());
        }
        task.setStatus(Status.SUBMITTING);
        try {
            KarajanChannel channel = ChannelManager.getManager().reserveChannel(channelContext);
            ChannelManager.getManager().reserveLongTerm(channel);
            SubmitJobCommand cmd = new SubmitJobCommand(task);
            cmd.executeAsync(channel, this);
        }
        catch (Exception e) {
            logger.info(block.getId() + ":" + getId() + " submission failed " + task.getIdentity());
            taskFailed(null, e);
        }
    }
    
    public void shutdown() {
        if (running != null) {
            logger.warn(block.getId() + "- " + id + "Job still running while shutting down", new Throwable());
            running.fail("Shutting down worker", null);
        }
        try {
            KarajanChannel channel = ChannelManager.getManager().reserveChannel(channelContext);
            ChannelManager.getManager().reserveLongTerm(channel);
            ShutdownCommand cmd = new ShutdownCommand();
            cmd.executeAsync(channel, this);
        }
        catch (Exception e) {
            logger.info("Failed to shut down worker", e);
            block.forceShutdown();
        }
    }

    public int compareTo(Object obj) {
        Cpu o = (Cpu) obj;
        TimeInterval diff = timelast.subtract(o.timelast);
        if (diff.getMilliseconds() == 0) {
            return id - o.id;
        }
        else {
            return (int) diff.getMilliseconds();
        }
    }

    public synchronized Job getRunning() {
        return running;
    }

    public Time getTimeLast() {
        return timelast;
    }

    public String toString() {
        return id + ":" + timelast;
    }

    public List getDoneJobs() {
        return done;
    }

    public void taskFailed(String msg, Exception e) {
        if (running == null) {
            if (starttime == null) {
                starttime = Time.now();
            }
            if (endtime == null) {
                endtime = starttime.add(block.getWalltime());
            }
            running = block.getAllocationProcessor().request(endtime.subtract(Time.now()));
        }
        if (running != null) {
            running.fail("Block task failed: " + msg, e);
        }
    }

    public void errorReceived(Command cmd, String msg, Exception t) {
        if (cmd instanceof ShutdownCommand) {
            logger.info("Failed to shut down " + this + ": " + msg, t);
            block.forceShutdown();
        }
        else {
            taskFailed(msg, t);
        }
    }

    public void replyReceived(Command cmd) {
        if (cmd instanceof ShutdownCommand) {
            logger.info(this + " shut down successfully");
        }
        else {
            SubmitJobCommand sjc = (SubmitJobCommand) cmd;
            Task task = sjc.getTask();
            task.setStatus(Status.SUBMITTED);
        }
    }

    private static class PullThread extends Thread {
        private Block block;
        private LinkedList queue;
        private int qseq;

        public PullThread(Block block) {
            setName("Job pull");
            setDaemon(true);
            this.block = block;
            queue = new LinkedList();
        }

        public synchronized void enqueue(Cpu cpu) {
            qseq++;
            queue.add(cpu);
            notify();
        }

        public void run() {
            while (true) {
                Cpu cpu;
                synchronized (this) {
                    while (!stateChanged()) {
                        try {
                            wait(100);
                        }
                        catch (InterruptedException e) {
                            return;
                        }
                    }
                    cpu = (Cpu) queue.removeFirst();
                }
                cpu.pull();
            }
        }
        
        private int lastseq = 0;
        
        private boolean stateChanged() {
            int nseq = qseq + block.getAllocationProcessor().getQueueSeq();
            if (nseq != lastseq) {
                lastseq = nseq;
                return true;
            }
            else {
                return false;
            }
        }
    }

    public void statusChanged(StatusEvent event) {
        if (event.getStatus().isTerminal()) {
            running.getTask().removeStatusListener(this);
            running.setEndTime(Time.now());
            jobTerminated();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setRunning(Job r) {
        this.running = r;
    }

    public void addDoneJob(Job d) {
        done.add(d);
    }
}