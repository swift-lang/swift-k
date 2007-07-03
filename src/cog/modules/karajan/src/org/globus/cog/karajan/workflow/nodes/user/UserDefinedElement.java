// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jun 6, 2003
 */
package org.globus.cog.karajan.workflow.nodes.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.Arguments;
import org.globus.cog.karajan.arguments.OptionalArgument;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.translator.atoms.Transliterator;
import org.globus.cog.karajan.util.ChannelIdentifier;
import org.globus.cog.karajan.util.DefUtil;
import org.globus.cog.karajan.util.DefinitionEnvironment;
import org.globus.cog.karajan.util.Identifier;
import org.globus.cog.karajan.util.NonCacheable;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.events.ControlEvent;
import org.globus.cog.karajan.workflow.events.ControlEventType;
import org.globus.cog.karajan.workflow.futures.Future;
import org.globus.cog.karajan.workflow.nodes.AbstractSequentialWithArguments;
import org.globus.cog.karajan.workflow.nodes.Info;
import org.globus.cog.karajan.workflow.nodes.Sequential;

public abstract class UserDefinedElement extends AbstractSequentialWithArguments implements
		NonCacheable {

	public static final String KMODE = "_kmode";
	public static final String SKIP = "_skip";

	public static final Arg A_NAME = new Arg.Positional("name");
	public static final Arg A_ARGUMENTS = new Arg.Positional("arguments");
	public static final Arg A_VARGS = new Arg.Positional("vargs");
	public static final Arg A_NAMED = new Arg.Positional("named");
	public static final Arg A_CHANNELS = new Arg.Positional("channels");
	public static final Arg A_OPTARGS = new Arg.Positional("optargs");

	public static final ControlEventType START_BODY = new ControlEventType("START_INSTANCE", 2);

	private static final Logger logger = Logger.getLogger(UserDefinedElement.class);

	public static final String FNARGS = "#fnargs";
	public static final String PREFIX = "#def#";

	public static final String ARGUMENTS_THREAD = "#argthread";
	public static final String BODY_THREAD = "#bodythread";

	public static final List NO_CHANNELS = Collections.EMPTY_LIST;

	public static final String[] NO_ARGUMENTS = new String[0];

	private Sequential seq;

	private int callcount;

	private String[] arguments;

	private String[] optargs;

	private boolean vargs, named;

	private String type;

	private List channels;

	private boolean kmode;

	private int skip;

	public UserDefinedElement() {
		setOptimize(false);
		callcount = 0;
	}

	protected void initializeStatic() {
		super.initializeStatic();
		type = TypeUtil.toString(A_NAME.getStatic(this));
		String args = TypeUtil.toString(A_ARGUMENTS.getStatic(this));
		if (args != null) {
			args = args.toLowerCase();
		}
		String opt = TypeUtil.toString(A_OPTARGS.getStatic(this));
		if (opt != null) {
			opt = opt.toLowerCase();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Initializing " + type);
		}
		if (type == null && args == null && opt == null) {
			setProperty(KMODE, true);
			if (elementCount() < 1) {
				throw new KarajanRuntimeException("Missing identifier and/or argument list");
			}
		}
		else {
			vargs = TypeUtil.toBoolean(A_VARGS.getStatic(this, Boolean.FALSE));
			named = TypeUtil.toBoolean(A_NAMED.getStatic(this, Boolean.FALSE));
			if (args != null) {
				arguments = TypeUtil.toStringArray(args);
			}
			else {
				arguments = NO_ARGUMENTS;
			}

			if (opt != null) {
				optargs = TypeUtil.toStringArray(opt);
			}
			else {
				optargs = NO_ARGUMENTS;
			}

			if (A_CHANNELS.isPresentStatic(this)) {
				channels = new LinkedList();
				String[] cn = TypeUtil.toStringArray(A_CHANNELS.getStatic(this));
				for (int i = 0; i < cn.length; i++) {
					channels.add(new Arg.Channel(cn[i]));
				}
			}
			else {
				channels = NO_CHANNELS;
			}
		}
	}

	public void pre(VariableStack stack) throws ExecutionException {
		super.pre(stack);
		skip = 0;
		if (!kmode) {
			if (type != null) {
				DefUtil.updateEnvCache(stack, stack.parentFrame());
				DefUtil.addDef(stack, stack.parentFrame(),
						TypeUtil.toString(stack.getVar("#namespaceprefix")), type,
						new UDEDefinition(this, getEnv(stack)));
			}
			else {
				VariableStack env = stack.copy();
				env.leave();
				ArgUtil.getVariableReturn(stack).append(new UDEDefinition(this, getEnv(stack)));
			}
		}
	}

	public abstract void startInstance(VariableStack stack, UDEWrapper wrapper,
			DefinitionEnvironment env) throws ExecutionException;

	public final void startBody(VariableStack stack, Arguments fnargs) throws ExecutionException {
		if (logger.isDebugEnabled()) {
			logger.debug(this + ": starting body evaluation");
		}
		prepareInstanceArguments(stack, fnargs);
		callcount++;
		stack.setBarrier();
		stack.setVar(CALLER, this);
		if (kmode) {
			setIndex(stack, skip);
		}
		else {
			setIndex(stack, 0);
		}
		startNext(stack);
	}

	protected void prepareInstanceArguments(VariableStack stack, Arguments fnargs)
			throws ExecutionException {
		for (int i = 0; i < arguments.length; i++) {
			String name = arguments[i];
			Object value = fnargs.getNamed().getArgument(name);
			if (value != null) {
				stack.setVar(name, value);
			}
			else {
				if (fnargs.getVargs().size() > 0) {
					stack.setVar(name, fnargs.getVargs().removeFirst());
				}
				else {
					throw new ExecutionException("Missing argument " + name + " for "
							+ Info.ppDef(getElementType(), this));
				}
			}
		}

		if (optargs != null) {
			for (int i = 0; i < optargs.length; i++) {
				String name = optargs[i];
				Object value = fnargs.getNamed().getArgument(name);
				if (value != null) {
					stack.setVar(name, value);
				}
			}
		}

		if (this.vargs) {
			if (kmode) {
				stack.setVar("...", fnargs.getVargs());
			}
			else {
				stack.setVar("vargs", fnargs.getVargs());
			}
		}

		if (channels != null) {
			Iterator i = channels.iterator();
			while (i.hasNext()) {
				Arg.Channel channel = (Arg.Channel) i.next();
				stack.setVar(channel.getName(), fnargs.getChannels().get(channel));
			}
		}

		if (this.named) {
			Map named = new HashMap();
			Iterator i = fnargs.getNamed().getNames();
			while (i.hasNext()) {
				String name = (String) i.next();
				Object value = fnargs.getNamed().getArgument(name);
				if (value instanceof Future) {
					named.put(name, ((Future) value).getValue());
				}
				else {
					named.put(name, value);
				}
			}
			if (kmode) {
				stack.setVar("#", named);
			}
			else {
				stack.setVar("named", named);
			}
		}
	}

	protected void controlEvent(ControlEvent e) throws ExecutionException {
		if (START_BODY.equals(e.getType())) {
			Arguments fnargs = (Arguments) e.getStack().getVar(FNARGS);
			e.getStack().currentFrame().deleteVar(FNARGS);
			startBody(e.getStack(), fnargs);
		}
		else {
			super.controlEvent(e);
		}
	}

	protected void childCompleted(VariableStack stack) throws ExecutionException {
		if (kmode && skip == 0) {
			int index = getIndex(stack);
			if (index == 1) {
				/*
				 * See if the first arg is an identifier. In that case, evaluate
				 * the second. Otherwise it's an anonymous element
				 */
				if (!checkFirstArg(stack)) {
					super.childCompleted(stack);
				}
			}
			else if (index == 2) {
				/*
				 * At this point it's either correct arguments or exception
				 */
				checkBothArgs(stack);
				return;
			}
		}
		else {
			super.childCompleted(stack);
		}
	}

	protected boolean checkFirstArg(VariableStack stack) throws ExecutionException {
		VariableArguments vargs = Arg.VARGS.get(stack);
		if (vargs.size() == 0) {
			throw new ExecutionException("Expected identifier or argument list");
		}
		Object arg = vargs.get(0);
		if (arg instanceof List) {
			setProperty(SKIP, 1);
			setUDEArguments((List) this.checkClass(arg, List.class, "list"));

			VariableStack env = stack.copy();
			env.leave();

			Arg.VARGS.getReturn(stack).append(new UDEDefinition(this, getEnv(stack)));

			complete(stack);
			return true;
		}
		return false;
	}

	protected void checkBothArgs(VariableStack stack) throws ExecutionException {
		VariableArguments vargs = Arg.VARGS.get(stack);
		Identifier ident = null;
		if (vargs.size() == 2) {
			ident = (Identifier) this.checkClass(vargs.get(0), Identifier.class, "identifier");
			A_NAME.setStatic(this, ident.getName());
			setUDEArguments((List) this.checkClass(vargs.get(1), List.class, "list"));
			String namespaceprefix = TypeUtil.toString(stack.getVar("#namespaceprefix"));
			DefUtil.updateEnvCache(stack, stack.parentFrame());
			DefUtil.addDef(stack, stack.parentFrame(), namespaceprefix,
					Transliterator.transliterate(ident.getName()), new UDEDefinition(this,
							getEnv(stack)));
		}
		else {
			throw new ExecutionException("Expected two arguments. Got " + vargs.size() + ": "
					+ vargs);
		}
		setProperty(SKIP, 2);
		complete(stack);
	}

	protected void startArguments(VariableStack stack, UDEWrapper wrapper)
			throws ExecutionException {
		if (logger.isDebugEnabled()) {
			logger.debug(this + ": starting argument evaluation");
		}
		wrapper.executeWrapper(stack);
	}

	protected boolean isArgumentsThread(VariableStack stack) {
		return stack.currentFrame().isDefined(ARGUMENTS_THREAD);
	}

	protected void executeChildren(VariableStack stack) throws ExecutionException {
		if (kmode) {
			if (skip == 0) {
				setQuotedArgs(true);
				super.pre(stack);
				Arg.VARGS.create(stack);
				super.executeChildren(stack);
			}
			else if (skip == 1) {
				Arg.VARGS.getReturn(stack).append(new UDEDefinition(this, getEnv(stack)));
				complete(stack);
			}
			else {
				complete(stack);
			}
		}
		else {
			complete(stack);
		}
	}

	private static final String[] STRING_ARRAY = new String[0];

	protected void setUDEArguments(List args) {
		if (logger.isDebugEnabled()) {
			logger.debug(this + ": setting UDE args to " + args);
		}
		Iterator i = args.iterator();
		List normal = new ArrayList();
		channels = new LinkedList();
		List optional = new ArrayList();
		while (i.hasNext()) {
			Identifier id = (Identifier) i.next();
			if (id.getName().equals("...")) {
				A_VARGS.setStatic(this, true);
				continue;
			}
			if (id.getName().equals("#")) {
				A_NAMED.setStatic(this, true);
				continue;
			}
			if (id instanceof ChannelIdentifier) {
				ChannelIdentifier ci = (ChannelIdentifier) id;
				channels.add(new Arg.Channel(ci.getName(), ci.isCommutative()));
			}
			else if (id instanceof OptionalArgument) {
				optional.add(id.getName());
			}
			else {
				normal.add(id.getName());
			}
		}
		this.arguments = (String[]) normal.toArray(STRING_ARRAY);
		this.optargs = (String[]) optional.toArray(STRING_ARRAY);
		A_CHANNELS.setStatic(this, unfold(this.channels));
		A_ARGUMENTS.setStatic(this, unfold(this.arguments));
		A_OPTARGS.setStatic(this, unfold(this.optargs));
	}

	private String unfold(String[] array) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(array[i]);
		}
		return sb.toString();
	}

	private String unfold(Collection collection) {
		StringBuffer sb = new StringBuffer();
		Iterator i = collection.iterator();
		while (i.hasNext()) {
			sb.append(i.next());
			if (i.hasNext()) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	public String[] getArguments() {
		if (arguments == null) {
			if (A_ARGUMENTS.isPresentStatic(this)) {
				arguments = TypeUtil.toStringArray(A_ARGUMENTS.getStatic(this));
			}
			else {
				arguments = STRING_ARRAY;
			}
		}
		return arguments;
	}

	public String[] getOptargs() {
		if (optargs == null) {
			if (A_OPTARGS.isPresentStatic(this)) {
				optargs = TypeUtil.toStringArray(A_OPTARGS.getStatic(this));
			}
			else {
				optargs = STRING_ARRAY;
			}
		}
		return optargs;
	}

	public boolean hasNamed() {
		return named;
	}

	public boolean hasNestedArgs() {
		return arguments.length != 0 || optargs.length != 0;
	}

	public boolean hasVargs() {
		return vargs;
	}

	public List getChannels() {
		return channels;
	}

	public boolean hasChannels() {
		return channels != null && channels.size() > 0;
	}

	protected boolean getKmode() {
		return kmode;
	}

	public int getSkip() {
		return skip;
	}

	public void setProperty(String name, Object value) {
		super.setProperty(name, value);
		if (KMODE.equals(name)) {
			kmode = TypeUtil.toBoolean(value);
		}
		else if (SKIP.equals(name)) {
			skip = TypeUtil.toInt(value);
		}
	}

	public void setProperties(Map properties) {
		super.setProperties(properties);
		Object value;
		value = properties.get(KMODE);
		if (value != null) {
			kmode = TypeUtil.toBoolean(value);
		}
		value = properties.get(SKIP);
		if (value != null) {
			skip = TypeUtil.toInt(value);
		}
	}

	public void addStaticArgument(String name, Object value) {
		super.addStaticArgument(name, value);
		if ("vargs".equals(name)) {
			vargs = TypeUtil.toBoolean(value);
		}
		else if ("named".equals(name)) {
			named = TypeUtil.toBoolean(value);
		}
	}

	protected DefinitionEnvironment getEnv(VariableStack stack) {
		DefinitionEnvironment env;
		try {
			env = (DefinitionEnvironment) stack.getVar(DefUtil.ENV_CACHE);
		}
		catch (VariableNotFoundException e) {
			// nothing defined so far
			env = new DefinitionEnvironment();
		}
		if (env.getStack() == null) {
			try {
				env.setPrev((DefinitionEnvironment) stack.getVar(DefUtil.ENV));
			}
			catch (VariableNotFoundException e) {
				// If it's not there, it's not there
			}
			VariableStack copy = stack.copy();
			while ((copy.frameCount() > 0) && !copy.currentFrame().isDefined(DefUtil.ENV_CACHE)) {
				copy.leave();
			}
			env.setStack(copy);
		}
		return env;
	}

	public String getTextualName() {
		String name = (String) A_NAME.getStatic(this);
		if (name == null) {
			return super.getTextualName();
		}
		else {
			return name;
		}
	}
}