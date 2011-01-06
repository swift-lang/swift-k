
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.util.celleditors;

import javax.swing.JList;

/**
 * A simple string editor
 */
public class BooleanEditor extends JList implements Editor{
	
	public BooleanEditor() {
		super(new String[]{"TRUE", "FALSE"});
	}
	
    public void setValue(Object value) {
        boolean val = ((Boolean) value).booleanValue();
        if (val) {
        	setSelectedIndex(0);
        }
        else {
        	setSelectedIndex(1);
        }
    }

    public Object getValue() {
        return Boolean.valueOf(getSelectedIndex() == 0);
    }
}
