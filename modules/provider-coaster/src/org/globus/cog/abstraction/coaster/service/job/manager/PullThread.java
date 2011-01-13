package org.globus.cog.abstraction.coaster.service.job.manager;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

class PullThread extends Thread {
    
    Logger logger = Logger.getLogger(PullThread.class);
    
    private LinkedList<Cpu> queue, sleeping;
    private long sleepTime, runTime, last;
    private BlockQueueProcessor bqp;

    public PullThread(BlockQueueProcessor bqp) {
        this.bqp = bqp;
        setName("PullThread");
        setDaemon(true);
        queue = new LinkedList<Cpu>();
        sleeping = new LinkedList<Cpu>();
    }

    public synchronized void enqueue(Cpu cpu) {
        queue.add(cpu);
        notify();
    }

    public synchronized void sleep(Cpu cpu) {
        sleeping.add(cpu);
    }

    public synchronized int sleepers() {
        return sleeping.size();
    }
    
    public synchronized Cpu getSleeper() {
        Cpu result = null;
        try { 
            result = sleeping.remove();
        }
        catch (NoSuchElementException e) { 
            return null;
        }
        return result; 
    }
    
    public void run() {
        last = System.currentTimeMillis();
        while (true) {
            Cpu cpu;
            synchronized (this) {
                while (queue.isEmpty()) {
                    if (!awakeUseable()) {
                        try {
                            mwait(50);
                        }
                        catch (InterruptedException e) {
                            return;
                        }
                    }
                }
                cpu = queue.removeFirst();
            }
            cpu.pull();
        }
    }

    private boolean awakeUseable() {
        int seq = bqp.getQueueSeq();
        int sz = sleeping.size();
        Iterator<Cpu> i = sleeping.iterator();
        while (i.hasNext()) {
            Cpu cpu = i.next();
            if (cpu.getLastSeq() < seq) {
                enqueue(cpu);
                i.remove();
            }
        }
        return sz != sleeping.size();
    }

    private void mwait(int ms) throws InterruptedException {
        runTime += countAndResetTime();
        wait(ms);
        sleepTime += countAndResetTime();
        if (runTime + sleepTime > 10000) {
            logger.info("runTime: " + runTime + ", sleepTime: " + sleepTime);
            runTime = 0;
            sleepTime = 0;
        }
    }

    private long countAndResetTime() {
        long t = System.currentTimeMillis();
        long d = t - last;
        last = t;
        return d;
    }
}
