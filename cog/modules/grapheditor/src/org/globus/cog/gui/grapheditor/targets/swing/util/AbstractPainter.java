/*
 * Created on Aug 26, 2004
 *  
 */
package org.globus.cog.gui.grapheditor.targets.swing.util;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.apache.log4j.Logger;

public abstract class AbstractPainter implements ScalingPainter {
	private static Logger logger = Logger.getLogger(AbstractPainter.class);

	private static int count;

	protected boolean canceled, painting, destroyed;

	private boolean completed;

	protected BufferedImage buffer;

	private PainterListener pl;

	private Dimension d;

	protected Rectangle bounds;

	protected Rectangle paintArea;

	public AbstractPainter() {
	}

	public final void run() {
		count++;
		logger.debug("New painter; " + count + " total");
		while (!destroyed) {
			canceled = false;
			if (completed) {
				logger.debug("Previous paint completed. Entering wait state");
				synchronized (this) {
					try {
						wait();
					}
					catch (InterruptedException e) {
					}
				}
				completed = false;
			}
			if (destroyed) {
				break;
			}
			painting = true;
			try {
				paintRun();
			}
			catch (Exception e) {
				logger.warn("Exception caught during paint run ", e);
			}
			painting = false;
			if (!canceled) {
				completed = true;
				pl.paintCompleted(this);
			}
		}
		count--;
		logger.debug("Painter destroyed; " + count + " left");
	}

	public abstract void paintRun();

	public void setPainterListener(PainterListener pl) {
		this.pl = pl;
	}

	public void wake() {
		synchronized (this) {
			notify();
		}
	}

	public void cancel() {
		canceled = true;
	}

	public void destroy() {
		destroyed = true;
		canceled = true;
	}

	public void setBuffer(BufferedImage buffer) {
		this.buffer = buffer;
	}

	public boolean isPainting() {
		return painting;
	}

	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}

	public Rectangle getBounds() {
		return bounds;
	}

	public void setBufferDimension(Dimension d) {
		this.d = d;
	}

	public Dimension getBufferDimension() {
		return d;
	}

	public BufferedImage getBuffer() {
		return buffer;
	}

	public Rectangle getPaintArea() {
		return paintArea;
	}

	public void setPaintArea(Rectangle paintArea) {
		this.paintArea = paintArea;
	}

	protected void fireBufferUpdated() {
		pl.bufferUpdated(this);
	}
}