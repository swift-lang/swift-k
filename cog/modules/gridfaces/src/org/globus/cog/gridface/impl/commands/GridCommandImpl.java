
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.commands;

import java.net.URI;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.task.FileOperationSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.FileTransferSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.FileTransferTask;
import org.globus.cog.abstraction.impl.common.task.JobSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.JobSubmissionTask;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.abstraction.interfaces.FileTransferSpecification;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.gridface.interfaces.GridCommand;
/**
 * Base class for all Grid Commands. Follows adapter pattern.
 * Specific commands extend this class to override a few methods
 */
public abstract class GridCommandImpl implements GridCommand {
    private Vector arguments = null;
    private Hashtable attributes = null;
    private Hashtable taskAttributes = null;
    private String command = null;
    private Vector statusListeners = null;
    private int objectType = 0;
    static Logger logger = Logger.getLogger(GridCommandImpl.class.getName());

    private Status status = null;
    protected Task task = null;
    
    private static int idCounter=0;
    private Integer id=null;
//    private Identity id = null;

    public GridCommandImpl() {
        attributes = new Hashtable();
        taskAttributes = new Hashtable();
        arguments = new Vector();
        statusListeners = new Vector();
        //this.status = new StatusImpl();
 
        this.id = new Integer(idCounter++);
//        task = new TaskImpl();
//        id = task.getIdentity();
    }
    /**
     * Set many arguments at a time
     */
    public void setArguments(String[] args) {
        this.arguments = new Vector();
        for (int i = 0; i < args.length; i++) {
            this.arguments.add(args[i]);
        }
    }

    /** Add one argument at a time */
    public void addArgument(String arg) {
        this.arguments.add(arg);
    }

    //TODO isnt Command and Name the same thing?
    /**
     * set command name
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /** Return command name */
    public String getCommand() {
        return command;
    }

    /** Return the nth argument */
    public String getArgument(int n) {
        return (String) arguments.elementAt(n);
    }

    /** Return the total number of arguments */
    public int getArgumentsSize() {
        return arguments.size();
    }

    /**
     * Returns all arguments in a vector. This method enables easy
     * copy of arguments into the specification
     */
    public Vector getArguments() {
        return arguments;
    }


    /** prepare a file operation task */
    public Task prepareFileOperationTask() throws Exception {
    	
    	
        task = new TaskImpl(getCommand(), Task.FILE_OPERATION);
        objectType = Task.FILE_OPERATION;
        task.setProvider((String) getAttribute("provider"));
        ServiceContact serviceContact = null;
        SecurityContext securityContext = null;
        String provider = (String) getAttribute("provider");
        
        if (getAttribute("servicecontact") != null) {
            //task.setServiceContact(
              serviceContact =  (ServiceContact) getAttribute("ServiceContact");
        
        } else {
            //task.setSecurityContext(
            //securityContext =  getDefaultSecurityContext(provider);
        	// TODO: why did nythia earlier not have this symetric? the if sets
        	//       serviceContact, but the else seted securityContext..this was breaking so
        	//       I, rwinch, changed it but do not know if she had done it on purpose for some
        	//       reason
        	serviceContact =  getDefaultServiceContact(provider);
        }
        if (getAttribute("SecurityContext") != null) {
            //task.setSecurityContext(
              securityContext = (SecurityContext) getAttribute("SecurityContext");
        } else {
            //task.setServiceContact(
            //serviceContact =  getDefaultServiceContact(provider);
        	// TODO: why did nythia earlier not have this symetric? the if sets
        	//       securityContext, but the else seted serviceContact this was breaking so
        	//       I, rwinch, changed it but do not know if she had done it on purpose for some
        	//       reason
        	securityContext =  getDefaultSecurityContext(provider);
        }
        logger.debug("sc: " + serviceContact.getContact());
        // now create service and add it
        Service service =
            new ServiceImpl(
                provider,
                Service.FILE_OPERATION,
                serviceContact,
                securityContext);
        
        task.setName(getCommand());
        FileOperationSpecification spec = new FileOperationSpecificationImpl();
        spec.setOperation(getCommand());
        
        Iterator iArgs = getArguments().iterator();
        while(iArgs.hasNext()) {
        	Object arg = iArgs.next();
        	spec.addArgument(String.valueOf(arg));
        }
        Iterator iAttr = getAttributes().keySet().iterator();
        while(iAttr.hasNext()) {
        	Object name = iAttr.next();
        	Object value = getAttributes().get(name);
        	
        	task.setAttribute(String.valueOf(name),value);
        	spec.setAttribute(String.valueOf(name),value);
        }
        
//        spec.setArguments(getArguments());
//        spec.setAttributes(getAttributes());
        task.setSpecification(spec);
        task.setService(Service.JOB_SUBMISSION_SERVICE,service);
        logger.debug("sc22: " + task.getService(0).getServiceContact().getContact());
        logger.debug("sc22: " + task.getService(0).getServiceContact().getHost());
        logger.debug("sc22: " + task.getService(0).getServiceContact().getPort());
        
        
        
//        task.setAttribute("sessionID",getAttribute("SessionId"));
        task.addStatusListener(this);
        return task;
        
        ////////////////
        
        
//    	//TODO cog4 changes
//        //Task task = new TaskImpl(getCommand(), Task.FILE_OPERATION);
//    	this.task = new FileOperationTask(getCommand());
//    	
//        objectType = Task.FILE_OPERATION;
//
//        String provider = (String) getAttribute("provider");
//        
//        ServiceContact serviceContact=null;
//        SecurityContext securityContext =null;
//            
//        
//        //TODO cog4 change
//        task.setProvider(provider);
//        if (getAttribute("servicecontact") != null) {
////            task.setServiceContact(
////                (ServiceContact) getAttribute("ServiceContact"));
//        	serviceContact = (ServiceContact) getAttribute("ServiceContact");
//            
//        } else {
////            task.setSecurityContext(
////                getDefaultSecurityContext((String) getAttribute("provider")));
//        	serviceContact = getDefaultServiceContact(provider);
//        }
//        if (getAttribute("SecurityContext") != null) {
////            task.setSecurityContext(
////                (SecurityContext) getAttribute("SecurityContext"));
//        	securityContext = (SecurityContext) getAttribute("SecurityContext");
//        } else {
////            task.setServiceContact(
////                getDefaultServiceContact((String) getAttribute("provider")));
//        	securityContext = getDefaultSecurityContext(provider);
//        }
//        Service service =
//            new ServiceImpl(
//                provider,
//                Service.FILE_OPERATION,
//                serviceContact,
//                securityContext);
//
//        //TODO this was not there before
//        task.addService(service);
//        
//        //task.setName(getCommand());
//        FileOperationSpecification spec = new FileOperationSpecificationImpl();
//        spec.setCommand(getCommand());
//        spec.setArguments(getArguments());
//        spec.setAttributes(getAttributes());
//        task.setSpecification(spec);
//
//        spec.setSessionId((Identity) getAttribute("SessionId"));
//        task.addStatusListener(this);
//        return this.task;
    }

    /** 
     * Prepares a file transfer task with File transfer specification
     */
    public Task prepareFileTransferTask() throws Exception {
    	//TODO cog4 change
        //Task task = new TaskImpl(getCommand(), Task.FILE_TRANSFER);
    	this.task = new FileTransferTask(getCommand());
    	
    	URI sourceURI = new URI((String) getAttribute("source"));
    	URI destinationURI = new URI((String) getAttribute("destination"));
        
        objectType = Task.FILE_TRANSFER;

        String provider = (String) getAttribute("provider");
		
        //TODO CoG4 changes
        // create te source service
        SecurityContext sourceSecurityContext =
            AbstractionFactory.newSecurityContext(sourceURI.getScheme());
        // selects the default credentials
        sourceSecurityContext.setCredentials(null);

        ServiceContact sourceServiceContact = new ServiceContactImpl();
        sourceServiceContact.setHost(sourceURI.getHost());
        sourceServiceContact.setPort(sourceURI.getPort());

        Service sourceService =
            new ServiceImpl(
                sourceURI.getScheme(),
                Service.FILE_TRANSFER,
                sourceServiceContact,
                sourceSecurityContext);

        // create te destination service
        SecurityContext destinationSecurityContext =
            AbstractionFactory.newSecurityContext(destinationURI.getScheme());
        // selects the default credentials
        destinationSecurityContext.setCredentials(null);

        ServiceContact destinationServiceContact = new ServiceContactImpl();
        destinationServiceContact.setHost(destinationURI.getHost());
        destinationServiceContact.setPort(destinationURI.getPort());

        Service destinationService =
            new ServiceImpl(
                destinationURI.getScheme(),
                Service.FILE_TRANSFER,
                destinationServiceContact,
                destinationSecurityContext);
        
        // add the source service at index 0
        this.task.setService(
            Service.FILE_TRANSFER_SOURCE_SERVICE,
            sourceService);

        // add the destination service at index 1
        this.task.setService(
            Service.FILE_TRANSFER_DESTINATION_SERVICE,
            destinationService);
        
		
        //set provider
        task.setProvider(provider);

        //TODO change for CoG 4
//        // Set security context
//        if (getAttribute("securitycontext") != null) {
//            task.setSecurityContext(
//                (SecurityContext) getAttribute("securitycontext"));
//            
//        } else {
//            task.setSecurityContext(
//                getDefaultSecurityContext((String) getAttribute("provider")));
//        }
//        // set service contact
//        if (getAttribute("servicecontact") != null) {
//            task.setServiceContact(
//                (ServiceContact) getAttribute("servicecontact"));
//        } else {
//            task.setServiceContact(
//                getDefaultServiceContact((String) getAttribute("provider")));
//        }

        
        
        // set specification
        FileTransferSpecification spec = new FileTransferSpecificationImpl();
        //TODO cog4 changes
//        if (getAttribute("sourcehost") != null) {
//            spec.setSourceHost((String) getAttribute("sourcehost"));
//        }
//        if (getAttribute("destinationhost") != null) {
//            spec.setDestinationHost((String) getAttribute("destinationhost"));
//        }
//        if (getAttribute("sourceport") != null) {
//            spec.setSourcePort((String) getAttribute("sourceport"));
//        }
//        if (getAttribute("destinationport") != null) {
//            spec.setDestinationPort((String) getAttribute("destinationport"));
//        }
        if (getAttribute("sourcedirectory") != null) {
            spec.setSourceDirectory((String) getAttribute("sourcedirectory"));
        }
        if (getAttribute("destinationdirectory") != null) {
            spec.setDestinationDirectory(
                (String) getAttribute("destinationdirectory"));
        }
        if (getAttribute("sourcefile") != null) {
            spec.setSourceFile((String) getAttribute("sourcefile"));
        }
        if (getAttribute("destinationfile") != null) {
            spec.setDestinationFile((String) getAttribute("destinationfile"));
        }
        if (getAttribute("source") != null) {
            spec.setSource((String) getAttribute("source"));
        }
        if (getAttribute("destination") != null) {
            spec.setDestination((String) getAttribute("destination"));
        }
        //TODO the new Cog does this automatically
//        if (getAttribute("directorytransfer") != null) {
//            spec.setDirectoryTransfer(
//                new Boolean((String) getAttribute("directorytransfer"))
//                    .booleanValue());
//        }
        if (getAttribute("thirdparty") != null) {
            spec.setThirdParty(
                new Boolean((String) getAttribute("thirdparty"))
                    .booleanValue());
        }
        spec.setSource(sourceURI.getPath());
        spec.setDestination(destinationURI.getPath());
        
        // set task attributes
        Enumeration keys = taskAttributes.keys();
        while (keys != null && keys.hasMoreElements()) {
            spec.setAttribute(
                keys.toString(),
                getTaskAttribute(keys.toString()));
            keys.nextElement();
        }
        
        task.setSpecification(spec);
        
        //add listener
        task.addStatusListener(this);
        return task;
    }

    /**
     *  Prepares job submission task with JobSpecification
     */
    public Task prepareJobSubmissionTask() throws Exception {
    	//TODO cog4 change
        //Task task = new TaskImpl(getCommand(), Task.JOB_SUBMISSION);
    	this.task = new JobSubmissionTask(getCommand());
    	
        objectType = Task.JOB_SUBMISSION;
 
        String provider = (String) getAttribute("provider");
		
        //TODO CoG4 changes,
		Service service = new ServiceImpl(Service.JOB_SUBMISSION);
		service.setProvider(provider.toLowerCase());
		
		
        // Set provider
        task.setProvider(provider);

        // Set security context
        if (getAttribute("securitycontext") != null) {
//        	TODO CoG4 changes,
//            task.setSecurityContext(
//                (SecurityContext) getAttribute("securitycontext"));
        	service.setSecurityContext((SecurityContext) getAttribute("securitycontext"));
        } else {
//        	TODO CoG4 changes,
//            task.setSecurityContext(
//                getDefaultSecurityContext(provider));
            service.setSecurityContext(getDefaultSecurityContext(provider));
        }

        // set service contact
        if (getAttribute("servicecontact") != null) {
//        	TODO CoG4 changes,
//            task.setServiceContact(
//                (ServiceContact) getAttribute("servicecontact"));
            service.setServiceContact((ServiceContact) getAttribute("servicecontact"));
        } else {
//        	TODO CoG4 changes,
//            task.setServiceContact(
//                getDefaultServiceContact((String) getAttribute("provider")));
            service.setServiceContact(getDefaultServiceContact(provider));
        }

        // set specification 
        JobSpecification spec = new JobSpecificationImpl();
        spec.setExecutable((String) getAttribute("executable"));

        if (getAttribute("taskarguments") != null) {
            spec.setArguments((String) getAttribute("taskarguments"));
        }
        if (getAttribute("stdinput") != null) {
            spec.setStdOutput((String) getAttribute("stdinput"));
        }
        if (getAttribute("stdoutput") != null) {
            spec.setStdOutput((String) getAttribute("stdoutput"));
        }
        if (getAttribute("stderror") != null) {
            spec.setStdError((String) getAttribute("stderror"));
        }
        if (getAttribute("directory") != null) {
            spec.setDirectory((String) getAttribute("directory"));
        }
        if (getAttribute("redirected") != null) {
            spec.setRedirected(
                new Boolean((String) getAttribute("redirected"))
                    .booleanValue());
        }
        if (getAttribute("localexecutable") != null) {
            spec.setLocalExecutable(
                new Boolean((String) getAttribute("localexecutable"))
                    .booleanValue());
        }
        if (getAttribute("batchjob") != null) {
            spec.setBatchJob(
                new Boolean((String) getAttribute("batchjob")).booleanValue());
        }

        // set task attributes
        Enumeration keys = taskAttributes.keys();
        while (keys != null && keys.hasMoreElements()) {
            spec.setAttribute(
                keys.toString(),
                getTaskAttribute(keys.toString()));
            keys.nextElement();
        }
        
/** RWINCH added
System.out.println("TEST");
        String path = System.getProperty("user.home") + File.separator
		+".globus"+ File.separator;

		String file = "cog.properties";

		String filePath = path + file;

System.out.println(CoGProperties.getDefault().getIPAddress());

        CoGProperties properties = new CoGProperties();
        
System.out.println(properties.getIPAddress());

		CoGProperties.setDefault(properties);

System.out.println(CoGProperties.getDefault().getIPAddress());

        

end added */

        task.setSpecification(spec);
        task.addService(service);
        
        //add Listener
        task.addStatusListener(this);

        return this.task;
    }

    /**
     * set attributes for the given command
     */
    public void setAttribute(String name, Object value) {
        this.attributes.put(name.toLowerCase(), value);
    }

	public void setAttributes(Hashtable attribs) {
		this.attributes = attribs;

	}
    /** Get the attribute with given name */
    public Object getAttribute(String name) {
        return this.attributes.get(name.toLowerCase());
    }

    /** Get all attributes in the form of a hash table */
    public Hashtable getAttributes() {
        return attributes;
    }
    
	public Integer getId() {
		return this.id;
	}
	
	public Task getTask() {
		return this.task;
	}
	public void setTask(Task newTask) {
		//TODO , 10/8/04 used when loading from xmlfile
		//This needs to be looked into for bugs
		this.task = newTask;
    	setName(newTask.getName());
    	setCommand(newTask.getName());
	}
    /** Add status listener */
    public void addStatusListener(StatusListener listener) {    	
        statusListeners.add(listener);
    }

    /** Remove status listener */
    public void removeStatusListener(StatusListener listener) {
        statusListeners.remove(listener);
    }

    /** Get a list of status listeners */
    public Enumeration getStatusListeners() {
        return statusListeners.elements();
    }

    /** Validate the inputs for this command */
    public boolean validate() {
        if (getAttribute("provider") != null)
            return true;
        else
            return false;
    }

	/** Set status of the command */
	public void setStatus(int status) {
		Status newStatus = new StatusImpl();
		newStatus.setPrevStatusCode(this.status.getStatusCode());
		newStatus.setStatusCode(status);
		this.setStatus(newStatus);
	}

    /** Set status of the command */
    public void setStatus(Status status) {
        this.status = status;

        if (this.statusListeners == null) {
            return;
        }

        int size = this.statusListeners.size();

        StatusEvent event = new StatusEvent(this, this.status);
        for (int i = 0; i < size; i++) {
            StatusListener listener =
                (StatusListener) this.statusListeners.elementAt(i);
            listener.statusChanged(event);
        }

    }

    /** Return status of the command */
    public Status getStatus() {
    	//TODO  change, 10/8/04
    	//return status;
    	return task.getStatus();
    	
    }

    /** Listen to status of the tasks prepared by this command */
    public void statusChanged(StatusEvent event) {
        setStatus(event.getSource().getStatus());
    }

    /** Set id for this command */
    public void setIdentity(Identity id) {
    	//TODO dont need this as each task has its own identity: 
        //this.id = id;
    }

    /** Return id for this command */
    public Identity getIdentity() {
    	//TODO  change
        //return id;
    	return task.getIdentity();
    }

    /** Set name for this command */
    public void setName(String name) {
        this.attributes.put("name", name);
        //TODO  change, 10/8/04
        task.setName(name);
    }

    /** Return name of this command */
    public String getName() {
    	//TODO  change, 10/8/04
        //return (String) this.attributes.get("name");
    	return task.getName();
    }

    /** Get error message of the task belonging to this command */
    public String getError() {
        return task.getStdError();    	 
    }

    /** set attributes for the task */
    public void setTaskAttribute(String key, Object value) {
        taskAttributes.put(key.toLowerCase(), value);
    }

    /** Return task attributes */
    public Object getTaskAttribute(String key) {
        return taskAttributes.get(key.toLowerCase());
    }

    /** Return the default security context. Used by the prepareTask method. */
    private SecurityContext getDefaultSecurityContext(String provider)
        throws Exception {
    	//TODO COG 4 changes, 
//        SecurityContext securityContext =
//            SecurityContextFactory.newSecurityContext(provider);
    	SecurityContext securityContext =AbstractionFactory.newSecurityContext(provider);
        securityContext.setCredentials(null);
        return securityContext;
    }

    /** Return the default service contact. Used by the prepareTask method. */
    private ServiceContact getDefaultServiceContact(String provider)
        throws Exception {
    	//TODO need to refine this , 
        //ServiceContact serviceContact = new ServiceContactImpl(null);
    	ServiceContact serviceContact = new ServiceContactImpl();
    	
        return serviceContact;
    }

    public int getObjectType(){
    	return objectType;
    }

	public Exception getException() {
//		TODO  change, 10/8/04
//	  if(status == null) {
//	  	return null;
//	  }
//	  return status.getException();
		  if(getStatus() == null) {
		  	return null;
		  }
		  return getStatus().getException();
	}
	
	public String getExceptionString() {
		return org.globus.cog.gridface.impl.util.LoggerImpl.getExceptionString(getException());
	}
}
