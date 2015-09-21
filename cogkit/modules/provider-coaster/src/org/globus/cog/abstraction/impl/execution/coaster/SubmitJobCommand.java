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

/*
 * Created on Feb 12, 2008
 */
package org.globus.cog.abstraction.impl.execution.coaster;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.SubmitJobHandler;
import org.globus.cog.abstraction.coaster.service.job.manager.Settings;
import org.globus.cog.abstraction.impl.common.execution.WallTime;
import org.globus.cog.abstraction.interfaces.EnvironmentVariable;
import org.globus.cog.abstraction.interfaces.ExecutionService;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.interfaces.StagingSetEntry;
import org.globus.cog.abstraction.interfaces.StagingSetEntry.Mode;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.commands.Command;

public class SubmitJobCommand extends Command {
    public static final Logger logger = Logger.getLogger(SubmitJobCommand.class);

    public static final String NAME = "SUBMITJOB";

    public static final Set<String> IGNORED_ATTRIBUTES;

    private String id;
    
    static {
        IGNORED_ATTRIBUTES = new HashSet<String>();
        for (int i = 0; i < Settings.NAMES.length; i++) {
            IGNORED_ATTRIBUTES.add(Settings.NAMES[i].toLowerCase());
        }
    }
    
    public static final Set<String> ABSOLUTIZE = new HashSet<String>() {
        {}
    };

    private Task t;
    private boolean compression = SubmitJobHandler.COMPRESSION;
    private boolean simple;
    private String configId;

    public boolean getCompression() {
        return compression;
    }

    public void setCompression(boolean compression) {
        this.compression = compression;
    }
    
    public SubmitJobCommand(Task t) {
        this(t, null);
    }

    public SubmitJobCommand(Task t, String configId) {
        super(NAME);
        this.t = t;
        this.configId = configId;
    }

    public void send() throws ProtocolException {
        try {
            serialize();
        }
        catch (Exception e) {
            throw new ProtocolException("Could not serialize job specification", e);
        }
        super.send();
    }
    
    private char[] buf;

    protected void serialize() throws IOException {
        // I'd use Java serialization if not for the fact that a similar
        // thing needs to be done to communicate with the perl client
        JobSpecification spec = (JobSpecification) t.getSpecification();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
        Writer sb;
        Deflater def = null;
        buf = new char[128];
        if (compression) {
            DeflaterOutputStream dos = new DeflaterOutputStream(baos, def = new Deflater(2) {
                @Override
                protected void finalize() {
                    // override to avoid having it sit in the finalizer queue
                }
            });
            sb = new BufferedWriter(new OutputStreamWriter(dos, UTF8));
        }
        else {
            sb = new BufferedWriter(new OutputStreamWriter(baos, UTF8));
        }
                
        String identity = t.getIdentity().toString();
        if (!simple) {
            add(sb, "configid", configId);
        }
        add(sb, "identity", identity);
        add(sb, "executable", spec.getExecutable());
        add(sb, "directory", spec.getDirectory());
        if (!simple) {
            add(sb, "batch", spec.isBatchJob());
        }
        add(sb, "stdin", spec.getStdInput());
        add(sb, "stdout", spec.getStdOutput());
        add(sb, "stderr", spec.getStdError());
        
        if (spec.isRedirected() || 
                spec.getStdOutputLocation().overlaps(FileLocation.MEMORY) || 
                spec.getStdErrorLocation().overlaps(FileLocation.MEMORY)) {
            add(sb, "redirect", true);
        }

        for (String arg : spec.getArgumentsAsList())
            add(sb, "arg", arg);

        for (EnvironmentVariable var : spec.getEnvironment()) {
            add(sb, "env", var.getName() + "=" + var.getValue());
        }
    
        if (simple) {
        	addKey(sb, "attr");
        	sb.write("maxwalltime=");
        	sb.write(formatWalltime(spec.getAttribute("maxwalltime")));
        	sb.write('\n');

        	if (spec.getAttribute("traceperformance") != null) {
        		addKey(sb, "attr");
        		sb.write("traceperformance=");
        		sb.write(String.valueOf(spec.getAttribute("traceperformance")));
        		sb.write('\n');
        	}
        	if (spec.getAttribute("softimage") != null) {
        	    String value = (String) spec.getAttribute("softimage");
        	    String[] sd = value.split("\\s+");
        	    addKey(sb, "attr");
        	    sb.write("softimage=");
        	    escape(sb, sd[0]);
        	    sb.write(" ");
        	    escape(sb, sd[1]);
        	    sb.write('\n');
        	}
        }
        else {
            for (String name : spec.getAttributeNames())
                if (!IGNORED_ATTRIBUTES.contains(name) || spec.isBatchJob()) {
                	addKey(sb, "attr");
                	sb.write(name);
                	sb.write('=');
                	escape(sb, String.valueOf(spec.getAttribute(name)));
                	sb.write('\n');
                }
        }
            
        if (spec.getStageIn() != null) {
            for (StagingSetEntry e : spec.getStageIn()) {
            	stagingLine(sb, "stagein", e);
            }
        }
        
        if (spec.getStageOut() != null) {
            for (StagingSetEntry e : spec.getStageOut()) {
            	stagingLine(sb, "stageout", e);
            }
        }

        if (spec.getCleanUpSet() != null)
            for (String cleanup : spec.getCleanUpSet())
                add(sb, "cleanup", cleanup);

        if (!simple) {
            Service s = t.getService(0);
            add(sb, "contact", s.getServiceContact().toString());
            add(sb, "provider", s.getProvider());
    
            if (s instanceof ExecutionService) {
                add(sb, "jm", ((ExecutionService) s).getJobManager());
            }
            else {
                add(sb, "jm", "fork");
            }
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Job data: " + baos.toString());
        }
        sb.close();
        if (def != null) {
            def.end();
        }
        
        byte[] bytes = baos.toByteArray();
        buf = null;

        addOutData(bytes);
    }

    private void stagingLine(Writer sb, String name, StagingSetEntry e) throws IOException {
    	addKey(sb, name);
        escape(sb, e.getSource());
        sb.write("\\n");
        escape(sb, e.getDestination());
        sb.write("\\n");
        sb.write(String.valueOf(Mode.getId(e.getMode())));
        sb.write('\n');
    }

    private String formatWalltime(Object value) {
        if (value == null) {
        	return "600";
        }
        else {
        	return String.valueOf(new WallTime(value.toString()).getSeconds());
        }
    }

    private void add(Writer sb, String key, boolean value) throws IOException {
        add(sb, key, String.valueOf(value));
    }
    
    private static final Charset UTF8 = Charset.forName("UTF-8");

    @SuppressWarnings("fallthrough")
    private void add(Writer sb, final String key, final String value) throws IOException {
        if (value != null) {
        	addKey(sb, key);
            escape(sb, value);
            sb.write('\n');
        }
    }

    private void addKey(Writer sb, String key) throws IOException { 
    	sb.write(key);
    	sb.write('=');
    }

    private void escape(Writer sb, String value) throws IOException {
    	for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\n':
                    c = 'n';
                case '\\':
                    sb.write('\\');
                default:
                    sb.write(c);
            }
        }
    }
    
    public void receiveCompleted() {
        id = getInDataAsString(0);
        super.receiveCompleted();
    }

    public Task getTask() {
        return t;
    }

    public boolean getSimple() {
        return simple;
    }

    public void setSimple(boolean simple) {
        this.simple = simple;
    }
    
    public static void main(String[] args) {
        try {
            URL u = new URL("/some path");
            System.out.println(u.getProtocol());
            System.out.println(u.getHost());
            System.out.println(u.getPort());
            System.out.println(u.getPath());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
