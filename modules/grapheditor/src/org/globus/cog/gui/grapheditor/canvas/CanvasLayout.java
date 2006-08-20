
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.canvas;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;

/**
 * A class that doesn't do much
 */

public class CanvasLayout implements LayoutManager2{
    
    public void addLayoutComponent(Component comp, Object constraints){
    }
    
    public void addLayoutComponent(String name, Component comp){
        addLayoutComponent(comp, null);
    }
    
    public void layoutContainer(Container parent){
    }
    
    public Dimension minimumLayoutSize(Container parent){
        return parent.getSize();
    }
    
    public Dimension preferredLayoutSize(Container parent){
        return parent.getSize();
    }
    
    public Dimension maximumLayoutSize(Container parent){
        return parent.getSize();
    }
    
    public void removeLayoutComponent(Component comp){
    }
    
    public float getLayoutAlignmentX(Container parent){
        return (float) 0.5;
    }
    
    public float getLayoutAlignmentY(Container parent){
        return (float) 0.5;
    }
    
    public void invalidateLayout(Container target){
    }
}
