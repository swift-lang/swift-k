
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.nodes;

import org.apache.log4j.Logger;

/**
 * A node component that supports editing (has a canvas in other words)
 */
public abstract class EditableNodeComponent extends AbstractNodeComponent {
	private static Logger logger = Logger.getLogger(EditableNodeComponent.class);

    public EditableNodeComponent() {
        super();
    }
}
