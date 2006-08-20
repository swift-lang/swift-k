
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;

public class ProjectPropertyNode extends AntNode implements NodeComponent, PropertyChangeListener{

    String file;

    public ProjectPropertyNode(){
        super();
        setComponentType("property");
        loadIcon("images/ant-property.png");
        setClassRendererClass(AntRenderer.class);
        addPropertyChangeListener(this);
    }

    public GraphComponent newInstance() {
        return new ProjectPropertyNode();
    }

    /**
     * instead of <include file="..."> Ant opted for the confusing
     * <property name="..." value="..."/> or <property file="...">.
     * This method catches whichever property gets set first
     * @param evt
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("name")){
            //label.setText((String) evt.getNewValue());
        }
        if (evt.getPropertyName().equals("file")){
            //label.setText("file: "+(String) evt.getNewValue());
        }
    }
}
