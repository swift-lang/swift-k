/**
 * 
 */
package org.globus.cog.util;

/**
 * Waits for an external process to exit, reports to user callback
 * @author wozniak
 */
public class ProcessMonitor extends Thread {
    
    Process process;
    ProcessListener listener;
    boolean error = false;
    int exitCode = -1;
    
    public ProcessMonitor(Process process, ProcessListener listener) { 
        this.process = process;
        this.listener = listener;
    }
    
    public void run() {
        try {
            exitCode = process.waitFor();
            if (exitCode != 0)
                error = true;
            
            listener.callback(this);
        }
        catch (InterruptedException e) {
            error = true;
        }        
    }
    
    public boolean getError() { 
        return error;
    }
    
    public int getExitCode() {
        return exitCode;
    }
    
    public Process getProcess() { 
        return process;
    }
}
