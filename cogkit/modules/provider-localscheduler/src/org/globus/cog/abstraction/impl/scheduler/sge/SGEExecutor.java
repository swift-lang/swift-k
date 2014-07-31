/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.scheduler.sge;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractExecutor;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractQueuePoller;
import org.globus.cog.abstraction.impl.scheduler.common.Job;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessException;
import org.globus.cog.abstraction.impl.scheduler.common.ProcessListener;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.gsi.gssapi.auth.AuthorizationException;

/**
 * Java CoG interface for Sun/Oracle Grid Engine
 */
public class SGEExecutor extends AbstractExecutor {

    public static final Pattern JOB_ID_LINE = Pattern.compile(".*[Yy]our job (\\d+) \\(.*\\) has been submitted");
	public static final Logger logger = Logger.getLogger(SGEExecutor.class);
    private static QueuePoller poller;
    private static final String[] QSUB_PARAMS = new String[] {"-S", "/bin/bash"};
    private static int unique = 0; 
    private static NumberFormat IDF = new DecimalFormat("000000");
    
    public SGEExecutor(Task task, ProcessListener listener) {
        super(task, listener);
        verifyQueueInformation();
    }
    
    /**
     * Create a new Job
     * @param jobid - String representing SGE job ID
     * @param stdout
     * @param stdOutputLocation
     * @param stderr
     * @param stdErrorLocation
     * @param exitcode
     * @param executor
     */
    protected Job createJob(String jobid, String stdout,
            FileLocation stdOutputLocation, String stderr,
            FileLocation stdErrorLocation, String exitcode,
            AbstractExecutor executor) {
        return new Job(jobid, stdout, stdOutputLocation, stderr,
            stdErrorLocation, exitcode, executor);
    }

    /**
     * Return additional submit parameters
     * @return String representing qsub parameters
     */
    protected String[] getAdditionalSubmitParameters() {
        return QSUB_PARAMS;
    }

    /**
     * Get an attribute from a job specification
     * If the attribute does not exist, return the default value
     * @param spec JobSpecification to search
     * @param name Attribute to search for
     * @param defaultValue This value is return if the attribute is not found
     * @return String representing an attribute (if found) or the default value
     */
    private String getAttribute(JobSpecification spec, String name,
            String defaultValue) {
        Object value = spec.getAttribute(name);
        if (value == null) {
            return defaultValue;
        }
        else {
            return value.toString();
        }
    }

    /** 
     * getName - Return the name of this provider
     * @return String representing provider name
     */
    protected String getName() {
        return "SGE";
    }
    
    /**
     * getProperties - Return SGE properties
     * @return Properties as an AbstractProperties object
     */
    protected AbstractProperties getProperties() {
        return Properties.getProperties();
    }

    /**
     * getQueuePoller - return the Queue Poller
     * @return AbstractQueuePoller
     */
    protected AbstractQueuePoller getQueuePoller() {
        synchronized (SGEExecutor.class) {
            if (poller == null) {
                poller = new QueuePoller(getProperties());
                poller.start();
            }
            return poller;
        }
    }

    /**
     * getSGEProperties - Return SGE properties
     * @return Properties as a Properties object
     */
    protected Properties getSGEProperties() {
        return (Properties) getProperties();
    }

    /**
     * Create SGE job name
     * @param task
     * @return String containing task name
     */
	private String makeName(Task task) {
		String name = task.getName();
		if (name == null) {
			int i = 0;
			synchronized(SGEExecutor.class) {
				i = unique++;
			}
			name = "cog-" + IDF.format(i);
		}
		else if (name.length() > 15) {
		    name = name.substring(0, 15);
		}
		if (logger.isDebugEnabled()) {
		    logger.debug("SGE name: for: " + task.getName() + 
                         " is: " + name);
		}
		return name;
	}
    
    /**
     * parseSubmitCommandOutput - Given qsub output, return job ID
     * @param out - String that contains qsub output
     * @return String containing job id
     * @throws IOException
     */
    protected String parseSubmitCommandOutput(String out) throws IOException {
        // > your job 2494189 ("t1.sub") has been submitted
        BufferedReader br = new BufferedReader(new CharArrayReader(out.toCharArray()));
        String line = br.readLine();
        while (line != null) {
            Matcher m = JOB_ID_LINE.matcher(line);
            if (m.matches()) {
                String id = m.group(1);
                if (logger.isInfoEnabled()) {
                    logger.info("Job id from qsub: " + id);
                }
                return id;
            }
            line = br.readLine();
        }
        throw new IOException("None of the qsub lines matches the required patten: " + JOB_ID_LINE);
    }

    /**
     * @see AbstractExecutor#start()
     */
    public void start() throws AuthorizationException,
           IOException, ProcessException {
    	try {
    		Thread.sleep(Integer.valueOf(getSGEProperties().getSubmissionDelay()));
    	}
    	catch (InterruptedException e) {
    		logger.error(e.getStackTrace());
    	}
    	super.start();
    }
    
    /**
     * Check that job specification values are valid for this system
     * @throws IllegalArgumentException
     */
    private void verifyQueueInformation() {

    	// A queue must be defined in order to gather information about it
    	poller = (QueuePoller) getQueuePoller();
    	JobSpecification spec = getSpec();
        String queue = (String) spec.getAttribute("queue");
        if(queue == null) {
        	logger.error("Error: No queue defined");
        	return;
        }

    	QueueInformation qi = poller.getQueueInformation(queue);
    	String error="";
        
        // Verify the queue is available
    	if(!poller.isValidQueue(queue)) {
    		error = "Invalid queue \"" + queue + "\"\nAvailable queues are: ";
    		for(String s : poller.getAllQueues()) {
    			error += s + " ";
    		}
    		logger.error(error);
    		return;
    	}
    	                
        // Check that pe is defined
        String pe = (String) spec.getAttribute("pe");
        if(pe == null) {
        	error = "Error: No parallel environment specified";
        	logger.error(error);
        	return;
        }
        
        // Check that pe is available for the queue
        if(!qi.getPe_list().contains(pe)) {
        	error = "Parallel environment " + pe + " is not valid for " + queue + " queue\n";
        	error += "Valid PEs are: ";
        	for(String s : qi.getPe_list()) {
        		error += s + " ";
        	}
        	logger.error(error);
        	return;
        }
     
        // Check that requested walltime fits into time limits
        String maxWalltimeAttribute = (String) spec.getAttribute("maxwalltime");
        if(maxWalltimeAttribute != null) {
        	int requestedWalltimeSeconds = WallTime.timeToSeconds(maxWalltimeAttribute);
        	String queueWalltimeString = qi.getWalltime();
        	if(!queueWalltimeString.equalsIgnoreCase("INFINITY")) {
        		int queueWalltime = WallTime.timeToSeconds(queueWalltimeString);
        		if(requestedWalltimeSeconds > queueWalltime) {
        			error = "Requested wall time of " + requestedWalltimeSeconds
        				+ " seconds is greater than queue limit of " + queueWalltime;
        		}
            }        	
        }
        
        // Give a warning if CPUs are being underutilized (this may be intentional due to memory restrictions)
        Object jobsPerNodeAttribute = spec.getAttribute("jobsPerNode");
        if(jobsPerNodeAttribute != null) {
        	String jobsPerNode = String.valueOf(jobsPerNodeAttribute);
        	if(Integer.valueOf(jobsPerNode) < qi.getSlots()) {
        		if(logger.isInfoEnabled()) {
        			logger.info("Requesting only " + jobsPerNode + "/" + qi.getSlots() + " CPUs per node");
        		}
        	}
        }
    }
    

    /**
     * writeAttr - Write a specification attribute to a submit file
     * If the attribute is not found, use null
     * @param attrName Specification attribute to write
     * @param arg The SGE argument (eg. -N for the job name)
     * @param wr A Writer object representing the submit file
     * @throws IOException
     */
    protected void writeAttr(String attrName, String arg, Writer wr)
            throws IOException {
    	
    	writeAttr(attrName, arg, wr, null);
    }



    /**
     * writeAttr - Write a specification attribute to a submit file
     * 
     * @param attrName Specification attribute to write
     * @param arg The SGE argument (eg. -N for the job name)
     * @param wr A Writer object representing the submit file
     * @param defaultValue If the requested attribute is not found, use this default value
     * @throws IOException
     */
    protected void writeAttr(String attrName, String arg, Writer wr,
            String defaultValue) throws IOException {

    	Object value = getSpec().getAttribute(attrName);
        if (value != null) {
            wr.write("#$ " + arg + String.valueOf(value) + '\n');
        }
        else if (defaultValue != null) {
            wr.write("#$ " + arg + String.valueOf(defaultValue) + '\n');
        }
    }

    /**
     * writeMultiJobPreamble - Add multiple jobs to a single submit file
     * @param wr Writer A Writer object representing the submit file
     * @param exitcodefile Filename where application exit code should be written
     * @throws IOException
     */
    protected void writeMultiJobPreamble(Writer wr, String exitcodefile)
            throws IOException {
        wr.write("NODES=`cat $PE_HOSTFILE | awk '{print $1}'`\n");
        wr.write("ECF=" + exitcodefile + "\n");
        wr.write("INDEX=0\n");
        wr.write("for NODE in $NODES; do\n");
        wr.write("  echo \"N\" >$ECF.$INDEX\n");
        wr.write("  ssh $NODE /bin/bash -c \\\" \"");
    }


    /**
     * writeScript - Write the SGE submit script
     * @param wr A Writer object representing the submit file
     * @param exitcodefile Filename where exit code will be written
     * @param stdout Filename where standard output will be written
     * @param stderr Filename where standard error will be written
     * @throws IOException
     */
    protected void writeScript(Writer wr, String exitcodefile, String stdout,
            String stderr) throws IOException {

    	Task task = getTask();
        JobSpecification spec = getSpec();
		
        String type = (String) spec.getAttribute("jobType");
        boolean multiple = false;
        if ("multiple".equals(type)) {
            multiple = true;
        }
        
        int count = Integer.valueOf(getAttribute(spec, "count", "1"));
        String queue = (String)spec.getAttribute("queue");
        
        int coresPerNode = Integer.valueOf(getAttribute(spec, "coresPerNode", 
        		String.valueOf(poller.getQueueInformation(queue).getSlots())));
        int jobsPerNode = Integer.valueOf(getAttribute(spec, "jobsPerNode", 
        		String.valueOf(coresPerNode)));
        int coresToRequest = ( count * jobsPerNode + coresPerNode - 1) / coresPerNode * coresPerNode;
   
        wr.write("#!/bin/bash\n");
        wr.write("#$ -N " + makeName(task) + '\n');
        wr.write("#$ -V\n");
        writeAttr("project", "-A ", wr);
    	writeAttr("queue", "-q ", wr);
                
        String peValue = "-pe " +  getAttribute(spec, "pe", getSGEProperties().getDefaultPE()) + " ";
        writeAttr("null", peValue, wr, String.valueOf(coresToRequest));
        	
        writeWallTime(wr);
        writeSoftWallTime(wr);
        
        if (spec.getStdInput() != null) {
            wr.write("#$ -i " + quote(spec.getStdInput()) + '\n');
        }
        wr.write("#$ -o " + quote(stdout) + '\n');
        wr.write("#$ -e " + quote(stderr) + '\n');

        if (!spec.getEnvironmentVariableNames().isEmpty()) {
            wr.write("#$ -v ");
            Iterator<String> i = spec.getEnvironmentVariableNames().iterator();
            while (i.hasNext()) {
                String name = i.next();
                wr.write(name);
                wr.write('=');
                wr.write(quote(spec.getEnvironmentVariable(name)));
                if (i.hasNext()) {
                    wr.write(',');
                }
            }
            wr.write('\n');
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Job type: " + type);
        }
        
        if (type != null) {
            String wrapper = Properties.getProperties().getProperty(
                "wrapper." + type);
            if (logger.isDebugEnabled()) {
                logger.debug("Wrapper: " + wrapper);
            }
            if (wrapper != null) {
                wrapper = replaceVars(wrapper);
                wr.write(wrapper);
                wr.write(' ');
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Wrapper after variable substitution: " + wrapper);
            }
        }

        wr.write("\nwrite_exitcode()\n");
        wr.write("{\n");
        wr.write("echo $1 > " + exitcodefile + "\n");
        wr.write("exit 0\n"); 
        wr.write("}\n\n");

        wr.write("# Trap all signals\n");
        wr.write("SIGNAL=1\n");
        wr.write("while [ $SIGNAL -le 30 ];\n");
        wr.write("do\n");
        wr.write("   trap \"echo Received signal $SIGNAL; write_exitcode $SIGNAL\" $SIGNAL\n");
        wr.write("   (( SIGNAL+=1 ))\n");
        wr.write("done\n\n");

        if (spec.getDirectory() != null) {
            wr.write("cd " + quote(spec.getDirectory()) + " && ");
        }

        if (multiple) {
            writeMultiJobPreamble(wr, exitcodefile);
        }
        
        wr.write(quote(spec.getExecutable()));
        List<String> args = spec.getArgumentsAsList();
        if (args != null && args.size() > 0) {
            wr.write(' ');
            Iterator<String> i = args.iterator();
            while (i.hasNext()) {
                wr.write(quote((String) i.next()));
                if (i.hasNext()) {
                    wr.write(' ');
                }
            }
        }

        if (spec.getStdInput() != null) {
            wr.write(" < " + quote(spec.getStdInput()));
        }

        if (multiple) {
            writeMultiJobPostamble(wr, stdout, stderr);
        } else {
            wr.write(" &\n");
            wr.write("wait $!\n");
            wr.write("write_exitcode $?\n");
        }
        wr.close();
    }
 
    /**
     * writeWallTime - Convert time into correct format and write to submit file
     * Use maxtime first is available, otherwise use maxwalltime
     * @param wr Writer A Writer object representing the submit file
     * @throws IOException
     */
    protected void writeWallTime(Writer wr) throws IOException {
    	Object walltime = getSpec().getAttribute("maxwalltime");
        if (walltime != null) {
            wr.write("#$ -l h_rt="
                    + WallTime.normalize(walltime.toString(), "sge-native")
                    + '\n');
        }
    }
    
    protected void writeSoftWallTime(Writer wr) throws IOException {
    	String walltime = (String)getSpec().getAttribute("maxwalltime");
        if (walltime != null) {
        	int walltimeSeconds = WallTime.timeToSeconds(walltime) - 10;
        	wr.write("#$ -l s_rt="
        	        + WallTime.format("sge-native", walltimeSeconds)
        	        + '\n');
        }
    }
    

}
