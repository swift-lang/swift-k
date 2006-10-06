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

import org.globus.cog.abstraction.interfaces.JobSpecification;
import org.globus.cog.abstraction.interfaces.Specification;

public class JobSpecificationImpl implements JobSpecification {
	private int type;
	private Map attributes;
	private Map additionalAttributes;
	private List arguments;
	private Map environment;

	public JobSpecificationImpl() {
		this.type = Specification.JOB_SUBMISSION;
		this.attributes = new HashMap();
		this.additionalAttributes = new HashMap();
		this.arguments = new ArrayList();
		this.environment = new HashMap();
	}

	public void setType(int type) {
		// By default = JOB_SUBMISSION
	}

	public int getType() {
		return this.type;
	}

	public void setSpecification(String specification) {
		this.attributes.put("rsl", specification);
	}

	public String getSpecification() {
		return (String) this.attributes.get("rsl");
	}

	public void setExecutable(String executable) {
		this.attributes.put("executable", executable);
	}

	public String getExecutable() {
		return (String) this.attributes.get("executable");
	}

	public void setDirectory(String directory) {
		this.attributes.put("directory", directory);
	}

	public String getDirectory() {
		return (String) this.attributes.get("directory");
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
		this.attributes.put("stdout", output);
	}

	public String getStdOutput() {
		return (String) this.attributes.get("stdout");
	}

	public void setStdInput(String input) {
		this.attributes.put("stdin", input);
	}

	public String getStdInput() {
		return (String) this.attributes.get("stdin");
	}

	public void setStdError(String error) {
		this.attributes.put("stderror", error);
	}

	public String getStdError() {
		return (String) this.attributes.get("stderror");
	}

	public void setBatchJob(boolean bool) {
		this.attributes.put("batchJob", bool ? "true" : "false");
	}

	public boolean isBatchJob() {
		Boolean bool = Boolean.valueOf((String) this.attributes.get("batchJob"));
		return bool.booleanValue();
	}

	public void setRedirected(boolean bool) {
		this.attributes.put("redirected", bool ? "true" : "false");
	}

	public boolean isRedirected() {
		Boolean bool = Boolean.valueOf((String) this.attributes.get("redirected"));
		return bool.booleanValue();
	}

	public void setLocalInput(boolean bool) {
		this.attributes.put("localInput", bool ? "true" : "false");
	}

	public boolean isLocalInput() {
		Boolean bool = Boolean.valueOf((String) this.attributes.get("localInput"));
		return bool.booleanValue();
	}

	public void setLocalExecutable(boolean bool) {
		this.attributes.put("localExecutable", Boolean.valueOf(bool));
	}

	public boolean isLocalExecutable() {
		return getBooleanAttribute("localExecutable", false);
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

	public void setAttribute(String name, Object value) {
		this.additionalAttributes.put(name, value);
	}

	public Object getAttribute(String name) {
		return this.additionalAttributes.get(name);
	}

	public Enumeration getAllAttributes() {
		return new Vector(additionalAttributes.keySet()).elements();
	}
	
	public Collection getAttributeNames() {
		return additionalAttributes.keySet();
	}

	public boolean isDelegationEnabled() {
		return getBooleanAttribute("delegationEnabled", false);
	}

	public void setDelegationEnabled(boolean delegation) {
		this.attributes.put("delegationEnabled", Boolean.valueOf(delegation));
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
}