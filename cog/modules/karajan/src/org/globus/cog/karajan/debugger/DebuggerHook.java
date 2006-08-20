// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 18, 2005
 */
package org.globus.cog.karajan.debugger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadedElement;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.ControlEvent;
import org.globus.cog.karajan.workflow.events.ControlEventType;
import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventBus;
import org.globus.cog.karajan.workflow.events.EventClass;
import org.globus.cog.karajan.workflow.events.EventHook;
import org.globus.cog.karajan.workflow.events.EventListener;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEventType;
import org.globus.cog.karajan.workflow.nodes.FlowContainer;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.FlowNode;
import org.globus.cog.karajan.workflow.nodes.ProjectNode;

public class DebuggerHook implements EventHook {
	private Set fileBreakpoints;
	private Set userBreakpoints;

	private Set breakpointListeners;

	private Map events;

	private boolean step;

	private Set stepOvers;

	private ElementTree tree;

	public DebuggerHook(ElementTree tree) {
		super();
		this.tree = tree;
		events = new Hashtable();
		breakpointListeners = new HashSet();
		fileBreakpoints = new HashSet();
		userBreakpoints = new HashSet();
		stepOvers = new HashSet();
		step = true;
	}

	public void event(EventListener element, Event e) throws ExecutionException {
		if (e.getEventClass().equals(EventClass.CONTROL_EVENT)) {
			ControlEvent ce = (ControlEvent) e;
			FlowElement el = (FlowElement) element;
			if (ce.getType().equals(ControlEventType.START)) {
				ThreadingContext tc = ThreadingContext.get(e.getStack());
				if (userBreakpoints.contains(new SyntheticFlowElement((FlowElement) el))) {
					breakpointReached(element, e);
					return;
				}
				else if (isStepOver(tc)) {
					EventBus.send(element, e);
					return;
				}
				else {
					if (step) {
						stepReached(element, e);
						return;
					}
				}
			}
		}
		else {
			if (e.getEventClass().equals(EventClass.NOTIFICATION_EVENT)) {
				NotificationEvent ne = (NotificationEvent) e;
				if (ne.getType().equals(NotificationEventType.EXECUTION_COMPLETED)) {
					Iterator i = stepOvers.iterator();
					ThreadingContext tc = ThreadingContext.get(e.getStack());
					while (i.hasNext()) {
						ThreadedElement te = (ThreadedElement) i.next();
						if (te.getElement() == ne.getFlowElement()) {
							i.remove();
						}
					}
				}
			}
		}
		EventBus.send(element, e);
	}

	private boolean isStepOver(ThreadingContext tc) {
		Iterator i = stepOvers.iterator();
		while (i.hasNext()) {
			ThreadedElement te = (ThreadedElement) i.next();
			if (tc.isSubContext(te.getThread())) {
				System.out.println(te + " - " + tc);
				return true;
			}
		}
		return false;
	}

	private void breakpointReached(EventListener element, Event e) {
		try {
			step = true;
			ThreadingContext tc = ThreadingContext.get(e.getStack());
			ThreadedElement te = new ThreadedElement((FlowElement) element, tc);
			events.put(te, e);

			Iterator i = new ArrayList(breakpointListeners).iterator();
			while (i.hasNext()) {
				try {
					((BreakpointListener) i.next()).breakpointReached(te);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		catch (VariableNotFoundException e1) {
			e1.printStackTrace();
		}
	}

	private void stepReached(EventListener element, Event e) {
		try {
			if ((element instanceof FlowElement) && ((FlowElement) element).hasProperty(FlowElement.LINE)) {
				ThreadingContext tc = ThreadingContext.get(e.getStack());
				ThreadedElement te = new ThreadedElement((FlowElement) element, tc);
				events.put(te, e);
				Iterator i = new ArrayList(breakpointListeners).iterator();
				while (i.hasNext()) {
					try {
						((BreakpointListener) i.next()).stepReached(te);
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			else {
				EventBus.send(element, e);
			}
		}
		catch (VariableNotFoundException e1) {
			e1.printStackTrace();
		}
	}

	public void addBreakpointListener(BreakpointListener l) {
		breakpointListeners.add(l);
	}

	public void removeBreakpointListener(BreakpointListener l) {
		breakpointListeners.remove(l);
	}

	public VariableStack getStack(ThreadedElement te) {
		Event e = (Event) events.get(te);
		if (e == null) {
			return null;
		}
		return e.getStack();
	}

	public void stepInto(ThreadedElement te) {
		Event e = (Event) events.remove(te);
		Iterator i = new ArrayList(breakpointListeners).iterator();
		while (i.hasNext()) {
			((BreakpointListener) i.next()).resumed(te);
		}
		if (e != null) {
			if (te.getElement() instanceof FlowContainer) {
				((FlowContainer) te.getElement()).setOptimize(false);
			}
			EventBus.send(te.getElement(), e);
		}
	}

	public void stepOver(ThreadedElement te) {
		Event e = (Event) events.remove(te);
		if (e == null) {
			throw new RuntimeException("No event found for " + te);
		}
		stepOvers.add(te);
		Iterator i = new ArrayList(breakpointListeners).iterator();
		while (i.hasNext()) {
			((BreakpointListener) i.next()).resumed(te);
		}
		if (e != null) {
			EventBus.send(te.getElement(), e);
		}
	}

	public void run(ThreadedElement te) {
		Event e = (Event) events.remove(te);
		Iterator i = new ArrayList(breakpointListeners).iterator();
		step = false;
		while (i.hasNext()) {
			((BreakpointListener) i.next()).resumed(te);
		}
		if (e != null) {
			EventBus.send(te.getElement(), e);
		}
	}

	public void addBreakpoint(FlowElement e) {
		if (e == null) {
			System.err.println("Did not add breakpoint for null element");
			return;
		}
		if (e instanceof SyntheticFlowElement) {
			userBreakpoints.add(e);
		}
		else {
			userBreakpoints.add(new SyntheticFlowElement(e));
		}
	}

	public void removeBreakpoint(FlowElement element) {
		if (element == null) {
			System.err.println("Did not remove breakpoint for null element");
			return;
		}
		userBreakpoints.remove(element);
	}

	public FlowElement findElement(String file, int line) {
		FlowElement fe = findElement(file, line, tree.getRoot());
		if (fe == null) {
			return new SyntheticFlowElement(file, new Integer(line));
		}
		else {
			return new SyntheticFlowElement(fe);
		}
	}

	protected FlowElement findElement(String file, int line, FlowElement element) {
		String efile = (String) FlowNode.getTreeProperty(FlowElement.FILENAME, element);
		int eline = ((Integer) element.getProperty(FlowElement.LINE)).intValue();
		if (file.equals(efile) && line == eline) {
			return element;
		}
		else {
			for (int i = 0; i < element.elementCount(); i++) {
				FlowElement ret = findElement(file, line, element.getElement(i));
				if (ret != null) {
					return ret;
				}
			}
		}
		return null;
	}

	private static class SyntheticFlowElement implements FlowElement {
		private String file;
		private Integer line;
		private FlowElement flowElement;
		private static final Integer NOLINE = new Integer(-1);

		public SyntheticFlowElement(FlowElement real) {
			this.flowElement = real;
			file = (String) FlowNode.getTreeProperty(FlowElement.FILENAME, real);
			line = (Integer) real.getProperty(FlowElement.LINE);
			if (line == null) {
				line = NOLINE;
			}
		}

		public SyntheticFlowElement(String file, Integer line) {
			this.file = file;
			this.line = line;
		}

		public void addElement(FlowElement element) {
		}

		public FlowElement getElement(int index) {
			return null;
		}

		public int elementCount() {
			return 0;
		}

		public List elements() {
			return null;
		}

		public void setElementType(String type) {
		}

		public String getElementType() {
			return null;
		}

		public void setProperty(String name, Object value) {
			if (FlowElement.LINE.equals(name)) {
				line = (Integer) value;
			}
			else if (FlowElement.FILENAME.equals(name)) {
				file = (String) value;
			}
		}

		public Object getProperty(String name) {
			if (FlowElement.LINE.equals(name)) {
				return line;
			}
			else if (FlowElement.FILENAME.equals(name)) {
				return file;
			}
			return null;
		}

		public boolean hasProperty(String name) {
			return FlowElement.LINE.equals(name) || FlowElement.FILENAME.equals(name);
		}

		public Collection propertyNames() {
			return null;
		}

		public void setParent(FlowElement parent) {
		}

		public FlowElement getParent() {
			return null;
		}

		public void failImmediately(VariableStack stack, String string) throws ExecutionException {
		}

		public ProjectNode getProjectNode() {
			return null;
		}

		public boolean acceptsInlineText() {
			return false;
		}

		public void event(Event e) throws ExecutionException {
		}

		public boolean equals(Object obj) {
			if (obj instanceof SyntheticFlowElement) {
				SyntheticFlowElement fe = (SyntheticFlowElement) obj;
				if (file == null) {
					return fe.getFile() == null && line.equals(fe.getLine());
				}
				else {
					return file.equals(fe.getFile()) && line.equals(fe.getLine());
				}
			}
			else if (obj instanceof FlowElement) {
				new Throwable().printStackTrace();
				throw new RuntimeException("Got a FE");
			}
			return false;
		}

		public int hashCode() {
			return line.intValue() + ((file != null) ? file.hashCode() : 0);
		}

		public void removeProperty(String name) {
		}

		protected String getFile() {
			return file;
		}

		protected Integer getLine() {
			return line;
		}

		public String toString() {
			return "SFE @ " + file + ":" + line;
		}

		public void replaceElement(int index, FlowElement element) {
		}

		public void removeElement(int index) {
		}

		public void setElements(List elements) {
		}

		public void setProperties(Map properties) {
		}

		public void addStaticArgument(String name, Object value) {
		}

		public void setStaticArguments(Map args) {
		}

		public Map getStaticArguments() {
			return null;
		}

	}
}
