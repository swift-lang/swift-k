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
        int r = range;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > r) {
                r = values[i];
            }
        }
        for (int i = 0; i < values.length; i++) {
            if (r > 0) {
                getBar(i).setValue((float) values[i] / r);
            }
            else {
                getBar(i).setValue(0);
            }
        }
    }
    
    public void setRange(int range) {
        this.range = range;
    }
    
    public void setOtherValue(int i, int ov) {
        if (range != 0) {
            getBar(i).setOtherValue((float) ov / range);
        }
        else {
            getBar(i).setOtherValue(0);
        }
    }
    
    public void setText(int index, String text) {
        getBar(index).setText(text);
    }

    private LevelBar getBar(int i) {
        return (LevelBar) getComponents().get(i);
    }

    protected void validate() {
        Iterator<Component> i = getComponents().iterator();
        int j = 0;
        while (i.hasNext()) {
            i.next().setSize(width, 1);
        }
        super.validate();
    }
}
