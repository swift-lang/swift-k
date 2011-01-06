// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 14, 2005
 */
package org.globus.cog.karajan.util.serialization;

import org.globus.cog.karajan.util.StateManager._Checkpoint;
import org.globus.cog.karajan.util.StateManager._State;
import org.globus.cog.karajan.workflow.nodes.ProjectNode;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class CheckpointConverter extends AbstractKarajanConverter {
	
	public CheckpointConverter(KarajanSerializationContext kcontext) {
		super(kcontext);
	}

	public boolean canConvert(Class type) {
		return _Checkpoint.class.equals(type);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		throw new ConversionException(
				"The checkpoint converter can only be used for unmarshalling checkpoints");
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		_Checkpoint checkpoint = new _Checkpoint();
		checkpoint.projectNode = (ProjectNode) unmarshalObject(reader, context, ProjectNode.class, checkpoint);
		getKContext().getTree().setRoot(checkpoint.projectNode);
		getKContext().setSource(false);
		checkpoint.state = (_State) unmarshalObject(reader, context, _State.class, checkpoint);
		return checkpoint;
	}

}