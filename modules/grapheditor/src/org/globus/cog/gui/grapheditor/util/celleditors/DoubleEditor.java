
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.util.celleditors;

import javax.swing.JTextField;

/**
 * A simple string editor
 */
public class DoubleEditor extends JTextField implements Editor{
    public void setValue(Object value) {
        setText(value.toString());
    }

    public Object getValue() {
        return new Double(getText());
    }
}
