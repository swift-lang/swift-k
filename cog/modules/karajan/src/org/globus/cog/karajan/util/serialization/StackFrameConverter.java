// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 14, 2004
 */
package org.globus.cog.karajan.util.serialization;

import java.util.Iterator;

import org.globus.cog.karajan.stack.DefaultStackFrame;
import org.globus.cog.karajan.stack.StackFrame;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class StackFrameConverter extends AbstractKarajanConverter {

	public StackFrameConverter(KarajanSerializationContext kcontext) {
		super(kcontext);
	}

	public boolean canConvert(Class cls) {
		return StackFrame.class.isAssignableFrom(cls);
	}

	public void marshal(Object elem, HierarchicalStreamWriter wr, MarshallingContext context) {
		StackFrame frame = (StackFrame) elem;

		if (frame.hasBarrier()) {
			wr.addAttribute("barrier", "true");
		}
		if (frame.getRegs().getIA() != 0) {
			wr.addAttribute("iA", String.valueOf(frame.getRegs().getIA()));
		}
		if (frame.getRegs().getIB() != 0) {
			wr.addAttribute("iB", String.valueOf(frame.getRegs().getIB()));
		}
		if (frame.getRegs().getBA()) {
			wr.addAttribute("bA", "true");
		}
		if (frame.getRegs().getBB()) {
			wr.addAttribute("bB", "true");
		}
		Iterator i = frame.names().iterator();
		while (i.hasNext()) {
			String name = (String) i.next();
			wr.startNode("variable");
			wr.addAttribute("name", name);
			Object item = frame.getVar(name);
			if (item == null) {
				wr.addAttribute("type", getKContext().getClassMapper().serializedClass(
						ClassMapper.Null.class));
			}
			else if (item instanceof FlowElement) {
				wr.addAttribute("type", "flowelement");
				Converter c = getKContext().getConverterLookup().lookupConverterForType(
						FlowElement.class);
				c.marshal(item, wr, context);
				// context.convertAnother(item);
			}
			else {
				wr.addAttribute("type", getKContext().getClassMapper().serializedClass(
						item.getClass()));
				context.convertAnother(item);
			}
			wr.endNode();
		}
	}

	public Object unmarshal(HierarchicalStreamReader rd, UnmarshallingContext context) {
		String name = rd.getNodeName();
		if (!name.equals("frame")) {
			throw new ConversionException("Expected <frame...>, got <" + name + "...>");
		}
		StackFrame frame = new DefaultStackFrame();
		if ("true".equals(rd.getAttribute("barrier"))) {
			frame.setBarrier(true);
		}
		if ("true".equals(rd.getAttribute("bA"))) {
			frame.getRegs().setBA(true);
		}
		if ("true".equals(rd.getAttribute("bB"))) {
			frame.getRegs().setBB(true);
		}
		if (rd.getAttribute("iA") != null) {
			frame.getRegs().setIA(Integer.parseInt(rd.getAttribute("iA")));
		}
		if (rd.getAttribute("iB") != null) {
			frame.getRegs().setIB(Integer.parseInt(rd.getAttribute("iB")));
		}
		readVariables(frame, rd, context);
		return frame;
	}

	protected void readVariables(StackFrame frame, HierarchicalStreamReader rd,
			UnmarshallingContext context) {
		while (rd.hasMoreChildren()) {
			rd.moveDown();
			String type = rd.getAttribute("type");
			if (!rd.getNodeName().equalsIgnoreCase("variable")) {
				throw new ConversionException("Expected <variable...>, got <" + rd.getNodeName()
						+ "...>");
			}
			String name = rd.getAttribute("name");
			// force the VariableConverter with _Variable.class
			Object value = context.convertAnother(frame, getKContext().getClassMapper().realClass(
					type));
			frame.setVar(name, value);
			rd.moveUp();
		}
	}

}