//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 12, 2005
 */
package org.globus.cog.karajan.util.serialization;

import org.globus.cog.karajan.workflow.nodes.ProjectNode;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

public class ProjectNodeConverter extends ElementConverter {

	public ProjectNodeConverter(KarajanSerializationContext kcontext) {
		super(kcontext);
	}
	
	public boolean canConvert(Class cls) {
		return ProjectNode.class.isAssignableFrom(cls);
	}
	
	public Object unmarshal(HierarchicalStreamReader rd, UnmarshallingContext context) {
		String name = rd.getNodeName();
		ProjectNode projectNode = new ProjectNode();
		setProperties(projectNode, rd, context);
		getKContext().getTree().setRoot(projectNode);
		readChildren(projectNode, rd, context);
		return projectNode;
	}
}
