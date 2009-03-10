// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jun 6, 2003
 *  
 */
package org.globus.cog.karajan.workflow.nodes.user;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.stack.Trace;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.DefUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.ControlEventType;
import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.nodes.AbstractSequentialWithArguments;

public class UDEWrapper extends AbstractSequentialWithArguments implements EventListener {
	public static final ControlEventType EXECUTE_DEF = new ControlEventType("EXECUTE_DEF", 3);

	private static final Logger logger = Logger.getLogger(UDEWrapper.class);
	private static int count = 0;

	private UDEDefinition cachedDef;

	private Hashtable arguments;

	private boolean initialized, named;

	private Set argumentNames;

	private String[] sortedArgs;

	public UDEWrapper() {
		this(null);
	}

	public UDEWrapper(UDEDefinition def) {
		setOptimize(false);
		setAcceptsInlineText(true);
		this.cachedDef = def;
		if (def != null) {
			initialize(def);
		}
	}
	
	public String toString() {
		return super.toString();
	}

	private synchronized void initialize(UDEDefinition def) {
		if (initialized) {
			return;
		}

		initializeDefArgs(def.getUde());
		super.initializeStatic();

		initialized = true;
	}

	public void pre(VariableStack stack) throws ExecutionException {
		UDEDefinition uded = getDef(stack);
		stack.setVar(Trace.ELEMENT, this);
		uded.getUde().startInstance(stack, this, uded.getEnv());
	}

	protected UDEDefinition getDefInternal(VariableStack stack) throws ExecutionException {
		Object def = DefUtil.getDef(stack, getElementType(), getParent()).getDef();
		if (def == null) {
			throw new ExecutionException("Definition for " + getElementType() + " not found");
		}

		if (def instanceof UDEDefinition) {
			return (UDEDefinition) def;
		}
		else {
			throw new ExecutionException("Unrecognized definition for " + getElementType() + ": "
					+ def);
		}
	}

	public synchronized UDEDefinition getDef(VariableStack stack) throws ExecutionException {
		// for now, we optimize
		if (cachedDef != null) {
			return cachedDef;
		}

		UDEDefinition def = getDefInternal(stack);

		if (def != cachedDef) {
			cachedDef = def;
			initialize(def);
		}
		return def;
	}

	public void executeChildren(VariableStack stack) throws ExecutionException {

	}

	public void executeWrapper(VariableStack stack) throws ExecutionException {
		stack.enter();
		stack.setCaller(this);
		super.executeChildren(stack);
	}
	

	/*
	 * Override to give the def access
	 */
	protected void setNestedArgs(boolean nestedArgs) {
		super.setNestedArgs(nestedArgs);
	}

	/*
	 * Override to give the def access
	 */
	protected void setHasVargs(boolean hasVargs) {
		super.setHasVargs(hasVargs);
	}

	/*
	 * Override to give the def access Is this proper design? It doesn't really
	 * look like it. Or maybe it is. Anyway, it gets the job done.
	 */
	protected void initializeArgs(VariableStack stack) throws ExecutionException {
		super.initializeArgs(stack);
	}

	/*
	 * Override to give the def access
	 */
	protected List getNonpropargs() {
		return super.getNonpropargs();
	}

	protected boolean checkArgument(String name, UserDefinedElement def) {
		// return def.getValidArguments().contains(name);
		return true;
	}

	protected void checkArguments() {
		// let the definition do the checks
	}

	public Object getArgument(String name) {
		if (arguments == null) {
			return null;
		}
		else {
			return arguments.get(name);
		}
	}

	public boolean hasArgument(String name) {
		if (arguments == null) {
			return false;
		}
		else {
			return arguments.containsKey(name);
		}
	}

	public void setArgument(String name, Object value) {
		if (arguments == null) {
			arguments = new Hashtable();
		}
		arguments.put(name, value);
	}

	protected Set getArgumentNames() {
		return argumentNames;
	}

	protected String[] getSortedArgs() {
		return sortedArgs;
	}

	private void initializeDefArgs(UserDefinedElement def) {
		sortedArgs = def.getArguments();
		argumentNames = new HashSet();
		argumentNames.addAll(Arrays.asList(def.getArguments()));
		argumentNames.addAll(Arrays.asList(def.getOptargs()));
	}
}