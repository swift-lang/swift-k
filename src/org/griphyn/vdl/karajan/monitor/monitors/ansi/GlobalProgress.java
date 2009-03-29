//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 27, 2009
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi;

import java.io.IOException;
import java.util.TimerTask;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSI;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSIContext;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Component;

public class GlobalProgress extends Component {
    private SystemState state;

    public GlobalProgress(SystemState state) {
        this.state = state;
        GlobalTimer.getTimer().schedule(new TimerTask() {
            public void run() {
                redraw();
            }
        }, 1000, 1000);
    }

    protected void draw(ANSIContext context) throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append("Est. progress: ");
        int total = state.getTotal();
        int crt = state.getCompleted();
        if (total != 0) {
            sb.append(crt * 100 / total);
            sb.append("%");
        }
        else {
            sb.append("N/A");
        }
        sb.append("   Elapsed time: ");
        sb.append(format(System.currentTimeMillis() - state.getStart()));
        sb.append("   Est. time left: ");
        if (total != 0 && crt != 0) {
            sb.append(et(state.getCompleted(), total, state.getStart()));
        }
        else {
            sb.append("N/A");
        }
        int p = 0;
        if (total != 0) {
            p = crt * width / total;
        }
        String s = sb.toString();
        int pad = (width - s.length()) / 2;
        s = spaces(pad) + s + spaces(width - pad - s.length());
        context.moveTo(sx, sy);
        context.bgColor(ANSI.RED);
        context.fgColor(ANSI.WHITE);
        context.text(s.substring(0, p));
        context.bgColor(ANSI.WHITE);
        context.fgColor(ANSI.BLACK);
        context.text(s.substring(p));
        
    }

    private String et(int crt, int total, long start) {
        long time = System.currentTimeMillis() - start;
        long et = (time * total / crt) - time;
        return format(et);
    }

    private String format(long v) {
        v = v / 1000;
        StringBuffer sb = new StringBuffer();
        int h = (int) (v / 3600);
        if (h < 10) {
            sb.append('0');
        }
        sb.append(v / 3600);
        sb.append(':');
        int m = (int) ((v % 3600) / 60);
        if (m < 10) {
            sb.append('0');
        }
        sb.append(m);
        sb.append(':');
        int s = (int) (v % 60);
        if (s < 10) {
            sb.append('0');
        }
        sb.append(s);
        return sb.toString();
    }
    
    private String spaces(int count) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < count; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
