
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.util.tables;

import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.properties.*;

import javax.swing.*;
import java.awt.*;

/**
 * An abstract property editor
 */
public abstract class PropertyEditor {

    private NodeComponent nodeComponent;
    private Property property;
    private Component component;

    public PropertyEditor() {
        component = new JLabel("null editor");
    }

    public void edit(NodeComponent nodeComponent, Property property) {
        this.nodeComponent = nodeComponent;
        this.property = property;
    }

    public Component getComponent() {
        return component;
    }

    public Object getValue() {
        return null;
    }
}
