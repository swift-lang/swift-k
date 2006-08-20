/*
 * 
 */
package org.globus.cog.gridshell.tasks.examples;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.taskgraph.TaskGraphHandlerImpl;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.TaskGraph;
import org.globus.cog.abstraction.interfaces.TaskGraphHandler;
import org.globus.cog.gridshell.util.CredSupport;
import org.globus.cog.util.ArgumentParser;
import org.globus.cog.util.ArgumentParserException;

/**
 * 
 */
public abstract class AbstractExecutionExample {
    private static final Logger logger = Logger.getLogger(AbstractExecutionExample.class);
    
    public static final String ARG_PROVIDER = "provider";
    public static final String ARG_PORT = "port";
    public static final String ARG_SERVICE_CONTACT = "serviceContact";
    public static final String ARG_HELP = "help";
    
    public static final String DEFAULT_PROVIDER = "gt2";
    public static final int DEFAULT_PORT = -1;
    
    protected ArgumentParser ap;
    
    public abstract TaskGraph createTaskGraph(Object credentials,String provider, String serviceContact,int port)
    	throws Exception;
    
    public abstract void completed(StatusEvent sEvent);
    public abstract void failed(StatusEvent sEvent);
    
    public static Object createCredentials(String provider,String serviceContact) {
        Object result = null;
        if("ssh".equals(provider) || "ftp".equals(provider)) {
            result = new CredSupport(null).getCredentials();
        }
        return result;
    }
    
    public void runExample(String[] args) throws Exception {
        ap = createArgParser();
        ap.parse(args);
        if(ap.isPresent(ARG_HELP)) {
            ap.usage();
        }else {
            try {
                ap.checkMandatory();
            }catch(ArgumentParserException exception) {
                ap.usage();
                throw exception;
            }
	        
	        String provider = ap.getStringValue(ARG_PROVIDER,DEFAULT_PROVIDER);
	        String serviceContact = ap.getStringValue(ARG_SERVICE_CONTACT);
	        int port = ap.getIntValue(ARG_PORT,DEFAULT_PORT);
	                
	        Object credentials = createCredentials(provider,serviceContact);
	        logger.debug("credentials="+credentials);
	        TaskGraph graph = createTaskGraph(credentials,provider,serviceContact,port);
	        graph.addStatusListener(new StatusListener() {
	            /* (non-Javadoc)
	             * @see org.globus.cog.abstraction.interfaces.StatusListener#statusChanged(org.globus.cog.abstraction.impl.common.StatusEvent)
	             */
	            public void statusChanged(StatusEvent sEvent) {
	                logger.debug("GRAPH.status="+sEvent.getStatus().getStatusString());
	                int statusCode = sEvent.getStatus().getStatusCode();
	                if(statusCode == Status.COMPLETED) {
	                    completed(sEvent);
	                }else if(statusCode == Status.FAILED) {
	                    TaskGraph t = (TaskGraph)sEvent.getSource();
	                    logger.error(t.getStatus().getException());
	                    failed(sEvent);
	                }
	            }
	        }); 
	        TaskGraphHandler handler = new TaskGraphHandlerImpl();
	        handler.submit(graph);
        }
    }
    
    public ArgumentParser createArgParser() {
        ArgumentParser result = new ArgumentParser();
        result.addOption(ARG_SERVICE_CONTACT,"the host to execute",ArgumentParser.NORMAL);
        result.addFlag(ARG_HELP,"displays the help");
        result.addOption(ARG_PORT,"the port to execute",ArgumentParser.OPTIONAL);
        result.addOption(ARG_PROVIDER,"the provider - default gt2",ArgumentParser.OPTIONAL);
        return result;
    }
    
    public static void println(Object value) {
        System.out.println(value);
    }
}
