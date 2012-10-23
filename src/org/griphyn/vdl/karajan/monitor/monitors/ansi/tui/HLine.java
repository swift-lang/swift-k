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

public class HLine extends Component {
    
    private boolean leftEndCap, rightEndCap;
    
    public HLine(boolean leftEndCap, boolean rightEndCap) {
        this.leftEndCap = leftEndCap;
        this.rightEndCap = rightEndCap;
    }
    
    public HLine(boolean endCaps) {
        this(endCaps, endCaps);
    }
    
    public HLine() {
        this(false);
    }

	protected void draw(ANSIContext context) throws IOException {
		context.lock();
		try {
			context.bgColor(bgColor);
			context.fgColor(fgColor);
			context.moveTo(sx, sy);
			for (int i = 0; i < width; i++) {
				context.lineArt(ANSI.GCH_H_LINE);
			}
			if (leftEndCap) {
			    context.moveTo(sx - 1, sy);
			    context.lineArt(ANSI.GCH_ML_CORNER);
			}
			if (rightEndCap) {
			    context.moveTo(sx + width, sy);
			    context.lineArt(ANSI.GCH_MR_CORNER);
			}
		}
		finally {
			context.unlock();
		}
	}
}
