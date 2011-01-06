// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 23, 2004
 */
package org.globus.cog.gui.grapheditor.targets.swing;

import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.EventObject;

import javax.swing.Icon;
import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.generic.RootNode;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasAction;
import org.globus.cog.gui.grapheditor.targets.swing.util.CanvasActionEvent;
import org.globus.cog.gui.grapheditor.targets.swing.util.WindowVisibilitySwitchLink;
import org.globus.cog.gui.grapheditor.util.swing.MemoryStatisticsFrame;
import org.globus.cog.gui.grapheditor.util.swing.SwingInspectorFrame;
import org.globus.cog.util.ImageLoader;

public class SwingRootCanvasRenderer extends SwingCanvasRenderer implements WindowListener {
	private static Logger logger = Logger.getLogger(SwingRootCanvasRenderer.class);
	private static Icon ICON_NEW = ImageLoader.loadIcon("images/16x16/co/filenew.png");
	private static Icon ICON_LOAD = ImageLoader.loadIcon("images/16x16/co/fileopen.png");
	private static Icon ICON_SAVE = ImageLoader.loadIcon("images/16x16/co/filesave.png");
	private static Icon ICON_SAVEAS = ImageLoader.loadIcon("images/16x16/co/filesaveas.png");
	private CanvasAction load, save, saveAs, newWindow, swingInspector, gc, memstat;

	private String fileName;
	private String lastDir;

	private JFrame memstatFrame, swingInspectorFrame;

	public SwingRootCanvasRenderer() {
		fileName = null;
		lastDir = new File(".").getAbsolutePath();
		addMenuItem(newWindow = new CanvasAction("File>1#New", ICON_NEW, CanvasAction.ACTION_NORMAL));
		addMenuItem(load = new CanvasAction("File>10#Load...", ICON_LOAD,
				CanvasAction.ACTION_NORMAL));
		addMenuItem(save = new CanvasAction("File>20#Save", ICON_SAVE, CanvasAction.ACTION_NORMAL));
		addMenuItem(saveAs = new CanvasAction("File>21#Save As...", ICON_SAVEAS,
				CanvasAction.ACTION_NORMAL));
		addMenuItem(swingInspector = new CanvasAction("Help>50#Debug>10#Swing Inspector",
				CanvasAction.ACTION_SWITCH));
		addMenuItem(gc = new CanvasAction("Help>50#Debug>11#Invoke Garbage Collector",
				CanvasAction.ACTION_NORMAL));
		addMenuItem(memstat = new CanvasAction("Help>50#Debug>12#Memory Monitor",
				CanvasAction.ACTION_SWITCH));

		new WindowVisibilitySwitchLink(swingInspector,
				new WindowVisibilitySwitchLink.Controler() {
					public JFrame createWindow() {
						return new SwingInspectorFrame((Frame) getRootContainer());
					}
				});
		
		new WindowVisibilitySwitchLink(memstat,
				new WindowVisibilitySwitchLink.Controler() {
					public JFrame createWindow() {
						return new MemoryStatisticsFrame();
					}
				});
	}

	public void event(EventObject o) {
		if (o instanceof CanvasActionEvent) {
			CanvasActionEvent e = (CanvasActionEvent) o;
			if (e.getCanvasAction() == load) {
				((GraphFrame) getRootContainer()).load();
				e.setConsumed(true);
			}
			else if (e.getCanvasAction() == save) {
				((GraphFrame) getRootContainer()).save();
				e.setConsumed(true);
			}
			else if (e.getCanvasAction() == saveAs) {
				((GraphFrame) getRootContainer()).saveAs();
				e.setConsumed(true);
			}
			else if (e.getCanvasAction() == newWindow) {
				new GraphFrame(new RootNode()).activate();
				e.setConsumed(true);
			}
			else if (e.getCanvasAction() == gc) {
				Runtime.getRuntime().gc();
				e.setConsumed(true);
			}
			else {
				super.event(o);
			}
			return;
		}
		super.event(o);
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		if (e.getWindow() == swingInspectorFrame) {
			swingInspector.setSelected(false);
		}
		else if (e.getWindow() == memstatFrame) {
			memstat.setSelected(false);
		}
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

}
