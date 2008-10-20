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
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.commands.Command;

public class SubmitJobCommand extends Command {
    public static final Logger logger = Logger.getLogger(SubmitJobCommand.class);
    
    public static final String NAME = "SUBMITJOB";

    private Task t;
    private String id;

    public SubmitJobCommand(Task t) {
        super(NAME);
        this.t = t;
    }

    public void send() throws ProtocolException {
        try {
            serialize();
        }
        catch (Exception e) {
            throw new ProtocolException(
                    "Could not serialize job specification", e);
        }
        super.send();
    }

    protected void serialize() throws IOException {
        //I'd use Java serialization if not for the fact that a similar
        //thing needs to be done to communicate with the perl client
        JobSpecification spec = (JobSpecification) t.getSpecification();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String identity = t.getIdentity().getValue();
        add(baos, "identity", identity);
        add(baos, "executable", spec.getExecutable());
        add(baos, "directory", spec.getDirectory());
        add(baos, "stdin", spec.getStdInput());
        add(baos, "stdout", spec.getStdOutput());
        add(baos, "stderr", spec.getStdError());

        Iterator i;
        i = spec.getArgumentsAsList().iterator();
        while (i.hasNext()) {
            add(baos, "arg", (String) i.next());
        }

        i = spec.getEnvironmentVariableNames().iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            add(baos, "env", name + "=" + spec.getEnvironmentVariable(name));
        }

        i = spec.getAttributeNames().iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            add(baos, "attr", name + "=" + spec.getAttribute(name));
        }

        Service s = t.getService(0);
        add(baos, "contact", s.getServiceContact().toString());
        add(baos, "provider", s.getProvider());

        if (s instanceof ExecutionService) {
            add(baos, "jm", ((ExecutionService) s).getJobManager());
        }
        else {
            add(baos, "jm", "fork");
        }
        baos.close();
        if (logger.isDebugEnabled()) {
            logger.debug("Job data: " + baos.toString());
        }
        addOutData(baos.toByteArray());
    }

    private void add(ByteArrayOutputStream baos, String key, String value)
            throws IOException {
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
