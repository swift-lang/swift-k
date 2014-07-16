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
 * Created on Jan 30, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

public class ANSI {
	public static final char ESC = 27;
	public static final String AESC = ESC + "[";
	public static final String CSI = AESC;

	public static final int BLACK = 0;
	public static final int RED = 1;
	public static final int GREEN = 2;
	public static final int YELLOW = 3;
	public static final int BLUE = 4;
	public static final int MAGENTA = 5;
	public static final int CYAN = 6;
	public static final int WHITE = 7;
	public static final int DEFAULT = 9;

	/*
	 * UL UM UR
	 * ML CR MR
	 * LL LM LR
	 */
	public static final int GCH_LR_CORNER = 106;
	public static final int GCH_UR_CORNER = 107;
	public static final int GCH_UL_CORNER = 108;
	public static final int GCH_LL_CORNER = 109;
	public static final int GCH_CROSS = 110;
	public static final int GCH_H_LINE = 113;
	public static final int GCH_V_LINE = 120;
	public static final int GCH_ML_CORNER = 116;
	public static final int GCH_MR_CORNER = 117;
	public static final int GCH_LM_CORNER = 118;
	public static final int GCH_UM_CORNER = 119;
	public static final int GCH_HASH = 97;
	
	public static final int GCH_BULLET = 96;
	public static final int GCH_ARROW_UP = 94;
	public static final int GCH_ARROW_DOWN = 95;
	public static final int GCH_ARROW_LEFT = 60;
	public static final int GCH_ARROW_RIGHT = 62;

	public static String moveTo(int x, int y) {
		return AESC + y + ';' + x + 'H';
	}

	public static String moveUp(int lines) {
		return AESC + lines + 'A';
	}

	public static String moveDown(int lines) {
		return AESC + lines + 'B';
	}

	public static String moveRight(int rows) {
		return AESC + rows + 'C';
	}

	public static String moveLeft(int rows) {
		return AESC + rows + 'D';
	}

	public static String clear() {
		return AESC + "2J";
	}

	public static String clearLine() {
		return AESC + 'K';
	}

	public static String fgColor(int color) {
		return AESC + (30 + color) + 'm';
	}

	public static String bgColor(int color) {
		return AESC + (40 + color) + 'm';
	}

	public static String underline(boolean underline) {
		if (underline) {
			return AESC + 4 + 'm';
		}
		else {
			return AESC + 24 + 'm';
		}
	}

	public static String bold(boolean bold) {
		if (bold) {
			return AESC + 1 + 'm';
		}
		else {
			return AESC + 22 + 'm';
		}
	}

	public static String cursorVisible(boolean visible) {
		if (visible) {
			return AESC + '?' + 25 + 'h';
		}
		else {
			return AESC + '?' + 25 + 'l';
		}
	}
	
	public static String reset() {
	    return AESC + "!p";
	}

	public static String lineArt(boolean la) {
		if (la) {
			return "\033(0";
		}
		else {
			return "\033(B";
		}

	}

	public static String message(String msg) {
		return moveTo(0, 0) + bgColor(RED) + fgColor(YELLOW) + msg;
	}
}
