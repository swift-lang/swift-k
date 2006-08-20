// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 24, 2004
 */
package org.globus.cog.gui.grapheditor.util;

import java.awt.Rectangle;

public class RectUtil {

	/**
	 * "Normalizes" the rectangle. A "normalized" rectangle would have a
	 * positive width and height
	 */
	public static Rectangle abs(Rectangle rect) {
		if (rect.width < 0) {
			rect.x += rect.width;
			rect.width = -rect.width;
		}
		if (rect.height < 0) {
			rect.y += rect.height;
			rect.height = -rect.height;
		}
		return rect;
	}

	/**
	 * Grows a border around a rectangle
	 */
	public static Rectangle border(Rectangle rect, int border) {
		rect.x -= sgn(rect.width) * border;
		rect.y -= sgn(rect.height) * border;
		rect.width += sgn(rect.width) * border * 2;
		rect.height += sgn(rect.height) * border * 2;
		return rect;
	}

	/**
	 * Grows the rectangle in such a way that it will contain the point (x, y)
	 */
	public static Rectangle grow(Rectangle rect, int x, int y) {
		if ((x - rect.x) * sgn(rect.width) < 0) {
			rect.width -= (x - rect.x);
			rect.x = x;
		}
		else if ((x - (rect.x + rect.width)) * sgn(rect.width) > 0) {
			rect.width = x - rect.x;
		}
		if ((y - rect.y) * sgn(rect.height) < 0) {
			rect.height -= (y - rect.y);
			rect.y = y;
		}
		else if ((y - (rect.y + rect.height)) * sgn(rect.height) > 0) {
			rect.height = y - rect.y;
		}
		return rect;
	}

	private static int sgn(int x) {
		if (x >= 0) {
			return 1;
		}
		else {
			return -1;
		}
	}
}