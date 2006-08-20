// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.ActiveTaskException;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.SecurityContextImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskHandler;

/**
 * The base class for task handlers in all file providers
 */
public class TaskHandlerImpl implements TaskHandler, StatusListener {
    private Vector submittedList = null;
    private Vector activeList = null;
    private Vector suspendedList = null;
    private Vector resumedList = null;
    private Vector failedList = null;
    private Vector canceledList = null;
    private Vector completedList = null;
    private Hashtable handleMap = null;
    private Hashtable activeFileResources;
    private Identity defaultSessionId = null;

    private Vector oneWordCommands = null;
    private Vector twoWordCommands = null;
    private Vector threeWordCommands = null;
    private int type;

    private Task task = null;
    private FileResource fileResource = null;

    static Logger logger = Logger.getLogger(TaskHandlerImpl.class.getName());

    public TaskHandlerImpl() {

        this.submittedList = new Vector();
        this.activeList = new Vector();
        this.suspendedList = new Vector();
        this.resumedList = new Vector();
        this.failedList = new Vector();
        this.canceledList = new Vector();
        this.completedList = new Vector();
        this.handleMap = new Hashtable();
        this.type = TaskHandler.FILE_OPERATION;
        this.activeFileResources = new Hashtable();
        oneWordCommands = new Vector();
        twoWordCommands = new Vector();
        threeWordCommands = new Vector();
        addValidCommands();

    }

    /** set type of task handler */
    public void setType(int type) {
        this.type = type;
    }

    /** return type of task handler */
    public int getType() {
        return this.type;
    }

    /**
     * submit the task for execution. Synchronized because additions and
     * deletions are made to the same data resource
     */
    public synchronized void submit(Task task) throws IllegalSpecException,
            InvalidSecurityContextException, InvalidServiceContactException,
            TaskSubmissionException {
        FileResource fileResource = null;
        if (task.getStatus().getStatusCode() != Status.UNSUBMITTED) {
            throw new TaskSubmissionException(
                    "TaskHandler can execute only UNSUBMITTED tasks.");
        }

        try {
            this.task = task;
            task.setStatus(Status.SUBMITTED);
            FileOperationSpecification spec = (FileOperationSpecification) task
                    .getSpecification();

            if (spec.getOperation().equalsIgnoreCase(
                    FileOperationSpecification.START)) {
                // if the operation is "start", then always create a new
                // FileResource
                submit(task, null);
            } else {

                /*
                 * Obtain the file resource corresponding to the sessionid or
                 * the default resource for all other operations
                 */
                Identity identity = (Identity) task.getAttribute("sessionID");
                if (identity != null) {
                    fileResource = (FileResource) this.activeFileResources
                            .get(identity);
                } else {
                    if (this.defaultSessionId != null) {
                        fileResource = (FileResource) this.activeFileResources
                                .get(this.defaultSessionId);
                    } else {
                        throw new TaskSubmissionException(
                                "No default active connection available");
                    }
                }

                // if found file resource, submit task to file resource
                if (fileResource != null) {
                    submit(task, fileResource);
                } else {
                    throw new TaskSubmissionException(
                            "Corresponding connection not active");
                }
            }
        } catch (Exception e) {
            Status status = new StatusImpl();
            status.setStatusCode(Status.FAILED);
            status.setException(e);
            task.setStatus(status);
            return;
        }
        task.setStatus(Status.COMPLETED);
    }

    /**
     * submit task to the appropriate file resource to carry out the operation.
     * Called from submit(Task)
     * 
     * @throws TaskSubmissionException
     * @throws IllegalSpecException
     * @throws InvalidProviderException
     * @throws ProviderMethodException
     * @throws InvalidSecurityContextException
     * @throws IllegalHostException
     * @throws GeneralException
     * @throws FileNotFoundException
     * @throws DirectoryNotFoundException
     */
    public void submit(Task task, FileResource fileResource)
            throws TaskSubmissionException, IllegalSpecException,
            InvalidProviderException, ProviderMethodException,
            IllegalHostException, InvalidSecurityContextException,
            DirectoryNotFoundException, FileNotFoundException, GeneralException {

        this.task = task;

        FileOperationSpecification spec = (FileOperationSpecification) this.task
                .getSpecification();

        if (task.getStatus().getStatusCode() == Status.CANCELED)
            throw new TaskSubmissionException("Task has been canceled");
        task.setStatus(Status.ACTIVE);

        if (spec.getOperation().equalsIgnoreCase(
                FileOperationSpecification.START)) {
            // check if the security context and service contact attributes are
            // present
            if (!isValidSpecification(spec)) {
                throw new IllegalSpecException(
                        "Invalid security context or service contact");
            }

            String provider = task.getProvider();
            if (provider == null) {
                throw new InvalidProviderException("Provider not available");
            }
            provider = provider.toLowerCase();
            fileResource = AbstractionFactory.newFileResource(provider);
            if (fileResource == null) {
                throw new InvalidProviderException("Invalid provider");
            }

            fileResource.setServiceContact(task.getService(0)
                    .getServiceContact());
            fileResource.setSecurityContext(getSecurityContext());
            fileResource.start();
            Identity sessionId = new IdentityImpl();
            task.setAttribute("output", sessionId);
            this.activeFileResources.put(sessionId, fileResource);
            if (this.defaultSessionId == null) {
                this.defaultSessionId = sessionId;
            }

        } else if (spec.getOperation().equalsIgnoreCase(
                FileOperationSpecification.STOP)) {
            fileResource.stop();
            Identity identity = (Identity) this.task.getAttribute("sessionID");
            this.activeFileResources.remove(identity);
            if (this.defaultSessionId.equals(identity)) {
                this.defaultSessionId = null;
            }

        } else {
            if (!isValidSpecification(spec)) {
                throw new IllegalSpecException(
                        "Either "
                                + spec.getOperation()
                                + " is not a valid command or it is not supported with "
                                + spec.getArgumentSize() + " arguments");
            }
            Object output = execute(fileResource, spec);
            if (output != null) {
                task.setAttribute("output", output);
            }
        }
    }

    /**
     * execute the command on the file resource. Called from submit(Task,
     * FileResource)
     * 
     * @throws GeneralException
     * @throws DirectoryNotFoundException
     * @throws GeneralException
     * @throws FileNotFoundException
     */
    protected Object execute(FileResource fileResource,
            FileOperationSpecification spec) throws DirectoryNotFoundException,
            FileNotFoundException, GeneralException {
        Object output = null;
        String operation = spec.getOperation();
        try {
            //System.err.println(operation + " + " + Thread.currentThread());
            if (operation.equalsIgnoreCase(FileOperationSpecification.LS)
                    && spec.getArgumentSize() == 0) {
                output = fileResource.list();
            } else if (operation
                    .equalsIgnoreCase(FileOperationSpecification.LS)
                    && spec.getArgumentSize() == 1) {
                output = fileResource.list(spec.getArgument(0));
            } else if (operation
                    .equalsIgnoreCase(FileOperationSpecification.PWD)) {
                output = fileResource.getCurrentDirectory();
            } else if (operation
                    .equalsIgnoreCase(FileOperationSpecification.CD)
                    && spec.getArgumentSize() == 1) {
                fileResource.setCurrentDirectory(spec.getArgument(0));
            } else if (operation
                    .equalsIgnoreCase(FileOperationSpecification.MKDIR)
                    && spec.getArgumentSize() == 1) {
                fileResource.createDirectory(spec.getArgument(0));
            } else if (operation
                    .equalsIgnoreCase(FileOperationSpecification.RMDIR)
                    && spec.getArgumentSize() == 2) {
                fileResource.deleteDirectory(spec.getArgument(0), Boolean
                        .valueOf(spec.getArgument(1)).booleanValue());
            } else if (operation
                    .equalsIgnoreCase(FileOperationSpecification.RMFILE)
                    && spec.getArgumentSize() == 1) {
                fileResource.deleteFile(spec.getArgument(0));
            } else if (operation
                    .equalsIgnoreCase(FileOperationSpecification.ISDIRECTORY)
                    && spec.getArgumentSize() == 1) {
                output = Boolean.valueOf(fileResource.isDirectory(spec
                        .getArgument(0)));
            } else if (operation
                    .equalsIgnoreCase(FileOperationSpecification.EXISTS)
                    && spec.getArgumentSize() == 1) {
                output = Boolean.valueOf(fileResource.exists(spec
                        .getArgument(0)));
            } else if (operation
                    .equalsIgnoreCase(FileOperationSpecification.GETFILE)
                    && spec.getArgumentSize() == 2) {
                fileResource.getFile(spec.getArgument(0), spec.getArgument(1));
            } else if (operation
                    .equalsIgnoreCase(FileOperationSpecification.PUTFILE)
                    && spec.getArgumentSize() == 2) {
                fileResource.putFile(spec.getArgument(0), spec.getArgument(1));
            } else if (operation
                    .equalsIgnoreCase(FileOperationSpecification.GETDIR)
                    && spec.getArgumentSize() == 2) {
                fileResource.getDirectory(spec.getArgument(0), spec
                        .getArgument(1));
            } else if (operation
                    .equalsIgnoreCase(FileOperationSpecification.PUTDIR)
                    && spec.getArgumentSize() == 2) {
                fileResource.putDirectory(spec.getArgument(0), spec
                        .getArgument(1));
            } else if (operation
                    .equalsIgnoreCase(FileOperationSpecification.RENAME)
                    && spec.getArgumentSize() == 2) {
                fileResource.rename(spec.getArgument(0), spec.getArgument(1));
            } else if (operation
                    .equalsIgnoreCase(FileOperationSpecification.CHMOD)
                    && spec.getArgumentSize() == 2) {
                fileResource.changeMode(spec.getArgument(0), Integer.valueOf(
                        spec.getArgument(1)).intValue());
            }
            return output;
        } finally {
            //System.err.println(operation + " - " + Thread.currentThread());
        }
    }

    /** is the specification valid */
    public boolean isValidSpecification(FileOperationSpecification spec) {

        if (spec.getOperation().equalsIgnoreCase(
                FileOperationSpecification.START) == true) {
            if (task.getService(0).getServiceContact() == null) {
                return false;
            }
            if (task.getService(0).getSecurityContext() == null) {
                return false;
            }
        }

        if ((spec.getArgumentSize() == 0)
                && !oneWordCommands.contains(spec.getOperation())) {
            return false;
        } else if ((spec.getArgumentSize() == 1)
                && !twoWordCommands.contains(spec.getOperation())) {
            return false;
        } else if ((spec.getArgumentSize() == 2)
                && !threeWordCommands.contains(spec.getOperation())) {
            return false;
        } else {
            return true;
        }
    }

    /** Suspend a task */
    public void suspend(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException {
    }

    /** resume a task */
    public void resume(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException {
    }

    /** cancel a task */
    public void cancel(Task task) throws InvalidSecurityContextException,
            TaskSubmissionException {
        if (task.getStatus().getStatusCode() >= Status.FAILED) {
            throw new TaskSubmissionException(
                    "Cancel cannot be performed on tasks that are not active");
        }
        if (task.getStatus().getStatusCode() <= Status.SUBMITTED) {
            task.setStatus(Status.CANCELED);
        } else {
            try {
                String currentDirectory = fileResource.getCurrentDirectory();
                fileResource.stop();
                fileResource.start();
                fileResource.setCurrentDirectory(currentDirectory);
                task.setStatus(Status.CANCELED);
            } catch (Exception e) {
                throw new TaskSubmissionException(
                        "Exception in File TaskHandler while performing cancel",
                        e);
            }
        }
    }

    /** remove a task completely */
    public void remove(Task task) throws ActiveTaskException {
        if (!handleMap.containsKey(task)) {
            return;
        }
        int status = task.getStatus().getStatusCode();
        if ((status == Status.ACTIVE) || (status == Status.SUSPENDED)) {
            throw new ActiveTaskException(
                    "Cannot remove an active or suspended Task");
        } else {
            this.failedList.remove(task);
            this.canceledList.remove(task);
            this.completedList.remove(task);
            this.activeList.remove(task);
            this.suspendedList.remove(task);
            this.submittedList.remove(task);
            this.handleMap.remove(task);
        }
    }

    /** return a collection of all tasks submitted to the handler */
    public Collection getAllTasks() {
        try {
            return new ArrayList(handleMap.keySet());
        } catch (Exception e) {
            return null;
        }
    }

    /** return a collection of active tasks */
    public Collection getActiveTasks() {
        return new ArrayList(this.activeList);
    }

    /** return a collection of failed tasks */
    public Collection getFailedTasks() {
        return new ArrayList(this.failedList);
    }

    /** return a collection of completed tasks */
    public Collection getCompletedTasks() {
        return new ArrayList(this.completedList);
    }

    /** return a collection of suspended tasks */
    public Collection getSuspendedTasks() {
        return new ArrayList(this.suspendedList);
    }

    /** return a collection of resumed tasks */
    public Collection getResumedTasks() {
        return new ArrayList(this.resumedList);
    }

    /** return a collection of canceled tasks */
    public Collection getCanceledTasks() {
        return new ArrayList(this.canceledList);
    }

    /** listen to the status changes of the task */
    public void statusChanged(StatusEvent event) {
        Task task = (Task) event.getSource();
        Status status = event.getStatus();
        int prevStatus = status.getPrevStatusCode();
        int curStatus = status.getStatusCode();
        switch (prevStatus) {
            case Status.SUBMITTED:
                this.submittedList.remove(task);
                break;
            case Status.ACTIVE:
                this.activeList.remove(task);
                break;
            case Status.SUSPENDED:
                this.suspendedList.remove(task);
                break;
            case Status.RESUMED:
                this.resumedList.remove(task);
                break;
            case Status.FAILED:
                this.failedList.remove(task);
                break;
            case Status.CANCELED:
                this.canceledList.remove(task);
                break;
            case Status.COMPLETED:
                this.completedList.remove(task);
                break;
            default:
                break;
        }
        if (task != null) {
            switch (curStatus) {
                case Status.SUBMITTED:
                    this.submittedList.add(task);
                    break;
                case Status.ACTIVE:
                    this.activeList.add(task);
                    break;
                case Status.SUSPENDED:
                    this.suspendedList.add(task);
                    break;
                case Status.RESUMED:
                    this.resumedList.add(task);
                    break;
                case Status.FAILED:
                    this.failedList.add(task);
                    break;
                case Status.CANCELED:
                    this.canceledList.add(task);
                    break;
                case Status.COMPLETED:
                    this.completedList.add(task);
                    break;
                default:
                    break;
            }
        }
    }

    /** Add a list of valid commands to a vector */
    public void addValidCommands() {
        // Add all one word commands
        oneWordCommands.add(FileOperationSpecification.START);
        oneWordCommands.add(FileOperationSpecification.STOP);
        oneWordCommands.add(FileOperationSpecification.LS);
        oneWordCommands.add(FileOperationSpecification.PWD);

        // Add all two word commands
        twoWordCommands.add(FileOperationSpecification.LS);
        twoWordCommands.add(FileOperationSpecification.MKDIR);
        twoWordCommands.add(FileOperationSpecification.RMDIR);
        twoWordCommands.add(FileOperationSpecification.RMFILE);
        twoWordCommands.add(FileOperationSpecification.EXISTS);
        twoWordCommands.add(FileOperationSpecification.CD);
        twoWordCommands.add(FileOperationSpecification.ISDIRECTORY);

        // Add all three word commands
        threeWordCommands.add(FileOperationSpecification.RMDIR);
        threeWordCommands.add(FileOperationSpecification.GETFILE);
        threeWordCommands.add(FileOperationSpecification.PUTFILE);
        threeWordCommands.add(FileOperationSpecification.GETDIR);
        threeWordCommands.add(FileOperationSpecification.PUTDIR);
        threeWordCommands.add(FileOperationSpecification.RENAME);
        threeWordCommands.add(FileOperationSpecification.CHMOD);
    }

    private SecurityContext getSecurityContext() {
        SecurityContext securityContext = this.task.getService(0)
                .getSecurityContext();
        if (securityContext == null) {
            // create default credentials
            securityContext = new SecurityContextImpl();
        }
        return securityContext;
    }

    protected FileResource getResource() {
        return this.fileResource;
    }
}
