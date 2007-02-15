//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 11, 2005
 */
package org.globus.cog.abstraction.impl.scheduler.cobalt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.scheduler.common.Job;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessException;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessListener;

public class CobaltJob extends Job {
    public static final Logger logger = Logger.getLogger(CobaltJob.class);

    private String stdout, stderr, tstdout, tstderr;
    private boolean redirect;
    private int exitcode;

    public CobaltJob(String jobID, boolean redirect, String stdout,
            String stderr, String tstdout, String tstderr,
            ProcessListener listener) {
        super(jobID, null, null, null, listener);
        this.redirect = redirect;
        this.stdout = stdout;
        this.stderr = stderr;
        this.tstdout = tstdout;
        this.tstderr = tstderr;
        int exitcode = Integer.MIN_VALUE;
    }

    public boolean close() {
        if (processStderr() && processStdout()) {
            if (exitcode == Integer.MIN_VALUE) {
                listener.processFailed("Did not find the exitcode in the logs");
            }
            else {
                listener.processCompleted(exitcode);
            }
        }
        return true;
    }

    protected boolean processStdout() {
        try {
            CharArrayWriter caw = null;
            if (redirect) {
                caw = new CharArrayWriter();
            }
            BufferedWriter bw = null;
            if (tstdout != null) {
                bw = new BufferedWriter(new FileWriter(tstdout));
            }
            BufferedReader br = new BufferedReader(new FileReader(stdout));
            String line;
            do {
                line = br.readLine();
                if (line != null) {
                    if (redirect) {
                        caw.write(line);
                        caw.write('\n');
                    }
                    else if (tstdout != null) {
                        bw.write(line);
                        bw.write('\n');
                    }
                }
            } while (line != null);
            br.close();
            if (caw != null) {
                caw.close();
                listener.stdoutUpdated(caw.toString());
            }
            if (bw != null) {
                bw.close();
            }
            return true;
        }
        catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Exception caught while reading STDOUT", e);
            }
            listener.processFailed(new ProcessException(
                    "Exception caught while reading STDOUT", e));
            return false;
        }
    }

    protected boolean processStderr() {
        try {
            CharArrayWriter caw = null;
            if (redirect) {
                caw = new CharArrayWriter();
            }
            BufferedWriter bw = null;
            if (tstdout != null) {
                bw = new BufferedWriter(new FileWriter(tstderr));
            }
            BufferedReader br = new BufferedReader(new FileReader(stderr));
            String line;
            boolean started = false;
            
            do {
                line = br.readLine();
                if (line != null) {
                    if (line.startsWith("<") && line.indexOf("(Info)") != -1
                            && line.indexOf("Starting job") != -1) {
                        started = true;
                    }
                    if (started) {
                        if (!line.startsWith("<")
                                || line.indexOf("(Info)") == -1) {
                            if (redirect) {
                                caw.write(line);
                                caw.write('\n');
                            }
                            else if (tstdout != null) {
                                bw.write(line);
                                bw.write('\n');
                            }
                        }
                        else {
                            int index = line.indexOf("BG/L job exit status =");
                            if (index != -1) {
                                String es = line.substring(index + 
                                        "BG/L job exit status =".length()).trim();
                                if (!es.startsWith("(") && !es.endsWith(")")) {
                                    throw new IOException(
                                            "Could not parse job exit status. Invalid exit status line: "
                                                    + line);
                                }
                                else {
                                    exitcode = Integer.parseInt(es.substring(1, es.length() - 1));
                                }
                            }
                        }
                    }

                }
            } while (line != null);
            br.close();
            if (caw != null) {
                caw.close();
                listener.stderrUpdated(caw.toString());
            }
            if (bw != null) {
                bw.close();
            }
            
            return true;
        }
        catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Exception caught while reading STDERR", e);
            }
            listener.processFailed(new ProcessException(
                    "Exception caught while reading STDERR", e));
            return false;
        }
    }
}
