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
	        return false;
	    }
		int[] size = context.querySize();
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
