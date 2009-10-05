//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 12, 2008
 */
package org.globus.cog.abstraction.impl.execution.coaster;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.DeflaterOutputStream;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.SubmitJobHandler;
import org.globus.cog.abstraction.coaster.service.job.manager.Settings;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.commands.Command;

public class SubmitJobCommand extends Command {
    public static final Logger logger = Logger.getLogger(SubmitJobCommand.class);

    public static final String NAME = "SUBMITJOB";

    public static final Set IGNORED_ATTRIBUTES;

    static {
        IGNORED_ATTRIBUTES = new HashSet();
        for (int i = 0; i < Settings.NAMES.length; i++) {
            IGNORED_ATTRIBUTES.add(Settings.NAMES[i].toLowerCase());
        }
    }

    private Task t;
    private String id;
    private boolean compression = SubmitJobHandler.COMPRESSION;

    public boolean getCompression() {
        return compression;
    }

    public void setCompression(boolean compression) {
        this.compression = compression;
    }

    public SubmitJobCommand(Task t) {
        super(NAME);
        this.t = t;
    }

    public void send() throws ProtocolException {
        try {
            serialize();
        }
        catch (Exception e) {
            throw new ProtocolException("Could not serialize job specification", e);
        }
        super.send();
    }

    protected void serialize() throws IOException {
        // I'd use Java serialization if not for the fact that a similar
        // thing needs to be done to communicate with the perl client
        JobSpecification spec = (JobSpecification) t.getSpecification();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream dos;
        if (compression) {
            dos = new DeflaterOutputStream(baos);
            //dos = baos;
        }
        else {
            dos = baos;
        }
        String identity = t.getIdentity().getValue();
        add(dos, "identity", identity);
        add(dos, "executable", spec.getExecutable());
        add(dos, "directory", spec.getDirectory());
        add(dos, "batch", spec.isBatchJob());
        add(dos, "stdin", spec.getStdInput());
        add(dos, "stdout", spec.getStdOutput());
        add(dos, "stderr", spec.getStdError());

        Iterator i;
        i = spec.getArgumentsAsList().iterator();
        while (i.hasNext()) {
            add(dos, "arg", (String) i.next());
        }

        i = spec.getEnvironmentVariableNames().iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            add(dos, "env", name + "=" + spec.getEnvironmentVariable(name));
        }

        i = spec.getAttributeNames().iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            if (!IGNORED_ATTRIBUTES.contains(name)) {
                add(dos, "attr", name + "=" + spec.getAttribute(name));
            }
        }

        Service s = t.getService(0);
        add(dos, "contact", s.getServiceContact().toString());
        add(dos, "provider", s.getProvider());

        if (s instanceof ExecutionService) {
            add(dos, "jm", ((ExecutionService) s).getJobManager());
        }
        else {
            add(dos, "jm", "fork");
        }
        dos.close();
        if (logger.isDebugEnabled()) {
            logger.debug("Job data: " + baos.toString());
        }

        addOutData(baos.toByteArray());
    }

    private void add(OutputStream baos, String key, boolean value) throws IOException {
        add(baos, key, String.valueOf(value));
    }

    private void add(OutputStream baos, String key, String value) throws IOException {
        if (value != null) {
            baos.write(key.getBytes("UTF-8"));
            baos.write('=');
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                switch (c) {
                    case '\n':
                    case '\\':
                        baos.write('\\');
                    default:
                        baos.write(c);
                }
            }
            
            baos.write('\n');
        }
    }

    public void receiveCompleted() {
        id = getInDataAsString(0);
        super.receiveCompleted();
    }

    public Task getTask() {
        return t;
    }
}
