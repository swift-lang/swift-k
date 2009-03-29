//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 28, 2009
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.util.Iterator;

public class LevelBars extends Container {
    private int range;
    private int[] values; 
    
    public LevelBars(int bars) {
        values = new int[bars];
        for (int i = 0; i < bars; i++) {
            LevelBar lb = new LevelBar();
            lb.setLocation(0, i);
            add(lb);
        }
    }
    
    public void setValue(int index, int v) {
        values[index] = v;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > range) {
                range = values[i];
            }
        }
        for (int i = 0; i < values.length; i++) {
            getBar(i).setValue((float) values[i] / range);
        }
    }

    private LevelBar getBar(int i) {
        return (LevelBar) getComponents().get(i);
    }

    protected void validate() {
        Iterator i = getComponents().iterator();
        int j = 0;
        while (i.hasNext()) {
            ((Component) i.next()).setSize(width, 1);
        }
        super.validate();
    }
    
    
}
