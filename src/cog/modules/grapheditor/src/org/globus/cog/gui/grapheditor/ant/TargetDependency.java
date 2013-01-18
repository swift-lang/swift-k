
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant;

import org.globus.cog.gui.grapheditor.edges.EdgeComponent;
import org.globus.cog.gui.grapheditor.edges.SimpleArrow;

/**
 * Does not do anything, but it allows class-based filtering
 */
public class TargetDependency extends SimpleArrow implements EdgeComponent{
    public TargetDependency(){
		loadIcon("images/ant-dependency.png");
    }
}
