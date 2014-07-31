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
 * Created on Sep 24, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

public class ScreenBuffer {
	public static final int ATTR_MASK_UNDERLINE = 0x01000000;
	public static final int ATTR_MASK_BOLD = 0x02000000;
	public static final int ATTR_MASK_LINE_ART = 0x04000000;
	public static final int ATTR_MASK_FG_COLOR = 0x000f0000;
	public static final int ATTR_MASK_BG_COLOR = 0x00f00000;
	
	// every 10 seconds send the full buffer in case the terminal
	// garbles up parts of the screen
	public static final int FULL_SYNC_INTERVAL = 10000;
	
	private int width, height;
	private int[] actual;
	private int[] buf, tmp;
	private ANSIContext context;
	private int fgColor, bgColor, pos;
	private boolean underline, bold;
	private boolean lineArt;
	private CountingWriter os;
	private int lastattr;
	private long lastFullSyncTime;

	public ScreenBuffer(ANSIContext context, int width, int height) {
		this.context = context;
		this.os = new CountingWriter(context.getOutputStream());
		resize(width, height);
		lastattr = -1;
	}

	private int encode(char c) {
		return c + (fgColor << 16) + (bgColor << 20) + (underline ? ATTR_MASK_UNDERLINE : 0)
				+ (bold ? ATTR_MASK_BOLD : 0) + (lineArt ? ATTR_MASK_LINE_ART : 0);
	}

	private int attrs() {
		return (fgColor << 16) + (bgColor << 20) + (underline ? ATTR_MASK_UNDERLINE : 0)
				+ (bold ? ATTR_MASK_BOLD : 0) + (lineArt ? ATTR_MASK_LINE_ART : 0);
	}

	public void moveTo(int x, int y) {
		pos = (y - 1) * width + (x - 1);
	}

	public void clear() {
		int value = ' ' + attrs();
		Arrays.fill(buf, value);
	}

	public void fgColor(int color) {
		this.fgColor = color;
	}

	public void bgColor(int color) {
		this.bgColor = color;
	}

	public void bold(boolean bold) {
		this.bold = bold;
	}

	public void underline(boolean underline) {
		this.underline = underline;
	}

	public void write(String text) {
		int attrs = attrs();
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\n') {
				pos = ((pos / width) + 1) * width;
			}
			else if (c == '\t') {
			    pos = pos + 4;
			}
			else {
			    if (i + pos < buf.length) {
			        buf[i + pos] = text.charAt(i) + attrs;
			    }
			}
		}
		pos += text.length();
	}

	public void lineArt(boolean enabled) {
		this.lineArt = enabled;
	}
	
	private void setAttrs(int attrs) throws IOException {
		os.write(ANSI.underline((attrs & ATTR_MASK_UNDERLINE) != 0));
		os.write(ANSI.bold((attrs & ATTR_MASK_BOLD) != 0));
		os.write(ANSI.lineArt((attrs & ATTR_MASK_LINE_ART) != 0));
		os.write(ANSI.fgColor((attrs & ATTR_MASK_FG_COLOR) >> 16));
		os.write(ANSI.bgColor((attrs & ATTR_MASK_BG_COLOR) >> 20));
	}
	
	private void updateAttr(int dest, int actual) throws IOException {
		int diff = dest ^ actual;
		if ((diff & ATTR_MASK_UNDERLINE) != 0) {
			os.write(ANSI.underline((dest & ATTR_MASK_UNDERLINE) != 0));
		}
		if ((diff & ATTR_MASK_BOLD) != 0) {
			os.write(ANSI.bold((dest & ATTR_MASK_BOLD) != 0));
		}
		if ((diff & ATTR_MASK_LINE_ART) != 0) {
			os.write(ANSI.lineArt((dest & ATTR_MASK_LINE_ART) != 0));
		}
		if ((diff & ATTR_MASK_FG_COLOR) != 0) {
			os.write(ANSI.fgColor((dest & ATTR_MASK_FG_COLOR) >> 16));
		}
		if ((diff & ATTR_MASK_BG_COLOR) != 0) {
			os.write(ANSI.bgColor((dest & ATTR_MASK_BG_COLOR) >> 20));
		}
	}

	public synchronized void sync() throws IOException {
		System.arraycopy(buf, 0, tmp, 0, buf.length);
		os.reset();
		int p = 0;
		int last = -2;
		if (lastattr == -1) {
			lastattr = attrs();
			setAttrs(lastattr);
		}
		
		boolean fullSync = false;
		long now = System.currentTimeMillis();
		if (now - this.lastFullSyncTime > FULL_SYNC_INTERVAL) {
		    this.lastFullSyncTime = now;
		    fullSync = true;
		}
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int b = tmp[p];
				if ((b != actual[p]) || fullSync) {
					int attr = b & 0xffff0000;
					updateAttr(attr, lastattr);
					lastattr = attr;
					if (p - last > 1) {
						os.write(ANSI.moveTo(x + 1, y + 1));
					}
					last = p;
					os.write(b & 0x0000ffff);
				}
				p++;
			}
		}
		os.flush();
		System.arraycopy(buf, 0, actual, 0, buf.length);
	}

	public void spaces(int count) {
		if (count < 0) {
			return;
		}
		if (pos >= buf.length) {
			return;
		}
		else if (pos + count >= buf.length) {
			count -= buf.length - pos;
		}
		int attrs = attrs();
		try {
			Arrays.fill(buf, pos, pos + count, ' ' + attrs);
		}
		catch (IndexOutOfBoundsException e) {
		}
		pos += count;
	}

	public void write(char c) {
		write((int) c);
	}

	public void write(int c) {
		try {
			buf[pos++] = (c & 0x0000ffff) + attrs();
		}
		catch (IndexOutOfBoundsException e) {			
		}
	}
	
	public char getChar() {
        return (char) (buf[pos++] & 0x0000ffff);
    }

	public void writeln(String string) {
		write(string);
		write('\n');
	}

	public void invalidate() {
		Arrays.fill(actual, 0);
	}
	
	public static class CountingWriter extends Writer {
		private Writer w;
		private int count;
		
		public void reset() {
			count = 0;
		}
		
		public int get() {
			return count;
		}
		
		public CountingWriter(Writer w) {
			this.w = w;
		}

		public void close() throws IOException {
			w.close();
		}

		public boolean equals(Object obj) {
			return w.equals(obj);
		}

		public void flush() throws IOException {
			w.flush();
		}

		public int hashCode() {
			return w.hashCode();
		}

		public String toString() {
			return w.toString();
		}

		public void write(char[] cbuf, int off, int len) throws IOException {
			count += len;
			w.write(cbuf, off, len);
		}

		public void write(char[] cbuf) throws IOException {
			count += cbuf.length;
			w.write(cbuf);
		}

		public void write(int c) throws IOException {
			count++;
			w.write(c);
		}

		public void write(String str, int off, int len) throws IOException {
			count += len;
			w.write(str, off, len);
		}

		public void write(String str) throws IOException {
			count += str.length();
			w.write(str);
		}	
	}

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        actual = new int[width * height];
        buf = new int[width * height];
        tmp = new int[width * height];
    }
}
