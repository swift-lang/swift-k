
package org.globus.cog.gridshell.tasks.examples;


import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.taskgraph.TaskGraphHandlerImpl;
import org.globus.cog.abstraction.impl.common.taskgraph.TaskGraphImpl;
import org.globus.cog.abstraction.interfaces.TaskGraph;
import org.globus.cog.abstraction.interfaces.TaskGraphHandler;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.gridshell.model.ScopeImpl;
import org.globus.cog.gridshell.tasks.AbstractTask;
import org.globus.cog.gridshell.tasks.ExecTask;
import org.globus.cog.util.ArgumentParser;

/**
 */
public class EchoExample extends AbstractExecutionExample {
    private static final Logger logger = Logger.getLogger(MatrixMultiplyImpl.class);
    
    public static final String ARG_MAX = "max";
    
    private Scope scope = new ScopeImpl();
    private String executable = "/bin/echo";
    private String arguments = "hi";
    
    
    public static void main(String[] args) {
        try {
            new EchoExample().runExample(args);
        }catch(Exception e) {
            logger.fatal("Fatal error in main",e);
            System.exit(1);
        }
    }
    
    public ArgumentParser createArgParser() {
        ArgumentParser result = super.createArgParser();        
        result.addOption(ARG_MAX,"the number of times that it will echo (the number of tasks to create)",ArgumentParser.NORMAL);
        return result;
    }
    
    public void completed(StatusEvent sEvent) {
        logger.debug("Graph COMPLETED!!");
        showOutput(scope,getMax());
        System.exit(0);
    }
    public void failed(StatusEvent sEvent) {
        logger.debug("Graph FAILED!!");
        System.exit(1);
    }
    
    public static void showOutput(Scope scope, int MAX) {
        for(int i=0;i<MAX;i++) {
           println("i="+i);
           println(scope.getValue(String.valueOf(i)));
	    }       
    }
    
    public int getMax() {
        return ap.getIntValue(ARG_MAX);
    }
    
    public TaskGraph createTaskGraph(Object credentials, String provider, String serviceContact, int port) throws Exception {
        TaskGraph graph = new TaskGraphImpl();
        TaskGraphHandler handler = new TaskGraphHandlerImpl();
        final int MAX = getMax();
        AbstractTask t;
        for(int i=0;i<MAX;i++) {
            logger.debug("i="+i);
	        t = new ExecTask("EchoTask",credentials,provider,serviceContact,port,executable,arguments+i);
	        
	        t.addScopeStatusListener(scope,String.valueOf(i));
	        t.initTask();
	        graph.add(t);
	    }

        return graph;
    } 
}
