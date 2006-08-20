/*
 * 
 */
package org.globus.cog.gridshell.tasks.test;

import java.net.PasswordAuthentication;

import org.apache.log4j.Logger;
import org.globus.cog.gridshell.tasks.AbstractFileOperationTask;
import org.globus.cog.gridshell.tasks.CdTask;
import org.globus.cog.gridshell.tasks.LsTask;
import org.globus.cog.gridshell.tasks.MkdirTask;
import org.globus.cog.gridshell.tasks.PwdTask;
import org.globus.cog.gridshell.tasks.RmdirTask;
import org.globus.cog.gridshell.tasks.StartTask;
import org.globus.cog.gridshell.tasks.StopTask;

/**
 * 
 */
public class FileOperationTest {
	private static final Logger logger = Logger.getLogger(FileOperationTest.class);
	public static void main(String[] args) {
		try {
//			cd();
//			ftp();
			gsiftp();
		}catch(Exception e) {
			logger.fatal("Exception in main",e);
			System.exit(1);
		}
	}
	
	public static void ftp() throws Exception {
		logger.info("ftp()");
		
		Object result = null;
		AbstractFileOperationTask operation;
		
		StartTask connection = new StartTask(new PasswordAuthentication("anonymous","".toCharArray()),"ftp","ftp.mcs.anl.gov",21);
		connection.initTask();
		result = connection.submitAndWait();
		logger.debug("Result: "+result);
	
		operation = new StopTask(connection);
		operation.initTask();
		result = operation.submitAndWait();
		logger.debug("Result: "+result);
	}
	
	public static void cd() throws Exception {
		logger.info("gsiftp()");
		
		Object result = null;
		AbstractFileOperationTask operation;
		
		StartTask connection = new StartTask(null,"gsiftp","wiggum.mcs.anl.gov",2811);
		connection.initTask();
		result = connection.submitAndWait();
		logger.debug("Result: "+result);
		
		operation = new CdTask(connection,"tmprob");
		operation.initTask();
		result = operation.submitAndWait();
		logger.debug("Result: "+result);
		
		operation = new LsTask(connection,null);
		operation.initTask();
		result = operation.submitAndWait();
		logger.debug("Result: "+result);
		
		operation = new StopTask(connection);
		operation.initTask();
		result = operation.submitAndWait();
		logger.debug("Result: "+result);
	}
	
	public static void gsiftp() throws Exception {
		logger.info("gsiftp()");
		
		Object result = null;
		AbstractFileOperationTask operation;
		
		StartTask connection = new StartTask(null,"gsiftp","wiggum.mcs.anl.gov",2811);
		connection.initTask();
		result = connection.submitAndWait();
		logger.debug("Result: "+result);
		
		operation = new LsTask(connection,null);
		operation.initTask();
		result = operation.submitAndWait();
		logger.debug("Result: "+result);
		
		operation = new PwdTask(connection);
		operation.initTask();
		result = operation.submitAndWait();
		logger.debug("Result: "+result);
		
		operation = new MkdirTask(connection,"tempFileOperationTest");
		operation.initTask();
		result = operation.submitAndWait();
		logger.debug("Result: "+result);
		
		operation = new CdTask(connection,"tempFileOperationTest");
		operation.initTask();
		result = operation.submitAndWait();
		logger.debug("Result: "+result);
		
		operation = new PwdTask(connection);
		operation.initTask();
		result = operation.submitAndWait();
		logger.debug("Result: "+result);
		
		operation = new CdTask(connection,"..");
		operation.initTask();
		result = operation.submitAndWait();
		logger.debug("Result: "+result);
		
		operation = new PwdTask(connection);
		operation.initTask();
		result = operation.submitAndWait();
		logger.debug("Result: "+result);
		
		operation = new RmdirTask(connection,"tempFileOperationTest",true);
		operation.initTask();
		result = operation.submitAndWait();		
		logger.debug("Result: "+result);
		
		operation = new LsTask(connection);
		operation.initTask();
		result = operation.submitAndWait();
		logger.debug("Result: "+result);
		
		operation = new StopTask(connection);
		operation.initTask();
		result = operation.submitAndWait();
		logger.debug("Result: "+result);		
	}
}
