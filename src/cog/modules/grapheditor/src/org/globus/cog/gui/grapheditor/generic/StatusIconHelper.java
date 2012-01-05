
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 *
 * Created on Feb 3, 2004
 *
 */
package org.globus.cog.gui.grapheditor.generic;

import java.awt.Image;

import javax.swing.ImageIcon;

import org.globus.cog.gui.grapheditor.util.graphics.ImageProcessor;
import org.globus.cog.util.ImageLoader;

public class StatusIconHelper {

	public static ImageIcon makeRunningIcon(Image image) {
		return new ImageIcon(
				ImageProcessor.compose(
						ImageProcessor.highlight(image, 0.5),
						ImageLoader.loadIcon("images/16x16/co/overlay-running.png").getImage()));
	}

	public static ImageIcon makeFailedIcon(Image image) {
		return new ImageIcon(
				ImageProcessor.compose(
						image,
						ImageLoader.loadIcon("images/16x16/co/overlay-failed.png").getImage()));
	}

	public static ImageIcon makeCompletedIcon(Image image) {
		return new ImageIcon(
				ImageProcessor.compose(
						image,
						ImageLoader.loadIcon("images/16x16/co/overlay-completed.png").getImage()));
	}

	public static ImageIcon makeStoppedIcon(Image image) {
		return new ImageIcon(image);
	}

}
