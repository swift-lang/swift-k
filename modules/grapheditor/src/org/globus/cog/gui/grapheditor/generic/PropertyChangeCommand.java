
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.generic;

public class PropertyChangeCommand {
	private String nodeid;
	private String property;
	private String value;
	
	public PropertyChangeCommand(String nodeid, String property, String value){
		setNodeid(nodeid);
		setProperty(property);
		setValue(value);
	}
	
	public void setNodeid(String nodeid) {
		this.nodeid = nodeid;
	}
	
	public String getNodeid() {
		return nodeid;
	}
	
	public void setProperty(String property) {
		this.property = property;
	}
	
	public String getProperty() {
		return property;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
}

