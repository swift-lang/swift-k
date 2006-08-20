
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant;

import org.globus.cog.gui.grapheditor.canvas.AbstractCanvas;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;

public class TaskCanvas extends AbstractCanvas {
	public TaskCanvas(){
        this(null);
    }

    public TaskCanvas(NodeComponent owner) {
        super(owner);
        getSupportedNodes().clear();
        getSupportedEdges().clear();
        //set the supported nodes
        /*
        Note that DefaultNode is added before EchoNode
        When the createNode() method is called it first checks the
        last added node. This allows for specialization of classes
        along the inheritance tree.
        If the order is changed, DefaultNode is checked first. It will
        accept to render any type of object within the buildfile, and hence
        EchoNode will never have a chance.
        */
        addNodeType(new TaskNode());
        addNodeType(new EchoNode());
		addNodeType(new SerialNode());
		addNodeType(new ParallelNode());
		addNodeType(new ForNode());
        //set the default view
    }

    public boolean supportsType(String type){
        //we support anything that might come along
        return true;
    }
}

