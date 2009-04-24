// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.execution.gt2;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractDelegatedTaskHandler;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.Delegation;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.gram.GramException;
import org.globus.gram.GramJob;
import org.globus.gram.GramJobListener;
import org.globus.gram.internal.GRAMConstants;
import org.globus.io.gass.server.GassServer;
import org.globus.io.gass.server.JobOutputListener;
import org.globus.io.gass.server.JobOutputStream;
import org.globus.rsl.Binding;
import org.globus.rsl.Bindings;
import org.globus.rsl.NameOpValue;
import org.globus.rsl.ParseException;
import org.globus.rsl.RSLParser;
import org.globus.rsl.RslNode;
import org.globus.rsl.Value;
import org.globus.rsl.VarRef;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;

public class JobSubmissionTaskHandler extends AbstractDelegatedTaskHandler implements 
        GramJobListener, JobOutputListener {
    static Logger logger = Logger.getLogger(JobSubmissionTaskHandler.class
            .getName());
    private GramJob gramJob;
    private Vector jobList;
    private boolean startGassServer;
    private GassServer gassServer;
    private SecurityContext securityContext;
    private JobOutputStream stdoutStream;
    private JobOutputStream stderrStream;
    private GSSCredential credential;
    private String jobManager;
    private boolean clean;

    public void submit(Task task) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        checkAndSetTask(task);
        task.setStatus(Status.SUBMITTING);
        String server = getServer();
        this.securityContext = getSecurityContext(task);
        this.credential = (GSSCredential) securityContext.getCredentials();
        JobSpecification spec;
        try {
            spec = (JobSpecification) task.getSpecification();
        }
        catch (Exception e) {
            throw new IllegalSpecException(
                    "Exception while retreiving Job Specification", e);
        }
        RslNode rslTree = null;
        try {
            rslTree = prepareSpecification(spec);
        }
        catch (Throwable e) {
            throw new IllegalSpecException("Cannot parse the given RSL", e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("RSL: " + rslTree);
        }

        if (rslTree.getOperator() == RslNode.MULTI) {
            task.setAttribute("jobCount", "multiple");
            submitMultipleJobs(rslTree, spec);
        }
        else {
            task.setAttribute("jobCount", "single");
            submitSingleJob(rslTree, spec, server);
        }
    }

    private void submitSingleJob(RslNode rsl, JobSpecification spec,
            String server) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {

        this.gramJob = new GramJob(rsl.toString());
        try {
            this.gramJob.setCredentials(this.credential);
        }
        catch (IllegalArgumentException iae) {
            throw new InvalidSecurityContextException(
                    "Cannot set the SecurityContext twice", iae);
        }

        if (!spec.isBatchJob()) {
            CallbackHandlerManager.increaseUsageCount(this.credential);
            this.gramJob.addListener(this);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Execution server: " + server);
        }
        boolean limitedDeleg = isLimitedDelegation(this.securityContext);
        if (spec.getDelegation() == Delegation.FULL_DELEGATION) {
            limitedDeleg = false;
        }
        try {
            // check if the task has not been canceled after it was
            // submitted for execution

            this.gramJob.request(server, spec.isBatchJob(), limitedDeleg);
            if (logger.isDebugEnabled()) {
                logger.debug("Submitted job with Globus ID: "
                        + this.gramJob.getIDAsString());
            }
            getTask().setStatus(Status.SUBMITTED);
            if (spec.isBatchJob()) {
                getTask().setStatus(Status.COMPLETED);
            }
        }
        catch (GramException ge) {
            cleanup();
            throw new TaskSubmissionException("Cannot submit job", ge);
        }
        catch (GSSException gsse) {
            cleanup();
            throw new InvalidSecurityContextException("Invalid GSSCredentials",
                    gsse);
        }
    }

    private String getServer() throws InvalidServiceContactException {
        Service service = getTask().getService(0);
        ServiceContact serviceContact = service.getServiceContact();
        String server = serviceContact.getContact();

        // if the jobmanager attribute is specified, handle it
        if (service instanceof ExecutionService) {
            jobManager = ((ExecutionService) service).getJobManager();
        }
        else {
            jobManager = extractJobManager(server);
        }
        if (jobManager != null) {
            jobManager = jobManager.toLowerCase();
            server = substituteJobManager(server, jobManager);
        }
        return server;
    }

    private boolean isLimitedDelegation(SecurityContext sc) {
        if (sc instanceof GlobusSecurityContextImpl) {
            return ((GlobusSecurityContextImpl) securityContext)
                    .getDelegation() != Delegation.FULL_DELEGATION;
        }
        else {
            return true;
        }
    }

    private void submitMultipleJobs(RslNode rslTree, JobSpecification spec)
            throws IllegalSpecException, InvalidSecurityContextException,
            InvalidServiceContactException, TaskSubmissionException {

        MultiJobListener listener = new MultiJobListener(getTask());
        this.jobList = new Vector();
        List jobs = rslTree.getSpecifications();
        Iterator iter = jobs.iterator();
        RslNode node;
        NameOpValue nv;
        String rmc;
        String rsl;
        while (iter.hasNext()) {
            node = (RslNode) iter.next();
            rsl = node.toRSL(true);
            nv = node.getParam("resourceManagerContact");
            if (nv == null) {
                throw new IllegalSpecException(
                        "Error: No resource manager contact for job.");
            }
            else {
                Object obj = nv.getFirstValue();
                if (obj instanceof Value) {
                    rmc = ((Value) obj).getValue();
                    multiRunSub(rsl, rmc, listener);
                }
                getTask().setStatus(Status.SUBMITTED);
            }
        }
    }

    private void multiRunSub(String rsl, String rmc, MultiJobListener listener)
            throws InvalidSecurityContextException, TaskSubmissionException {
        GramJob job = new GramJob(rsl);

        job.addListener(listener);

        try {
            job.setCredentials(this.credential);
        }
        catch (IllegalArgumentException iae) {
            throw new InvalidSecurityContextException(
                    "Cannot set the SecurityContext twice", iae);
        }

        boolean limitedDeleg = isLimitedDelegation(this.securityContext);
        try {
            job.request(rmc, false, limitedDeleg);
            if (logger.isDebugEnabled()) {
                logger.debug("Submitted job with Globus ID: "
                        + job.getIDAsString());
            }
        }
        catch (GramException ge) {
            listener.failed(true);
            throw new TaskSubmissionException("Cannot submit job", ge);
        }
        catch (GSSException gsse) {
            listener.failed(true);
            throw new InvalidSecurityContextException("Invalid GSSCredentials",
                    gsse);
        }
        listener.runningJob();
    }

    public void suspend() throws InvalidSecurityContextException,
            TaskSubmissionException {
        // not implemented yet
    }

    public void resume() throws InvalidSecurityContextException,
            TaskSubmissionException {
        // not implemented yet
    }

    public void cancel() throws InvalidSecurityContextException,
            TaskSubmissionException {
        cancel("Canceled");
    }

    public void cancel(String message) throws InvalidSecurityContextException,
            TaskSubmissionException {
        try {
            if (getTask().getStatus().getStatusCode() == Status.UNSUBMITTED) {
                getTask().setStatus(new StatusImpl(Status.CANCELED, message,
                        null));
                return;
            }
            String jobCount = (String) getTask().getAttribute("jobCount");
            if (jobCount.equalsIgnoreCase("multiple")) {
                Iterator iterator = this.jobList.iterator();
                while (iterator.hasNext()) {
                    GramJob job = (GramJob) iterator.next();
                    job.cancel();
                }
            }
            else {
                this.gramJob.cancel();
            }
            getTask().setStatus(new StatusImpl(Status.CANCELED, message, null));
        }
        catch (GramException ge) {
            cleanup();
            throw new TaskSubmissionException("Cannot cancel job", ge);
        }
        catch (GSSException gsse) {
            cleanup();
            throw new InvalidSecurityContextException("Invalid GSSCredentials",
                    gsse);
        }
    }

    private RslNode prepareSpecification(JobSpecification spec)
            throws IllegalSpecException, TaskSubmissionException {
        RslNode rsl = new RslNode(RslNode.AND);
        if (spec.getSpecification() != null) {
            try {
                return RSLParser.parse(spec.getSpecification());
            }
            catch (ParseException e) {
                throw new IllegalSpecException("Failed to parse specification",
                        e);
            }
        }
        else {
            boolean batchJob = spec.isBatchJob();
            if ("sge".equals(jobManager) && !batchJob) {
                logger
                        .info("Forcing redirection because the SGE JM is broken.");
                spec.setStdOutputLocation(FileLocation.MEMORY);
                spec.setStdErrorLocation(FileLocation.MEMORY);
            }
            boolean redirected = spec.getStdOutputLocation().overlaps(
                    FileLocation.MEMORY_AND_LOCAL)
                    || spec.getStdErrorLocation().overlaps(
                            FileLocation.MEMORY_AND_LOCAL);

            if (batchJob && redirected) {
                throw new IllegalSpecException(
                        "Cannot redirect the output/error of a batch job");
            }

            if (redirected
                    || FileLocation.LOCAL.equals(spec.getStdInputLocation())
                    || FileLocation.LOCAL.equals(spec.getExecutableLocation())) {
                this.startGassServer = true;
                String gassURL = startGassServer();
                Bindings subst = new Bindings("rsl_substitution");
                subst.add(new Binding("GLOBUSRUN_GASS_URL", gassURL));
                rsl.add(subst);
            }
            // sets the executable
            if (spec.getExecutable() != null) {
                if (FileLocation.LOCAL.equals(spec.getExecutableLocation())) {
                    rsl.add(new NameOpValue("executable", NameOpValue.EQ,
                            new VarRef("GLOBUSRUN_GASS_URL", null, new Value(
                                    fixAbsPath(spec.getExecutable())))));
                }
                else {
                    rsl.add(new NameOpValue("executable", NameOpValue.EQ, spec
                            .getExecutable()));
                }
            }
            else {
                throw new IllegalSpecException("Missing executable");
            }

            // sets other parameters
            NameOpValue args = new NameOpValue("arguments", NameOpValue.EQ);
            if (!spec.getArgumentsAsList().isEmpty()) {
                Iterator i = spec.getArgumentsAsList().iterator();
                while (i.hasNext()) {
                    if ("condor".equals(jobManager)) {
                        args.add(condorify((String) i.next()));
                    }
                    else {
                        args.add((String) i.next());
                    }
                }
                rsl.add(args);
            }

            if (spec.getDirectory() != null) {
                rsl.add(new NameOpValue("directory", NameOpValue.EQ, spec
                        .getDirectory()));
            }

            Collection environment = spec.getEnvironmentVariableNames();
            if (environment.size() > 0) {
                NameOpValue env = new NameOpValue("environment", NameOpValue.EQ);
                Iterator iterator = environment.iterator();
                while (iterator.hasNext()) {
                    String name = (String) iterator.next();
                    String value = spec.getEnvironmentVariable(name);
                    List l = new LinkedList();
                    l.add(new Value(name));
                    l.add(new Value(value));
                    env.add(l);
                }
                rsl.add(env);
            }

            // sets the stdin
            if (spec.getStdInput() != null) {
                if (FileLocation.LOCAL.equals(spec.getStdInputLocation())) {
                    rsl.add(new NameOpValue("stdin", NameOpValue.EQ,
                            new VarRef("GLOBUSRUN_GASS_URL", null, new Value(
                                    fixAbsPath(spec.getStdInput())))));
                }
                else {
                    rsl.add(new NameOpValue("stdin", NameOpValue.EQ, spec
                            .getStdInput()));
                }
            }

            // if output is to be redirected
            if (FileLocation.MEMORY_AND_LOCAL.overlaps(spec
                    .getStdOutputLocation())) {
                Value v;
                // if no output file is specified, use the stdout
                if (FileLocation.MEMORY.overlaps(spec.getStdOutputLocation())) {
                    v = new Value("/dev/stdout-"
                            + getTask().getIdentity().toString());
                }
                else {
                    v = new Value(fixAbsPath(spec.getStdOutput()));
                }
                rsl.add(new NameOpValue("stdout", NameOpValue.EQ, new VarRef(
                        "GLOBUSRUN_GASS_URL", null, v)));
            }
            else if (spec.getStdOutput() != null) {
                // output on the remote machine
                rsl.add(new NameOpValue("stdout", NameOpValue.EQ, spec
                        .getStdOutput()));
            }
            // if error is to be redirected
            if (FileLocation.MEMORY_AND_LOCAL.overlaps(spec
                    .getStdErrorLocation())) {
                Value v;
                // if no error file is specified, use the stdout
                if (FileLocation.MEMORY.overlaps(spec.getStdErrorLocation())) {
                    v = new Value("/dev/stderr-"
                            + getTask().getIdentity().toString());
                }
                else {
                    v = new Value(fixAbsPath(spec.getStdError()));
                }
                rsl.add(new NameOpValue("stderr", NameOpValue.EQ, new VarRef(
                        "GLOBUSRUN_GASS_URL", null, v)));
            }
            else if (spec.getStdError() != null) {
                // error on the remote machine
                rsl.add(new NameOpValue("stderr", NameOpValue.EQ, spec
                        .getStdError()));
            }

            if (spec.getAttribute("condor_requirements") != null) {
                String requirementString = (String) spec
                        .getAttribute("condor_requirements");
                NameOpValue req = new NameOpValue("condorsubmit",
                        NameOpValue.EQ);
                List l = new LinkedList();
                l.add(new Value("Requirements"));
                l.add(new Value(requirementString));
                req.add(l);
                rsl.add(req);
            }

            Iterator i = spec.getAttributeNames().iterator();
            while (i.hasNext()) {
                try {
                    String key = (String) i.next();
                    String value = (String) spec.getAttribute(key);
                    if (key.equals("condor_requirements")) {
                        continue;
                    }
                    if (key.toLowerCase().equals("maxwalltime")) {
                        value = WallTime.normalize(value, jobManager);
                    }
                    rsl.add(new NameOpValue(key, NameOpValue.EQ, value));
                }
                catch (Exception e) {
                    throw new IllegalSpecException(
                            "Cannot parse the user defined attributes", e);
                }
            }
            return rsl;
        }
    }

    public static final char QUOTE = '\'';

    private String condorify(String s) {
        if (s.indexOf(' ') != -1) {
            StringBuffer sb = new StringBuffer();
            sb.append(QUOTE);
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == QUOTE) {
                    sb.append(QUOTE);
                }
                sb.append(c);
            }
            sb.append(QUOTE);
            return sb.toString();
        }
        else {
            return s;
        }
    }

    private String startGassServer() throws TaskSubmissionException {
        GlobusSecurityContextImpl securityContext = (GlobusSecurityContextImpl) getTask()
                .getService(0).getSecurityContext();
        String gassURL = null;

        try {
            this.gassServer = GassServerFactory.getGassServer(this.credential);
            this.gassServer.registerDefaultDeactivator();
        }
        catch (Exception e) {
            throw new TaskSubmissionException(
                    "Problems while creating a Gass Server", e);
        }

        gassURL = gassServer.getURL();
        this.stdoutStream = new JobOutputStream(this);
        this.stderrStream = new JobOutputStream(this);
        
        String identity = getTask().getIdentity().toString();

        gassServer.registerJobOutputStream("err-"
                + identity, this.stderrStream);
        gassServer.registerJobOutputStream("out-"
                + identity, this.stdoutStream);
        logger.debug("Started the GASS server");
        return gassURL;
    }

    public void statusChanged(GramJob job) {
        int status = job.getStatus();
        switch (status) {
            case GRAMConstants.STATUS_ACTIVE:
                getTask().setStatus(Status.ACTIVE);
                break;
            case GRAMConstants.STATUS_FAILED:
                int errorCode = job.getError();
                Exception e = new GramException(errorCode);
                failTask(null, e);
                break;
            case GRAMConstants.STATUS_DONE:
                getTask().setStatus(Status.COMPLETED);
                break;
            case GRAMConstants.STATUS_SUSPENDED:
                getTask().setStatus(Status.SUSPENDED);
                break;
            case GRAMConstants.STATUS_UNSUBMITTED:
                getTask().setStatus(Status.UNSUBMITTED);
                break;
            default:
                break;
        }
        if ((status == GRAMConstants.STATUS_FAILED) || (status == GRAMConstants.STATUS_DONE)) {
            cleanup();
        }
    }

    private synchronized void cleanup() {
        if (clean) {
            logger.warn("Job cleaned before");
            return;
        }
        clean = true;
        try {
            this.gramJob.removeListener(this);
            CallbackHandlerManager.decreaseUsageCount(this.credential);
            if (gassServer != null) {
                GassServerFactory.decreaseUsageCount(gassServer);
            }
        }
        catch (Exception e) {
            logger.warn("Failed to clean up job", e);
        }
    }

    public void outputChanged(String s) {
        String output = getTask().getStdOutput();
        if (output == null) {
            output = s;
        }
        else {
            output += s;
        }
        getTask().setStdOutput(output);
    }

    public void outputClosed() {
    }

    private SecurityContext getSecurityContext(Task task)
            throws InvalidSecurityContextException {
        SecurityContext sc = task.getService(0).getSecurityContext();
        if (sc == null) {
            // create default credentials
            sc = new GlobusSecurityContextImpl();
            GSSManager manager = ExtendedGSSManager.getInstance();
            try {
                sc.setCredentials(manager
                        .createCredential(GSSCredential.INITIATE_AND_ACCEPT));
            }
            catch (GSSException e) {
                throw new InvalidSecurityContextException(e);
            }
        }
        return sc;
    }

    private String substituteJobManager(String server, String jobmanager)
            throws InvalidServiceContactException {
        if (server.indexOf("/jobmanager-") != -1) {
            // the jobmanager attribute takes precedence
            server = server.substring(0, server.lastIndexOf("/jobmanager-"));
        }
        String lcjm = jobmanager.toLowerCase();
        server = server + "/jobmanager-" + lcjm;
        return server;
    }

    private String extractJobManager(String server) {
        if (server.indexOf("/jobmanager-") != -1) {
            return server.substring(server.lastIndexOf("/jobmanager-") + 1);
        }
        else {
            return null;
        }
    }

    private String fixAbsPath(String path) {
        if (path == null) {
            return null;
        }
        else if (path.startsWith("/") && !path.startsWith("///")) {
            return "//" + path;
        }
        else {
            return path;
        }
    }
}
