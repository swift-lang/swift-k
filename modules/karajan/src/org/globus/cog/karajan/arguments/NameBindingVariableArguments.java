// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 20, 2005
 */
package org.globus.cog.karajan.arguments;

import java.util.Arrays;
import java.util.List;

import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

/**
 * An implementation of a channel that binds values to a set of NamedArguments
 * based on a list of names. This is done in order to allow access
 * to arguments by name while supplying them in a positional fashion.
 * 
 * @author Mihael Hategan
 *
 */
public class NameBindingVariableArguments extends VariableArgumentsImpl {
	private final NamedArguments named;
	private final List<String> names;
	private int index;
	private final boolean hasVargs;
	private final FlowElement owner;

	public NameBindingVariableArguments(NamedArguments named, List<String> names, boolean hasVargs,
			FlowElement owner) {
		this.named = named;
		this.names = names;
		index = 0;
		this.hasVargs = hasVargs;
		this.owner = owner;
	}

	public synchronized void append(Object value) {
	    for (String name : names) {
	        if (!named.hasArgument(name)) {
	            named.add(name, value);
	            return;
	        }
	    }
		
		if (!hasVargs) {
			System.out.println("" + value);
			throw new KarajanRuntimeException("Illegal extra argument `" + value + "' to " + owner);
		}
		else {
			super.append(value);
		}
	}

	public void appendAll(List args) {
		int i = 0;
		for (String name : names) {
            if (!named.hasArgument(name)) {
                if (i < args.size()) {
                	named.add(name, args.get(i++));
                }
                else {
                    return;
                }
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
        for (String name : names) {
            if (!named.hasArgument(name)) {
                if (i < args.size()) {
                    named.add(name, args.get(i++));
                }
                else {
                    return;
                }
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
