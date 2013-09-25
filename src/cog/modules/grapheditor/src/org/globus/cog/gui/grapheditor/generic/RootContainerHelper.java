
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 26, 2004
 */
package org.globus.cog.gui.grapheditor.generic;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.canvas.StatusManager;
import org.globus.cog.gui.grapheditor.util.GraphToXML;
import org.globus.cog.gui.grapheditor.util.LoadUpdateListener;
import org.globus.cog.gui.grapheditor.util.XMLToGraph;

public class RootContainerHelper implements LoadUpdateListener {
	private static Logger logger = Logger.getLogger(RootContainerHelper.class);

	private String fileName;

	private GraphCanvas canvas;

	private int total;

	public RootContainerHelper(String fileName, GraphCanvas canvas, int total) {
		this.fileName = fileName;
		this.canvas = canvas;
		this.total = total;
	}

	public static void load(String fname, GraphCanvas canvas) throws Exception {
		if (fname != null) {
			canvas.getStatusManager().push("Loading " + fname, StatusManager.BUSY_ICON);
			canvas.getGraph().clear();
			//sweep the file to get an estimate of the number of elements
			int est = 0;
			try {
				BufferedReader br = new BufferedReader(new FileReader(fname));
				String line = null;
				do {
					line = br.readLine();
					if (line != null) {
						if (line.indexOf("</") != -1) {
							est++;
						}
						if (line.indexOf("/>") != -1) {
							est++;
						}
					}
				}
				while (line != null);
				br.close();
			}
			catch (Exception e) {
				logger.warn("Could not get an estimate on the number of elements", e);
			}
			if (est != 0) {
				logger.debug("Estimated number of elements is "+est);
				canvas.getStatusManager().initializeProgress(est);
			}
			XMLToGraph.read(canvas.getOwner(), new FileReader(fname), new RootContainerHelper(
				fname, canvas, est));
			logger.debug("Graph has " + canvas.getGraph().edgeCount() + " edges");

			canvas.getStatusManager().pop();
			if (est != 0) {
				canvas.getStatusManager().removeProgress();
			}
			canvas.getStatusManager().setDefaultText(
				fname + ": " + canvas.getGraph().nodeCount() + " nodes, "
					+ canvas.getGraph().edgeCount() + " edges");
		}
	}

	public static void save(String fileName, GraphCanvas canvas) throws Exception {
		FileWriter writer = new FileWriter(fileName);
		GraphToXML.write(canvas.getOwner(), writer, 0, true);
		writer.close();
	}

	public void elementsLoaded(int count) {
		canvas.getStatusManager().setProgress(Math.min(count, total));
	}

}
