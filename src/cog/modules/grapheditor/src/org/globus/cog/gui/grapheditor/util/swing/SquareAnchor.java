
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.util.swing;

import java.awt.Dimension;
import java.awt.Graphics;

import org.globus.cog.gui.grapheditor.targets.swing.util.Anchor;
import org.globus.cog.gui.grapheditor.targets.swing.util.GraphComponentWrapper;

/**
 * An implementation of an anchor as a small square
 */
public class SquareAnchor extends Anchor{

    public SquareAnchor(GraphComponentWrapper owner){
        super(owner);
    }
    
    public void paint(Graphics g){
        Dimension s = getSize();
        g.clearRect(0, 0, s.width - 1, s.height - 1);
        g.drawRect(0, 0, s.width - 1, s.height - 1);
    }
}
