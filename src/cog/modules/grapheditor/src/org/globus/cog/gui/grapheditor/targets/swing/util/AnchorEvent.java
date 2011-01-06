
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.targets.swing.util;

import java.awt.AWTEvent;
import java.awt.Component;
/**
 * Defines events for anchors
 */
public class AnchorEvent extends AWTEvent{
    public static int UNDEFINED = 0;
    public static int BEGIN_DRAG = 1;
    public static int END_DRAG = 2;
    public static int DRAG = 3;
    public static int MOVE = 4;
    public static int CONNECT = 5;
    
    private int type;
    private int xc, yc;
    private Object target;
    
    public AnchorEvent(Component source, int type){
        super(source, -1);
        this.type = type;
    }
    
    public void setType(int type){
        this.type = type;
    }
    
    public int getType(){
        return type;
    }
    
    public void setCoords(int xc, int yc){
        this.xc = xc;
        this.yc = yc;
    }
    
    public int getX(){
        return xc;
    }
    
    public int getY(){
        return yc;
    }
    
    public void setTarget(Object target){
        this.target = target;
    }
    
    public Object getTarget(){
        return target;
    }
}
