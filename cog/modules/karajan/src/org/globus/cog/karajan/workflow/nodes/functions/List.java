// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on May 11, 2004
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.KarajanIterator;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureIterator;

public class List extends FunctionsCollection {
    public static final Arg PA_LIST = new Arg.Positional("list");

	static {
		setArguments("list_size", new Arg[] { PA_LIST });
	}

	public double list_size(VariableStack stack) throws ExecutionException {
		return TypeUtil.toList(PA_LIST.getValue(stack)).size();
	}

	static {
		setArguments("list_prepend", new Arg[] { PA_LIST, Arg.VARGS });
	}

	public Object list_prepend(VariableStack stack) throws ExecutionException {
		Object[] items = Arg.VARGS.asArray(stack);
		java.util.List l;
		if (PA_LIST.isPresent(stack)) {
			l = TypeUtil.toList(PA_LIST.getValue(stack));
		}
		else {
			l = new LinkedList();
		}
		for (int i = 0; i < items.length; i++) {
			l.add(0, items[i]);
		}
		return l;
	}

	static {
		setArguments("list_join", new Arg[] { Arg.VARGS });
	}

	public Object list_join(VariableStack stack) throws ExecutionException {
		Object[] items = Arg.VARGS.asArray(stack);
		ArrayList l = new ArrayList();
		for (int i = 0; i < items.length; i++) {
			l.addAll(TypeUtil.toList(items[i]));
		}
		return l;
	}
    
    public static final Arg OA_ITEMS = new Arg.Optional("items", null);

	static {
		setArguments("list_append", new Arg[] { PA_LIST, OA_ITEMS, Arg.VARGS });
	}

	public Object list_append(VariableStack stack) throws ExecutionException {
		Object lo = PA_LIST.getValue(stack);
		java.util.List items = null; 
		if (OA_ITEMS.isPresent(stack)) {
			items = TypeUtil.toList(OA_ITEMS.getValue(stack));
		}
		else {
			items = Arg.VARGS.asList(stack);
		}
		if (lo instanceof VariableArguments) {
			((VariableArguments) lo).appendAll(items);
		}
		else {
			TypeUtil.toList(lo).addAll(items);
		}
		return lo;
	}

	static {
		setArguments("list_list", new Arg[] { OA_ITEMS, Arg.VARGS });
	}

	public Object list_list(VariableStack stack) throws ExecutionException {
		ArrayList l = new ArrayList();
		if (OA_ITEMS.isPresent(stack)) {
			l.addAll(TypeUtil.toList(OA_ITEMS.getValue(stack)));
		}
		Object[] items = Arg.VARGS.asArray(stack);
		for (int i = 0; i < items.length; i++) {
			l.add(items[i]);
		}
		return l;
	}

	static {
		setArguments("list_butlast", new Arg[] { PA_LIST });
	}

	public Object list_butlast(VariableStack stack) throws ExecutionException {
		Object arg = PA_LIST.getValue(stack);
		java.util.List orig = TypeUtil.toList(arg);
		return orig.subList(0, orig.size() - 1);
	}

	static {
		setArguments("list_butfirst", new Arg[] { PA_LIST });
	}

	public Object list_butfirst(VariableStack stack) throws ExecutionException {
		Object arg = PA_LIST.getValue(stack);
		if (arg instanceof FutureIterator) {
			((FutureIterator) arg).next();
			return arg;
		}
		else if (arg instanceof VariableArguments) {
			return ((VariableArguments) arg).butFirst();
		}
		else {
			java.util.List orig = TypeUtil.toList(arg);
			return orig.subList(1, orig.size());
		}
	}

	static {
		setArguments("list_last", new Arg[] { PA_LIST });
	}

	public Object list_last(VariableStack stack) throws ExecutionException {
		java.util.List orig = TypeUtil.toList(PA_LIST.getValue(stack));
		if (orig.isEmpty()) {
			throw new ExecutionException("Empty list");
		}
		return orig.get(orig.size() - 1);
	}

	static {
		setArguments("list_first", new Arg[] { PA_LIST });
	}

	public Object list_first(VariableStack stack) throws ExecutionException {
		Object arg = PA_LIST.getValue(stack);
		if (arg instanceof java.util.List) {
			return ((java.util.List) arg).get(0);
		}
		if (arg instanceof KarajanIterator) {
			return ((KarajanIterator) arg).peek();
		}
		if (arg instanceof VariableArguments) {
			return ((VariableArguments) arg).get(0);
		}
		throw new ExecutionException("Not a vector: " + arg);
	}
    
    public static final Arg PA_INDEX = new Arg.Positional("index");
	
	static {
		setArguments("list_get", new Arg[] { PA_LIST, PA_INDEX });
	}

	public Object list_get(VariableStack stack) throws ExecutionException {
		Object arg = PA_LIST.getValue(stack);
		int index = TypeUtil.toInt(PA_INDEX.getValue(stack));
		if (index < 1) {
		    throw new ExecutionException("index < 1");
		}
		index--;
		if (arg instanceof java.util.List) {
			return ((java.util.List) arg).get(index);
		}
		if (arg instanceof VariableArguments) {
			return ((VariableArguments) arg).get(index);
		}
		throw new ExecutionException("Not a vector: " + arg);
	}


	static {
		setArguments("list_isempty", new Arg[] { PA_LIST });
	}

	public boolean list_isempty(VariableStack stack) throws ExecutionException {
		Object orig = PA_LIST.getValue(stack);
		if (orig instanceof VariableArguments) {
			return ((VariableArguments) orig).isEmpty();
		}
		else if (orig instanceof Collection) {
			return ((Collection) orig).isEmpty();
		}
		else {
			throw new ExecutionException("Invalid argument: " + orig);
		}
	}
}