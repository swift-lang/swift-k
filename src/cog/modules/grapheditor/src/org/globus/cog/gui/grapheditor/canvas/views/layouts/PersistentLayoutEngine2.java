// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jun 25, 2003
 */
package org.globus.cog.gui.grapheditor.canvas.views.layouts;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.canvas.StatusManager;
import org.globus.cog.gui.grapheditor.targets.swing.views.GraphView;
import org.globus.cog.util.graph.GraphChangedEvent;
import org.globus.cog.util.graph.GraphInterface;
import org.globus.cog.util.graph.GraphListener;
import org.globus.cog.util.graph.Node;

public class PersistentLayoutEngine2 implements PropertyChangeListener, GraphLayoutEngine2,
		GraphListener, StatusReporter {
	private static Logger logger = Logger.getLogger(PersistentLayoutEngine2.class);

	private static String file;
	private static int commitInterval = 10;
	private static int tics;
	private static boolean dirty;
	private static Timer timer;
	private GraphInterface lastGraph;
	private static Hashtable graphs;
	private GraphStructure gstr;
	private GraphLayoutEngine layoutEngine;
	private StatusManager statusManager;

	static {
		String sep = System.getProperty("file.separator");
		file = System.getProperty("user.home") + sep + ".gridviz";
		File f = new File(file);
		if (!f.exists()) {
			f.mkdir();
		}
		file = file + sep + "graphs2";
		dirty = false;
		tics = 0;
	}

	public PersistentLayoutEngine2() {
		this(new ExtendedSpringLayout());
	}

	public PersistentLayoutEngine2(GraphLayoutEngine layoutEngine) {
		this.layoutEngine = layoutEngine;
		synchronized (PersistentLayoutEngine2.class) {
			if (timer == null) {
				load();
				timer = new Timer();
				timer.schedule(new Task(), 1000, 1000);
			}
		}
	}

	public void timerEvent() {
		tics++;
		if (dirty && (tics >= commitInterval)) {
			commit();
		}
	}

	private static synchronized void commit() {
		if (graphs == null) {
			graphs = new Hashtable();
		}
		save();
	}

	public Hashtable layoutGraph(GraphInterface graph, Hashtable fixedNodes) {
		return layoutGraph(graph, fixedNodes, false);
	}

	public Hashtable layoutGraph(GraphInterface graph, Hashtable fixedNodes,
			boolean ignorePersistence) {
		if ((graph.nodeCount() < 30) && !ignorePersistence) {
			if (lastGraph != null) {
				Iterator i = lastGraph.getNodesIterator();
				while (i.hasNext()) {
					Node n = (Node) i.next();
					// this will fail miserably if used in a different context
					if (n.getContents() instanceof GraphComponent) {
						GraphComponent gc = (GraphComponent) n.getContents();
						gc.removePropertyChangeListener(this);
					}
				}
			}
			if (lastGraph != null) {
				lastGraph.removeGraphListener(this);
			}
			lastGraph = graph;
			graph.addGraphListener(this);
			tics = 0;
			gstr = new GraphStructure(graph);
			if (lastGraph != null) {
				Iterator i = lastGraph.getNodesIterator();
				while (i.hasNext()) {
					Node n = (Node) i.next();
					// this will fail miserably if used in a different context
					if (n.getContents() instanceof GraphComponent) {
						GraphComponent gc = (GraphComponent) n.getContents();
						gc.addPropertyChangeListener(this);
					}
				}
			}
			if (graphs.containsKey(gstr.getHash())
					&& ((fixedNodes == null) || (fixedNodes.size() == 0))) {
				Hashtable h = new Hashtable();
				GraphStructure gs = (GraphStructure) graphs.get(gstr.getHash());
				synchronized (gs) {
					int[] ri = new int[graph.nodeCount()];
					for (int i = 0; i < graph.nodeCount(); i++) {
						ri[gstr.getCanonicalIndex(i)] = i;
					}
					Iterator ni = graph.getNodesIterator();
					for (int i = 0; i < graph.nodeCount(); i++) {
						Node n = (Node) ni.next();
						Point p = gs.getPoint(ri[i]);
						if (p == null) {
							logger.error("Error in gs: hash=" + gs.getHash() + " point#=" + i);
							h.put(n, new Point(0, 0));
						}
						else {
							h.put(n, new Point(p.x, p.y));
						}
					}
				}
				return h;
			}
		}
		if (layoutEngine instanceof StatusReporter) {
			((StatusReporter) layoutEngine).setStatusManager(statusManager);
		}
		return layoutEngine.layoutGraph(graph, fixedNodes);
	}

	private static void save() {
		logger.info("Flushing layout table...");
		synchronized (graphs) {
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(file));
				Iterator i = graphs.keySet().iterator();
				while (i.hasNext()) {
					String hash = (String) i.next();
					bw.write(hash + "\n");
					GraphStructure gs = (GraphStructure) graphs.get(hash);
					bw.write(gs.getCoords());
					bw.write("\n");
				}
				bw.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			dirty = false;
		}
		logger.info("done");
	}

	private static void load() {
		logger.info("Loading database...");
		graphs = new Hashtable();
		synchronized (graphs) {
			try {
				String line = null;
				String hash = null;
				int state = 0;
				BufferedReader br = new BufferedReader(new FileReader(file));
				while ((line = br.readLine()) != null) {
					if (state == 1) {
						graphs.put(hash, new GraphStructure(hash, line));
					}
					else {
						hash = line;
					}
					state = 1 - state;
				}
				br.close();
			}
			catch (Exception e) {
				logger.info("not there...");
			}
		}
		logger.info("done");
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(GraphView.LOCATION)) {
			synchronized (graphs) {
				graphs.put(gstr.getHash(), gstr);
				gstr.updateCoords();
				dirty = true;
				tics = 0;
			}
		}
	}

	public GraphLayoutEngine getLayoutEngine() {
		return this.layoutEngine;
	}

	public void setLayoutEngine(GraphLayoutEngine layoutEngine) {
		this.layoutEngine = layoutEngine;
	}

	public void graphChanged(GraphChangedEvent e) {
		gstr = new GraphStructure(lastGraph);
	}

	public void setGraph(GraphInterface graph) {
		if (lastGraph != null) {
			lastGraph.removeGraphListener(this);
			Iterator i = lastGraph.getNodesIterator();
			while (i.hasNext()) {
				Node n = (Node) i.next();
				// this will fail miserably if used in a different context
				if (n.getContents() instanceof GraphComponent) {
					GraphComponent gc = (GraphComponent) n.getContents();
					gc.removePropertyChangeListener(this);
				}
			}
		}
		lastGraph = graph;
		if (lastGraph != null) {
			lastGraph.addGraphListener(this);
			tics = 0;
			gstr = new GraphStructure(lastGraph);
			Iterator i = lastGraph.getNodesIterator();
			while (i.hasNext()) {
				Node n = (Node) i.next();
				// this will fail miserably if used in a different context
				if (n.getContents() instanceof GraphComponent) {
					GraphComponent gc = (GraphComponent) n.getContents();
					gc.addPropertyChangeListener(this);
				}
			}
		}
	}

	public void setStatusManager(StatusManager statusManager) {
		this.statusManager = statusManager;
	}

	public void setIgnoredEdges(Set ignoredEdges) {
		if (layoutEngine instanceof GraphLayoutEngine2) {
			((GraphLayoutEngine2) layoutEngine).setIgnoredEdges(ignoredEdges);
		}
	}

	private class Task extends TimerTask {
		public void run() {
			timerEvent();
		}
	}
}
