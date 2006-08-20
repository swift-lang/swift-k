/*
 * 
 */
package org.globus.cog.gridshell.tasks.examples;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.taskgraph.TaskGraphHandlerImpl;
import org.globus.cog.abstraction.impl.common.taskgraph.TaskGraphImpl;
import org.globus.cog.abstraction.interfaces.TaskGraph;
import org.globus.cog.abstraction.interfaces.TaskGraphHandler;
import org.globus.cog.gridshell.GridShellProperties;
import org.globus.cog.gridshell.interfaces.Scope;
import org.globus.cog.gridshell.model.ScopeImpl;
import org.globus.cog.gridshell.tasks.AbstractTask;
import org.globus.cog.gridshell.tasks.ExecTask;
import org.globus.cog.gridshell.tasks.PutfileTask;
import org.globus.cog.gridshell.tasks.StartTask;
import org.globus.cog.gridshell.tasks.StopTask;
import org.globus.cog.util.ArgumentParser;

/**
 * <p>
 * Transfers MMRowXCol.java using gsiftp to a remote location, compiles it, and
 * then does a distributed Matrix Multiply. This example could easily add other
 * hosts if desired
 * </p>
 */
public class MatrixMultiplyImpl extends AbstractExecutionExample {
    private static final Logger logger = Logger.getLogger(MatrixMultiplyImpl.class);
    
    public static final String ARG_RLOCATION = "remote-location";
    public static final String ARG_JAVAHOME = "javahome";
    
    public static final String EXAMPLE_PATH = GridShellProperties.getDefault().getProperty("gridshell.examples.basepath");
    
    public static final String JAVA_CLASS = "MMRowXCol";
    
    public static final String SPACE = " ";
    public static final String TAB = "\t";
    
    private Scope[] scopes;
    
    public static final int[][] MATRIX_A = {
        {1,2,3,5},
        {4,5,8,8},
        {2,5,1,6},
        {5,2,3,5}
    };
    
    public static final int[][] MATRIX_B = {
        {3,2,8,3},
        {2,5,4,1},
        {3,6,3,3},
        {1,5,3,7},
    };
    
    public ArgumentParser createArgParser() {
        ArgumentParser result = super.createArgParser();
        result.addOption(ARG_JAVAHOME,"java home of remote machine",ArgumentParser.NORMAL);
        result.addOption(ARG_RLOCATION,"the location you want MMRowXCol to go and be executed",ArgumentParser.NORMAL);
        return result;
    }
    
    public String getJavaHome() {
        return ap.getStringValue(ARG_JAVAHOME);
    }
    public String getRemoteLocation() {
        return ap.getStringValue(ARG_RLOCATION);
    }
    
    public void transferFile(String serviceContact) 
    		throws Exception {
        String FILE_NAME = JAVA_CLASS+".java";
        String source = EXAMPLE_PATH+FILE_NAME;
        String destination = getRemoteLocation()+FILE_NAME;
                
        logger.debug("source="+source);
        logger.debug("destination="+destination);
        
        StartTask connection = new StartTask(null,"gsiftp",serviceContact,-1);
        connection.initTask();
        connection.submitAndWait();
        AbstractTask transfer = new PutfileTask(connection,source,destination);
        transfer.initTask();
        transfer.submitAndWait();
        AbstractTask close = new StopTask(connection);
        close.initTask();
        close.submitAndWait();        
    }  
    public static void outputMatrix(Scope[] scopes) {
        int rows = scopes.length;
        for(int r=0;r<rows;r++) {
            int col = scopes[r].getVariableNames().size();
            for(int c=0;c<col;c++) {
                Object val = scopes[r].getValue(String.valueOf(c));
                System.out.print("'"+val+"'");
                System.out.print(TAB);
            }
            System.out.println();
        }
    }
    
    public static String getRow(int[][] a,int row) {
        logger.debug("row="+row);
        StringBuffer result = new StringBuffer();
        for(int c=0;c<a[0].length;c++) {
            int i = a[row][c];
            result.append(i);
            result.append(SPACE);
        }
        logger.debug("result="+result.toString());
        return result.toString();
    }
    
    public static String getCol(int[][] b,int col) {
        logger.debug("col="+col);
        StringBuffer result = new StringBuffer();
        for(int r=0;r<b.length;r++) {
            logger.debug("r="+r);
            int i = b[r][col];
            result.append(i);
            if(r<b.length-1) {
                result.append(SPACE);
            }
        }
        logger.debug("result="+result.toString());
        return result.toString();
    }

    /* (non-Javadoc)
     * @see org.globus.cog.gridshell.tasks.examples.AbstractExecutionExample#createTaskGraph(java.lang.Object, java.lang.String, java.lang.String, int)
     */
    public TaskGraph createTaskGraph(Object credentials, String provider, String serviceContact, int port) throws Exception {
        transferFile(serviceContact);       
        
        TaskGraph graph = new TaskGraphImpl();
        TaskGraphHandler handler = new TaskGraphHandlerImpl();
        
        final int row = MATRIX_A.length;
        final int col = MATRIX_B.length;
        
        logger.debug("col="+col);
        logger.debug("row="+row);
        
        logger.debug("credentials="+credentials);
        
        scopes = new Scope[row];
        
        // create the compile task
        String executable = getJavaHome()+"bin/javac";
        String arguments = "-sourcepath "+getRemoteLocation()+" "+JAVA_CLASS+".java";       
        AbstractTask compile = new ExecTask("MMRowXColCompile",credentials,provider,serviceContact,port,executable,arguments);
        compile.initTask();
        graph.add(compile);
        
        // the rest are going to be running MMRowXCol
        executable = getJavaHome()+"bin/java";
        String runArguments = "-classpath "+getRemoteLocation()+" "+JAVA_CLASS+" ";
        AbstractTask t;
        for(int r=0;r<row;r++) {
            Scope scope = scopes[r] = new ScopeImpl();
            String rowString = getRow(MATRIX_A,r);
            for(int c=0;c<row;c++) {
                String colString = getCol(MATRIX_B,c);
                arguments = runArguments+rowString+colString;
                logger.debug("args="+arguments);                
    	        t = new ExecTask("MMTaskName",credentials,provider,serviceContact,port,executable,arguments);
    	        t.addScopeStatusListener(scope,String.valueOf(c));
    	        t.initTask();
    	        graph.add(t);
    	        graph.addDependency(compile,t);
            }
        }
        return graph;
    }    
    /* (non-Javadoc)
     * @see org.globus.cog.gridshell.tasks.examples.AbstractExecutionExample#completed(org.globus.cog.abstraction.impl.common.StatusEvent)
     */
    public void completed(StatusEvent sEvent) {
        outputMatrix(scopes);
        System.exit(0);
    }
    /* (non-Javadoc)
     * @see org.globus.cog.gridshell.tasks.examples.AbstractExecutionExample#failed(org.globus.cog.abstraction.impl.common.StatusEvent)
     */
    public void failed(StatusEvent sEvent) {
        System.exit(1);
    }    
    
    public static void main(String[] args) {
        try {
            new MatrixMultiplyImpl().runExample(args);
        }catch(Exception e) {
            logger.fatal("Fatal error in main",e);
            System.exit(1);
        }        
    }
}
