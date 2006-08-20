// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 16, 2005
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.NameBindingVariableArguments;
import org.globus.cog.karajan.arguments.NamedArguments;
import org.globus.cog.karajan.arguments.NamedArgumentsImpl;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.arguments.VariableArgumentsImpl;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.stack.VariableUtil;
import org.globus.cog.karajan.util.AdaptiveArrayList;
import org.globus.cog.karajan.util.ArgumentsMap;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.futures.Future;

public abstract class AbstractSequentialWithArguments extends Sequential {

	public static final String QUOTED = "#quoted";

	public static Set commonArguments;

	static {
		commonArguments = new HashSet();
		commonArguments.add("annotation");
	}

	private int argElements = 0;
	private boolean quotedArgs;
	private boolean nestedArgs, hasVargs, hasChannels;
	private List nonpropargs;

	private boolean text;

	private static AdaptiveArrayList.Context npc = new AdaptiveArrayList.Context();

	protected void initializeStatic() {
		super.initializeStatic();
		checkArguments();
		hasVargs = hasVariableArguments();
		nestedArgs = hasNestedArguments();
		hasChannels = hasChannels();

		nonpropargs = new AdaptiveArrayList(npc);
		Map propargs = this.getStaticArguments();
		text = acceptsInlineText() && hasProperty(TEXT) && !(elementCount() > 0);
		String[] sorted = getSortedArgs();
		Set names = getArgumentNames();
		if (names == null) {
			return;
		}
		if (sorted != null) {
			if (text) {
				for (int i = 0; i < sorted.length; i++) {
					if (!propargs.containsKey(sorted[i])) {
						propargs.put(sorted[i], getProperty(TEXT));
						removeProperty(TEXT);
					}
				}
			}
			for (int j = 0; j < sorted.length; j++) {
				if (!propargs.containsKey(sorted[j])) {
					nonpropargs.add(sorted[j]);
				}
			}
		}
		argElements = nonpropargs.size();
	}

	protected final Object getArgument(Arg arg, VariableStack stack) throws ExecutionException {
		return getArgument0(arg.getName(), stack);
	}

	private final Object getArgument(String name, VariableStack stack) throws ExecutionException {
		return getArgument0(name.toLowerCase(), stack);
	}

	private Object getArgument0(String name, VariableStack stack) throws ExecutionException {
		NamedArguments args = ArgUtil.getNamedArguments(stack);
		if (args == null) {
			throw new ExecutionException("No named arguments on current frame");
		}
		Object value = args.getArgument(name);
		if (value == null) {
			if (args.hasArgument(name)) {
				return null;
			}
			else {
				throw new ExecutionException("Missing argument " + name);
			}
		}
		else {
			if (value instanceof Future) {
				return ((Future) value).getValue();
			}
			else {
				return value;
			}
		}
	}

	protected final Object[] getArguments(Arg[] args, VariableStack stack)
			throws ExecutionException {
		Object[] ret = new Object[args.length];
		NamedArguments named = ArgUtil.getNamedArguments(stack);
		for (int i = 0; i < args.length; i++) {
			ret[i] = args[i].getValue(stack);
		}
		return ret;
	}

	public boolean getQuotedArgs() {
		return quotedArgs;
	}

	public void setQuotedArgs(boolean quotedArgs) {
		this.quotedArgs = quotedArgs;
	}

	protected void argumentsEvaluated(VariableStack stack) throws ExecutionException {

	}

	public void pre(VariableStack stack) throws ExecutionException {
		super.pre(stack);
		initializeArgs(stack);
		if (elementCount() == 0) {
			argumentsEvaluated(stack);
			if (quotedArgs) {
				stack.currentFrame().deleteVar(QUOTED);
			}
		}
	}

	protected void initializeArgs(final VariableStack stack) throws ExecutionException {
		if (quotedArgs) {
			stack.setVar(QUOTED, true);
		}
		boolean vargsInitialized = false;
		if (nestedArgs) {
			NamedArgumentsImpl nargs = new NamedArgumentsImpl(getArgumentNames(), this);
			// add arguments from properties
			final Iterator i = getStaticArguments().entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry e = (Map.Entry) i.next();
				nargs.add((String) e.getKey(), VariableUtil.expand(e.getValue(), stack));
			}
			if (nonpropargs.size() > 0 && elementCount() > 0) {
				VariableArguments args = newNameBindingVariableArguments(nargs, nonpropargs,
						hasVargs);
				ArgUtil.setVariableArguments(stack, args);
				vargsInitialized = true;
			}
			ArgUtil.setNamedArguments(stack, nargs);
		}
		if (hasVargs && !vargsInitialized) {
			ArgUtil.setVariableArguments(stack, newVariableArguments());
		}
		if (hasChannels) {
			ArgUtil.createChannels(stack, getChannels());
		}
	}

	protected VariableArguments newVariableArguments() {
		return new VariableArgumentsImpl();
	}

	protected VariableArguments newNameBindingVariableArguments(NamedArguments nargs,
			List nonpropargs2, boolean hasVargs) {
		return new NameBindingVariableArguments(nargs, nonpropargs, hasVargs, this);
	}

	protected void childCompleted(VariableStack stack) throws ExecutionException {
		if (elementCount() == 1 || getIndex(stack) == elementCount()) {
			processArguments(stack);
			argumentsEvaluated(stack);
			if (quotedArgs) {
				stack.currentFrame().deleteVar(QUOTED);
			}
			post(stack);
		}
		else {
			super.childCompleted(stack);
		}
	}

	protected void processArguments(VariableStack stack) throws ExecutionException {

	}

	protected static void setVargs(Object owner, boolean value) {
		ArgumentsMap.getMap().getVargs().add(owner);
	}

	protected static void addChannel(Object owner, Arg.Channel channel) {
		ArgumentsMap.getMap().addChannel(owner, channel);
	}

	protected boolean hasVariableArguments() {
		return ArgumentsMap.getMap().getVargs().contains(getCanonicalType());
	}

	protected boolean hasChannels() {
		return ArgumentsMap.getMap().getChannels(getCanonicalType()) != null;
	}

	protected boolean hasNestedArguments() {
		// return validArgs.containsKey(getCanonicalType());
		return true;
	}

	protected static final void setArguments(Object owner, Arg[] arga) {
		Map args = new HashMap(arga.length * 4 / 3);
		ArgumentsMap.getMap().getValidArgs().put(owner, args);
		int maxindex = -1;
		int implicitIndex = 0;
		for (int i = 0; i < arga.length; i++) {
			if (arga[i] == Arg.VARGS) {
				setVargs(owner, true);
				continue;
			}
			else if (arga[i].getIndex() == Arg.CHANNEL) {
				addChannel(owner, (Arg.Channel) arga[i]);
				continue;
			}
			if (arga[i].getIndex() == Arg.IMPLICIT) {
				arga[i] = new Arg.Positional(arga[i].getName(), implicitIndex++);
			}
			args.put(arga[i].getName(), new Integer(arga[i].getIndex()));
			if (arga[i].getIndex() > maxindex) {
				maxindex = arga[i].getIndex();
			}
		}
		if (maxindex > arga.length) {
			throw new KarajanRuntimeException("Invalid argument indexing. Maxindex was " + maxindex
					+ " while the total number of args was " + arga.length);
		}
		String[] sorted;
		Set optional = new HashSet();

		sorted = new String[maxindex + 1];
		for (int i = 0; i < arga.length; i++) {
			if (arga[i] == Arg.VARGS) {
				continue;
			}
			int index = arga[i].getIndex();
			if (index == Arg.CHANNEL) {
				continue;
			}
			if (index == Arg.NOINDEX) {
				optional.add(arga[i].getName());
				continue;
			}
			if (sorted[index] != null) {
				throw new KarajanRuntimeException("Invalid index for argument " + arga[i].getName()
						+ ". It was used before by " + sorted[index]);
			}
			sorted[index] = arga[i].getName();
		}
		for (int i = 0; i < sorted.length; i++) {
			if (sorted[i] == null) {
				throw new KarajanRuntimeException("No argument for index " + i);
			}
		}

		if (sorted != null && sorted.length > 0) {
			ArgumentsMap.getMap().getSortedArgs().put(owner, sorted);
		}
		if (optional.size() != 0) {
			ArgumentsMap.getMap().getOptionals().put(owner, optional);
		}
		ArgumentsMap.getMap().getMaxIndices().put(owner, new Integer(maxindex));
	}

	protected String[] getSortedArgs() {
		return (String[]) ArgumentsMap.getMap().getSortedArgs().get(getCanonicalType());
	}

	protected Set getOptionalArgs() {
		return (Set) ArgumentsMap.getMap().getOptionals().get(getCanonicalType());
	}

	/*
	 * protected static void setArguments(Object owner, String[] arga) { Map
	 * args = new HashMap(arga.length * 4 / 3);
	 * ArgumentsMap.getMap().getValidArgs().put(owner, args);
	 * 
	 * for (int i = 0; i < arga.length; i++) { args.put(arga[i], new
	 * Integer(i)); } ArgumentsMap.getMap().getSortedArgs().put(owner, arga);
	 * ArgumentsMap.getMap().getMaxIndices().put(owner, new
	 * Integer(arga.length)); }
	 */

	protected int getMaxArgIndex() {
		return ((Integer) ArgumentsMap.getMap().getMaxIndices().get(getCanonicalType())).intValue();
	}

	protected int getArgumentIndex(String name) {
		Map args = (Map) ArgumentsMap.getMap().getValidArgs().get(getCanonicalType());
		if (args == null || !args.containsKey(name)) {
			throw new KarajanRuntimeException("Internal error: " + name
					+ " is not defined as an argument for " + getCanonicalType()
					+ " but its value was queried");
		}
		return ((Integer) args.get(name)).intValue();
	}

	private Set argNames;

	protected Set getArgumentNames() {
		if (argNames == null) {
			Map args = (Map) ArgumentsMap.getMap().getValidArgs().get(getCanonicalType());
			if (args == null) {
				argNames = Collections.EMPTY_SET;
			}
			else {
				argNames = args.keySet();
			}
		}
		return argNames;
	}

	private List channels;

	protected List getChannels() {
		if (channels == null) {
			channels = ArgumentsMap.getMap().getChannels(getCanonicalType());
		}
		return channels;
	}

	protected int getArgumentCount() {
		Map args = (Map) ArgumentsMap.getMap().getValidArgs().get(getCanonicalType());
		if (args == null) {
			return 0;
		}
		else {
			return args.size();
		}
	}

	protected void checkArguments() {
		Map args = (Map) ArgumentsMap.getMap().getValidArgs().get(getCanonicalType());
		for (Iterator i = getStaticArguments().keySet().iterator(); i.hasNext();) {
			String name = (String) i.next();
			if (args == null || !args.containsKey(name)) {
				if (commonArguments.contains(name)) {
					continue;
				}
				if (args != null) {
					throw new KarajanRuntimeException("\tUnsupported argument: " + name
							+ ". Valid arguments are: " + args.keySet());
				}
				else {
					throw new KarajanRuntimeException("\tUnsupported argument: " + name
							+ ". This element does not support any arguments.");
				}
			}
		}
	}

	protected void setHasVargs(boolean hasVargs) {
		this.hasVargs = hasVargs;
	}

	protected void setNestedArgs(boolean nestedArgs) {
		this.nestedArgs = nestedArgs;
	}

	protected List getNonpropargs() {
		return nonpropargs;
	}

	protected void setNonpropargs(List nonpropargs) {
		this.nonpropargs = nonpropargs;
	}
}
