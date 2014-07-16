/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.griphyn.vdl.karajan.monitor.monitors.ansi;

import java.io.IOException;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.common.GlobalTimer;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSI;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.ANSIContext;
import org.griphyn.vdl.karajan.monitor.monitors.ansi.tui.Component;

public class GlobalProgress extends Component {
    private SystemState state;
    private long start;

    public GlobalProgress(SystemState state) {
        this.state = state;
        this.start = System.currentTimeMillis();
        GlobalTimer.getTimer().schedule(new SafeTimerTask(getScreen()) {
            public void runTask() {
                redraw();
            }
        }, 1000, 1000);
    }

    protected void draw(ANSIContext context) throws IOException {
        int total = state.getTotal();
        int crt = state.getCompleted();
        String s = state.getGlobalProgressString();
        
        int p = 0;
        if (total != 0) {
            p = crt * width / total;
        }

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

       
    private String spaces(int count) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < count; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
