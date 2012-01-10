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

public class VScrollbar extends Component {
	private int total, current;

	protected void draw(ANSIContext context) throws IOException {
		context.bgColor(bgColor);
		context.fgColor(fgColor);
		context.lineArt(true);
		for (int i = 0; i < height; i++) {
			context.moveTo(sx, sy + i);
			context.putChar(ANSI.GCH_HASH);
		}
		int pos = 0;
		if (total > 1) {
			if (current > total) {
				current = total;
			}
			if (current < 0) {
				current = 0;
			}
			pos = (height - 1) * current / (total - 1);
		}
		context.bgColor(fgColor);
		context.fgColor(bgColor);
		context.moveTo(sx, sy + pos);
		context.putChar(' ');
		context.lineArt(false);
	}

	public int getCurrent() {
		return current;
	}

	public void setCurrent(int current) {
		this.current = current;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

}
