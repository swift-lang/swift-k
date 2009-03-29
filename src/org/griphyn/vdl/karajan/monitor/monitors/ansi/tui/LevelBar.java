//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 28, 2009
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.IOException;

public class LevelBar extends Component {
    private float value;
    
    public LevelBar() {
        bgColor = ANSI.BLACK;
        fgColor = ANSI.RED;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    protected void draw(ANSIContext context) throws IOException {
        context.moveTo(sx, sy);
        context.bgColor(fgColor);
        int c = (int) (value * width);
        context.spaces(c);
        context.bgColor(bgColor);
        context.spaces(width - c);
    }
}
