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
