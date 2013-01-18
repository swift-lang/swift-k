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

import org.globus.cog.karajan.Optimizer;
import org.globus.cog.karajan.stack.DefaultStackFrame;
import org.globus.cog.karajan.stack.LinkedStack;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.DefList;
import org.globus.cog.karajan.util.ListKarajanIterator;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.util.StateManager._Checkpoint;
import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.KarajanRuntimeException;
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
		registerConverter(new DefListConverter(kcontext));
		registerConverter(new UDEDefinitionConverter(kcontext));
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
		alias("deflist", DefList.class);
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
	
	public static ElementTree readSourceWithUIDs(Reader reader, String name) {
		return readSource(reader, name, true, true);
	}

	public static ElementTree readSourceNoUIDs(Reader reader, String name) {
		return readSource(reader, name, true, false);
	}
	
	public static ElementTree readSourceNoUIDs(Reader reader, String name, boolean lineNumbers) {
		return readSource(reader, name, lineNumbers, false);
	}

	public static ElementTree readSource(Reader reader, String name, boolean lineNumbers, boolean uids) {
		XMLConverter loader = new XMLConverter();
		loader.getKContext().setKmode(lineNumbers);
		loader.getKContext().setUIDs(uids);
		loader.registerConverter(new ProjectNodeConverter(loader.kcontext));
		loader.alias("project", ProjectNode.class);
		loader.alias("karajan", ProjectNode.class);
		loader.xstream.setMode(XStream.ID_REFERENCES);
		loader.xstream.getClassMapper().setOverrideRootType(FlowElement.class);
		Object o = loader.xstream.fromXML(reader);
		ElementTree tree = loader.getKContext().getTree();
		tree.setRoot((FlowElement) o);
		tree.setName(name);
		Optimizer.optimize(tree.getRoot());
		return tree;
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
			Optimizer.optimize(parent);
		}
		catch (CannotResolveClassException e) {
			throw new KarajanRuntimeException("Tag not recognized: " + e.getMessage());
		}
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