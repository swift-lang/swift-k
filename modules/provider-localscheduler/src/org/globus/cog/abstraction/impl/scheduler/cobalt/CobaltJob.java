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
import java.io.Writer;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.util.NullWriter;
import org.globus.cog.abstraction.impl.common.util.WriterMultiplexer;
import org.globus.cog.abstraction.impl.scheduler.common.Job;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessException;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessListener;
import org.globus.cog.abstraction.interfaces.FileLocation;

public class CobaltJob extends Job {
    public static final Logger logger = Logger.getLogger(CobaltJob.class);

    private String stdout, stderr, tstdout, tstderr;
    private FileLocation outLoc, errLoc;
    private int exitcode;

    public CobaltJob(String jobID, String stdout,
            String stderr, String tstdout, FileLocation outLoc, String tstderr, FileLocation errLoc, 
            ProcessListener listener) {
        super(jobID, null, null, null, null, null, listener);
        this.stdout = stdout;
        this.stderr = stderr;
        this.tstdout = tstdout;
        this.outLoc = outLoc;
        this.tstderr = tstderr;
        this.errLoc = errLoc;
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
            Writer wr = null;
            CharArrayWriter caw = null;
            if (FileLocation.MEMORY.overlaps(outLoc)) {
                caw = new CharArrayWriter();
                wr = caw;
            }
            if (tstdout != null && LOCAL_AND_REMOTE.overlaps(outLoc)) {
                wr = WriterMultiplexer.multiplex(wr, new BufferedWriter(new FileWriter(tstdout)));
            }
            if (wr == null) {
                wr = new NullWriter();
            }
            BufferedReader br = new BufferedReader(new FileReader(stdout));
            String line;
            do {
                line = br.readLine();
                if (line != null) {
                    wr.write(line);
                    wr.write('\n');
                }
            } while (line != null);
            br.close();
            wr.close();
            if (caw != null) {
                listener.stdoutUpdated(caw.toString());
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
    
    private static final FileLocation LOCAL_AND_REMOTE = FileLocation.LOCAL.and(FileLocation.REMOTE);

    protected boolean processStderr() {
        try {
            Writer wr = null;
            CharArrayWriter caw = null;
            if (FileLocation.MEMORY.overlaps(errLoc)) {
                caw = new CharArrayWriter();
                wr = caw;
            }
            if (tstdout != null && LOCAL_AND_REMOTE.overlaps(errLoc)) {
                wr = WriterMultiplexer.multiplex(wr, new BufferedWriter(new FileWriter(tstderr)));
            }
            if (wr == null) {
                wr = new NullWriter();
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
                            wr.write(line);
                            wr.write('\n');
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
            wr.close();
            if (caw != null) {
                listener.stderrUpdated(caw.toString());
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
