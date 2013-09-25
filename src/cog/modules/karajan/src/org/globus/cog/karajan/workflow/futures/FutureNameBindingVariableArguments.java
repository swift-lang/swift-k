// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 20, 2005
 */
package org.globus.cog.karajan.workflow.futures;

import java.util.List;

import org.globus.cog.karajan.arguments.NamedArguments;
import org.globus.cog.karajan.arguments.VariableArguments;

public class FutureNameBindingVariableArguments extends FutureVariableArguments {
	private final NamedArguments named;
	private final String[] names;
	private int index;

	public FutureNameBindingVariableArguments(NamedArguments named, String[] names) {
		this.named = named;
		this.names = names;
		index = 0;
	}

	private static final String[] STRARRAY = new String[0];

	public FutureNameBindingVariableArguments(NamedArguments named, List names) {
		this.named = named;
		this.names = (String[]) names.toArray(STRARRAY);
		index = 0;
	}

	public synchronized void append(Object value) {
		while (index < names.length && named.hasArgument(names[index])) {
			index++;
		}
		if (index < names.length) {
			named.add(names[index++], value);
		}
		else {
			super.append(value);
		}
	}

	public synchronized void appendAll(List args) {
		int i = 0;
		while (index < names.length) {
			while (index < names.length && named.hasArgument(names[index])) {
				index++;
			}
			if (index < names.length) {
				named.add(names[index++], args.get(i++));
			}
			else {
				super.appendAll(args.subList(i, args.size()));
				return;
			}
		}
		super.appendAll(args.subList(i, args.size()));
	}

	public void merge(VariableArguments args) {
		appendAll(args.getAll());
	}

	public synchronized void set(List args) {
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
}
