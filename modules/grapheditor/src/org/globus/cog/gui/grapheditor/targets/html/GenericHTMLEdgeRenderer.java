// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor.targets.html;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.RootContainer;
import org.globus.cog.gui.grapheditor.StreamRenderer;
import org.globus.cog.gui.grapheditor.edges.EdgeRenderer;
import org.globus.cog.gui.grapheditor.util.swing.JArrow;

public class GenericHTMLEdgeRenderer extends EdgeRenderer implements StreamRenderer {
	private static Logger logger = Logger.getLogger(GenericHTMLEdgeRenderer.class);
	private static double SNAP = 4.0;
	private static double ZOOM_COMPENSATION = 1.0;
	private RootContainer cachedRootContainer;

	public void render(Writer wr) throws IOException {
		Point fp = getEdge().getControlPoint(0);
		Point tp = getEdge().getControlPoint(1);
		int width = (int) ((tp.x - fp.x) * ZOOM_COMPENSATION);
		int height = (int) ((tp.y - fp.y) * ZOOM_COMPENSATION);
		Dimension d = new SnapDimension(width, height);
		String imageName = "arrow" + d.width + "x" + d.height + ".png";
		String outputDir = (String) getRootNode().getPropertyValue("html.outputdir");
		File imageFile = new File(outputDir, imageName);
		if (!imageFile.exists()) {
			JArrow arrow = new JArrow();
			arrow.setPoint(0, 0, 0);
			arrow.setPoint(1, d.width, d.height);
			Rectangle bbox = arrow.getBoundingBox();
			BufferedImage bi = new BufferedImage(Math.abs(bbox.width), Math.abs(bbox.height),
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = bi.createGraphics();
			g.translate(-Math.min(bbox.x, bbox.x + bbox.width), -Math.min(bbox.y, bbox.y
					+ bbox.height));
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			arrow.paint(g);

			logger.debug("Writing image " + imageFile.getAbsolutePath());
			ImageIO.write(bi, "png", imageFile);
		}
		wr.write("\"" + imageName + "\",");
	}

	private class SnapDimension extends Dimension {

		public SnapDimension(int width, int height) {
			super();
			int snapx = normalize(width);
			int snapy = normalize(height);
			setSize(snapx, snapy);
		}

		private int normalize(int dim) {
			return sgn(dim)
					* ((int) Math.pow(Math.exp((int) (SNAP * Math.log(Math.abs(dim + 1)))),
							1 / SNAP) - 1);
		}

		private int sgn(int v) {
			if (v > 0) {
				return 1;
			}
			if (v < 0) {
				return -1;
			}
			return 0;
		}
	}

	public static double getZOOM_COMPENSATION() {
		return ZOOM_COMPENSATION;
	}

	public static void setZOOM_COMPENSATION(double zoom_compensation) {
		ZOOM_COMPENSATION = zoom_compensation;
	}
}