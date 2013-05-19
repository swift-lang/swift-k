//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 14, 2005
 */
package org.globus.cog.karajan.workflow;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.LinkedStack;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Cache;
import org.globus.cog.karajan.util.DefUtil;
import org.globus.cog.karajan.util.KarajanProperties;
import org.globus.cog.karajan.util.StateManager;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.nodes.CacheNode;
import org.globus.cog.karajan.workflow.nodes.Define;
import org.globus.cog.karajan.workflow.nodes.ElementDefNode;
import org.globus.cog.karajan.workflow.nodes.Export;
import org.globus.cog.karajan.workflow.nodes.Include;
import org.globus.cog.karajan.workflow.nodes.Namespace;
import org.globus.cog.karajan.workflow.nodes.ProjectNode;
import org.globus.cog.karajan.workflow.nodes.functions.Argument;
import org.globus.cog.karajan.workflow.nodes.functions.NumericValue;
import org.globus.cog.karajan.workflow.nodes.functions.QuotedList;
import org.globus.cog.karajan.workflow.nodes.functions.StringValue;
import org.globus.cog.karajan.workflow.nodes.functions.Variable;

/**
 * An execution context is a structure that is considered static for the duration of
 * one execution of an entire tree. It should not be re-used for multiple runs. Instead
 * different instances
 */
public class ExecutionContext implements EventListener {
	private static final Logger logger = Logger.getLogger(ExecutionContext.class);

	public static final Arg.Channel STDOUT = new Arg.Channel("stdout");
	public static final Arg.Channel STDERR = new Arg.Channel("stderr");

	public static final String CMDLINE_ARGS = "cmdline:arguments";

	private boolean monitoringEnabled;
	private final transient StateManager stateManager;
	private final transient ElementTree tree;
	private transient boolean done, failed;
	private boolean dumpState;
	private transient final List<EventListener> eventListeners;
	private transient final KarajanProperties properties;
	private List<String> arguments;
	private transient Throwable failure;
	private transient VariableArguments stdout, stderr;
	private int id;
	private long startTime, endTime;
	private String cwd;
	private Map<String, Object> attributes;
	private Cache cache;

	public Cache getCache() {
		return cache;
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}

	public ExecutionContext(ElementTree tree) {
		this(tree, KarajanProperties.getDefault());
	}

	public ExecutionContext(ElementTree tree, KarajanProperties properties) {
		if (tree == null) {
			throw new IllegalArgumentException("The tree cannot be null");
		}
		stateManager = new StateManager(this);
		this.tree = tree;
		eventListeners = new LinkedList<EventListener>();
		this.properties = properties;
		cwd = new File(".").getAbsolutePath();
		cache = new Cache();
	}

	public boolean isMonitoringEnabled() {
		return monitoringEnabled;
	}

	public void setMonitoringEnabled(boolean monitoringEnabled) {
		this.monitoringEnabled = monitoringEnabled;
	}

	public StateManager getStateManager() {
		return stateManager;
	}

	public ElementTree getTree() {
		return tree;
	}

	protected void define(VariableStack stack, String name, Class<?> cls) {
		DefUtil.addDef(stack, stack.firstFrame(), "kernel", name, new JavaElement(cls.getName()));
	}

	public void start() {
		start(new LinkedStack(this));
	}

	public void start(VariableStack stack) {
		EventBus.initialize();
		startTime = System.currentTimeMillis();
		if (arguments == null) {
			arguments = Collections.emptyList();
		}
		setGlobals(stack);
		defineKernel(stack);
		stack.enter();
		if (stdout == null) {
			stdout = new PrintStreamChannel(System.out, false);
		}
		if (stderr == null) {
			stderr = new PrintStreamChannel(System.err, true);
		}
		STDOUT.create(stack, stdout);
		STDERR.create(stack, stderr);
		stack.setVar("#namespaceprefix", "");
		stack.setCaller(this);
		ThreadingContext.set(stack, new ThreadingContext());
		stack.setVar("...", arguments);
		EventBus.post(tree.getRoot(), stack);
	}

	protected void setGlobals(VariableStack stack) {
		stack.setVar("user.home", System.getProperty("user.home"));
		stack.setVar("user.name", System.getProperty("user.name"));
		stack.setVar(CMDLINE_ARGS, arguments);
		setEnvironmentVariables(stack);
	}

	private void setEnvironmentVariables(VariableStack stack) {
		Map<String, String> env = System.getenv();
		for(Map.Entry<String, String> e : env.entrySet()) {
			stack.setVar("env." + e.getKey().toLowerCase(), e.getValue());
		}
	}

	protected void defineKernel(VariableStack stack) {
		define(stack, "include", Include.class);
		define(stack, "import", Include.class);
		define(stack, "export", Export.class);
		define(stack, "define", Define.class);
		// system includes use it
		define(stack, "cache", CacheNode.class);
		define(stack, "namespace", Namespace.class);
		define(stack, "elementdef", ElementDefNode.class);
		define(stack, "named", Argument.class);
		define(stack, "number", NumericValue.class);
		define(stack, "string", StringValue.class);
		define(stack, "variable", Variable.class);
		define(stack, "quotedlist", QuotedList.class);
		define(stack, "project", ProjectNode.class);
		define(stack, "karajan", ProjectNode.class);

		stack.firstFrame().setVar("false", Boolean.FALSE);
		stack.firstFrame().setVar("true", Boolean.TRUE);
	}

	public void failed(VariableStack stack, ExecutionException e)
	throws ExecutionException {
		// User-readable output
		printFailure(e);
		if (logger.isInfoEnabled()) {
			logger.info("Detailed exception: ", e);
		}
		// Actually propagate the failure
		failedQuietly(stack, e);
	}

	protected void printFailure(ExecutionException e) {
		stderr.append("\nExecution failed:\n");
		stderr.append(e.toString());
		stderr.append("\n");
	}

	/**
	   Like failed() but allow caller to control output
	 */
	public void failedQuietly(VariableStack stack,
			                  ExecutionException e) {
		stateManager.stop();
		synchronized(this) {
			if (failed) {
				return;
			}
			else {
				failed = true;
			}
			setDone();
		}
		notifyFailed(stack, e);
	}

	public void completed(VariableStack stack) throws ExecutionException {
		stateManager.stop();
		setDone();
		notifyCompleted(stack);
	}

	protected synchronized void setDone() {
		this.done = true;
		this.endTime = System.currentTimeMillis();
		notifyAll();
	}

	public boolean done() {
		return this.done;
	}

	public synchronized void waitFor() throws InterruptedException {
		while (!done) {
			wait();
		}
	}

	private Throwable getInitialCause(Throwable t) {
		if (t == null) {
			return null;
		}
		if (t.getCause() != null) {
			return getInitialCause(t.getCause());
		}
		else {
			return t;
		}
	}

	public boolean getDumpState() {
		return dumpState;
	}

	public void setDumpState(boolean dumpState) {
		this.dumpState = dumpState;
	}

	public String getBasedir() {
		return tree.getBasedir();
	}

	public KarajanProperties getProperties() {
		return properties;
	}

	public void notifyCompleted(VariableStack stack) {
		if (eventListeners != null) {
			for (EventListener l : eventListeners) {
				try {
					l.completed(stack);
				}
				catch (Exception e) {
					logger.warn("Notification failed", e);
				}
			}
		}
	}

	public void notifyFailed(VariableStack stack, ExecutionException e) {
		if (eventListeners != null) {
			for (EventListener l : eventListeners) {
				try {
					l.failed(stack, e);
				}
				catch (Exception ee) {
					logger.warn("Notification failed", ee);
				}
			}
		}
	}

	public void addEventListener(EventListener listener) {
		if (!eventListeners.contains(listener)) {
			eventListeners.add(listener);
		}
	}

	public void removeEventListener(EventListener listener) {
		eventListeners.remove(listener);
	}

	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

	public void addArgument(String arg) {
		if (this.arguments == null) {
			this.arguments = new ArrayList<String>();
		}
		this.arguments.add(arg);
	}

	@Override
	public String toString() {
		return tree.getName();
	}

	public static String getMeaningfulMessage(Throwable ex) {
		StringBuffer messages = new StringBuffer();
		Throwable t = ex;
		String prev = null;
		while (t != null) {
			String msg = t.getMessage();
			if (msg == null) {
				t = t.getCause();
				continue;
			}
			boolean changed = false;
			if (prev == null || prev.indexOf(msg) == -1) {
				prev = msg;
				messages.append(msg);
				changed = true;
			}
			t = t.getCause();
			if (t != null && changed) {
				if (msg.endsWith(":")) {
					messages.append(' ');
				}
				else if (!msg.endsWith(": ")) {
					messages.append(": ");
				}
			}
		}
		return messages.toString();
	}

	public boolean isFailed() {
		return failed;
	}

	public Throwable getFailure() {
		return failure;
	}

	public VariableArguments getStderr() {
		return stderr;
	}

	public void setStderr(VariableArguments stderr) {
		this.stderr = stderr;
	}

	public VariableArguments getStdout() {
		return stdout;
	}

	public void setStdout(VariableArguments stdout) {
		this.stdout = stdout;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getEndTime() {
		return endTime;
	}

	public long getStartTime() {
		return startTime;
	}

	protected void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	protected void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public String getCwd() {
		return cwd;
	}

	public void setCwd(String cwd) {
	    if (cwd == null) {
	        throw new IllegalArgumentException("CWD cannot be null");
	    }
		this.cwd = cwd;
	}

	public synchronized void setAttribute(String name, Object value) {
	    if (attributes == null) {
	        attributes = new HashMap<String, Object>();
	    }
	    attributes.put(name, value);
	}

	public synchronized Object getAttribute(String name) {
		if (attributes == null) {
		    return null;
		}
		else {
		    return attributes.get(name);
		}
	}
}
