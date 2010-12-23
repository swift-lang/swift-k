package org.globus.cog.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** 
 * Kill external process after delay.  Registers a shutdown 
 * hook in case Thread does not complete normally
 * @author wozniak
 */
public class ProcessKiller extends Thread {
    
    Process process = null;
    
    private static final Set<Process> outstanding = 
        Collections.synchronizedSet(new HashSet<Process>());
    
    /**
       Delay time in milliseconds
     */
    long delay = 0;
    
    static ProcessKillerHook hook = null;
    
    public ProcessKiller(Process process, long delay) { 
        this.process = process;
        this.delay = delay;
   
        // Setup shutdown sequence if we haven't already
        if (hook == null) 
            synchronized (outstanding) {
                hook = new ProcessKillerHook();
                Runtime.getRuntime().addShutdownHook(hook);
            }
            
        outstanding.add(process);
    }
    
    public void run() {
        try {
            Thread.sleep(delay);
        }
        catch (InterruptedException e) {
        }
        outstanding.remove(process);
        process.destroy();
    }
    
    class ProcessKillerHook extends Thread {
        public void run() {
            synchronized (outstanding) { 
                for (Process p : outstanding) 
                    p.destroy();
                outstanding.clear();
            }
        }
    }
}
