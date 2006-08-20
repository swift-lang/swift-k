// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 20, 2005
 */
package org.globus.cog.karajan.arguments;

import java.util.List;

import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

public class NameBindingVariableArguments extends VariableArgumentsImpl {
	private final NamedArguments named;
	private final String[] names;
	private int index;
	private final boolean hasVargs;
	private final FlowElement owner;

	public NameBindingVariableArguments(NamedArguments named, String[] names, boolean hasVargs,
			FlowElement owner) {
		this.named = named;
		this.names = names;
		index = 0;
		this.hasVargs = hasVargs;
		this.owner = owner;
	}

	private static final String[] STRARRAY = new String[0];

	public NameBindingVariableArguments(NamedArguments named, List names, boolean hasVargs,
			FlowElement owner) {
		this(named, (String[]) names.toArray(STRARRAY), hasVargs, owner);
	}

	public synchronized void append(Object value) {
		while (index < names.length && named.hasArgument(names[index])) {
			index++;
		}
		if (index < names.length) {
			named.add(names[index++], value);
		}
		else {
			if (!hasVargs) {
				throw new KarajanRuntimeException("Illegal extra argument to " + owner);
			}
			else {
				super.append(value);
			}
		}
	}

	public void appendAll(List args) {
		int i = 0;
		while (index < names.length) {
			while (index < names.length && named.hasArgument(names[index])) {
				index++;
			}
			if (index < names.length) {
				if (i < args.size()) {
					named.add(names[index++], args.get(i++));
				}
				else {
					return;
				}
			}
			else {
				break;
			}
		}
		if (!args.isEmpty()) {
			if (args.size() > i) {
				if (!hasVargs) {
					throw new KarajanRuntimeException("Illegal extra argument to " + owner);
				}
				else {
					super.appendAll(args.subList(i, args.size()));
				}
			}
		}
	}

	public void merge(VariableArguments args) {
		appendAll(args.getAll());
	}

	public void set(List args) {
		int i = 0;
		while (index < names.length) {
			while (index < names.length && named.hasArgument(names[index])) {
				index++;
			}
			if (index < names.length) {
				named.add(names[index++], args.get(i++));
			}
			else {
				super.set(args.subList(i, args.size() - 1));
				return;
			}
		}
		super.set(args.subList(i, args.size() - 1));
	}

	public void set(VariableArguments other) {
		set(other.getAll());
	}

	public int argSize() {
		return index;
	}
}
