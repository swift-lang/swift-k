// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gui.grapheditor.targets.swing.util;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.PaintEvent;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.util.swing.EventTrappingContainer;
import org.globus.cog.gui.grapheditor.util.swing.ExtendedMouseEvent;
import org.globus.cog.gui.grapheditor.util.swing.MouseEventDispatcher;
import org.globus.cog.gui.grapheditor.util.swing.RepaintListener;
import org.globus.cog.gui.grapheditor.util.swing.RepaintMonitoringContainer;

public class ScalingContainer extends EventTrappingContainer implements ActionListener,
		RepaintListener, MouseListener, MouseMotionListener, PainterListener {

	private static Logger logger = Logger.getLogger(ScalingContainer.class);
	
	protected static final int SWING_SCALING_PAINTER = 0;

	private Timer timer;

	protected static int titleHeight = 14;

	private volatile boolean antiAliasing;

	private BufferedImage[] buffers = new BufferedImage[2];

	private int currentBuffer = 0;

	private BufferedImage buffer, swapBuffer, lastDraw;

	private int cx1, cy1, cx2, cy2;

	private ImageIcon icon;

	private boolean keepAspectRatio;

	private int maxx = Integer.MIN_VALUE, maxy = Integer.MIN_VALUE;

	private int minx = Integer.MAX_VALUE, miny = Integer.MAX_VALUE;

	private boolean outline;

	private double sx, sy;

	private String title;

	private int updateInterval = 50;

	private Object paintLock;

	private boolean dirty;

	protected ScalingPainter painter;

	private List painterListeners;

	private boolean immediate;

	private Component dragged;

	private int xcursor, ycursor;

	private Rectangle dirtyArea;

	public ScalingContainer(Component comp) {
		super(comp);
		painterListeners = new LinkedList();
		setTitle("-");
		antiAliasing = true;
		keepAspectRatio = false;
		dirty = true;
		enableMouseEvents();
		timer = new Timer(true);
		timer.schedule(new Painter(this), 200, 50);
	}

	public void addPainterListener(PainterListener l) {
		if (!painterListeners.contains(l)) {
			painterListeners.add(l);
		}
	}

	public void removePainterListener(PainterListener l) {
		painterListeners.remove(l);
	}

	public void fireBufferUpdated(ScalingPainter source) {
		for (int i = 0; i < painterListeners.size(); i++) {
			((PainterListener) painterListeners.get(i)).bufferUpdated(source);
		}
	}

	public void firePaintCompleted(ScalingPainter source) {
		Iterator i = painterListeners.iterator();
		while (i.hasNext()) {
			((PainterListener) i.next()).paintCompleted(source);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (getComponent() == null) {
			return;
		}
		if (!dirty || ((painter != null) &&(painter.isPainting()))) {
			return;
		}
		if (paintLock != null) {
			synchronized (paintLock) {
				paintBuffer();
			}
		}
		else {
			paintBuffer();
		}
		synchronized (this) {
			dirtyArea = null;
			dirty = false;
		}
	}

	public Dimension computeSize() {
		minx = Integer.MAX_VALUE;
		miny = Integer.MAX_VALUE;
		maxx = Integer.MIN_VALUE;
		maxy = Integer.MIN_VALUE;
		computeSize(getComponent(), 0, 0);
		return new Dimension(maxx - minx, maxy - miny);
	}

	public void computeSize(Component c, int tx, int ty) {
		if (!c.isVisible()) {
			return;
		}
		int ax = c.getX() + tx, ay = c.getY() + ty;
		if ((c instanceof Container) && !(c instanceof ScalingContainer)
				&& !(c instanceof GraphComponentWrapper)) {
			Container cc = (Container) c;
			Component[] cs = cc.getComponents();
			for (int i = 0; i < cs.length; i++) {
				computeSize(cs[i], ax, ay);
			}
		}

		if (!(c instanceof GraphComponentWrapper)) {
			return;
		}

		int bx = ax + c.getWidth(), by = ay + c.getHeight();
		if (ax < minx) {
			minx = ax;
		}
		if (ay < miny) {
			miny = ay;
		}
		if (bx > maxx) {
			maxx = bx;
		}
		if (by > maxy) {
			maxy = by;
		}
	}

	public void dispose() {
		timer.cancel();
		clear();
		setComponent(null);
	}

	public void doPaintBuffer() {
		if (!getComponent().isValid()) {
			validateTree(getComponent());
		}
		Dimension s = computeSize();
		Dimension d = getSize();
		BufferedImage newBuffer = null;
		boolean frameDrawn = false;
		if ((d.width <= 0) || (d.height <= 0)) {
			dirty = false;
			logger.debug("Zero or less sized container");
			return;
		}
		newBuffer = buffers[currentBuffer];
		if ((newBuffer == null) || (newBuffer.getWidth() != d.width)
				|| (newBuffer.getHeight() != d.height)) {
			newBuffer = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
			buffers[currentBuffer] = newBuffer;
		}
		else {
			frameDrawn = true;
		}
		currentBuffer = 1 - currentBuffer;
		Graphics2D g = newBuffer.createGraphics();
		if (antiAliasing) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setBackground(getBackground());
		g.setColor(Color.black);
		g.clearRect(1, titleHeight + 2, d.width - 2, d.height - titleHeight - 3);
		if (!frameDrawn) {
			g.drawRect(0, 0, d.width - 1, d.height - 1);
			g.drawRect(0, 0, d.width - 1, titleHeight + 1);
			g.setColor(Color.lightGray);
			g.fillRect(1, 1, d.width - 3, titleHeight);
			int buttonSize = titleHeight - 2;
			g.fill3DRect(d.width - buttonSize - 4, 2, buttonSize, buttonSize, true);
			g.setColor(Color.BLACK);
			g.drawRect(d.width - buttonSize - 2, 1 + buttonSize / 2, buttonSize - 5, 1);
			int xorg = 1;
			if (icon != null) {
				g.drawImage(icon.getImage(), 1, 1, titleHeight, titleHeight, 0, 0, icon
						.getIconWidth(), icon.getIconHeight(), null);
				xorg = titleHeight;
			}
			if (title != null) {
				g.setColor(Color.black);
				g.setFont(Font.decode("Lucida Sans Regular-PLAIN-10"));
				g.drawString(title, 3 + xorg, 13);
			}
		}
		if ((s.width > 0) && (s.height > 0)) {
			if ((maxx > minx) && (maxy > miny)) {
				double sx = s.width / d.width;
				double sc = Math.max(sx, s.height / d.height);
				if (sc > 4) {
					setOutline(true);
				}
				long size = 4 * s.width * s.height;
				long freeMem = Runtime.getRuntime().maxMemory()
						- Runtime.getRuntime().totalMemory();
				if ((painter != null) && (painter.isPainting())) {
					logger.warn("Already painting");
				}
				if ((freeMem / 4 > size) && !outline) {
					paintNormal();
				}
				else {
					paintOutline();
				}
			}
			else {
				logger.debug("Odd sized thing");
			}
		}
		else {
			logger.debug("Zero sized thing");
			g.setClip(1, titleHeight + 2, d.width - 2, d.width);
			g.drawString("Unsupported view", 2, 2);
		}
		if (buffer == null) {
			buffer = newBuffer;
			super.repaint();
		}
		swapBuffer = newBuffer;
	}

	public void paintNormal() {
		logger.debug("Painting normal");
		ScalingPainter painter = getPainter(SWING_SCALING_PAINTER);
		Rectangle bounds = new Rectangle(minx, miny, maxx - minx, maxy - miny);
		Dimension d = getSize();
		if ((dirtyArea != null) && (lastDraw != null) && (lastDraw.getHeight() == bounds.height)
				&& (lastDraw.getWidth() == bounds.width)) {
			painter.setPaintArea(dirtyArea);
			painter.setBuffer(lastDraw);
		}
		painter.setBounds(bounds);
		painter.setBufferDimension(d);
		wakeCurrentPainter();
	}
	

	public void paintOutline() {
		//TODO
	}
	
	protected ScalingPainter getPainter(int type) {
		if (type == SWING_SCALING_PAINTER) {
			if (!(painter instanceof SwingScalingPainter)) {
				destroyCurrentPainter();
				painter = new SwingScalingPainter(getComponent(), Color.WHITE);
				painter.setPainterListener(this);
				new Thread(painter).start();
			}
			return painter;
		}
		throw new RuntimeException("Could not create painter");
	}
	
	protected synchronized void destroyCurrentPainter() {
		if (painter != null) {
			painter.destroy();
			painter.wake();
			painter = null;
		}
	}
	
	protected synchronized void wakeCurrentPainter() {
		painter.wake();
	}

	public void paintCompleted(ScalingPainter p) {
		buffer = swapBuffer;
		bufferUpdated(p);
		super.repaint();
		firePaintCompleted(p);
	}

	public void bufferUpdated(ScalingPainter p) {
		BufferedImage bi = p.getBuffer();
		lastDraw = bi;
		Dimension d = getSize();
		if (swapBuffer == null) {
			return;
		}
		Graphics2D g = swapBuffer.createGraphics();
		if (antiAliasing) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		if (bi != null) {
			int targetWidth = d.width - 2;
			int targetHeight = d.height - titleHeight - 3;
			int sourceWidth = bi.getWidth();
			int sourceHeight = bi.getHeight();
			double sx = (double) targetWidth / sourceWidth;
			double sy = (double) targetHeight / sourceHeight;
			double scale = (sx < sy) ? sx : sy;
			sx = scale;
			sy = scale;
			int xSpace = (targetWidth - (int) (sx * sourceWidth)) / 2;
			int ySpace = (targetHeight - (int) (sy * sourceHeight)) / 2;
			cx1 = xSpace + 1;
			cx2 = d.width - xSpace - 1;
			cy1 = ySpace + titleHeight + 2;
			cy2 = d.height - ySpace - 1;
			Graphics2D g2 = bi.createGraphics();
			g2.setColor(Color.BLACK);
			g2.drawLine(xcursor, ycursor - 7, xcursor, ycursor + 7);
			g2.drawLine(xcursor - 7, ycursor, xcursor + 7, ycursor);
			g.drawImage(bi, cx1, cy1, cx2, cy2, 0, 0, sourceWidth, sourceHeight, null);
		}
		if (buffer == swapBuffer) {
			super.repaint();
		}
		fireBufferUpdated(p);
	}

	public Dimension getBufferSize() {
		Dimension d = getSize();
		int targetWidth = d.width - 2;
		int targetHeight = d.height - titleHeight - 3;
		return new Dimension(targetWidth, targetHeight);
	}

	private CP findComponent(MouseEvent e) {
		if ((e.getX() >= cx1) && (e.getY() >= cy1) && (e.getX() <= cx2) && (e.getY() <= cy2)) {
			if ((sx > 0) && (sy > 0)) {
				double tx = (e.getX() - cx1) * (maxx - minx) / (cx2 - cx1);
				double ty = (e.getY() - cy1) * (maxy - miny) / (cy2 - cy1);

				int cx = (int) tx;
				int cy = (int) ty;
				//TODO something's wrong here
				int lcx = cx;
				int lcy = cy;
				Component lc = getComponent();
				Component c = getComponent().getComponentAt(cx, cy);
				while (c != null) {
					lc = c;
					cx = cx - c.getX();
					cy = cy - c.getY();
					c = c.getComponentAt(cx, cy);
					if (c == null) {
						break;
					}
					if (c == lc) {
						break;
					}
					lcx = cx;
					lcy = cy;
				}
				return new CP(lc, cx, cy);
			}
		}
		return null;
	}

	public boolean getAntiAliasing() {
		return antiAliasing;
	}

	public ImageIcon getIcon() {
		return icon;
	}

	public boolean getKeepAspectRatio() {
		return keepAspectRatio;
	}

	public int getMaxx() {
		return maxx;
	}

	public int getMaxy() {
		return maxy;
	}

	public int getMinx() {
		return minx;
	}

	public int getMiny() {
		return miny;
	}

	public Dimension getPreferredSize() {
		return new Dimension(62, 62);
	}

	public String getTilte() {
		return title;
	}

	public void invalidate() {
		dirty = true;
	}

	public boolean isOutline() {
		return outline;
	}

	public int maxX() {
		return maxx;
	}

	public int maxY() {
		return maxy;
	}

	public int minX() {
		return minx;
	}

	public int minY() {
		return miny;
	}

	public void mouseClicked(MouseEvent e) {
		logger.debug("Mouse clicked: " + e);
		CP c = findComponent(e);
		if (c != null) {
			if (c.c instanceof MouseListener) {
				((MouseListener) c.c).mouseClicked(translateCoords(e, c));
			}
		}
		MouseListener[] listeners = getMouseListeners();
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].mouseClicked(e);
		}
	}

	public void mouseDragged(MouseEvent e) {
		CP c = findComponent(e);
		if (c != null) {
			if (c.c instanceof MouseMotionListener) {
				((MouseMotionListener) c.c).mouseDragged(translateCoords(e, c));
			}
		}
		MouseMotionListener[] listeners = getMouseMotionListeners();
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].mouseDragged(e);
		}
	}

	public void mouseEntered(MouseEvent e) {
		CP c = findComponent(e);
		if (c != null) {

			if (c.c instanceof MouseListener) {
				((MouseListener) c.c).mouseEntered(translateCoords(e, c));
			}
		}
		MouseListener[] listeners = getMouseListeners();
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].mouseEntered(e);
		}
	}

	public void mouseExited(MouseEvent e) {
		CP c = findComponent(e);
		if (c != null) {
			if (c.c instanceof MouseListener) {
				((MouseListener) c.c).mouseExited(translateCoords(e, c));
			}
		}
		MouseListener[] listeners = getMouseListeners();
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].mouseExited(e);
		}
	}

	public void mouseMoved(MouseEvent e) {
		CP c = findComponent(e);
		if (c != null) {
			if (c.c instanceof MouseMotionListener) {
				((MouseMotionListener) c.c).mouseMoved(translateCoords(e, c));
			}
		}
		MouseMotionListener[] listeners = getMouseMotionListeners();
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].mouseMoved(e);
		}
	}

	public void mousePressed(MouseEvent e) {
		CP c = findComponent(e);
		if (c != null) {
			if (c.c instanceof MouseListener) {
				((MouseListener) c.c).mousePressed(translateCoords(e, c));
			}
		}
		MouseListener[] listeners = getMouseListeners();
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].mousePressed(e);
		}
	}

	public void mouseReleased(MouseEvent e) {
		CP c = findComponent(e);
		if (c != null) {
			if (c.c instanceof MouseListener) {
				((MouseListener) c.c).mouseReleased(translateCoords(e, c));
			}
		}
		MouseListener[] listeners = getMouseListeners();
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].mouseReleased(e);
		}
	}

	public void paint(Graphics g0) {
		if (immediate) {
			dirty = true;
			while (buffer == null) {
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
				}
			}
		}
		if (buffer != null) {
			g0.drawImage(buffer, 0, 0, null);
		}
	}

	public void paintBuffer() {
		try {
			doPaintBuffer();
		}
		catch (Exception e) {
			logger.warn("Exception caught while painting scaling container", e);
			dirty = true;
		}
	}

	public void paintd(Component c, Graphics g, int ox, int oy, double sx, double sy) {
		if (c instanceof NodeComponentWrapper) {
			Point p = ((NodeComponentWrapper) c).getLocation();
			double tx = sx * (p.getX() + ox);
			double ty = sy * (p.getY() + oy);
			g.drawOval((int) tx, (int) ty, 1, 1);
		}
		else if (c instanceof EdgeComponentWrapper) {
			return;
		}
		else if (c instanceof Container) {
			Component[] comps = ((Container) c).getComponents();
			for (int i = 0; i < comps.length; i++) {
				Point q = comps[i].getLocation();
				paintd(comps[i], g, q.x, q.y, sx, sy);
			}
		}
	}

	public Point realToScaled(int x, int y) {
		if ((cx2 - cx1 == 0) || (cy2 - cy1 == 0)) {
			return new Point(0, 0);
		}
		double tx = (x - cx1) * (maxx - minx) / (cx2 - cx1) + minx;
		double ty = (y - cy1) * (maxy - miny) / (cy2 - cy1) + miny;
		return new Point((int) tx, (int) ty);
	}

	public Point realToScaled(Point p) {
		return realToScaled(p.x, p.y);
	}

	public void removeTitle() {
		titleHeight = 0;
		title = null;
	}

	public void repaint(PaintEvent e) {
		Rectangle r = e.getUpdateRect();
		r.translate(minx, miny);
		repaintBuffer(r);
	}

	public synchronized void repaintBuffer(Rectangle dirtyRect) {
		if (timer == null) {
			return;
		}
		if (dirtyArea == null) {
			dirtyArea = dirtyRect;
		}
		else {
			dirtyArea.add(dirtyRect);
		}
		dirty = true;
	}

	public void repaintImmediately() {
		super.repaint();
	}

	public Point scaledToReal(int x, int y) {
		int tx = x * (cx2 - cx1) / (maxx - minx) + cx1;
		int ty = y * (cy2 - cy1) / (maxy - miny) + cy1;
		return new Point(tx, ty);
	}

	public Point scaledToReal(Point p) {
		return scaledToReal(p.x, p.y);
	}

	public void setAntiAliasing(boolean aa) {
		antiAliasing = aa;
	}

	public void setComponent(Component comp) {
		removeAll();
		if (getComponent() instanceof RepaintMonitoringContainer) {
			((RepaintMonitoringContainer) getComponent()).removeRepaintListener(this);
		}
		super.setComponent(comp);
		if (getComponent() instanceof RepaintMonitoringContainer) {
			((RepaintMonitoringContainer) getComponent()).addRepaintListener(this);
		}
		if (comp != null) {
			add(comp);
			comp.setVisible(true);
			comp.setLocation(0, 0);
			comp.invalidate();
			comp.validate();
			invalidate();
		}
	}

	public void setEventsEnabled(boolean enabled) {
		logger.debug("Setting events enabled to " + enabled);
		if (enabled) {
			this.enableEvents(AWTEvent.MOUSE_EVENT_MASK);
			this.enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
		}
		else {
			this.disableEvents(AWTEvent.MOUSE_EVENT_MASK);
			this.disableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
		}
	}

	public void translateAndDispatchMouseEvent(MouseEvent e) {
		Point translated = realToScaled(e.getX(), e.getY());
		Rectangle oldc = new Rectangle(xcursor - 8, ycursor - 8, 16, 16);
		xcursor = translated.x - minx;
		ycursor = translated.y - miny;
		Rectangle newc = new Rectangle(xcursor - 8, ycursor - 8, 16, 16);
		oldc.add(newc);
		repaintBuffer(oldc);
		Component c;
		if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
			if (dragged == null) {
				c = ((Container) comp).findComponentAt(translated);
				dragged = c;
			}
			else {
				c = dragged;
			}
		}
		else {
			c = ((Container) comp).findComponentAt(translated);
			dragged = null;
		}
		if (e.getID() == MouseEvent.MOUSE_EXITED) {
			oldc = new Rectangle(xcursor - 8, ycursor - 8, 16, 16);
			xcursor = -1000;
			ycursor = -1000;
			repaintBuffer(oldc);
		}
		if (c != null) {
			ExtendedMouseEvent te = new ExtendedMouseEvent((Component) e.getSource(), e.getID(), e
					.getWhen(), e.getModifiers(), translated.x - c.getX(), translated.y - c.getY(),
					e.getClickCount(), false, e.getButton());
			if (e instanceof ExtendedMouseEvent) {
				te.setInvokerX(((ExtendedMouseEvent) e).getInvokerX());
				te.setInvokerY(((ExtendedMouseEvent) e).getInvokerY());
				te.setPopupInvoker(((ExtendedMouseEvent) e).getPopupInvoker());
			}
			else {
				if ((e.getID() == MouseEvent.MOUSE_PRESSED)
						&& ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0)) {
					te.setInvokerX(e.getX());
					te.setInvokerY(e.getY());
					te.setPopupInvoker(this);
				}
			}
			c.dispatchEvent(te);
		}
		else {
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			MouseEvent te = new MouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), e
					.getModifiers(), translated.x, translated.y, e.getClickCount(), false, e
					.getButton());
			comp.dispatchEvent(te);
		}
	}

	protected void processMouseEvent(MouseEvent e) {
		MouseEventDispatcher.dispatchMouseEvent(e, this);
	}

	protected void processMouseMotionEvent(MouseEvent e) {
		MouseEventDispatcher.dispatchMouseMotionEvent(e, this);
	}

	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}

	public void setKeepAspectRatio(boolean k) {
		this.keepAspectRatio = k;
	}

	public void setMaxx(int i) {
		this.maxx = i;
	}

	public void setMaxy(int i) {
		this.maxy = i;
	}

	public void setMinx(int i) {
		this.minx = i;
	}

	public void setMiny(int i) {
		this.miny = i;
	}

	public void setOutline(boolean b) {
		this.outline = b;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	private MouseEvent translateCoords(MouseEvent e, CP c) {
		return new MouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiers(),
				c.x, c.y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
	}

	public void validateTree(Component c) {
		if (!c.isVisible()) {
			return;
		}
		//That's because I don't know how to properly draw swing components in
		//offscreen buffers.
		if (!c.isDisplayable()) {
			c.addNotify();
		}
		if (!c.isValid()) {
			c.validate();
		}
		if (c instanceof Container) {
			Container cc = (Container) c;
			Component[] cs = cc.getComponents();
			for (int i = 0; i < cs.length; i++) {
				validateTree(cs[i]);
			}
		}
	}

	public void finalize() {
		logger.debug("A scaling container has left the building...");
	}

	public synchronized void clear() {
		if (painter != null) {
			painter.cancel();
		}
		if (outline) {
			buffer = null;
		}
		dirty = true;
	}

	private class CP {

		Component c;

		int x;

		int y;

		CP(Component c, int x, int y) {
			this.c = c;
			this.x = x;
			this.y = y;
		}
	}

	private class Painter extends TimerTask {

		private ScalingContainer sc;

		public Painter(ScalingContainer sc) {
			this.sc = sc;
		}

		public void run() {
			try {
				sc.actionPerformed(null);
			}
			catch (Exception e) {
				logger.warn("Exception caught while repainting ", e);
			}
		}
	}

	public Object getPaintLock() {
		return this.paintLock;
	}

	/**
	 * Since this is a multithreaded animal, provide a way to defer painting if
	 * some other thread is changing things on the scaled component
	 */
	public void setPaintLock(Object paintLock) {
		//wait until there are no locks on it
		//I wonder what happens if I don't
		if (this.paintLock != null) {
			synchronized (this.paintLock) {
				this.paintLock = paintLock;
			}
		}
		else {
			this.paintLock = paintLock;
		}
	}

	public boolean isImmediate() {
		return this.immediate;
	}

	public void setImmediate(boolean immediate) {
		this.immediate = immediate;
	}
}