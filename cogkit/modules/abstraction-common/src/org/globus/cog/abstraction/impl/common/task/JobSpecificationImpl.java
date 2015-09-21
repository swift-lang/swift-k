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

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.interfaces.CleanUpSet;
import org.globus.cog.abstraction.interfaces.Delegation;
import org.globus.cog.abstraction.interfaces.EnvironmentVariable;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Specification;
import org.globus.cog.abstraction.interfaces.StagingSet;
import org.globus.cog.util.StringUtil;

public class JobSpecificationImpl implements JobSpecification {

    Logger logger = Logger.getLogger(JobSpecificationImpl.class);

    private static final long serialVersionUID = 1L;
    private Boolean delegationEnabled;
    private int delegation;
    private String nativeSpecification;
    private String stdout;
    private String stderr;
    private String stdin;
    private boolean batchJob;
    private boolean redirected;
    private boolean localExecutable;

    private final int type;
    private Map<String, Object> attributes;
    private List<String> arguments;
    private List<EnvironmentVariable> environment;
    private String directory;
    private String executable;
    private FileLocation stdinLocation, stdoutLocation, stderrLocation,
            executableLocation;
    private StagingSet stagein, stageout;
    private CleanUpSet cleanUpSet;

    public JobSpecificationImpl() {
        this.type = Specification.JOB_SUBMISSION;
        this.arguments = new ArrayList<String>(4);
        this.stdinLocation = FileLocation.REMOTE;
        this.stdoutLocation = FileLocation.REMOTE;
        this.stderrLocation = FileLocation.REMOTE;
        this.executableLocation = FileLocation.REMOTE;
        this.delegation = Delegation.NO_DELEGATION;
    }

    public void setType(int type) {
        // By default = JOB_SUBMISSION
    }

    public int getType() {
        return this.type;
    }

    public void setSpecification(String specification) {
        this.nativeSpecification = specification;
    }

    public String getSpecification() {
        return this.nativeSpecification;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public String getExecutable() {
        return executable;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getDirectory() {
        return directory;
    }

    public void addArgument(String argument) {
        this.arguments.add(argument);
    }

    public void addArgument(int index, String argument) {
        // That was useless since it only increased the underlying array, not
        // that of the vector/list
        // this.arguments.ensureCapacity(index + 1);
        this.arguments.add(index, argument);
    }

    public void setArgument(int index, String argument) {
        this.arguments.set(index, argument);
    }

    public void removeArgument(String argument) {
        this.arguments.remove(argument);
    }

    public String removeArgument(int index) {
        return this.arguments.remove(index);
    }

    public Vector<String> getArgumentsAsVector() {
        return new Vector<String>(this.arguments);
    }

    public List<String> getArgumentsAsList() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(arguments);
        while (st.hasMoreTokens()) {
            this.arguments.add(st.nextToken());
        }
    }

    public String getArguments() {
        return getArgumentsAsString();
    }

    public String getArgumentsAsString() {
        if (!this.arguments.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            Iterator<String> i = this.arguments.iterator();
            while (i.hasNext()) {
                sb.append(i.next());
                if (i.hasNext()) {
                    sb.append(' ');
                }
            }
            return sb.toString();
        }
        else {
            return "";
        }
    }

    public void addEnvironmentVariable(String name, String value) {
        if (environment == null) {
            environment = new ArrayList<EnvironmentVariable>();
        }
        environment.add(new EnvironmentVariableImpl(name, value));
    }


    public void addEnvironmentVariable(String name, int i) {
        addEnvironmentVariable(name, Integer.toString(i));
    }

    public String removeEnvironmentVariable(String name) {
        if (environment != null) {
            Iterator<EnvironmentVariable> i = environment.iterator();
            while (i.hasNext()) {
                EnvironmentVariable e = i.next();
                if (name.equals(e.getName())) {
                    i.remove();
                    return e.getValue();
                }
            }
            return null;
        }
        else {
            return null;
        }
    }

    public String getEnvironmentVariable(String name) {
        if (environment != null) {
            Iterator<EnvironmentVariable> i = environment.iterator();
            while (i.hasNext()) {
                EnvironmentVariable e = i.next();
                if (name.equals(e.getName())) {
                    return e.getValue();
                }
            }
            return null;
        }
        else {
            return null;
        }
    }

    public List<EnvironmentVariable> getEnvironment() {
        if (environment != null) {
            return environment;
        }
        else {
            return Collections.emptyList();
        }
    }

    public Collection<String> getEnvironmentVariableNames() {
        if (environment != null) {
            return new AbstractList<String>() {
                @Override
                public String get(int index) {
                    return environment.get(index).getName();
                }

                @Override
                public int size() {
                    return environment.size();
                }
            };
        }
        else {
            return Collections.emptySet();
        }
    }

    @Override
    public void setEnvironmentVariables(List<EnvironmentVariable> env) {
        this.environment = env;
    }

    @Override
    public void addEnvironmentVariable(EnvironmentVariable e) {
        if (environment == null) {
            environment = new ArrayList<EnvironmentVariable>();
        }
        environment.add(e);
    }

    @Override
    public void setEnvironmentVariables(Map<String, String> env) {
        environment = new ArrayList<EnvironmentVariable>();
        for (Map.Entry<String, String> e : env.entrySet()) {
            addEnvironmentVariable(new EnvironmentVariableImpl(e.getKey(), e.getValue()));
        }
    }

    public void setStdOutput(String output) {
        this.stdout = output;
        this.stdoutLocation = inferStreamType(redirected, output != null,
                true);
    }

    public String getStdOutput() {
        return this.stdout;
    }

    public void setStdInput(String input) {
        this.stdin = input;
        this.stdinLocation = inferStreamType(localExecutable, input != null,
                false);
    }

    public String getStdInput() {
        return this.stdin;
    }

    public void setStdError(String error) {
        this.stderr = error;
        this.stderrLocation = inferStreamType(redirected, error != null,
                true);
    }

    public String getStdError() {
        return this.stderr;
    }

    public void setBatchJob(boolean bool) {
        this.batchJob = bool;
    }

    public boolean isBatchJob() {
        return this.batchJob;
    }

    public void setRedirected(boolean bool) {
        this.redirected = bool;
        this.stdoutLocation = inferStreamType(isRedirected(),
                getStdOutput() != null, true);
        this.stderrLocation = inferStreamType(isRedirected(),
                getStdError() != null, true);
    }

    protected FileLocation inferStreamType(boolean redir, boolean isSet,
            boolean mem) {
        FileLocation location = FileLocation.NONE;
        if (redir) {
            if (mem) {
                location = location.and(FileLocation.MEMORY);
            }
            if (isSet) {
                location = location.and(FileLocation.LOCAL);
            }
        }
        else {
            if (isSet) {
                location = location.and(FileLocation.REMOTE);
            }
        }
        return location;
    }

    public boolean isRedirected() {
        return this.redirected;
    }

    public void setLocalInput(boolean bool) {
        this.stdinLocation = bool ? FileLocation.LOCAL : FileLocation.REMOTE;
    }

    public boolean isLocalInput() {
        return FileLocation.LOCAL.equals(this.stdinLocation);
    }

    public void setLocalExecutable(boolean bool) {
        this.executableLocation = bool ? FileLocation.LOCAL
                : FileLocation.REMOTE;
    }

    public boolean isLocalExecutable() {
        return FileLocation.LOCAL.equals(this.executableLocation);
    }

    public void setAttribute(String name, Object value) {
        if (attributes == null) {
            attributes = new HashMap<String, Object>();
        }
        attributes.put(name.toLowerCase(), value);
    }

    /**
       Set attribute pair formatted as "key=value" or "key"
       The latter form sets the value to the empty String
       @param pair May not be null or the empty String
     */
    void setAttribute(String pair)
    {
        if (pair == null)
            throw new NullPointerException();
        if (pair.length() == 0)
            throw new IllegalArgumentException
            ("Attempted to set blank attribute!");
        String[] tokens = pair.split("=", 2);
        if (tokens.length == 1)
            setAttribute(tokens[0].trim(), "");
        else
            setAttribute(tokens[0].trim(), tokens[1].trim());
    }

    public Object getAttribute(String name) {
        if (attributes != null) {
            return attributes.get(name.toLowerCase());
        }
        else {
            return null;
        }
    }

    public Enumeration<String> getAllAttributes() {
        if (attributes != null) {
            return new Vector<String>(attributes.keySet()).elements();
        }
        else {
            return new Vector<String>().elements();
        }
    }

    public Collection<String> getAttributeNames() {
        if (attributes != null) {
            return attributes.keySet();
        }
        else {
            return Collections.emptySet();
        }
    }

    public Object removeAttribute(String name) {
        return attributes.remove(name);
    }

    public boolean isDelegationEnabled() {
        if (delegationEnabled != null) {
            return delegationEnabled.booleanValue();
        }
        else {
            return delegation != Delegation.NO_DELEGATION;
        }
    }

    public void setDelegationEnabled(boolean delegation) {
        this.delegation = delegation ? Delegation.FULL_DELEGATION : Delegation.NO_DELEGATION;
    }

    public int getDelegation() {
        return this.delegation;
    }

    public void setDelegation(int delegation) {
        this.delegation = delegation;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Job: ");
        sb.append("\n\texecutable: " + getExecutable());
        sb.append("\n\targuments:  " + getArguments());
        sb.append("\n\tstdout:     " + getStdOutput());
        sb.append("\n\tstderr:     " + getStdError());
        sb.append("\n\tdirectory:  " + getDirectory());
        sb.append("\n\tbatch:      " + isBatchJob());
        sb.append("\n\tredirected: " + isRedirected());
        sb.append("\n\tattributes: ");
        sb.append(StringUtil.toString(attributes));
        sb.append("\n\tenv:        ");
        sb.append(StringUtil.toString(environment));
        sb.append('\n');

        return sb.toString();
    }

    public FileLocation getStdErrorLocation() {
        return stderrLocation;
    }

    public FileLocation getStdInputLocation() {
        return stdinLocation;
    }

    public FileLocation getStdOutputLocation() {
        return stdoutLocation;
    }

    public void setStdErrorLocation(FileLocation type) {
        this.stderrLocation = type;
    }

    public void setStdInputLocation(FileLocation type) {
        this.stdinLocation = type;
    }

    public void setStdOutputLocation(FileLocation type) {
        this.stdoutLocation = type;
    }

    public FileLocation getExecutableLocation() {
        return executableLocation;
    }

    public void setExecutableLocation(FileLocation executableLocation) {
        if (FileLocation.MEMORY.equals(executableLocation)) {
            throw new IllegalArgumentException(
                    "Memory is not a valid setting for the executable location");
        }
        this.executableLocation = executableLocation;
    }

    public StagingSet getStageIn() {
        return stagein;
    }

    public StagingSet getStageOut() {
        return stageout;
    }

    public void setStageIn(StagingSet stagein) {
        this.stagein = stagein;
    }

    public void setStageOut(StagingSet stageout) {
        this.stageout = stageout;
    }

    public CleanUpSet getCleanUpSet() {
        return cleanUpSet;
    }

    public void setCleanUpSet(CleanUpSet cleanUpSet) {
        this.cleanUpSet = cleanUpSet;
    }

    @Override
    public Object clone() {
        JobSpecificationImpl result = null;
        try {
            result = (JobSpecificationImpl) super.clone();
            if (attributes != null) {
                result.attributes =
                    new HashMap<String,Object>(attributes);
            }
            result.arguments = new ArrayList<String>(arguments);
            if (environment != null) {
                result.environment = new ArrayList<EnvironmentVariable>(environment);
            }
            if (cleanUpSet != null) {
                result.cleanUpSet = (CleanUpSet) cleanUpSet.clone();
            }
            if (stagein != null) {
                result.stagein = (StagingSet) stagein.clone();
            }
            if (stageout != null) {
                result.stageout = (StagingSet) stageout.clone();
            }
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void unpackProviderAttributes() {
        String attrs = (String) getAttribute("providerAttributes");

        if (attrs == null)
            return;

        logger.debug("unpacking: " + attrs);
        String[] tokens = attrs.split("[;\n]");
        for (String token : tokens) {
            token = token.trim();
            if (token.length() > 0)
                setAttribute(token);
        }
    }
}
