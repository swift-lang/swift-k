package org.globus.cog.abstraction.impl.scheduler.slurm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractProperties;
import org.globus.cog.abstraction.impl.scheduler.common.AbstractQueuePoller;
import org.globus.cog.abstraction.impl.scheduler.common.Job;

public class QueuePoller extends AbstractQueuePoller {
	public static final Logger logger = Logger.getLogger(QueuePoller.class);
	public static final int FULL_LIST_THRESHOLD = 16;

	private Set<String> processed;

	public QueuePoller(AbstractProperties properties) {
		super("Slurm provider queue poller", properties);
		processed = new HashSet<String>();
	}

	private static String[] CMDARRAY;

	protected synchronized String[] getCMDArray() {
		if (getJobs().size() <= FULL_LIST_THRESHOLD) {
			CMDARRAY = new String[4];
			CMDARRAY[0] = getProperties().getPollCommand();
			CMDARRAY[1] = "--noheader";
			CMDARRAY[2] = "--jobs";
			boolean first = true;
			for (Job j : getJobs().values()) {
				if (first) {
					CMDARRAY[3] = j.getJobID();
					first = false;
				} else {
					CMDARRAY[3] += "," + j.getJobID();
				}
			}
		} else {
			CMDARRAY = new String[] { getProperties().getPollCommand(),
					"--noheader" };
		}
		return CMDARRAY;
	}

	@Override
	protected int getError(int ec, String stderr) {
		if (ec != 0) {
			BufferedReader sr = new BufferedReader(new StringReader(stderr));
			try {
				String line = sr.readLine();
				while (line != null) {
					if (!line.contains("Unknown Job Id")) {
						return ec;
					}
					line = sr.readLine();
				}
			} catch (IOException e) {
				// should not occur while reading from a string reader
				e.printStackTrace();
			}
			return 0;
		} else {
			return ec;
		}
	}

	protected void processStdout(InputStream is) throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		processed.clear();
		
		do {
			line = br.readLine();
			if(line != null) {
				String words[] = line.trim().split("\\s+");
				String jobid = words[0];
				String state = words[4];
				if (jobid == null || jobid.equals("") || state == null || state.equals("")) {
					throw new IOException("Failed to parse squeue line: " + line);
				}
								
				Job job = getJob(jobid);
				if (job == null){ continue; }
				processed.add(jobid);
				
				if (state.equals("PD")) {
					job.setState(Job.STATE_QUEUED);
				}
				else if(state.equals("R")) {
					job.setState(Job.STATE_RUNNING);
				}
			}
		} while (line != null);
		
		Iterator<Entry<String, Job>> i = getJobs().entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<String, Job> e = i.next();
			String id = e.getKey();
			if (!processed.contains(id)) {
				Job job = e.getValue();
				job.setState(Job.STATE_DONE);
				if (job.getState() == Job.STATE_DONE) {
					addDoneJob(id);
				}
			}
		}
	}
	
	protected void processStderr(InputStream is) throws IOException {
	}
}
