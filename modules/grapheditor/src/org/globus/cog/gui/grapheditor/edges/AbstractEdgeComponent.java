
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.edges;

import java.util.Iterator;
import java.util.List;

import org.globus.cog.gui.grapheditor.AbstractGraphComponent;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.properties.ComponentClassProperty;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.gui.grapheditor.util.ConservativeArrayList;

/**
 * Basic functionality for an edge component
 */
public abstract class AbstractEdgeComponent extends AbstractGraphComponent implements EdgeComponent {

    //private NodeComponent src, dest;
    private String from, to;
    private List controlPoints;
    private List controlPointListeners;
    
    static {
    	addClassProperty(new ComponentClassProperty(AbstractEdgeComponent.class, "from", Property.HIDDEN));
        addClassProperty(new ComponentClassProperty(AbstractEdgeComponent.class, "to", Property.HIDDEN));
    }

    public AbstractEdgeComponent() {
        setComponentType("edge");
		ControlPoint begin = new ControlPoint(0, 0);
		ControlPoint end = new ControlPoint(7, 7);
		controlPoints = new ConservativeArrayList(2);
		controlPoints.add(begin);
		controlPoints.add(end);
    }
	
	public ControlPoint updateControlPoint(int index, int x, int y){
		ControlPoint cp = (ControlPoint) controlPoints.get(index);
		cp.setLocation(x, y);
		fireControlPointUpdated(index);
		return cp;
	}
	
	public void addControlPoint(ControlPoint p){
		controlPoints.add(p);
	}
	
	public int numControlPoints(){
		return controlPoints.size();
	}
	
	public ControlPoint getControlPoint(int i){
		return (ControlPoint)controlPoints.get(i);
	}

    public NodeComponent getSource() {
        //return src;
	return null;
    }

    public NodeComponent getDestination() {
        //return dest;
	return null;
    }

    public void connectDestination(NodeComponent dest) {
        //this.dest = dest;
    }

    public void connectSource(NodeComponent src) {
        //this.src = src;
    }

    public void disconnectSource() {
        //src = null;
    }

    public void disconnectDestination() {
        //dest = null;
    }
    
    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public void setFrom(String string) {
        this.from = string;
    }

    public void setTo(String string) {
        this.to = string;
    }

	public synchronized void addControlPointListener(ControlPointListener l) {
		if (controlPointListeners == null) {
			controlPointListeners = new ConservativeArrayList(1);
		}
		if (!controlPointListeners.contains(l)){
			controlPointListeners.add(l);
		}

	}

	public synchronized void removeControlPointListener(ControlPointListener l) {
		if (controlPointListeners == null) {
			return;
		}
		controlPointListeners.remove(l);
	}
	
	public synchronized void fireControlPointUpdated(int index){
		if (controlPointListeners == null) {
			return;
		}
		Iterator i = controlPointListeners.iterator();
		while (i.hasNext()){
			((ControlPointListener) i.next()).controlPointUpdated(this, index);
		}
	}

}
