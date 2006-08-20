// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 27, 2004
 */
package org.globus.cog.karajan.viewer;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JLabel;

public class JLabelWithProgress extends JLabel {
	private static final long serialVersionUID = 4113755580775269329L;

	private double progressHorizontalSize, progressVerticalSize;

	private double progressXAlignment;

	private double progressYAlignment;

	private int progressRange, progressValue;

	private boolean progressVisible;

	private Color progressColor, progressBorderColor;

	public JLabelWithProgress() {
		super();
		initializeProgressParameters();
	}

	public JLabelWithProgress(String text) {
		super(text);
		initializeProgressParameters();
	}

	private void initializeProgressParameters() {
		progressVisible = false;
		progressHorizontalSize = 0.5;
		progressVerticalSize = 0.1;
		progressXAlignment = 1.0;
		progressYAlignment = 0.0;
		progressColor = Color.BLUE;
		progressBorderColor = Color.BLACK;
	}

	public void paint(Graphics g) {
		super.paint(g);
		if (progressVisible) {
			int pwidth = Math.max((int) (getWidth() * progressHorizontalSize), 5);
			int pheight = Math.max((int) (getHeight() * progressVerticalSize), 3);
			int px = (int) ((getWidth() - pwidth - 1) * progressXAlignment);
			int py = (int) ((getHeight() - pheight - 1) * progressYAlignment);
			g.setColor(progressBorderColor);
			g.drawRect(px, py, pwidth, pheight);
			if (progressRange > 0) {
				int bwidth = progressValue * (pwidth - 1) / progressRange;
				g.setColor(progressColor);
				g.fillRect(px + 1, py + 1, bwidth, pheight - 1);
			}
		}
	}

	public double getProgressHorizontalSize() {
		return progressHorizontalSize;
	}

	public void setProgressHorizontalSize(double progressHorizontalSize) {
		this.progressHorizontalSize = progressHorizontalSize;
		repaint();
	}

	public double getProgressVerticalSize() {
		return progressVerticalSize;
	}

	public void setProgressVerticalSize(double progressVerticalSize) {
		this.progressVerticalSize = progressVerticalSize;
		repaint();
	}

	public double getProgressXAlignment() {
		return progressXAlignment;
	}

	public void setProgressXAlignment(double progressXAlignment) {
		this.progressXAlignment = progressXAlignment;
		repaint();
	}

	public double getProgressYAlignment() {
		return progressYAlignment;
	}

	public void setProgressYAlignment(double progressYAlignment) {
		this.progressYAlignment = progressYAlignment;
		repaint();
	}

	public int getProgressRange() {
		return progressRange;
	}

	public void setProgressRange(int progressRange) {
		this.progressRange = progressRange;
		repaint();
	}

	public int getProgressValue() {
		return progressValue;
	}

	public void setProgressValue(int progressValue) {
		this.progressValue = progressValue;
		if (this.progressRange > 0) {
			setToolTipText(progressValue * 100 / progressRange + "% complete");
		}
		repaint();
	}

	public boolean isProgressVisible() {
		return progressVisible;
	}

	public void setProgressVisible(boolean progressVisible) {
		this.progressVisible = progressVisible;
		repaint();
	}

	public Color getProgressColor() {
		return progressColor;
	}

	public void setProgressColor(Color progressColor) {
		this.progressColor = progressColor;
		repaint();
	}

	public Color getProgressBorderColor() {
		return progressBorderColor;
	}

	public void setProgressBorderColor(Color progressBorderColor) {
		this.progressBorderColor = progressBorderColor;
	}
}