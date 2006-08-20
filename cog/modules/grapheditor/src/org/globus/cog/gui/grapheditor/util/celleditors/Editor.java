
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.util.celleditors;

/**
 * Interface for a cell editor
 */
public interface Editor {
    public void setValue(Object value);

    public Object getValue();
}
