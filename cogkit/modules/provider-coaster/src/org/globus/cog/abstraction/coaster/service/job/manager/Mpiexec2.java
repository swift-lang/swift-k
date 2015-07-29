/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2010-2014 University of Chicago
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

// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.abstraction.coaster.service.job.manager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.CleanUpSetImpl;
import org.globus.cog.abstraction.impl.common.IdentityImpl;
import org.globus.cog.abstraction.impl.common.StagingSetEntryImpl;
import org.globus.cog.abstraction.impl.common.StagingSetImpl;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.execution.coaster.CleanupCommand;
import org.globus.cog.abstraction.impl.execution.coaster.NotificationManager;
import org.globus.cog.abstraction.impl.execution.coaster.StageInCommand;
import org.globus.cog.abstraction.impl.execution.coaster.StageOutCommand;
import org.globus.cog.abstraction.impl.execution.coaster.SubmitJobCommand;
import org.globus.cog.abstraction.interfaces.CleanUpSet;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.StagingSet;
import org.globus.cog.abstraction.interfaces.StagingSetEntry;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.commands.Command;
import org.globus.cog.coaster.commands.Command.Callback;

/**
 * Runs MPI jobs by:
 * 1. staging input files to all nodes
 * 2. running mpirun only on the first node
 * 3. staging out only from the first node
 * 4. cleaning up on all nodes
 * 
 */
public class Mpiexec2 implements Callback, ExtendedStatusListener {
    public static final Logger logger = Logger.getLogger(Mpiexec2.class);
    
    private static enum State {
        START, STAGEIN, RUNNING, STAGEOUT, CLEANUP, ERROR;
    }
    
    private static enum FSMode {
        SHARED, NODE_LOCAL
    }
    
    private List<Cpu> cpus;
    private Job job;
    private int stageinsActive, cleanupsActive;
    private State state;
    private Time estCompletionTime;
    private Status lastStatus;
    private String jobdir;
    
    private String lastErrorMessage;
    private Exception lastException;
    
    private File hostfile;
    
    private FSMode fsMode;
    
    public Mpiexec2(List<Cpu> cpus, Job job, Time estCompletionTime) {
        this.cpus = cpus;
        this.job = job;
        this.estCompletionTime = estCompletionTime;
        this.state = State.START;
        JobSpecification spec = (JobSpecification) job.getTask().getSpecification();
        this.fsMode = getFSMode(spec);
        jobdir = spec.getDirectory();
        if (logger.isInfoEnabled()) {
            logger.info("New MPI job: " + job.getTask().getIdentity() + ", count=" + job.getCount() + ", fsmode=" + fsMode);
        }
    }

    private FSMode getFSMode(JobSpecification spec) {
        Object o = spec.getAttribute("mpi.fsmode");
        if ("node".equals(o)) {
            return FSMode.NODE_LOCAL;
        }
        else {
            return FSMode.SHARED;
        }
    }

    public boolean launch() {
        reserveCpus();
        if (makeHostList()) {
            stagein();
        }
        return true;
    }

    private boolean makeHostList() {
        try {
            hostfile = File.createTempFile("coaster-", ".hostfile");
            BufferedWriter br = new BufferedWriter(new FileWriter(hostfile));
            for (Cpu cpu : cpus) {
                br.write(cpu.getNode().getHostname());
                br.write("\n");
            }
            br.close();
            return true;
        }
        catch (IOException e) {
            setError("Failed to create host list", e);
            done();
            return false;
        }
    }

    private void stagein() {
        JobSpecification spec = (JobSpecification) job.getTask().getSpecification();
        if (logger.isInfoEnabled()) {
            logger.info("stagein " + job.getTask().getIdentity());
        }
        StagingSet si = spec.getStageIn();
        if (si == null) {
            si = new StagingSetImpl();
        }
        StagingSet headNodeSi = new StagingSetImpl(si);
        
        headNodeSi.add(new StagingSetEntryImpl("file://localhost/" + hostfile.getAbsolutePath(), jobdir + "/_hostfile"));
        stagein(headNodeSi, si);
    }
    
    private void stagein(StagingSet headNodeSi, StagingSet si) {
        state = State.STAGEIN;
        job.getTask().setStatus(Status.STAGE_IN);
                        
        StagingSet nsi = new StagingSetImpl();
        StagingSet nHeadNodeSi = new StagingSetImpl(); 
        
        for (StagingSetEntry e : si) {
            nsi.add(absolutizeStagein(e, jobdir));
        }
        
        for (StagingSetEntry e : headNodeSi) {
            nHeadNodeSi.add(absolutizeStagein(e, jobdir));
        }
        
        switch (fsMode) {
            case SHARED:
                // only stage in to rank 0
                stagein(nHeadNodeSi, nsi, Collections.singletonList(cpus.get(0)));
                break;
            case NODE_LOCAL:
                // stage in to all nodes
                stagein(nHeadNodeSi, nsi, cpus);
                break;
        }
    }

    private void stagein(StagingSet headNodeSi, StagingSet si, List<Cpu> cpus) {
        // build list of nodes
        // also account for the fact that some nodes may be
        // launched on the exact same host
        Node headNode = cpus.get(0).getNode();
        Set<String> hosts = new HashSet<String>();
        hosts.add(headNode.getHostname());
        Set<Node> nodes = new HashSet<Node>();
        nodes.add(headNode);
        for (Cpu cpu : cpus) {
            Node node = cpu.getNode();
            String host = node.getHostname();
            if (hosts.contains(host)) {
                // ignore
            }
            else {
                hosts.add(host);
                nodes.add(cpu.getNode());
            }
        }
                
        stageinsActive = nodes.size();
        int started = 0;
        for (Node node : nodes) {
            StageInCommand sc;
            if (node == headNode) {
                sc = new StageInCommand(headNodeSi);
            }
            else {
                sc = new StageInCommand(si);
            }
            try {
                sc.executeAsync(node.getChannel(), this);
                started++;
            }
            catch (ProtocolException e) {
                setError(e.getMessage(), e);
                int r;
                synchronized(this) {
                    stageinsActive -= (nodes.size() - started);
                    r = stageinsActive;
                }
                checkStageins(r);
                // don't start the rest
                break;
            }
        }
    }

    private void mpirun() {
        state = State.RUNNING;
        if (logger.isInfoEnabled()) {
            logger.info("mpirun " + job.getTask().getIdentity());
        }
        Node n = cpus.get(0).getNode();
        Task t = cloneTaskNoStaging();
        Identity nid = new IdentityImpl();
        nid.setValue(t.getIdentity().toString() + "-mpi");
        t.setIdentity(nid);
        addMPIRun((JobSpecification) t.getSpecification(), n.getHostname());
        
        NotificationManager.getDefault().registerListener(t.getIdentity().getValue(), t, this);
        
        SubmitJobCommand sjc = new SubmitJobCommand(t);
        try {
            sjc.executeAsync(n.getChannel(), this);
        }
        catch (ProtocolException e) {
            errorReceived(null, e.getMessage(), e);
        }
    }
    
    private void addMPIRun(JobSpecification spec, String hnHostname) {
        List<String> args = new ArrayList<String>();
        args.add("-hostfile");
        args.add("_hostfile");
        args.add(spec.getExecutable());
        args.addAll(spec.getArgumentsAsList());
        spec.setExecutable("mpirun");
        spec.setArguments(args);
        spec.addEnvironmentVariable("SWIFT_MPI_FSMODE", fsMode.toString());
        spec.addEnvironmentVariable("SWIFT_MPI_HEADNODE", hnHostname);
    }

    private void stageout(boolean failed) {
        JobSpecification spec = (JobSpecification) job.getTask().getSpecification();
        StagingSet so = spec.getStageOut();
        
        if (so == null || so.isEmpty()) {
            cleanup();
        }
        else {
            stageout(so, failed);
        }
    }
    
    private void stageout(StagingSet so, boolean failed) {
        state = State.STAGEOUT;
        job.getTask().setStatus(Status.STAGE_OUT);
        if (logger.isInfoEnabled()) {
            logger.info("stageout " + job.getTask().getIdentity());
        }
        StagingSet nso = new StagingSetImpl();
        
        for (StagingSetEntry e : so) {
            if (e.getMode().contains(StagingSetEntry.Mode.ALWAYS)) {
                nso.add(absolutizeStageout(e, jobdir));
            }
            else if (e.getMode().contains(StagingSetEntry.Mode.ON_ERROR) && failed) {
                nso.add(absolutizeStageout(e, jobdir));
            }
            else if (e.getMode().contains(StagingSetEntry.Mode.ON_SUCCESS) && !failed) {
                nso.add(absolutizeStageout(e, jobdir));
            }
        }
        
        StageOutCommand cmd = new StageOutCommand(so);
        try {
            cmd.executeAsync(cpus.get(0).getNode().getChannel(), this);
        }
        catch (ProtocolException e) {
            setError(e.getMessage(), e);
            cleanup();
        }
    }
    
    private StagingSetEntry absolutizeStagein(StagingSetEntry e, String dir) {
        if (dir == null) {
            return e;
        }
        if (e.getDestination().startsWith("/")) {
            return e;
        }
        else {
            return new StagingSetEntryImpl(e.getSource(), dir + "/" + e.getDestination(), e.getMode());
        }
    }
    
    private StagingSetEntry absolutizeStageout(StagingSetEntry e, String dir) {
        if (dir == null) {
            return e;
        }
        if (e.getSource().startsWith("/")) {
            return e;
        }
        else {
            return new StagingSetEntryImpl(dir + "/" + e.getSource(), e.getDestination(), e.getMode());
        }
    }


    private void cleanup() {
        JobSpecification spec = (JobSpecification) job.getTask().getSpecification();
        
        CleanUpSet cl = spec.getCleanUpSet();
        
        if (cl == null || cl.isEmpty()) {
            done();
        }
        else {
            cleanup(cl);
        }
    }
    
    private void cleanup(CleanUpSet cl) {
        state = State.CLEANUP;
        if (logger.isInfoEnabled()) {
            logger.info("cleanup " + job.getTask().getIdentity());
        }
        CleanUpSet ncl = new CleanUpSetImpl();
        
        for (String s : cl) {
            if (s.startsWith("/") || jobdir == null) {
                ncl.add(s);
            }
            else {
                ncl.add(jobdir  + "/" + s);
            }
        }
                
        Set<Node> nodes = new HashSet<Node>();
        for (Cpu cpu : cpus) {
            nodes.add(cpu.getNode());            
        }
                
        cleanupsActive = nodes.size();
        for (Node node : nodes) {
            CleanupCommand cmd = new CleanupCommand(ncl);
            try {
                cmd.executeAsync(node.getChannel(), this);
            }
            catch (ProtocolException e) {
                setError(e.getMessage(), e);
                int r;
                synchronized(this) {
                    cleanupsActive--;
                    r = cleanupsActive;
                }
                checkCleanups(r);
            }
        }
    }
    
    private void done() {
        if (logger.isInfoEnabled()) {
            logger.info("done " + job.getTask().getIdentity() + ", msg=" + lastErrorMessage + ", ex=" + lastException);
        }
        releaseNonLeadCpus();
        Status s;
        if (lastErrorMessage != null || lastException != null) {
            s = new StatusImpl(Status.FAILED, lastErrorMessage, lastException);
        }
        else {
            s = new StatusImpl(Status.COMPLETED);
        }
        job.getTask().setStatus(s);
        cpus.get(0).statusChanged(s, null, null);
        if (hostfile != null) {
            hostfile.delete();
        }
    }

    private Task cloneTaskNoStaging() {
        Task t = (Task) job.getTask().clone();
        JobSpecification spec = (JobSpecification) t.getSpecification();
        spec.setStageIn(null);
        spec.setStageOut(null);
        spec.setCleanUpSet(null);
        spec.setStdOutputLocation(FileLocation.MEMORY);
        spec.setStdErrorLocation(FileLocation.MEMORY);
        return t;
    }

    private void reserveCpus() {
        Map<Block, List<Cpu>> blocks = new HashMap<Block, List<Cpu>>();
        for (Cpu cpu : cpus) {
            List<Cpu> blockCpus = blocks.get(cpu.getBlock());
            if (blockCpus == null) {
                blockCpus = new ArrayList<Cpu>();
                blocks.put(cpu.getBlock(), blockCpus);
            }
            blockCpus.add(cpu);
        }
        
        // mark all cpus as used in the respective blocks
        for (Map.Entry<Block, List<Cpu>> e : blocks.entrySet()) {
            e.getKey().addAll(e.getValue(), estCompletionTime);
        }
    }
    
    
    private void checkStageins(int count) {
        if (count == 0) {
            if (lastErrorMessage != null || lastException != null) {
                cleanup();
            }
            else {
                mpirun();
            }
        }
    }

    @Override
    public void replyReceived(Command cmd) {
        if (logger.isInfoEnabled()) {
            logger.info("Reply to command: " + cmd);
        }
        switch (state) {
            case STAGEIN:
                int r;
                synchronized(this) {
                    stageinsActive--;
                    r = stageinsActive;
                }
                checkStageins(r);
                break;
            case RUNNING:
                // submit completed; wait for task status change
                break;
            case STAGEOUT:
                cleanup();
                break;
            case CLEANUP:
                int r2;
                synchronized(this) {
                    cleanupsActive--;
                    r2 = cleanupsActive;
                }
                checkCleanups(r2);
                break;
        }
    }

    private void checkCleanups(int count) {
        if (count == 0) {
            done();
        }
    }

    @Override
    public void errorReceived(Command cmd, String msg, Exception t) {
        setError(msg, t);
        switch (state) {
            case STAGEIN:
                int r;
                synchronized(this) {
                    stageinsActive--;
                    r = stageinsActive;
                }
                checkStageins(r);
                break;
            case RUNNING:
                stageout(true);
                break;
            case STAGEOUT:
                cleanup();
                break;
            case CLEANUP:
                int r2;
                synchronized(this) {
                    cleanupsActive--;
                    r2 = cleanupsActive;
                }
                checkCleanups(r2);
                break;
        }
    }

    private synchronized void setError(String msg, Exception t) {
        if (lastErrorMessage == null && lastException == null) {
            lastErrorMessage = msg;
            lastException = t;
        }
    }

    @Override
    public void statusChanged(Status s, String out, String err) {
        if (logger.isInfoEnabled()) {
            logger.info("status changed " + job.getTask().getIdentity() + ", status=" + s + ", out=" + out + ", err=" + err);
        }
        this.lastStatus = s;
        switch (s.getStatusCode()) {
            case Status.ACTIVE:
                job.getTask().setStatus(s);
                break;
            case Status.COMPLETED:
                stageout(false);
                break;
            case Status.FAILED:
                setError(s.getMessage() + "\n" + out + "\n" + err, s.getException());
                stageout(true);
                break;
        }
    }

    private void releaseNonLeadCpus() {
        for (int i = 1; i < cpus.size(); i++) {
            cpus.get(i).mpiRankTerminated();
        }
    }
}
