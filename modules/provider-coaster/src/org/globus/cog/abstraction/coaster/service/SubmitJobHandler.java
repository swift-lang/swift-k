//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2005
 */
package org.globus.cog.abstraction.coaster.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

import org.globus.cog.abstraction.coaster.service.job.manager.TaskNotifier;
import org.globus.cog.abstraction.impl.common.CleanUpSetImpl;
import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.impl.common.StagingSetEntryImpl;
import org.globus.cog.abstraction.impl.common.StagingSetImpl;
import org.globus.cog.abstraction.impl.common.task.ExecutionServiceImpl;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.CleanUpSet;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.StagingSet;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.handlers.RequestHandler;

public class SubmitJobHandler extends RequestHandler {
    public static final boolean COMPRESSION = true;

    public void requestComplete() throws ProtocolException {
        Task t;
        try {
            if (COMPRESSION) {
                t = read(new InflaterInputStream(new ByteArrayInputStream(getInData(0))));
                // t = read(new ByteArrayInputStream(getInData(0)));
            }
            else {
                t = read(new ByteArrayInputStream(getInData(0)));
            }
            ChannelContext channelContext = getChannel().getChannelContext();
            new TaskNotifier(t, channelContext);
            ((CoasterService) channelContext.getService()).getJobQueue().getCoasterQueueProcessor().setClientChannelContext(
                channelContext);
            ((CoasterService) channelContext.getService()).getJobQueue().enqueue(t);
            // make sure we'll have something to send notifications to
            ChannelManager.getManager().reserveLongTerm(getChannel());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ProtocolException("Could not deserialize job description", e);
        }
        sendReply(t.getIdentity().toString());
    }

    private Task read(InputStream is) throws IOException, ProtocolException, IllegalSpecException {
        Helper h = new Helper(is);

        Task t = new TaskImpl();
        t.setType(Task.JOB_SUBMISSION);
        JobSpecification spec = new JobSpecificationImpl();
        t.setSpecification(spec);

        String clientId = h.read("identity");
        if (clientId == null) {
            throw new IllegalSpecException("Missing job identity");
        }
        t.setIdentity(new IdentityImpl(clientId + "-" + new IdentityImpl().getValue()));
        spec.setExecutable(h.read("executable").intern());
        spec.setDirectory(h.read("directory"));
        spec.setBatchJob(h.readBool("batch"));
        spec.setStdInput(h.read("stdin"));
        spec.setStdOutput(h.read("stdout"));
        spec.setStdError(h.read("stderr"));
        String s;
        while ((s = h.read("arg")) != null) {
            spec.addArgument(s);
        }

        while ((s = h.read("env")) != null) {
            spec.addEnvironmentVariable(getKey(s), getValue(s));
        }

        while ((s = h.read("attr")) != null) {
            spec.setAttribute(getKey(s), getValue(s));
        }

        StagingSet ss = null;
        while ((s = h.read("stagein")) != null) {
            if (ss == null) {
                ss = new StagingSetImpl();
            }
            ss.add(new StagingSetEntryImpl(getSource(s), getDestination(s)));
        }
        if (ss != null) {
            spec.setStageIn(ss);
        }

        ss = null;
        while ((s = h.read("stageout")) != null) {
            if (ss == null) {
                ss = new StagingSetImpl();
            }
            ss.add(new StagingSetEntryImpl(getSource(s), getDestination(s)));
        }
        if (ss != null) {
            spec.setStageOut(ss);
        }
        
        CleanUpSet cs = null;
        
        while ((s = h.read("cleanup")) != null) {
            if (cs == null) {
                cs = new CleanUpSetImpl();
            }
            cs.add(s);
        }
        
        if (cs != null) {
            spec.setCleanUpSet(cs);
        }

        ExecutionService service = new ExecutionServiceImpl();

        setServiceParams(service, h.read("contact"), h.read("provider"), h.read("jm").intern());
        t.setService(0, service);

        return t;
    }

    protected void setServiceParams(ExecutionService s, String contact, String provider, String jm)
            throws IllegalSpecException {
        if (jm == null) {
            jm = "fork";
        }

        String[] els = jm.split(":");
        if (els.length == 2 && "fork".equals(els[1])) {
            s.setProvider("local");
        }
        else if (jm.equalsIgnoreCase("fork")) {
            s.setProvider("local");
        }
        else {
            s.setProvider("coaster");
            s.setJobManager(jm);
        }
        s.setServiceContact(new ServiceContactImpl(contact));
    }

    private String getSource(String s) {
        int i = s.indexOf('\n');
        return makeAbsolute(s.substring(0, i));
    }

    private String getDestination(String s) {
        int i = s.indexOf('\n');
        return makeAbsolute(s.substring(i + 1));
    }

    private String makeAbsolute(String path) {
        if (path.startsWith("file://localhost")) {
            return "coaster://" + getChannel().getChannelContext().getChannelID()
                    + path.substring("file://localhost".length());
        }
        else {
            return path;
        }
    }

    private String getKey(String s) throws ProtocolException {
        int i = s.indexOf('=');
        if (i == -1) {
            throw new ProtocolException("Invalid value: " + s);
        }
        return s.substring(0, i);
    }

    private String getValue(String s) {
        int i = s.indexOf('=');
        return s.substring(i + 1);
    }

    private class Helper {
        private InputStream is;
        private String key, value;

        public Helper(InputStream is) {
            this.is = is;
        }

        public boolean readBool(String key) throws IOException, ProtocolException {
            return Boolean.valueOf(read(key)).booleanValue();
        }

        public String read(String key) throws IOException, ProtocolException {
            if (this.key == null) {
                scan();
            }
            if (key.equals(this.key)) {
                this.key = null;
                return this.value;
            }
            else {
                return null;
            }
        }

        private void scan() throws IOException, ProtocolException {
            key = null;
            value = null;
            StringBuffer sb = new StringBuffer();
            int c = is.read();
            boolean nl = false;
            while (value == null) {
                switch (c) {
                    case '=': {
                        if (key == null) {
                            key = sb.toString();
                            sb = new StringBuffer();
                        }
                        else {
                            sb.append((char) c);
                        }
                        break;
                    }
                    case '\\': {
                        c = is.read();
                        if (c == 'n') {
                            sb.append('\n');
                        }
                        else if (c == '\\') {
                            sb.append('\\');
                        }
                        break;
                    }
                    case -1:
                    case '\n': {
                        if (key == null) {
                            throw new ProtocolException("Invalid line: " + sb.toString());
                        }
                        else {
                            value = sb.toString();
                        }
                        return;
                    }
                    default:
                        sb.append((char) c);
                }
                c = is.read();
            }
        }
    }
}
