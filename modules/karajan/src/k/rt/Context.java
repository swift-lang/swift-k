//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 7, 2013
 */
package k.rt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context {
	private List<String> arguments;
	private Map<String, Object> attributes;

	public List<String> getArguments() {
		return arguments;
	}

	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}
	
	public void setAttribute(String name, Object value) {
	    if (attributes == null) {
	        attributes = new HashMap<String, Object>();
	    }
	    attributes.put(name, value);
	}
	
	public Object getAttribute(String name) {
	    if (attributes == null) {
	        return null;
	    }
	    else {
	        return attributes.get(name);
	    }
	}
}
