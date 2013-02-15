// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.examples.taskgraph;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;

import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.impl.common.taskgraph.DependencyPair;
import org.globus.cog.abstraction.interfaces.Dependency;
import org.globus.cog.abstraction.interfaces.ExecutableObject;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.interfaces.TaskGraph;
import org.globus.cog.gui.grapheditor.RendererFactory;
import org.globus.cog.gui.grapheditor.RootContainer;
import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.generic.GenericEdge;
import org.globus.cog.gui.grapheditor.generic.GenericNode;
import org.globus.cog.gui.grapheditor.generic.RootNode;
import org.globus.cog.gui.grapheditor.properties.OverlayedProperty;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.gui.grapheditor.targets.swing.GraphFrame;
import org.globus.cog.gui.grapheditor.targets.swing.SwingCanvasRenderer;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasAction;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionEvent;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionListener;
import org.globus.cog.gui.grapheditor.targets.swing.views.GraphView;
import org.globus.cog.util.graph.Graph;
import org.globus.cog.util.graph.Node;

public class HierarchicalDAGVisualizer extends HierarchicalDAG implements
        StatusListener, ActionListener, CanvasActionListener {
    private Hashtable nodeMapping;
    private CanvasAction run;

    public HierarchicalDAGVisualizer() {
        super();
        this.nodeMapping = new Hashtable();
    }

    private RootNode init() {
        RootNode rootNode = new RootNode();
        rootNode.setName("Java CoG Kit Work Manager");
        GraphCanvas graphCanvas = rootNode.getCanvas();
        if (graphCanvas == null) {
            graphCanvas = rootNode.createCanvas();
        }
        Graph graph = new Graph();
        GenericNode gn = new GenericNode();
        createSubGraph(gn, this.taskGraph);
        graph.addNode(gn);
        this.nodeMapping.put(this.taskGraph, gn);
        graphCanvas.setGraph(graph);
        return rootNode;
    }

    private void updateRenderer(SwingCanvasRenderer renderer) {
        this.run = new CanvasAction("300#Control>1#Run", null,
                CanvasAction.ACTION_NORMAL);
        this.run.addCanvasActionListener(this);
        renderer.addToolBarItem(this.run);

        renderer.setView(new GraphView());
    }

    private void createSubGraph(GenericNode genericNode,
            ExecutableObject executableObject) {
        if (executableObject.getObjectType() == ExecutableObject.TASK) {
            Task task = (Task) executableObject;
            task.addStatusListener(this);
            setTaskProperties(genericNode, task);
        } else if (executableObject.getObjectType() == ExecutableObject.TASKGRAPH) {
            TaskGraph taskGraph = (TaskGraph) executableObject;
            taskGraph.addStatusListener(this);
            setGraphProperties(genericNode, taskGraph);

            GraphCanvas subCanvas = genericNode.getCanvas();
            if (subCanvas == null) {
                subCanvas = genericNode.createCanvas();
            }
            Graph graph = new Graph();

            Enumeration en = taskGraph.elements();
            while (en.hasMoreElements()) {
                ExecutableObject eo = (ExecutableObject) en.nextElement();
                GenericNode gn = new GenericNode();
                createSubGraph(gn, eo);
                graph.addNode(gn);
                this.nodeMapping.put(eo, gn);
            }

            // Draw the dependencies
            Dependency dependency = taskGraph.getDependency();
            if (dependency != null) {
                Enumeration en1 = dependency.elements();
                while (en1.hasMoreElements()) {
                    DependencyPair pair = (DependencyPair) en1.nextElement();
                    ExecutableObject eo1 = pair.getFrom();
                    ExecutableObject eo2 = pair.getTo();

                    GenericNode gn1 = (GenericNode) this.nodeMapping.get(eo1);
                    GenericNode gn2 = (GenericNode) this.nodeMapping.get(eo2);

                    Node node1 = graph.findNode(gn1);
                    Node node2 = graph.findNode(gn2);

                    graph.addEdge(node1, node2, new GenericEdge());
                }
            }
            subCanvas.setGraph(graph);
        }
    }

    private void setTaskProperties(GenericNode genericNode, Task task) {
        Property property;

        property = new OverlayedProperty(genericNode, "Identity");
        genericNode.addProperty(property);

        property = new OverlayedProperty(genericNode, "Type");
        genericNode.addProperty(property);

        property = new OverlayedProperty(genericNode, "StdError");
        genericNode.addProperty(property);

        property = new OverlayedProperty(genericNode, "StdOutput");
        genericNode.addProperty(property);

        property = new OverlayedProperty(genericNode, "Provider");
        genericNode.addProperty(property);

        property = new OverlayedProperty(genericNode, "ServiceContact");
        genericNode.addProperty(property);

        property = new OverlayedProperty(genericNode, "Specification");
        genericNode.addProperty(property);

        // set all the above defined properties:
        genericNode.getProperty("name").setValue(task.getName());
        genericNode.getProperty("Identity").setValue(
                task.getIdentity().toString());
        genericNode.getProperty("Type").setValue(
                Integer.toString(task.getType()));
        genericNode.getProperty("StdOutput").setValue(task.getStdOutput());
        genericNode.getProperty("StdError").setValue(task.getStdError());
        genericNode.getProperty("Provider").setValue(task.getProvider());
        genericNode.getProperty("status").setValue(
                new Integer(GenericNode.STATUS_STOPPED));
        genericNode.getProperty("Specification").setValue(
                task.getSpecification().getSpecification());
    }

    private void setGraphProperties(GenericNode genericNode, TaskGraph taskGraph) {
        Property property;

        property = new OverlayedProperty(genericNode, "Identity");
        genericNode.addProperty(property);

        property = new OverlayedProperty(genericNode, "Size");
        genericNode.addProperty(property);

        property = new OverlayedProperty(genericNode, "Unsubmitted");
        genericNode.addProperty(property);

        property = new OverlayedProperty(genericNode, "Submitted");
        genericNode.addProperty(property);

        property = new OverlayedProperty(genericNode, "Active");
        genericNode.addProperty(property);

        property = new OverlayedProperty(genericNode, "Suspended");
        genericNode.addProperty(property);

        property = new OverlayedProperty(genericNode, "Resumed");
        genericNode.addProperty(property);

        property = new OverlayedProperty(genericNode, "Canceled");
        genericNode.addProperty(property);

        property = new OverlayedProperty(genericNode, "Failed");
        genericNode.addProperty(property);

        property = new OverlayedProperty(genericNode, "Completed");
        genericNode.addProperty(property);

        // set all the above defined properties:
        genericNode.getProperty("name").setValue(taskGraph.getName());
        genericNode.getProperty("Identity").setValue(
                taskGraph.getIdentity().toString());
        genericNode.getProperty("status").setValue(
                new Integer(GenericNode.STATUS_STOPPED));
        genericNode.getProperty("Size").setValue(
                Integer.toString(taskGraph.getSize()));
        genericNode.getProperty("Unsubmitted").setValue(
                Integer.toString(taskGraph.getUnsubmittedCount()));
        genericNode.getProperty("Submitted").setValue(
                Integer.toString(taskGraph.getSubmittedCount()));
        genericNode.getProperty("Active").setValue(
                Integer.toString(taskGraph.getActiveCount()));
        genericNode.getProperty("Suspended").setValue(
                Integer.toString(taskGraph.getSuspendedCount()));
        genericNode.getProperty("Resumed").setValue(
                Integer.toString(taskGraph.getResumedCount()));
        genericNode.getProperty("Canceled").setValue(
                Integer.toString(taskGraph.getCanceledCount()));
        genericNode.getProperty("Failed").setValue(
                Integer.toString(taskGraph.getFailedCount()));
        genericNode.getProperty("Completed").setValue(
                Integer.toString(taskGraph.getCompletedCount()));
    }

    public void updateProperties(StatusEvent event) {
        TaskGraph taskGraph = (TaskGraph) event.getSource();
        GenericNode genericNode = (GenericNode) this.nodeMapping.get(event
                .getSource());

        genericNode.getProperty("Unsubmitted").setValue(
                Integer.toString(taskGraph.getUnsubmittedCount()));
        genericNode.getProperty("Submitted").setValue(
                Integer.toString(taskGraph.getSubmittedCount()));
        genericNode.getProperty("Active").setValue(
                Integer.toString(taskGraph.getActiveCount()));
        genericNode.getProperty("Suspended").setValue(
                Integer.toString(taskGraph.getSuspendedCount()));
        genericNode.getProperty("Resumed").setValue(
                Integer.toString(taskGraph.getResumedCount()));
        genericNode.getProperty("Canceled").setValue(
                Integer.toString(taskGraph.getCanceledCount()));
        genericNode.getProperty("Failed").setValue(
                Integer.toString(taskGraph.getFailedCount()));
        genericNode.getProperty("Completed").setValue(
                Integer.toString(taskGraph.getCompletedCount()));
    }

    public void statusChanged(StatusEvent event) {
        ExecutableObject executableObject = event.getSource();
        GenericNode gn = (GenericNode) this.nodeMapping.get(executableObject);
        int status = event.getStatus().getStatusCode();
        logger.debug("ID: " + executableObject.getIdentity().toString());
        logger.debug("Status: " + executableObject.getStatus().getStatusCode());
        switch (status) {
        case Status.UNSUBMITTED:
            gn.getProperty("status").setValue(
                    new Integer(GenericNode.STATUS_STOPPED));
            break;
        case Status.SUBMITTED:
            gn.getProperty("status").setValue(
                    new Integer(GenericNode.STATUS_RUNNING));
            break;
        case Status.ACTIVE:
            gn.getProperty("status").setValue(
                    new Integer(GenericNode.STATUS_RUNNING));
            break;
        case Status.FAILED:
            gn.getProperty("status").setValue(
                    new Integer(GenericNode.STATUS_FAILED));
            break;
        case Status.COMPLETED:
            gn.getProperty("status").setValue(
                    new Integer(GenericNode.STATUS_COMPLETED));
            break;
        case Status.CANCELED:
            gn.getProperty("status").setValue(
                    new Integer(GenericNode.STATUS_FAILED));
            break;
        case Status.SUSPENDED:
            gn.getProperty("status").setValue(
                    new Integer(GenericNode.STATUS_STOPPED));
            break;
        case Status.RESUMED:
            gn.getProperty("status").setValue(
                    new Integer(GenericNode.STATUS_RUNNING));
            break;
        default:
            break;
        }
        if (executableObject.getObjectType() == ExecutableObject.TASKGRAPH) {
            updateProperties(event);
        }
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("Run")) {
            if (!this.active) {
                this.active = true;
                submitDAG();
            }
        }
    }

    public void canvasActionPerformed(CanvasActionEvent e) {
        if (e.getSource() == this.run) {
            if (!this.active) {
                this.active = true;
                submitDAG();
            }
        }
    }

    public static void main(String[] args) {
        try {
            HierarchicalDAGVisualizer dagViz = new HierarchicalDAGVisualizer();
            dagViz.createDAG();
            RendererFactory.addRootContainer("swing", GraphFrame.class);
            RootContainer rootContainer = new GraphFrame();
            RootNode rootNode = dagViz.init();
            rootContainer.setRootNode(rootNode);
            dagViz.updateRenderer((SwingCanvasRenderer) rootContainer
                    .getCanvasRenderer());
            Thread thread = new Thread(rootContainer);
            thread.start();
        } catch (Exception e) {
            logger.error("Exception caught: ", e);
        }
    }
}