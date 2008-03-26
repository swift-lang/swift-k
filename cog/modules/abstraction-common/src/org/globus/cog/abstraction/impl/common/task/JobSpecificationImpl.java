// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

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

import org.globus.cog.abstraction.interfaces.Delegation;
import org.globus.cog.abstraction.interfaces.FileLocation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Specification;

public class JobSpecificationImpl implements JobSpecification {
    private Boolean delegationEnabled;
    private int delegation;
    private String nativeSpecification;
    private String stdout;
    private String stderr;
    private String stdin;
    private boolean batchJob;
    private boolean redirected;
    private boolean localExecutable;
    private boolean localInput;
    
    private int type;
    private Map attributes;
    private List arguments;
    private Map environment;
    private String directory;
    private String executable;
    private FileLocation stdinLocation, stdoutLocation, stderrLocation,
            executableLocation;

    public JobSpecificationImpl() {
        this.type = Specification.JOB_SUBMISSION;
        this.arguments = new ArrayList(4);
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
        return (String) this.arguments.remove(index);
    }

    public Vector getArgumentsAsVector() {
        return new Vector(this.arguments);
    }

    public List getArgumentsAsList() {
        return arguments;
    }

    public void setArguments(Vector arguments) {
        this.arguments = new ArrayList(arguments);
    }

    public void setArguments(List arguments) {
        this.arguments = arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = new ArrayList();
        StringTokenizer st = new StringTokenizer(arguments);
        while (st.hasMoreTokens()) {
            this.arguments.add(st.nextToken());
        }
    }

    public String getArguments() {
        return getArgumentsAsString();
    }

    public String getArgumentsAsString() {
        String arg;
        if (!this.arguments.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            Iterator i = this.arguments.iterator();
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
            environment = new HashMap();
        }
        environment.put(name, value);
    }

    public String removeEnvironmentVariable(String name) {
        if (environment != null) {
            return (String) environment.remove(name);
        }
        else {
            return null;
        }
    }

    public String getEnvironmentVariable(String name) {
        if (environment != null) {
            return (String) environment.get(name);
        }
        else {
            return null;
        }
    }

    public Collection getEnvironment() {
        if (environment != null) {
            return environment.keySet();
        }
        else {
            return Collections.EMPTY_MAP.keySet();
        }
    }

    public Collection getEnvironmentVariableNames() {
        if (environment != null) {
            return environment.keySet();
        }
        else {
            return Collections.EMPTY_MAP.keySet();
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
        FileLocation type = FileLocation.NONE;
        if (redir) {
            if (mem) {
                type = type.and(FileLocation.MEMORY);
            }
            if (isSet) {
                type = type.and(FileLocation.LOCAL);
            }
        }
        else {
            if (isSet) {
                type = type.and(FileLocation.REMOTE);
            }
        }
        return type;
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
            attributes = new HashMap();
        }
        attributes.put(name.toLowerCase(), value);
    }

    public Object getAttribute(String name) {
        if (attributes != null) {
            return attributes.get(name.toLowerCase());
        }
        else {
            return null;
        }
    }

    public Enumeration getAllAttributes() {
        if (attributes != null) {
            return new Vector(attributes.keySet()).elements();
        }
        else {
            return new Vector().elements();
        }
    }

    public Collection getAttributeNames() {
        if (attributes != null) {
            return attributes.keySet();
        }
        else {
            return Collections.EMPTY_MAP.keySet();
        }
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
        this.delegation = delegation ? Delegation.LIMITED_DELEGATION : Delegation.FULL_DELEGATION;
    }

    public int getDelegation() {
        return this.delegation;
    }

    public void setDelegation(int delegation) {
        this.delegation = delegation;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Job: ");
        sb.append("\n\texecutable: " + getExecutable());
        sb.append("\n\targuments:  " + getArguments());
        sb.append("\n\tstdout:     " + getStdOutput());
        sb.append("\n\tstderr:     " + getStdError());
        sb.append("\n\tdirectory:  " + getDirectory());
        sb.append("\n\tbatch:      " + isBatchJob());
        sb.append("\n\tredirected: " + isRedirected());
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
}