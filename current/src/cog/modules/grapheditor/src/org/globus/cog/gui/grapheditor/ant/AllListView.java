
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant;

import org.globus.cog.gui.grapheditor.targets.swing.views.ListView;

/**
 * All the nodes in a table. The ListView already does that, so just the name is changed
 */
public class AllListView extends ListView{
    public AllListView(){
        setName("All");
    }
}
