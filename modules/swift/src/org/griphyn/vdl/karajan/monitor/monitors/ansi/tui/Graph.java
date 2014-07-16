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


package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

public class Graph extends Component {
    private LinkedList<Double> data;

    public Graph() {
        data = new LinkedList<Double>();
    }

    public void push(double value) {
        data.addLast(Double.valueOf(value));
        if (data.size() >= getWidth() - 2) {
            data.removeFirst();
        }
    }

    protected void draw(ANSIContext context) throws IOException {
        double max = 1;
        double min = 0;
        Iterator<Double> i = data.iterator();
        while (i.hasNext()) {
            Double d = i.next();
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
        while (i.hasNext()) {
            Double d = i.next();
            double v = (d.doubleValue() - min) / (max - min) * (height - 3);
            int c = (int) v;
            if (last == Integer.MIN_VALUE || last == c) {
                context.moveTo(j, sy + height - c - 2);
                context.lineArt(ANSI.GCH_H_LINE);
            }
            else if (last > c) {
                for (int k = c; k <= last; k ++) {
                    context.moveTo(j, sy + height - k - 2);
                    if (k == last) {
                        context.lineArt(ANSI.GCH_UR_CORNER);
                    }
                    else {
                        context.lineArt(ANSI.GCH_V_LINE);
                    }
                }
                context.moveTo(j, sy + height - c - 2);
                context.lineArt(ANSI.GCH_LL_CORNER);
            }
            else {
                for (int k = last; k < c; k ++) {
                    context.moveTo(j, sy + height - k - 2);
                    if (k == last) {
                        context.lineArt(ANSI.GCH_LR_CORNER);
                    }
                    else {
                        context.lineArt(ANSI.GCH_V_LINE);
                    }
                }
                context.moveTo(j, sy + height - c - 2);
                context.lineArt(ANSI.GCH_UL_CORNER);
            }
            last = c;
            j++;
        }
        context.moveTo(sx, sy + 1);
        context.lineArt(ANSI.GCH_CROSS);
        context.text(String.valueOf(max));
        context.moveTo(sx, sy + height - 2);
        context.lineArt(ANSI.GCH_CROSS);
        context.text(String.valueOf(min));
    }
}
  
