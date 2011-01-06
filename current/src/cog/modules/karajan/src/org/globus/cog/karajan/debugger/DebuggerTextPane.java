// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 18, 2005
 */
package org.globus.cog.karajan.debugger;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

public class DebuggerTextPane extends JTextPane {
	private static final long serialVersionUID = -5812784078108171036L;

	private Map highlights;

	private static TabSet tabSet;
	private static SimpleAttributeSet style;
	private static final Font font = Font.decode("monospaced-PLAIN-10");
	public static int TAB_SIZE = 4;

	static {
		TabStop[] ts = new TabStop[64];
		for (int i = 0; i < 64; i++) {
			ts[i] = new TabStop((int) (i * TAB_SIZE * 6));
		}
		tabSet = new TabSet(ts);
		style = new SimpleAttributeSet();
		StyleConstants.setTabSet(style, tabSet);
	}

	public DebuggerTextPane() {
		highlights = new Hashtable();
		setFont(font);
		setEditable(false);
		setParagraphAttributes(style, false);

	}

	public void addLineHighlight(int line, Color color) {
		Integer iline = new Integer(line);
		Set hl = (Set) highlights.get(iline);
		if (hl == null) {
			hl = new HashSet();
			highlights.put(iline, hl);
		}
		hl.add(color);
		repaint();
	}

	public void removeLineHighlight(int line, Color color) {
		Integer iline = new Integer(line);
		Set hl = (Set) highlights.get(iline);
		if (hl != null) {
			hl.remove(color);
			if (hl.isEmpty()) {
				highlights.remove(iline);
			}
		}
		repaint();
	}

	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
		super.paint(g);
		FontMetrics fm = g2.getFontMetrics();
		Iterator i = new ArrayList(highlights.keySet()).iterator();
		while (i.hasNext()) {
			Integer iline = (Integer) i.next();
			int line = iline.intValue();
			Set hl = (Set) highlights.get(iline);
			if (hl == null) {
				continue;
			}
			int cr = 0, cg = 0, cb = 0, count = 0;
			Iterator j = hl.iterator();
			while (j.hasNext()) {
				Color c = (Color) j.next();
				cr += c.getRed();
				cg += c.getGreen();
				cb += c.getBlue();
				count++;
			}
			Graphics2D c = (Graphics2D) g2.create();
			Rectangle rect = getLineBounds(line);
			if (rect != BADLOC && rect != null) {
				c.translate(0, rect.getY());
				Highlighter.paint(c, new Color(cr / count, cg / count, cb / count), getWidth(),
						fm.getDescent() + fm.getAscent());
			}
		}
	}

	public static final Rectangle BADLOC = new Rectangle(0, 0, 1, 1);

	public Rectangle getLineBounds(int line) {
		Integer iel = (Integer) lines.get(new Integer(line));
		if (iel == null) {
			return BADLOC;
		}
		try {
			return modelToView(iel.intValue());
		}
		catch (BadLocationException e) {
			return BADLOC;
		}
	}

	public int getLine(int y) {
		int el = this.viewToModel(new Point(0, y));
		if (!countsValid) {
			buildCounts();
		}
		return search(0, counts.length, el) + 1;
	}

	private int search(int begin, int end, int value) {
		if (begin == end) {
			return counts[begin];
		}
		int mid = (begin + end) / 2;
		int midval = counts[mid];
		if (value == midval) {
			return mid;
		}
		if (value > midval) {
			return search(mid, end, value);
		}
		else {
			return search(begin, mid, value);
		}
	}

	private void buildCounts() {
		counts = new int[lines.size()];
		for (int i = 0; i < counts.length; i++) {
			Integer el = (Integer) lines.get(new Integer(i));
			if (el != null) {
				counts[i] = el.intValue();
			}
		}
	}

	private Map lines = new Hashtable();
	private int[] counts;
	private boolean countsValid;

	public void addLineMapping(int element, int line) {
		lines.put(new Integer(line), new Integer(element));
		countsValid = false;
	}

	public boolean getScrollableTracksViewportWidth() {
		return true;
	}
}
