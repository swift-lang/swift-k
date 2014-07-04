//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2005
 */
package org.globus.cog.coaster.handlers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.globus.cog.abstraction.coaster.service.CoasterService;
import org.globus.cog.abstraction.coaster.service.job.manager.Block;
import org.globus.cog.abstraction.coaster.service.job.manager.BlockQueueProcessor;
import org.globus.cog.abstraction.coaster.service.job.manager.Cpu;
import org.globus.cog.abstraction.coaster.service.job.manager.Job;
import org.globus.cog.abstraction.coaster.service.job.manager.JobQueue;
import org.globus.cog.abstraction.coaster.service.job.manager.Node;
import org.globus.cog.abstraction.coaster.service.job.manager.Time;
import org.globus.cog.abstraction.coaster.service.job.manager.TimeInterval;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.coaster.ProtocolException;


public class InfoHandler extends RequestHandler {
	
	public void requestComplete() throws ProtocolException {
	    String type = getInDataAsString(0);
	    String opts = getInDataAsString(1);
	    
	    Map<String, Block> blocks = new HashMap<String, Block>();
        CoasterService s = (CoasterService) getChannel().getChannelContext().getService();
        for (Map.Entry<String, JobQueue> e : s.getQueues().entrySet()) {
            blocks.putAll(((BlockQueueProcessor) e.getValue().getCoasterQueueProcessor()).getBlocks());
        }
        
	    if (type.equals("workers")) {
	        addOutData("           ID    Cores     Running");
            for (Block b : blocks.values()) {
                for (Node n : b.getNodes()) {
                    addOutData(formatWorkerStatusLine(n));
                }
            }
            sendReply();
	    }
	    else if (type.equals("jobs")) {
	        addOutData("          ID          Worker            Executable           Start Time     Walltime");
	        for (Block b : blocks.values()) {
                for (Node n : b.getNodes()) {
                    for (Cpu c : n.getCpus()) {
                        Job running = c.getRunning();
                        if (running != null) {
                            addOutData(formatJobStatusLine(running, n));
                        }
                    }
                }
	        }
	        sendReply();
	    }
	    else if (type.equals("blocks")) {
	        addOutData("    ID    State    Workers           Start Time    Walltime");
	        for (Block b : blocks.values()) {
	            addOutData(formatBlockStatusLine(b));
	        }
	        sendReply();
	    }
	    else {
	        sendError("Unknown type: '" + type + "'");
	    }
	}

    private String formatJobStatusLine(Job j, Node n) {
        StringBuilder sb = new StringBuilder();
        sb.append(j.getTask().getIdentity());
        sb.append("    ");
        sb.append(String.format("%6s:%06d", n.getBlock().getId(), n.getId()));
        sb.append("    ");
        sb.append(String.format("%20s", ((JobSpecification) j.getTask().getSpecification()).getExecutable()));
        sb.append("    ");
        sb.append(formatTime(j.getStartTime()));
        sb.append("    ");
        sb.append(WallTime.format(j.getMaxWallTime().getSeconds()));
        return sb.toString();
    }

    private String formatWorkerStatusLine(Node n) {
        return String.format("%6s:%06d    %5d    %7d", 
                n.getBlock().getId(), n.getId(), n.getCpus().size(), countRunning(n.getCpus()));
    }

    private int countRunning(Collection<Cpu> cpus) {
        int s = 0;
        for (Cpu c : cpus) {
            if (c.getRunning() != null) {
                s++;
            }
        }
        return s;
    }

    private String formatBlockStatusLine(Block b) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%6s", b.getId()));
        sb.append("        ");
        if (b.isRunning()) {
            sb.append("R");
        }
        else if (b.isDone()) {
            sb.append("C");
        }
        else if (b.isSuspended()) {
            sb.append("S");
        }
        else {
            sb.append("Q");
        }
        
        sb.append("    ");
        sb.append(String.format("%7d", b.getWorkerCount()));
        sb.append("    ");
        sb.append(formatTime(b.getStartTime()));
        sb.append("    ");
        if (b.getWalltime() == TimeInterval.FOREVER) {
            sb.append("-");
        }
        else {
            sb.append(WallTime.format(b.getWalltime().getSeconds()));
        }
        return sb.toString();
    }
    
    private static final DateFormat DF = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

    private String formatTime(Time t) {
        return DF.format(t.getMilliseconds());
    }
}
