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
import java.util.ArrayList;
import java.util.List;

public class TextArea extends Container {
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int CENTER = 2;

	private int justification;
	private String text;
	private List<String> lines;
	private VScrollbar sb;
	private int top;
	private boolean scrollBarVisible;

	public TextArea(String text) {
		this.text = text;
	}

	public TextArea() {
		this(null);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		invalidate();
	}

	private void updateLines() {
		lines = new ArrayList<String>();
		if (text == null) {
			return;
		}
		text = text.replace("\t", "    ");
		int crt = 0;
		int w = width - 1;
		while (crt < text.length()) {
			int end = Math.min(crt + w, text.length());
			int nlindex = text.indexOf('\n', crt);
			while (nlindex >= 0 && nlindex < end) {
				lines.add(text.substring(crt, nlindex));
				crt = nlindex + 1;
				end = Math.min(crt + w, text.length());
				nlindex = text.indexOf('\n', crt);
			}
			if (crt < end) {
				lines.add(text.substring(crt, end));
			}
			crt += w;
		}
	}

	public int getJustification() {
		return justification;
	}

	public void setJustification(int justification) {
		this.justification = justification;
	}

	protected void draw(ANSIContext context) throws IOException {
		context.lock();
		try {
			context.bgColor(bgColor);
			context.fgColor(fgColor);
			context.filledRect(sx, sy, width, height);
			for (int i = top; i < Math.min(top + height, lines.size()); i++) {
				context.moveTo(sx, sy + i - top);
				context.text(lines.get(i));
			}
		}
		finally {
			context.unlock();
		}
	}

	protected void validate() {
		updateLines();
		bgColor = getParent().getBgColor();
		fgColor = getParent().getFgColor();
		if (sb != null) {
			sb.setLocation(width - 1, 0);
			sb.setSize(1, height);
			sb.setBgColor(bgColor);
			sb.setFgColor(fgColor);
		}
		super.validate();
	}

	public boolean isScrollBarVisible() {
		return scrollBarVisible;
	}

	public void setScrollBarVisible(boolean scrollBarVisible) {
		if (this.scrollBarVisible != scrollBarVisible) {
			if (this.scrollBarVisible) {
				sb = new VScrollbar();
				add(sb);
			}
			else {
				remove(sb);
			}
		}
		this.scrollBarVisible = scrollBarVisible;
	}
}
