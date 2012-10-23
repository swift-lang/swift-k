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


/*
 * Created on Feb 21, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.IOException;

public class VLine extends Component {
    
    private boolean topEndCap, bottomEndCap;
    
    public VLine(boolean topEndCap, boolean bottomEndCap) {
        this.topEndCap = topEndCap;
        this.bottomEndCap = bottomEndCap;
    }
    
    public VLine(boolean endCaps) {
        this(endCaps, endCaps);
    }
    
    public VLine() {
        this(false);
    }

	protected void draw(ANSIContext context) throws IOException {
		context.bgColor(bgColor);
		context.fgColor(fgColor);
		for (int i = 0; i < height; i++) {
			context.moveTo(sx, sy + i);
			context.lineArt(ANSI.GCH_V_LINE);
		}
		if (topEndCap) {
            context.moveTo(sx, sy - 1);
            context.lineArt(ANSI.GCH_UM_CORNER);
        }
        if (bottomEndCap) {
            context.moveTo(sx, sy + height);
            context.lineArt(ANSI.GCH_LM_CORNER);
        }
	}
}
