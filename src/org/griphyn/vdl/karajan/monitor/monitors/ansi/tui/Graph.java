//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 27, 2009
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

public class Graph extends Component {
    private LinkedList data;

    public Graph() {
        data = new LinkedList();
    }

    public void push(double value) {
        data.addLast(new Double(value));
        if (data.size() >= getWidth() - 2) {
            data.removeFirst();
        }
    }

    protected void draw(ANSIContext context) throws IOException {
        double max = 1;
        double min = 0;
        Iterator i = data.iterator();
        while (i.hasNext()) {
            Double d = (Double) i.next();
            if (d.doubleValue() > max) {
                max = d.doubleValue();
            }
            if (d.doubleValue() < min) {
                min = d.doubleValue();
            }
        }
        context.bgColor(getBgColor());
        context.fgColor(getFgColor());
        context.filledFrame(sx, sy, width, height);
        int last = Integer.MIN_VALUE;
        i = data.iterator();
        int j = sx + 1;
        context.lineArt(true);
        while (i.hasNext()) {
            Double d = (Double) i.next();
            double v = (d.doubleValue() - min) / (max - min) * (height - 3);
            int c = (int) v;
            if (last == Integer.MIN_VALUE || last == c) {
                context.moveTo(j, sy + height - c - 2);
                context.putChar(ANSI.GCH_H_LINE);
            }
            else if (last > c) {
                for (int k = c; k <= last; k ++) {
                    context.moveTo(j, sy + height - k - 2);
                    if (k == last) {
                        context.putChar(ANSI.GCH_UR_CORNER);
                    }
                    else {
                        context.putChar(ANSI.GCH_V_LINE);
                    }
                }
                context.moveTo(j, sy + height - c - 2);
                context.putChar(ANSI.GCH_LL_CORNER);
            }
            else {
                for (int k = last; k < c; k ++) {
                    context.moveTo(j, sy + height - k - 2);
                    if (k == last) {
                        context.putChar(ANSI.GCH_LR_CORNER);
                    }
                    else {
                        context.putChar(ANSI.GCH_V_LINE);
                    }
                }
                context.moveTo(j, sy + height - c - 2);
                context.putChar(ANSI.GCH_UL_CORNER);
            }
            last = c;
            j++;
        }
        context.moveTo(sx, sy + 1);
        context.putChar(ANSI.GCH_CROSS);
        context.text(String.valueOf(max));
        context.moveTo(sx, sy + height - 2);
        context.putChar(ANSI.GCH_CROSS);
        context.text(String.valueOf(min));
        context.lineArt(false);
    }
}
  
