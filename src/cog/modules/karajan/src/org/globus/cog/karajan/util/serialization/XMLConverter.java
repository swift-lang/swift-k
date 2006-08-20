// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 12, 2005
 */
package org.globus.cog.karajan.util.serialization;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import org.globus.cog.karajan.stack.DefaultStackFrame;
import org.globus.cog.karajan.stack.LinkedStack;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ListKarajanIterator;
import org.globus.cog.karajan.util.StateManager;
import org.globus.cog.karajan.util.ThreadedElement;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.util.StateManager._Checkpoint;
import org.globus.cog.karajan.util.StateManager._RunningElement;
import org.globus.cog.karajan.util.StateManager._State;
import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.ExecutionContext;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;
import org.globus.cog.karajan.workflow.events.Event;
import org.globus.cog.karajan.workflow.events.EventTargetPair;
import org.globus.cog.karajan.workflow.futures.FutureVariableArguments;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.FlowNode;
import org.globus.cog.karajan.workflow.nodes.ProjectNode;
import org.globus.cog.karajan.workflow.nodes.user.ParallelImplicitExecutionUDE;
import org.globus.cog.karajan.workflow.nodes.user.SequentialImplicitExecutionUDE;
import org.globus.cog.karajan.workflow.nodes.user.UDEDefinition;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.alias.CannotResolveClassException;
import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.ext.MXppDriver;

public class XMLConverter {
	private XStream xstream;
	private KarajanSerializationContext kcontext;

	public XMLConverter() {
		this(new KarajanSerializationContext());
	}

	public XMLConverter(ElementTree tree) {
		this(new KarajanSerializationContext(tree));
	}

	public XMLConverter(KarajanSerializationContext kcontext) {
		this(kcontext, false);
	}

	public XMLConverter(KarajanSerializationContext kcontext, boolean restricted) {
		this.kcontext = kcontext;
		if (restricted) {
			xstream = new XStream(new MXppDriver()) {
				protected final ClassMapper buildMapper(String classAttributeIdentifier) {
					return new RestrictedClassMapper(super.buildMapper(classAttributeIdentifier));
				}
			};
		}
		else {
			xstream = new XStream(new MXppDriver());
		}
		xstream.addImmutableType(ParallelImplicitExecutionUDE.class);
		registerConverter(new NullConverter());
		registerConverter(new ElementConverter(kcontext));
		registerConverter(new ListLoopIteratorConverter(kcontext));
		alias("karajan-iterator", ListKarajanIterator.class);
		alias("element", SequentialImplicitExecutionUDE.class);
		alias("parallelElement", ParallelImplicitExecutionUDE.class);
		alias("ude-definition", UDEDefinition.class);
		alias("stack", LinkedStack.class);
		registerConverter(new ThreadingContextConverter(kcontext));
		alias("thread", ThreadingContext.class);
		// xstream.registerConverter(new ErrorHandlerConverter(kcontext));
		// xstream.alias("error-handler", ErrorHandler.class);
		registerConverter(new FutureVariableArgumentsConverter(kcontext));
		alias("future-vargs", FutureVariableArguments.class);
		registerConverter(new FutureVariableArgumentsIteratorConverter(kcontext));
		registerConverter(new SimpleDateFormatConverter(kcontext));
		registerConverter(new DecimalFormatConverter(kcontext));
		registerConverter(new ServiceConverter(kcontext));
		xstream.setMode(XStream.NO_REFERENCES);
		kcontext.setClassMapper(xstream.getClassMapper());
		kcontext.setConverterLookup(xstream.getConverterLookup());
	}

	public void registerConverter(Converter c, int priority) {
		xstream.registerConverter(c, priority);
	}
	
	public final void registerConverter(Converter c) {
		xstream.registerConverter(c);
	}

	public final void alias(String name, Class cls) {
		xstream.alias(name, cls);
	}

	public void writeTree(Writer writer) {
		xstream.toXML(kcontext.getTree().getRoot(), writer);
	}

	public void write(Object obj, Writer writer) {
		xstream.toXML(obj, writer);
	}

	public static ElementTree readSource(Reader reader, String name) {
		return readSource(reader, name, true);
	}

	public static ElementTree readSource(Reader reader, String name, boolean lineNumbers) {
		XMLConverter loader = new XMLConverter();
		loader.getKContext().setKmode(lineNumbers);
		loader.registerConverter(new ProjectNodeConverter(loader.kcontext));
		loader.alias("project", ProjectNode.class);
		loader.alias("karajan", ProjectNode.class);
		loader.xstream.setMode(XStream.ID_REFERENCES);
		loader.xstream.getClassMapper().setOverrideRootType(FlowElement.class);
		Object o = loader.xstream.fromXML(reader);
		ElementTree tree = loader.getKContext().getTree();
		tree.setRoot((FlowElement) o);
		tree.setName(name);
		return tree;
	}

	public static _Checkpoint readCheckpoint(Reader reader, String project, boolean lineNumbers) {
		XMLConverter loader = new XMLConverter();
		loader.getKContext().setKmode(lineNumbers);
		loader.registerConverter(new ProjectNodeConverter(loader.kcontext));
		loader.registerConverter(new StackFrameConverter(loader.kcontext));
		loader.registerConverter(new CheckpointConverter(loader.kcontext));
		loader.alias("project", ProjectNode.class);
		loader.alias("checkpoint", _Checkpoint.class);
		loader.alias("frame", DefaultStackFrame.class);
		loader.alias("flowelement", FlowElement.class);
		loader.alias("karajan", ProjectNode.class);
		loader.xstream.setMode(XStream.ID_REFERENCES);
		Object o = loader.xstream.fromXML(reader);
		if (o instanceof _Checkpoint) {
			return (_Checkpoint) o;
		}
		else {
			throw new ConversionException("Not a checkpoint: " + o);
		}
	}

	public static Object readObject(Reader reader) {
		return readObject(reader, new ElementTree());
	}
	
	public static Object readObjectRestricted(Reader reader) {
		return readObjectRestricted(reader, new ElementTree());
	}

	public static Object readObject(Reader reader, ElementTree tree) {
		return readObject(reader, tree, false);
	}

	public static Object readObjectRestricted(Reader reader, ElementTree tree) {
		return readObject(reader, tree, true);
	}

	public static Object readObject(Reader reader, ElementTree tree, boolean restricted) {
		KarajanSerializationContext kcontext = new KarajanSerializationContext(tree);
		XMLConverter loader = new XMLConverter(kcontext, restricted);
		loader.getKContext().setSource(false);
		loader.registerConverter(new ProjectNodeConverter(loader.kcontext));
		loader.registerConverter(new StackFrameConverter(loader.kcontext));
		loader.registerConverter(new CheckpointConverter(loader.kcontext));
		loader.alias("project", ProjectNode.class);
		loader.alias("checkpoint", _Checkpoint.class);
		loader.alias("frame", DefaultStackFrame.class);
		loader.alias("flowelement", FlowElement.class);
		loader.alias("karajan", ProjectNode.class);
		loader.xstream.setMode(XStream.ID_REFERENCES);
		return loader.xstream.fromXML(reader);
	}

	public static void readWithRoot(FlowNode parent, ElementTree tree, Reader reader, String project) {
		readWithRoot(parent, tree, reader, project, true);
	}

	public static void readWithRoot(FlowNode parent, ElementTree tree, Reader reader,
			String project, boolean lineNumbers) {
		XMLConverter loader = new XMLConverter(tree);
		loader.registerConverter(new RootlessConverter(loader.kcontext, parent));
		loader.alias("karajan", ProjectNode.class);
		loader.xstream.getClassMapper().setOverrideRootType(FlowNode.class);
		loader.kcontext.setParent(parent);
		loader.kcontext.setFileName(project);
		loader.kcontext.setKmode(lineNumbers);
		loader.xstream.fromXML(reader);
	}

	public static void read(FlowNode parent, ElementTree tree, Reader reader, String project) {
		read(parent, tree, reader, project, true);
	}

	public static void read(FlowNode parent, ElementTree tree, Reader reader, String project,
			boolean lineNumbers) {
		XMLConverter loader = new XMLConverter(tree);
		loader.xstream.registerConverter(new RootlessConverter(loader.kcontext, parent));
		loader.xstream.alias("karajan", ProjectNode.class);
		loader.xstream.alias("project", ProjectNode.class);
		loader.xstream.getClassMapper().setOverrideRootType(ProjectNode.class);
		loader.kcontext.setParent(parent);
		loader.kcontext.setFileName(project);
		loader.kcontext.setKmode(lineNumbers);
		try {
			loader.xstream.fromXML(reader);
		}
		catch (CannotResolveClassException e) {
			throw new KarajanRuntimeException("Tag not recognized: " + e.getMessage());
		}
	}

	public static void checkpoint(ExecutionContext ec, Writer fw) throws IOException {
		XMLConverter converter;
		StateManager manager = ec.getStateManager();
		fw.write("<checkpoint>\n");
		converter = createSourceMarshallingConverter(ec.getTree());
		converter.writeTree(fw);
		fw.write("\n");
		converter = createStateMarshallingConverter(ec.getTree());
		_State state = new _State();
		Map executing = manager.getExecuting();
		Iterator i;
		i = executing.keySet().iterator();
		while (i.hasNext()) {
			ThreadedElement te = (ThreadedElement) i.next();
			_RunningElement re = new _RunningElement(te.getElement(),
					(VariableStack) executing.get(te));
			state.addRunningElement(re);
		}
		i = manager.getEvents().iterator();
		while (i.hasNext()) {
			EventTargetPair etp = (EventTargetPair) i.next();
			state.addEvent(etp);
		}
		converter.write(state, fw);
		fw.write("\n</checkpoint>\n");
	}

	public static void serializeEvent(Event e, Writer fw) throws IOException {
		serializeEvent(e, e.getStack().getExecutionContext().getTree(), fw);
	}

	public static void serializeEvent(Event e, ElementTree tree, Writer fw) throws IOException {
		XMLConverter converter;
		KarajanSerializationContext kcontext = new KarajanSerializationContext(null);
		kcontext.setDetachedSource(true);
		converter = createStateMarshallingConverter(tree);
		converter.write(e, fw);
	}

	public static void serializeObject(Object o, Writer fw) {
		XMLConverter converter = new XMLConverter();
		converter.write(o, fw);
	}

	public static void serializeStack(VariableStack stack, Writer fw) throws IOException {
		XMLConverter converter;
		converter = createStateMarshallingConverter(stack.getExecutionContext().getTree());
		converter.write(stack, fw);
	}

	public static void serializeTree(ElementTree tree, Writer fw) throws IOException {
		XMLConverter converter = createSourceMarshallingConverter(tree);
		/*
		 * While I love XStream, I dislike the "parent decides how I should be
		 * serialized" concept.
		 */
		converter.alias(tree.getRoot().getElementType(), tree.getRoot().getClass());
		converter.writeTree(fw);
	}

	public static XMLConverter createSourceMarshallingConverter(ElementTree tree) {
		XMLConverter writer = new XMLConverter(tree);
		writer.xstream.registerConverter(new ProjectNodeConverter(writer.kcontext));
		writer.xstream.alias("project", ProjectNode.class);
		return writer;
	}

	public static XMLConverter createStateMarshallingConverter(ElementTree tree) {
		XMLConverter writer = new XMLConverter(tree);
		writer.xstream.registerConverter(new StackFrameConverter(writer.kcontext));
		writer.kcontext.setSource(false);
		writer.xstream.alias("frame", DefaultStackFrame.class);
		writer.xstream.setMode(XStream.ID_REFERENCES);
		return writer;
	}

	public static class _Variable {
		public Object value;
	}

	public KarajanSerializationContext getKContext() {
		return kcontext;
	}
}