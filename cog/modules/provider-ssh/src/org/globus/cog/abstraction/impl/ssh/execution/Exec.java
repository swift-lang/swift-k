// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Sshtools - Java SSH2 API
 * 
 * Copyright (C) 2002 Lee David Painter.
 * 
 * Written by: 2002 Lee David Painter <lee@sshtools.com>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.globus.cog.abstraction.impl.ssh.execution;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.execution.JobException;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.common.util.WriterMultiplexer;
import org.globus.cog.abstraction.impl.ssh.SSHTask;

import com.sshtools.j2ssh.session.SessionChannelClient;

public class Exec implements SSHTask {
    static Logger logger = Logger.getLogger(Exec.class.getName());
    private String cmd;
    private String dir;
    private String remoteOut, remoteErr, remoteIn;
    private String outFile, errFile;
    private boolean outMem, errMem;
    private CharArrayWriter out, err;
    private Map<String, String> envVars;

    public Exec() {
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getCmd() {
        return cmd;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String string) {
        dir = string;
    }

    public String getRemoteOut() {
        return remoteOut;
    }

    public void setRemoteOut(String remoteOut) {
        this.remoteOut = remoteOut;
    }

    public String getRemoteErr() {
        return remoteErr;
    }

    public void setRemoteErr(String remoteErr) {
        this.remoteErr = remoteErr;
    }

    public String getRemoteIn() {
        return remoteIn;
    }

    public void setRemoteIn(String remoteIn) {
        this.remoteIn = remoteIn;
    }
    
    public void addEnv(String name, String value) {
        if (envVars == null) {
            envVars = new HashMap<String, String>();
        }
        envVars.put(name, value);
    }
    
    public void removeEnv(String name) {
        if (envVars == null) {
            return;
        }
        envVars.remove(name);
    }

    public void execute(SessionChannelClient session)
            throws IllegalSpecException, InvalidSecurityContextException,
            InvalidServiceContactException, TaskSubmissionException,
            JobException {
        try {
            executeCommand(session);
            session.close();
        }
        catch (IOException sshe) {
            logger.error(sshe);
            throw new TaskSubmissionException("SSH Connection failed: "
                    + sshe.getMessage(), sshe);
        }
    }

    public void executeCommand(SessionChannelClient session)
            throws TaskSubmissionException, JobException {
        try {
            if (getCmd() == null) {
                throw new TaskSubmissionException("No executable specified");
            }
        
            if (!session.executeCommand("/bin/sh" + 
                    (remoteIn == null ? "" : " <" + remoteIn) + 
                    (remoteOut == null ? "" : " 1>" + remoteOut) +
                    (remoteErr == null ? "" : " 2>" + remoteErr))) {
                throw new TaskSubmissionException("Failed to start /bin/sh");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Executing " + getCmd());
            }
            OutputStream os = session.getOutputStream();
            if (getDir() != null) {
                os.write(("cd " + getDir() + "\n").getBytes());
            }
            if (envVars != null) {
                for (Map.Entry<String, String> e : envVars.entrySet()) {
                    os.write((e.getKey() + "='" + e.getValue() + "'; export " + e.getKey() + "\n").getBytes());
                }
            }
            os.write((getCmd() + "\n").getBytes());
            os.write("exit\n".getBytes());
            BufferedReader stdout = new BufferedReader(new InputStreamReader(
                    session.getInputStream()));
            BufferedReader stderr = new BufferedReader(new InputStreamReader(
                    session.getStderrInputStream()));

            Writer owr = null;
            if (outMem) {
                out = new CharArrayWriter();
                owr = out;
            }
            if (outFile != null) {
                owr = WriterMultiplexer.multiplex(owr, new FileWriter(outFile));
            }

            Writer ewr = null;
            if (errMem) {
                err = new CharArrayWriter();
                ewr = err;
            }
            if (errFile != null) {
                ewr = WriterMultiplexer.multiplex(ewr, new FileWriter(errFile));
            }

            /*
             * Read all output sent to stdout (line by line) and print it to our
             * own stdout.
             */
            char[] bufout = new char[1024];
            char[] buferr = new char[1024];
            boolean output = outMem || outFile != null;
            boolean error = errMem || errFile != null;

            while (true) {
                int charsout = 0;
                int charserr = 0;
                if (output) {
                    if (stdout.ready()) {
                        charsout = stdout.read(bufout, 0, 1024);
                    }
                    else {
                        charsout = 0;
                    }
                    if (charsout > 0) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Out bytes from process: " + new String(bufout, 0, charsout));
                        }
                        owr.write(bufout, 0, charsout);
                    }
                }
                if (error) {
                    if (stderr.ready()) {
                        charserr = stderr.read(buferr, 0, 1024);
                    }
                    else {
                        charserr = 0;
                    }
                    if (charserr > 0) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Error bytes from process: " + new String(buferr, 0, charserr));
                        }
                        ewr.write(buferr, 0, charsout);
                    }
                }

                if (charsout + charserr == 0) {
                    if (session.getInputStream().isClosed()) {
                        break;
                    }
                    Thread.sleep(20);
                }
            }

            if (owr != null) {
                owr.close();
            }
            if (ewr != null) {
                ewr.close();
            }

            Integer exitcode = session.getExitCode();

            if (exitcode != null) {
                if (exitcode.intValue() != 0) {
                    logger.info("Exit code " + exitcode.toString());
                    throw new JobException(exitcode.intValue());
                }
            }
        }
        catch (IOException ioe) {
            throw new TaskSubmissionException("The session failed: "
                    + ioe.getMessage(), ioe);
        }
        catch (InterruptedException e) {
            throw new TaskSubmissionException("The session was interrupted", e);
        }
        finally {
        }
    }

    public String getTaskOutput() {
        if (out != null) {
            return out.toString();
        }
        else {
            return null;
        }
    }

    public String getTaskError() {
        if (err != null) {
            return err.toString();
        }
        else {
            return null;
        }
    }

    public String getErrFile() {
        return errFile;
    }

    public void setErrFile(String errFile) {
        this.errFile = errFile;
    }

    public boolean getErrMem() {
        return errMem;
    }

    public void setErrMem(boolean errMem) {
        this.errMem = errMem;
    }

    public String getOutFile() {
        return outFile;
    }

    public void setOutFile(String outFile) {
        this.outFile = outFile;
    }

    public boolean getOutMem() {
        return outMem;
    }

    public void setOutMem(boolean outMem) {
        this.outMem = outMem;
    }
}