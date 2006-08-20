
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.broker.interfaces;

import java.util.Enumeration;

import org.globus.cog.abstraction.interfaces.Task;


public interface TaskToServiceMapper {
    public boolean match(Task task);
    
    public void setAttribute(String name, Object value);
    public Object getAttribute(String name);
    public Enumeration getAllAttributes();
}
