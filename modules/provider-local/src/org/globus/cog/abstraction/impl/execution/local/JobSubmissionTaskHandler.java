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

package org.globus.cog.abstraction.impl.execution.local;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.AbstractDelegatedTaskHandler;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.execution.JobException;
import org.globus.cog.abstraction.impl.common.task.IllegalSpecException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.InvalidServiceContactException;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;
import org.globus.cog.abstraction.impl.common.task.TaskSubmissionException;
import org.globus.cog.abstraction.impl.common.util.NullOutputStream;
import org.globus.cog.abstraction.impl.common.util.OutputStreamMultiplexer;
import org.globus.cog.abstraction.impl.file.FileResourceCache;
import org.globus.cog.abstraction.impl.file.SimplePathExpansion;
import org.globus.cog.abstraction.interfaces.CleanUpSet;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.RemoteFile;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.interfaces.Specification;
import org.globus.cog.abstraction.interfaces.StagingSet;
import org.globus.cog.abstraction.interfaces.StagingSetEntry;
import org.globus.cog.abstraction.interfaces.StagingSetEntry.Mode;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.Task;

/**
 * @author Kaizar Amin (amin@mcs.anl.gov)
 *
 */
public class JobSubmissionTaskHandler extends AbstractDelegatedTaskHandler implements Runnable {
    private static Logger logger = Logger.getLogger(JobSubmissionTaskHandler.class);

    private static ExecutorService pool =
            Executors.newCachedThreadPool(new DaemonThreadFactory(Executors.defaultThreadFactory()));

    private static final int STDOUT = 0;
    private static final int STDERR = 1;

    public static final int BUFFER_SIZE = 32768;

    private Process process;
    private volatile boolean killed;

    public void submit(Task task) throws IllegalSpecException, InvalidSecurityContextException,
            InvalidServiceContactException, TaskSubmissionException {
        /*
         *  see if the task wasn't submitted before and store it
         *  in the task field
         */
        checkAndSetTask(task);
        task.setStatus(Status.SUBMITTING);
        JobSpecification spec;
        try {
            spec = (JobSpecification) task.getSpecification();
        }
        catch (Exception e) {
            throw new IllegalSpecException("Exception while retrieving Job Specification", e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(spec.toString());
        }

        try {
            int count = 1;
            Object cv = spec.getAttribute("count");
            if (cv != null) {
                count = Integer.parseInt(cv.toString());
            }
            log(task, count);
            synchronized (this) {
                // run count copies of this task
                if (task.getStatus().getStatusCode() != Status.CANCELED) {
                    for (int i = 0; i < count; i++) {
                        pool.submit(this);
                    }
                    task.setStatus(Status.SUBMITTED);
                    if (spec.isBatchJob()) {
                        /*
                         *  "batch job" in Globus terms means
                         *  "start and forget", so set the
                         *  status to completed if this is
                         *  a batch job
                         */
                        task.setStatus(Status.COMPLETED);
                    }
                }
            }
        }
        catch (Exception e) {
            throw new TaskSubmissionException("Cannot submit job", e);
        }
    }

    void log(Task task, int count) {
        if (logger.isDebugEnabled()) {
            logger.debug("Submitting task " + task);
        }
        else if (logger.isDebugEnabled()) {
            Specification spec = task.getSpecification();
            if (spec instanceof JobSpecification) {
                JobSpecification jobspec = (JobSpecification) spec;
                logger.debug("Submit: " +
                    "in: " + jobspec.getDirectory() +
                    " command: " + jobspec.getExecutable() +
                    " " + jobspec.getArguments());
            }
        }
        if (count == 1) {
            if (logger.isDebugEnabled()) {
                logger.debug("Submitting single job");
            }
        }
        else {
            logger.debug("Submitting " + count + " jobs");
        }
    }

    public void suspend() throws InvalidSecurityContextException, TaskSubmissionException {
        // not supported
    }

    public void resume() throws InvalidSecurityContextException, TaskSubmissionException {
        // not supported
    }

    public void cancel(String message) throws InvalidSecurityContextException,
            TaskSubmissionException {
        synchronized (this) {
            killed = true;
            if (process != null) {
                process.destroy();
            }
            getTask().setStatus(new StatusImpl(Status.CANCELED, message, null));
        }
    }

    public void run() {
        try {
            // TODO move away from the multi-threaded approach
            JobSpecification spec = (JobSpecification) getTask().getSpecification();

            File dir = getJobDir(spec);
            stageIn(spec, dir);
            
            process = startProcess(spec, dir);
            getTask().setStatus(Status.ACTIVE);

            /*
             * Build a list of stream pairs (in, out) that need to
             * be processed during the execution. This is a list because
             * any combination of STDOUT and STDERR (namely none, OUT, ERR, and (OUT and ERR))
             * can be requested through the specification.
             */
            // TODO a single thread can be used here for all processes
            List<StreamPair> pairs = new LinkedList<StreamPair>();

            OutputStream os;
            os = prepareOutStream(spec.getStdOutput(), spec.getStdOutputLocation(), dir,
                getTask(), STDOUT);
            pairs.add(new StreamPair(process.getInputStream(), os));

            os = prepareOutStream(spec.getStdError(), spec.getStdErrorLocation(), dir,
                getTask(), STDERR);
            pairs.add(new StreamPair(process.getErrorStream(), os));

            /*
             * Start redirecting the streams
             */
            Processor p = new Processor(process, pairs);
            pool.execute(p);
            
            /*
             * Stdin needs to pe dealt with after we start dealing with stdout 
             * to avoid the following possible deadlock:
             * 1. stdin is fed in
             * 2. app (say cat) dumps data to stdout
             * 3. stdout is not read, so the buffer fills up
             * 4. that pauses the app
             * 5. stdin writing is blocked
             * 6. the stdin feed blocks
             * 7. the code never gets to reading stdout 
             */
            /*
             * In addition, STDIN must be dealt with separately
             * since OutputStreams don't have a way of detecting
             * when a write would block (whereas InputStreams 
             * have a way of detecting when reads would block),
             * so STDIN processing cannot easily be interwoven
             * with STDOUT/STDERR processing in a single thread
             */
            processIN(spec.getStdInput(), dir, process.getOutputStream());
            
            int exitCode = p.waitFor();

            if (logger.isDebugEnabled()) {
                logger.debug("Application " + spec.getExecutable() + " failed with an exit code of " + exitCode);
            }
            
            /*
             * If the process exited because of a cancel() request,
             * then don't stage out or clean up
             */
            if (killed) {
                return;
            }

            stageOut(spec, dir, exitCode == 0);
            if (exitCode == 0) {
                cleanUp(spec, dir);
                if (!spec.isBatchJob()) {
                    getTask().setStatus(Status.COMPLETED);
                }
            }
            else {
                if (logger.isDebugEnabled()) {
                    logger.debug("STDOUT: " + pairs.get(0).os.toString());
                    logger.debug("STDERR: " + pairs.get(1).os.toString());
                }
                throw new JobException(exitCode);
            }
        }
        catch (Exception e) {
            if (killed) {
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Exception while running local executable", e);
            }
            failTask(null, e);
        }
    }

    protected Process startProcess(JobSpecification spec, File dir) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(buildCmdArray(spec));
        pb.directory(dir);
        addEnvs(pb, spec);
        return pb.start();
    }

    protected File getJobDir(JobSpecification spec) {
        /*
         * Run the process in the specified directory or
         * in the current directory if the specification
         * directory is null
         */
        if (spec.getDirectory() != null) {
            return new File(spec.getDirectory());
        }
        else {
            return new File(".");
        }
    }

    protected void cleanUp(JobSpecification spec, File dir) throws TaskSubmissionException {
        CleanUpSet cs = spec.getCleanUpSet();
        if (cs == null || cs.isEmpty()) {
            return;
        }
        for (String e : cs) {
            File f = new File(e);
            if (!f.isAbsolute()) {
                f = new File(dir, f.getPath());
            }
            if (!f.getAbsolutePath().startsWith(dir.getAbsolutePath())) {
                throw new TaskSubmissionException("Cleaning outside of the job directory is not allowed " +
                		"(cleanup entry: " + f.getAbsolutePath() + ", jobdir: " + dir.getAbsolutePath());
            }
            removeRecursively(f);
        }
    }

    private void removeRecursively(File f) throws TaskSubmissionException {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            for (int i = 0; i < files.length; i++) {
                removeRecursively(files[i]);
            }
        }
        else {
            if (f.exists() && !f.delete()) {
                throw new TaskSubmissionException("Failed to remove file: " + f.getAbsolutePath());
            }
        }
    }

    protected void stageOut(JobSpecification spec, File dir, boolean jobSucceeded) throws Exception {
        StagingSet s = spec.getStageOut();
        if (s == null || s.isEmpty()) {
            return;
        }

        getTask().setStatus(Status.STAGE_OUT);
        stage(s, dir, jobSucceeded, true);
    }

    private void stage(StagingSet s, File dir, boolean jobSucceeded, boolean pathNameExpansion) throws Exception {
        for (StagingSetEntry e : s) {
            String src = e.getSource();
            if (pathNameExpansion && isPattern(src)) {
                if (e.getMode().contains(Mode.ON_SUCCESS) && !jobSucceeded) {
                    continue;
                }
                if (e.getMode().contains(Mode.ON_ERROR) && jobSucceeded) {
                    continue;
                }
                String dst = e.getDestination();
                RemoteFile srf = new RemoteFile(src);
                String srcScheme = defaultToLocal(srf.getProtocol());
                Service ss = new ServiceImpl(srcScheme, getServiceContact(srf), null);
                FileResource sres = FileResourceCache.getDefault().getResource(ss);
                RemoteFile drf = new RemoteFile(dst);
                Collection<String[]> paths = SimplePathExpansion.expand(srf, drf, sres);
                for (String[] pair : paths) {
                    copy(pair[0], pair[1], dir, e.getMode(), jobSucceeded);
                }
            }
            else {
                copy(e.getSource(), e.getDestination(), dir, e.getMode(), jobSucceeded);
            }
        }
    }

    private boolean isPattern(String path) {
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            switch (c) {
                case '?':
                case '*':
                    return true;
            }
        }
        return false;
    }

    protected void stageIn(JobSpecification spec, File dir) throws Exception {
        StagingSet s = spec.getStageIn();
        if (s == null || s.isEmpty()) {
            return;
        }

        getTask().setStatus(Status.STAGE_IN);
        // job is considered successful before it runs as far as
        // staging modes are concerned
        stage(s, dir, true, false);
    }

    private void copy(String src, String dest, File dir, EnumSet<Mode> mode, boolean jobSucceeded) throws Exception {
        src = dropCDMPrefix(src);
        RemoteFile srf = new RemoteFile(src);
        RemoteFile drf = new RemoteFile(dest);

        String srcScheme = defaultToLocal(srf.getProtocol());
        String dstScheme = defaultToLocal(drf.getProtocol());

        Service ss = new ServiceImpl(srcScheme, getServiceContact(srf), null);
        Service ds = new ServiceImpl(dstScheme, getServiceContact(drf), null);
        
        FileResource sres = FileResourceCache.getDefault().getResource(ss);
        FileResource dres = FileResourceCache.getDefault().getResource(ds);
                
        String srcPath = getPath(srf, dir);
        
        if (mode.contains(Mode.IF_PRESENT) && !sres.exists(srcPath)) {
            return;
        }
        if (mode.contains(Mode.ON_SUCCESS) && !jobSucceeded) {
            return;
        }
        if (mode.contains(Mode.ON_ERROR) && jobSucceeded) {
            return;
        }

        InputStream is = sres.openInputStream(srcPath);
        OutputStream os = dres.openOutputStream(getPath(drf, dir));
        byte[] buffer = new byte[BUFFER_SIZE];

        int len = is.read(buffer);
        while (len != -1) {
            os.write(buffer, 0, len);
            len = is.read(buffer);
        }
        
        FileResourceCache.getDefault().releaseResource(sres);
        FileResourceCache.getDefault().releaseResource(dres);
    }

    private String getPath(RemoteFile rf, File dir) {
        if (rf.getProtocol() == null && !rf.isAbsolute()) {
            return new File(dir, rf.getPath()).getAbsolutePath();
        }
        else {
            return rf.getPath();
        }
    }

    protected ServiceContact getServiceContact(RemoteFile rf) {
        ServiceContact sc = new ServiceContactImpl();
        if (rf.getHost() != null) {
            sc.setHost(rf.getHost());
        }
        if (rf.getPort() != -1) {
            sc.setPort(rf.getPort());
        }
        return sc;
    }

    private String defaultToLocal(String scheme) {
        return scheme == null ? "local" : scheme;
    }

    /** 
     * Strips off CDM prefixes, including "pinned:" 
     */
    private String dropCDMPrefix(String uri) throws Exception {
        if (uri.startsWith("pinned:")) {
            uri = uri.substring(7);
        }
        return uri;
    }
    
    protected void processIN(String in, File dir, OutputStream out) throws IOException {

        byte[] buf = new byte[BUFFER_SIZE];
        if (in != null) {
            File stdin;
            if (dir != null) {
                stdin = new File(dir, in);
            }
            else {
                stdin = new File(in);
            }

            FileInputStream file = new FileInputStream(stdin);
            int read = file.read(buf);
            while (read != -1) {
                out.write(buf, 0, read);
                read = file.read(buf);
            }
            file.close();
        }
        out.close();
    }

    protected OutputStream prepareOutStream(String out, FileLocation loc, File dir, Task task,
            int stream) throws IOException {

        OutputStream os = null;
        TaskOutputStream baos = null;
        if (FileLocation.MEMORY.overlaps(loc)) {
            baos = new TaskOutputStream(task, stream);
            os = baos;
        }
        if ((FileLocation.LOCAL.overlaps(loc) || FileLocation.REMOTE.equals(loc)) && out != null) {
            if (os != null) {
                /*
                 * This is reached when redirection is requested to
                 * both memory and a file, so use an output stream
                 * that in turn writes to two output streams 
                 */
                os = new OutputStreamMultiplexer(os, new FileOutputStream(out));
            }
            else {
                os = new FileOutputStream(out);
            }
        }
        if (os == null) {
            os = new NullOutputStream();
        }

        return os;
    }

    protected List<String> buildCmdArray(JobSpecification spec) {
        List<String> arguments = new ArrayList<String>();
        arguments.add(spec.getExecutable());
        arguments.addAll(spec.getArgumentsAsList());

        return arguments;
    }

    protected void addEnvs(ProcessBuilder pb, JobSpecification spec) {
        Collection<String> names = spec.getEnvironmentVariableNames();
        if (names.size() != 0) {
            Map<String, String> env = pb.environment();
            for (String name : names) { 
                env.put(name, spec.getEnvironmentVariable(name));
            }
        }
    }

    private static class StreamPair {
        public InputStream is;
        public OutputStream os;

        public StreamPair(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
        }
    }

    /**
     * A byte array-backed output stream that enforces 
     * a limit on the backing buffer, discarding earlier data.
     * Only MAX_SIZE / 2 bytes are guaranteed to be stored in the 
     * array.
     */
    private static class TaskOutputStream extends ByteArrayOutputStream {
        public static final int MAX_SIZE = 8192;
        private Task task;
        private int stream;

        public TaskOutputStream(Task task, int stream) {
            this.task = task;
            this.stream = stream;
        }

        @Override
        public void close() throws IOException {
            super.close();
            String value = toString();
            if (logger.isDebugEnabled()) {
                logger.debug((stream == STDOUT ? "STDOUT" : "STDERR") + " from job: " + value);
            }
            if (stream == STDOUT) {
                task.setStdOutput(value);
            }
            else {
                task.setStdError(value);
            }
        }

        @Override
        public synchronized void write(byte[] b, int off, int len) {
            super.write(b, off, len);
            checkSize();
        }

        @Override
        public synchronized void write(int b) {
            super.write(b);
            checkSize();
        }

        private void checkSize() {
            if (count > MAX_SIZE) {
                System.arraycopy(buf, MAX_SIZE / 2, buf, 0, MAX_SIZE / 2);
                count = MAX_SIZE / 2;
            }
        }
    }

    /**
     * A class to move data between multiple in -> out streams.
     */
    private class Processor implements Runnable {
        private Process p;
        private List<StreamPair> streamPairs;
        byte[] buf;
        private boolean done;

        public Processor(Process p, List<StreamPair> streamPairs) {
            this.p = p;
            this.streamPairs = streamPairs;
        }

        public void run() {
            try {
                run2();
            }
            catch (Exception e) {
                logger.warn("Exception caught while running process", e);
            }
        }

        public void run2() throws IOException, InterruptedException {
            IOException e = null;
            while (true) {
                boolean any;
                try {
                    any = processPairs();
                }
                catch (IOException ex) {
                    e = ex;
                    any = false;
                }
                if (processDone()) {
                    while(processPairs()) {}
                    closePairs();
                    synchronized(this) {
                        done = true;
                        notifyAll();
                    }
                    return;
                }
                else {
                    if (e != null) {
                        p.destroy();
                        throw e;
                    }
                    if (!any) {
                        Thread.sleep(20);
                    }
                }
            }
        }

        private boolean processPairs() throws IOException {
            boolean any = false;
         
            for (StreamPair sp : streamPairs) {
                if (buf == null) {
                    buf = new byte[BUFFER_SIZE];
                }
                
                try {
                    int avail = sp.is.available();
                    if (avail > 0) {
                        any = true;
                        int len = sp.is.read(buf, 0, Math.min(avail, BUFFER_SIZE));
                        sp.os.write(buf, 0, len);
                    }
                }
                catch (IOException e) {
                    // When a job is canceled, process.destroy() is called, which will
                    // call close() on the process streams. Attempting to read from 
                    // streams on which close() has been called explicitly will
                    // cause an exception to be thrown. So ignore IOExceptions
                    // when the process is killed.
                    synchronized(JobSubmissionTaskHandler.this) {
                        if (!killed) {
                            throw e;
                        }
                    }
                }
            }
            return any;
        }
        
        public int waitFor() throws InterruptedException {
            synchronized(this) {
                while (!done) {
                    wait();
                }
            }
            return p.waitFor();
        }

        private boolean processDone() {
            try {
                p.exitValue();
                return true;
            }
            catch (IllegalThreadStateException e) {
                return false;
            }
        }

        private void closePairs() throws IOException {
            for (StreamPair sp : streamPairs) { 
                sp.os.close();
                sp.is.close();
            }
        }

        public int getExitCode() {
            return p.exitValue();
        }
    }

    /**
     * A ThreadFactory that produces daemon threads
     */
    static class DaemonThreadFactory implements ThreadFactory {
        private ThreadFactory delegate;

        public DaemonThreadFactory(ThreadFactory delegate) {
            this.delegate = delegate;
        }

        public Thread newThread(Runnable r) {
            Thread t = delegate.newThread(r);
            t.setDaemon(true);
            return t;
        }

    }
    
    public String toString() {
        return "JobSubmissionTaskHandler(local)";
    }
}
