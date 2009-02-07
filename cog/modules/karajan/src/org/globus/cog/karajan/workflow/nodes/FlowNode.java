// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.nodes;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.LoadListener;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ErrorHandler;
import org.globus.cog.karajan.workflow.ExecutionContext;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.events.AbortEvent;
import org.globus.cog.karajan.workflow.events.AbortNotificationEvent;
import org.globus.cog.karajan.workflow.events.ControlEvent;
import org.globus.cog.karajan.workflow.events.ControlEventType;
import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.events.EventClass;
import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.events.FailureNotificationEvent;
import org.globus.cog.karajan.workflow.events.FlowEvent;
import org.globus.cog.karajan.workflow.events.MonitoringEvent;
import org.globus.cog.karajan.workflow.events.MonitoringEventType;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEventType;
import org.globus.cog.karajan.workflow.events.StatusMonitoringEvent;
import org.globus.cog.karajan.workflow.futures.FutureEvaluationException;
import org.globus.cog.karajan.workflow.futures.FutureFault;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;

public class FlowNode implements ExtendedFlowElement, LoadListener {
	public static final Logger logger = Logger.getLogger(FlowNode.class);

	public static final Arg A_INLINE_TEXT = new Arg.Optional(TEXT);

	public static final Arg.Channel STDOUT = ExecutionContext.STDOUT;
	public static final Arg.Channel STDERR = ExecutionContext.STDERR;

	private String locator;

	private Integer uid;

	private boolean checkpointable;

	private static int uidCounter = 0;

	private List elements;

	private String elementType;

	private Map properties, staticArguments;

	private FlowElement parent;

	private boolean inlineText;

	public static final Map threadTracker = new HashMap();

	public static long startCount = 0;

	public static boolean debug = false;

	private boolean frame, initialized;

	public FlowNode() {
		checkpointable = true;
		frame = true;
		elements = Collections.EMPTY_LIST;
		properties = Collections.EMPTY_MAP;
		staticArguments = Collections.EMPTY_MAP;
	}

	protected boolean executeErrorHandler(VariableStack stack, NotificationEvent error)
			throws ExecutionException {
		// TODO
		/*
		 * if (error instanceof ChainedFailureNotificationEvent) { return false; }
		 */
		FailureNotificationEvent evt = (FailureNotificationEvent) error;
		List set = stack.getAllVars("#errorhandlers");
		Iterator i = set.iterator();
		while (i.hasNext()) {
			List handlers = (List) i.next();
			Iterator j = handlers.iterator();
			while (j.hasNext()) {
				ErrorHandler handler = (ErrorHandler) j.next();
				if (handler.matches(evt.getMessage())) {
					handler.handleError(this, evt);
					return true;
				}
			}
		}
		return false;
	}

	public void failImmediately(VariableStack stack, FailureNotificationEvent fne) {
		try {
			if (FlowNode.debug) {
				threadTracker.remove(new FNTP(this, ThreadingContext.get(stack)));
			}
			boolean errorHandler = false;
			try {
				_finally(stack);
			}
			catch (Exception e) {
				fne = new FailureNotificationEvent(this, stack, e.getMessage(), e);
			}
			if (frame) {
				stack.leave();
			}
			try {
				errorHandler = executeErrorHandler(stack, fne);
			}
			catch (Exception e) {
				fne = new FailureNotificationEvent(this, stack, e.getMessage(), e);
			}
			if (!errorHandler) {
				fireNotificationEvent(fne, stack);
				fireStatusMonitoringEvent(StatusMonitoringEvent.EXECUTION_FAILED, stack,
						fne.getException());
			}
		}
		catch (ExecutionException e) {
			logger.error("Could not fail element", e);
		}
	}

	public void failImmediately(VariableStack stack, String message) {
		failImmediately(stack, new FailureNotificationEvent(this, stack, message, null));
	}

	public void failImmediately(VariableStack stack, Throwable exception) {
		failImmediately(stack, new FailureNotificationEvent(this, stack, exception.getMessage(),
				exception));
	}

	public void failImmediately(VariableStack stack, String message, Exception exception) {
		failImmediately(stack, new FailureNotificationEvent(this, stack, message, exception));
	}

	/**
	 * Notification events notify callers of the status of the execution
	 * (completed, failed, aborted, ...)
	 */
	public void fireNotificationEvent(final FlowEvent event, final VariableStack stack) {
		try {
			EventListener caller = (EventListener) stack.getVar(CALLER);
			if (caller == null) {
				logger.error("Caller is null");
				stack.dumpAll();
			}
			else {
				// optimize a little. it improves speed by about 50%
				EventBus.sendHooked(caller, event);
				// EventBus.post(caller, event);
			}
		}
		catch (VariableNotFoundException ee) {
			logger.debug("No #caller for: " + this, new Throwable());
			if (FlowNode.debug) {
				stack.dumpAll();
			}
			EventListener parent = getParent();
			EventBus.post(parent, new FailureNotificationEvent(this, stack,
					"No #caller found on stack for " + this, ee));
		}
	}

	public final void fireControlEvent(final FlowElement target, final FlowEvent event) {
		EventBus.post(target, event);
	}

	public final void fireMonitoringEvent(final MonitoringEvent event) {
		EventBus.post(event.getStack().getExecutionContext(), event);
	}

	public final void fireStatusMonitoringEvent(final MonitoringEventType type,
			final VariableStack stack, final Object detail) {
		if (stack.getExecutionContext().isMonitoringEnabled()) {
			fireMonitoringEvent(new StatusMonitoringEvent(this, type, stack, detail));
		}
	}

	protected final void checkFailed(VariableStack stack) {
		if (stack.currentFrame().isDefined("#failed")) {
			logger.debug("complete() or fail() called on a failed element", new Throwable());
		}
		stack.setVar("#failed", Boolean.TRUE);
	}

	protected final void checkCompleted(VariableStack stack) {
		if (stack.currentFrame().isDefined("#completed")) {
			logger.debug("complete() or fail() called on a completed element", new Throwable());
		}
		stack.setVar("#completed", Boolean.TRUE);
	}

	private final void checkStackReuse(VariableStack stack) throws VariableNotFoundException {
		FNTP fntp = new FNTP(this, ThreadingContext.get(stack));
		if (threadTracker.containsKey(fntp)) {
			logger.debug("Execution of element with the same context detected: " + fntp);
			logger.debug("Probable faulty element is " + stack.getVar(CALLER));
			if (stack == threadTracker.get(fntp)) {
				logger.debug("Even worse. The same stack object was used");
			}
		}
		else {
			threadTracker.put(new FNTP(this, ThreadingContext.get(stack)), stack);
		}
	}

	public final void restart(final VariableStack stack) throws ExecutionException {
		startCount++;
		if (logger.isInfoEnabled()) {
			logger.info("Executing " + this + "; thread: " + ThreadingContext.get(stack));
			if (FlowNode.debug) {
				checkStackReuse(stack);
			}
		}
		try {
			execute(stack);
		}
		catch (FutureFault e) {
			if (!logger.isDebugEnabled()) {
				logger.debug(this + " got future exception. Future is " + e.getFuture());
			}
			if (FlowNode.debug) {
				threadTracker.remove(new FNTP(this, ThreadingContext.get(stack)));
			}
			e.getFuture().addModificationAction(this,
					new ControlEvent(this, ControlEventType.RESTART, stack));
		}
		catch (ExecutionException e) {
			failImmediately(stack, e);
		}
		catch (KarajanRuntimeException e) {
			failImmediately(stack, e);
		}
	}
	
	protected void abort(final VariableStack stack) throws ExecutionException {
	    abort(stack, null);
	}

	protected void abort(final VariableStack stack, String message) throws ExecutionException {
		_finally(stack);
		if (frame) {
			stack.leave();
		}
		fireStatusMonitoringEvent(StatusMonitoringEvent.EXECUTION_FAILED, 
		        stack, message);
		if (FlowNode.debug) {
			threadTracker.remove(new FNTP(this, ThreadingContext.get(stack)));
		}
		fireNotificationEvent(new AbortNotificationEvent(this, stack, message), stack);
	}

	public final void start(final VariableStack stack) throws ExecutionException {
		if (!initialized) {
			initializeStatic();
			initialized = true;
		}
		if (frame) {
			stack.enter();
		}
		fireStatusMonitoringEvent(StatusMonitoringEvent.EXECUTION_STARTED, stack, null);
		restart(stack);
	}

	public void execute(VariableStack stack) throws ExecutionException {
		complete(stack);
	}

	public final void complete(final VariableStack stack) throws ExecutionException {
		if (FlowNode.debug) {
			checkFailed(stack);
			checkCompleted(stack);
			threadTracker.remove(new FNTP(this, ThreadingContext.get(stack)));
		}
		_finally(stack);
		if (frame) {
			stack.leave();
		}
		fireStatusMonitoringEvent(StatusMonitoringEvent.EXECUTION_COMPLETED, stack, null);
		fireNotificationEvent(new NotificationEvent(this,
				NotificationEventType.EXECUTION_COMPLETED, stack), stack);
	}

	protected void _finally(VariableStack stack) throws ExecutionException {
	}

	public final FlowElement copy() {
		try {
			FlowNode nfe = (FlowNode) this.getClass().newInstance();
			Iterator i = this.properties.keySet().iterator();
			while (i.hasNext()) {
				String key = (String) i.next();
				nfe.setProperty(key, properties.get(key));
			}
			for (int j = 0; j < elementCount(); j++) {
				nfe.addElement(getElement(j));
			}
			return nfe;
		}
		catch (InstantiationException e) {
			logger.error("Copying of element failed", e);
		}
		catch (IllegalAccessException e) {
			logger.error("Copying of element failed", e);
		}
		return null;
	}

	public void event(final Event e) throws ExecutionException {
		try {
			final EventClass cls = e.getEventClass();
			if (EventClass.CONTROL_EVENT.equals(cls)) {
				controlEvent((ControlEvent) e);
			}
			else if (EventClass.NOTIFICATION_EVENT.equals(cls)) {
				notificationEvent((NotificationEvent) e);
			}
			else if (EventClass.MONITORING_EVENT.equals(cls)) {
				monitoringEvent((MonitoringEvent) e);
			}
			else {
				if (e.hasStack()) {
					failImmediately(e.getStack(), "Unhandled event: " + e);
				}
				else {
					logger.warn("Unhandled event " + e);
				}
			}
		}
		catch (FutureNotYetAvailable ex) {
			ex.getFuture().addModificationAction(this, e);
		}
		catch (FutureEvaluationException ex) {
			failImmediately(e.getStack(), ex.getFault());
		}
		catch (ExecutionException ex) {
			if (ex.getStack() == null) {
				ex.setStack(e.getStack().copy());
			}
			failImmediately(e.getStack(), ex);
		}
		catch (RuntimeException ex) {
			failImmediately(e.getStack(), new ExecutionException(e.getStack().copy(),
					ex.getMessage(), ex));
		}
	}

	protected void notificationEvent(final NotificationEvent e) throws ExecutionException {
		try {
			if (FlowNode.debug) {
				if (!this.equals(e.getStack().getVar(CALLER))) {
					logger.warn("stack inconsistency detected");
				}
			}
			if (NotificationEventType.EXECUTION_FAILED.equals(e.getType())) {
				failImmediately(e.getStack(), (FailureNotificationEvent) e);
			}
			else {
				e.getStack().leave();
				fireNotificationEvent(e, e.getStack());
			}
		}
		catch (FutureFault ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("FE: ", ex);
			}
			ex.getFuture().addModificationAction(this, e);
		}
	}

	protected void controlEvent(final ControlEvent e) throws ExecutionException {
		final ControlEventType type = e.getType();
		if (ControlEventType.START.equals(type)) {
			start(e.getStack());
		}
		else if (ControlEventType.RESTART.equals(type)) {
			restart(e.getStack());
		}
		else if (AbortEvent.ABORT.equals(type)) {
			abortEvent((AbortEvent) e);
		}
	}

	protected void abortEvent(AbortEvent e) throws ExecutionException {
		abort(e.getStack());
	}

	protected void monitoringEvent(MonitoringEvent e) throws ExecutionException {

	}

	public void fail(VariableStack stack, String message, Throwable cause)
			throws ExecutionException {
		throw new ExecutionException(stack.copy(), message, cause);
	}

	public void fail(VariableStack stack, String message) throws ExecutionException {
		fail(stack, message, null);
	}

	public void failIfNotDefined(VariableStack stack, String var, String message)
			throws ExecutionException {
		if (!stack.isDefined(var)) {
			throw new ExecutionException(stack.copy(), message);
		}
	}

	public void failIfNull(VariableStack stack, String name) throws ExecutionException {
		if (failIfNull(name)) {
			throw new ExecutionException(stack.copy(), "Required property not present: " + name);
		}
	}

	public void failIfNull(VariableStack stack, String name, String message)
			throws ExecutionException {
		if (failIfNull(name)) {
			fail(stack, message);
		}
	}

	private boolean failIfNull(String name) {
		if (!hasProperty(name)) {
			return true;
		}
		if (getProperty(name) == null) {
			return true;
		}
		return false;
	}

	public void startElement(final int index, final VariableStack stack) throws ExecutionException {
		startElement(getElement(index), stack);
	}

	protected void startElement(final FlowElement c, final VariableStack stack)
			throws ExecutionException {
		final ControlEvent fee = new ControlEvent(this, ControlEventType.START, stack);
		fee.setStack(stack);
		fireControlEvent(c, fee);
	}

	public void restartElement(final FlowElement c, final VariableStack stack) {
		final ControlEvent fee = new ControlEvent(this, ControlEventType.RESTART, stack);
		fee.setStack(stack);
		fireControlEvent(c, fee);
	}

	protected void ret(final VariableStack stack, final Object value) throws ExecutionException {
		ArgUtil.getVariableReturn(stack).append(value);
	}

	public String get_locator() {
		return locator;
	}

	public void set_locator(String locator) {
		this.locator = locator;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getTextualName());
		if (hasProperty(ANNOTATION)) {
			sb.append(" (");
			sb.append(getStringProperty(ANNOTATION));
			sb.append(") ");
		}
		Object fileName = getTreeProperty(FILENAME, this);
		if (fileName instanceof String) {
			String fn = (String) fileName;
			fn = fn.substring(1 + fn.lastIndexOf('/'));
			sb.append(" @ ");
			sb.append(fn);

			if (hasProperty(LINE)) {
				sb.append(", line: ");
				sb.append(getProperty(LINE));
			}
		}
		return sb.toString();
	}

	protected String getTextualName() {
		String tmp = getElementType();
		if (tmp == null) {
			tmp = this.getClass().getName();
		}
		return tmp;
	}

	public List elements() {
		return elements;
	}

	public void setCheckpointable(boolean checkpointable) {
		this.checkpointable = checkpointable;
	}

	public boolean isCheckpointable() {
		return checkpointable;
	}

	public synchronized void addElement(FlowElement element) {
		if (elements.isEmpty()) {
			elements = new ArrayList();
		}
		elements.add(element);
	}

	public void setElements(List elements) {
		this.elements = elements;
	}

	public synchronized void replaceElement(int index, FlowElement element) {
		elements.set(index, element);
	}

	public synchronized void removeElement(int index) {
		elements.remove(index);
	}
	
	public synchronized void removeElement(FlowElement element) {
		elements.remove(element);
	}

	public FlowElement getElement(int index) {
		return (FlowElement) elements.get(index);
	}

	public int elementCount() {
		return elements.size();
	}

	public void setProperty(final String name, final Object value) {
		if (name.equals(UID)) {
			uid = (Integer) value;
			return;
		}
		if (properties.isEmpty()) {
			properties = new HashMap();
		}
		properties.put(name.toLowerCase(), value);
	}

	public void setProperties(Map properties) {
		uid = (Integer) properties.remove(UID);
		if (properties.size() == 0) {
			return;
		}
		else {
			this.properties = properties;
		}
	}

	public void removeProperty(String name) {
		properties.remove(name);
	}

	public Object getProperty(String name) {
		name = name.toLowerCase();
		if (name.equals(UID)) {
			return uid;
		}
		else {
			return properties.get(name.toLowerCase());
		}
	}

	public boolean hasProperty(final String name) {
		if (properties == null) {
			return false;
		}
		else {
			return properties.containsKey(name.toLowerCase());
		}
	}

	public void setProperty(final String name, final int value) {
		setProperty(name, new Integer(value));
	}

	public int getIntProperty(final String name) {
		Object prop = getProperty(name);
		if (prop instanceof String) {
			return Integer.valueOf((String) prop).intValue();
		}
		else {
			return ((Integer) prop).intValue();
		}
	}

	public int getIntProperty(String name, int defaultValue) {
		if (!hasProperty(name)) {
			return defaultValue;
		}
		else {
			return getIntProperty(name);
		}
	}

	public String getStringProperty(final String name) {
		return TypeUtil.toString(getProperty(name));
	}

	public void setProperty(String name, boolean value) {
		setProperty(name, Boolean.valueOf(value));
	}

	public boolean getBooleanProperty(String name) {
		Object prop = getProperty(name);
		return TypeUtil.toBoolean(prop);
	}

	public boolean getBooleanProperty(String name, boolean defaultValue) {
		if (!hasProperty(name)) {
			return defaultValue;
		}
		else {
			return getBooleanProperty(name);
		}
	}

	public void addStaticArgument(String name, Object value) {
		if (staticArguments.isEmpty()) {
			staticArguments = new HashMap();
		}
		staticArguments.put(name, value);
	}

	public void setStaticArguments(Map args) {
		this.staticArguments = args;
	}

	public Map getStaticArguments() {
		return staticArguments;
	}

	public String getElementType() {
		return elementType;
	}

	public void setElementType(String string) {
		elementType = string;
	}

	public Object getCanonicalType() {
		return getClass();
	}

	public FlowElement getParent() {
		return parent;
	}

	public void setParent(FlowElement element) {
		parent = element;
	}

	public Collection propertyNames() {
		return properties.keySet();
	}

	protected void echo(String message) {
		echo(message, true);
	}

	protected void echo(String message, boolean nl) {
		PrintStream ps = System.out;
		if (nl) {
			ps.println(message);
			ps.flush();
		}
		else {
			ps.print(message);
		}
		ps.flush();
	}

	protected void setAcceptsInlineText(boolean inlineText) {
		this.inlineText = inlineText;
	}

	public boolean acceptsInlineText() {
		return inlineText;
	}

	public final Object checkClass(final Object value, final Class cls, final String name)
			throws ExecutionException {
		if (value != null) {
			if (!cls.isAssignableFrom(value.getClass())) {
				String vc;
				if (value instanceof FlowElement) {
					vc = ((FlowElement) value).getElementType();
				}
				else {
					vc = value.getClass().getName();
				}
				throw new ExecutionException("Incompatible argument: expected " + name + "; got "
						+ vc + ". Offending argument: " + value);
			}
		}
		return value;
	}

	public static Object getTreeProperty(final String name, final FlowElement element) {
		if (element == null) {
			return null;
		}
		if (element.hasProperty(name)) {
			return element.getProperty(name);
		}
		else {
			if (element.getParent() != null) {
				return getTreeProperty(name, element.getParent());
			}
			else {
				return null;
			}
		}
	}

	protected boolean hasFrame() {
		return frame;
	}

	protected void setFrame(boolean frame) {
		this.frame = frame;
	}

	public void loadStarted() {

	}

	public Integer getUID() {
		return uid;
	}

	public void loadComplete() {
		initializeStatic();
		initialized = true;
	}

	protected void initializeStatic() {
	}

	protected boolean isSystemProperty(String name) {
		if (name.length() == 0) {
			throw new KarajanRuntimeException("Internal error: Empty name");
		}
		return name.charAt(0) == '_';
	}

	public static class FNTP {
		public EventListener node;
		public ThreadingContext tc;

		public FNTP(EventListener node, ThreadingContext tc) {
			this.node = node;
			this.tc = tc;
		}

		public int hashCode() {
			return node.hashCode() + tc.hashCode();
		}

		public boolean equals(Object other) {
			if (other instanceof FNTP) {
				FNTP fntp = (FNTP) other;
				return node.equals(fntp.node) && tc.equals(fntp.tc);
			}
			return false;
		}

		public String toString() {
			return node + " - " + tc;
		}
	}

	public boolean isSimple() {
		return false;
	}

	public void executeSimple(VariableStack stack) throws ExecutionException {
		throw new KarajanRuntimeException("Internal error: default executeSimple() called");
	}
}