// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.globus.cog.abstraction.interfaces.Delegation;
import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Specification;
import org.globus.cog.abstraction.interfaces.FileLocation;

public class JobSpecificationImpl implements JobSpecification {
    public static final String ATTR_DELEGATION_ENABLED = "delegationEnabled";
    public static final String ATTR_DELEGATION = "delegation";
    // "rsl" is for backwards compatibility purposes
    public static final String ATTR_NATIVE_SPECIFICATION = "rsl";
    public static final String ATTR_STDOUT = "stdout";
    public static final String ATTR_STDERR = "stderror";// ??
    public static final String ATTR_STDIN = "stdin";
    public static final String ATTR_BATCH_JOB = "batchJob";
    public static final String ATTR_REDIRECTED = "redirected";
    public static final String ATTR_LOCAL_EXECUTABLE = "localExecutable";
    public static final String ATTR_LOCAL_INPUT = "localInput";

    private int type;
    private Map attributes;
    private Map additionalAttributes;
    private List arguments;
    private Map environment;
    private String directory;
    private String executable;
    private FileLocation stdinLocation, stdoutLocation, stderrLocation,
            executableLocation;

    public JobSpecificationImpl() {
        this.type = Specification.JOB_SUBMISSION;
        this.attributes = new HashMap();
        this.additionalAttributes = new HashMap();
        this.arguments = new ArrayList();
        this.environment = new HashMap();
        this.stdinLocation = FileLocation.REMOTE;
        this.stdoutLocation = FileLocation.REMOTE;
        this.stderrLocation = FileLocation.REMOTE;
        this.executableLocation = FileLocation.REMOTE;
    }

    public void setType(int type) {
        // By default = JOB_SUBMISSION
    }

    public int getType() {
        return this.type;
    }

    public void setSpecification(String specification) {
        this.attributes.put(ATTR_NATIVE_SPECIFICATION, specification);
    }

    public String getSpecification() {
        return (String) this.attributes.get(ATTR_NATIVE_SPECIFICATION);
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
        this.environment.put(name, value);
    }

    public String removeEnvironmentVariable(String name) {
        return (String) this.environment.remove(name);
    }

    public String getEnvironmentVariable(String name) {
        return (String) this.environment.get(name);
    }

    public Collection getEnvironment() {
        return this.environment.keySet();
    }

    public Collection getEnvironmentVariableNames() {
        return this.environment.keySet();
    }

    public void setStdOutput(String output) {
        this.attributes.put(ATTR_STDOUT, output);
        this.stdoutLocation = inferStreamType(isRedirected(), output != null,
                true);
    }

    public String getStdOutput() {
        return (String) this.attributes.get(ATTR_STDOUT);
    }

    public void setStdInput(String input) {
        this.attributes.put(ATTR_STDIN, input);
        if (this.attributes.containsKey(ATTR_REDIRECTED)) {
            this.stdinLocation = inferStreamType(isLocalExecutable(),
                    input != null, false);
        }
    }

    public String getStdInput() {
        return (String) this.attributes.get(ATTR_STDIN);
    }

    public void setStdError(String error) {
        this.attributes.put(ATTR_STDERR, error);
        if (this.attributes.containsKey(ATTR_REDIRECTED)) {
            this.stderrLocation = inferStreamType(isRedirected(),
                    error != null, true);
        }
    }

    public String getStdError() {
        return (String) this.attributes.get(ATTR_STDERR);
    }

    public void setBatchJob(boolean bool) {
        this.attributes.put(ATTR_BATCH_JOB, Boolean.valueOf(bool));
    }

    public boolean isBatchJob() {
        return getBooleanAttribute(ATTR_BATCH_JOB, false);
    }

    public void setRedirected(boolean bool) {
        this.attributes.put(ATTR_REDIRECTED, Boolean.valueOf(bool));
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
        return getBooleanAttribute(ATTR_REDIRECTED, false);
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

    private boolean getBooleanAttribute(final String name, boolean def) {
        Boolean bool = ((Boolean) this.attributes.get(name));
        if (bool == null) {
            return def;
        }
        else {
            return bool.booleanValue();
        }
    }

    private int getIntAttribute(final String name, int def) {
        Integer val = ((Integer) this.attributes.get(name));
        if (val == null) {
            return def;
        }
        else {
            return val.intValue();
        }
    }

    public void setAttribute(String name, Object value) {
        this.additionalAttributes.put(name.toLowerCase(), value);
    }

    public Object getAttribute(String name) {
        return this.additionalAttributes.get(name.toLowerCase());
    }

    public Enumeration getAllAttributes() {
        return new Vector(additionalAttributes.keySet()).elements();
    }

    public Collection getAttributeNames() {
        return additionalAttributes.keySet();
    }

    public boolean isDelegationEnabled() {
        return getBooleanAttribute(
                ATTR_DELEGATION_ENABLED,
                getIntAttribute(ATTR_DELEGATION, Delegation.NO_DELEGATION) != Delegation.NO_DELEGATION);
    }

    public void setDelegationEnabled(boolean delegation) {
        this.attributes.put(ATTR_DELEGATION, new Integer(
                delegation ? Delegation.LIMITED_DELEGATION
                        : Delegation.FULL_DELEGATION));
    }

    public int getDelegation() {
        return getIntAttribute(ATTR_DELEGATION, Delegation.NO_DELEGATION);
    }

    public void setDelegation(int delegation) {
        this.attributes.put(ATTR_DELEGATION, new Integer(delegation));
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