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
 * Created on Jan 31, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.IOException;

import org.apache.log4j.Logger;

public class Screen extends LayeredContainer {
    public static final Logger logger = Logger.getLogger(Screen.class);
    
	private ANSIContext context;
	private String status;
    private boolean redraw;

	public Screen(ANSIContext context) {
		this.context = context;
		this.bgColor = ANSI.BLACK;
		this.fgColor = ANSI.WHITE;
	}

	public boolean init() throws IOException {
	    if (!context.init()) {
	        logger.info("Terminal does not support ANSI escape sequences!");
	        return false;
	    }
		int[] size = context.querySize();
		if (size == null) {
		    size = new int[] {80, 25};
		}
		setSize(size[0], size[1]);
		context.setScreen(this);
		context.clear();
		context.println("w: " + width + ", h: " + height);
		context.echo(false);
		sx = 1;
		sy = 1;
		return true;
	}

	public void redraw() {
		context.lock();
		try {
			context.bgColor(bgColor);
			context.fgColor(fgColor);
			drawTree(context);
			if (status != null) {
				context.moveTo(1, height);
				context.bgColor(ANSI.RED);
				context.fgColor(ANSI.YELLOW);
				context.text(status);
			}
			context.sync();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			context.unlock();
		}
	}

	public Screen getScreen() {
		return this;
	}

	public ANSIContext getContext() {
		return context;
	}

	public void status(String msg) {
		this.status = msg;
	}

	protected boolean isBranchVisible() {
		return true;
	}

	public boolean focus() {
		return true;
	}

    public void redrawLater() {
        context.redrawLater();
    }
}
