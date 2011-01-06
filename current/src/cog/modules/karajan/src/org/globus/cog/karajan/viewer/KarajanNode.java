
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Oct 30, 2003
 */
package org.globus.cog.karajan.viewer;

import java.awt.Image;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;

import org.globus.cog.gui.grapheditor.generic.GenericNode;
import org.globus.cog.gui.grapheditor.properties.ComponentProperty;
import org.globus.cog.gui.grapheditor.util.graphics.ImageProcessor;
import org.globus.cog.util.ImageLoader;

public class KarajanNode extends GenericNode {
	
	public static final int STATUS_SUSPENDED = 10;
	
	public static final String TOTAL = "total";
	public static final String CURRENT = "current";
	
	private static HashMap icons;
	private Boolean breakpoint;
	private List overlays;
	
	private static void iconsput(String name, String icon) {
		icons.put(new NamingWrapper(name), icon);
	}

	static {
		icons = new HashMap();
		iconsput("javabean", "images/karajan/bean.png");
		iconsput("echo", "images/karajan/echo.png");
		iconsput("print", "images/karajan/echo.png");
		iconsput("execute", "images/karajan/exec.png");
		iconsput("transfer", "images/karajan/transfer.png");
		iconsput("start", "images/start.png");
		iconsput("end", "images/end.png");
		iconsput("setvar", "images/karajan/setvar.png");
		iconsput("set", "images/karajan/setvar.png");
		iconsput("loop", "images/karajan/loop.png");
		iconsput("if", "images/karajan/if.png");
		iconsput("parallelchoice", "images/karajan/parallel-choice.png");
		setClassRendererClass(KarajanNode.class, KarajanNodeRenderer.class, "swing");
		setClassRendererClass(KarajanNode.class, KarajanHTMLNodeRenderer.class, "html");
	}

	public KarajanNode() {
		breakpoint = Boolean.FALSE;
		overlays = new LinkedList();
		addProperty(new ComponentProperty(this, "breakpoint"));
	}

	public void setComponentType(String name) {
		super.setComponentType(name);
		NamingWrapper w = new NamingWrapper(name);
		if (icons.containsKey(w)) {
			setIconfile((String) icons.get(w));
		}
	}

	public boolean hasBreakpoint() {
		return breakpoint.booleanValue();
	}

	public void setBreakpoint() {
		setBreakpoint(Boolean.TRUE);
	}

	public void addOverlay(String overlayName) {
		overlays.add(overlayName);
		overlayIcon();
	}

	public void removeOverlay(String overlayName) {
		overlays.remove(overlayName);
		overlayIcon();
	}

	protected void overlayIcon() {
		Image icon = ImageLoader.loadIcon(getIconfile()).getImage();
		Iterator i = overlays.iterator();
		while (i.hasNext()) {
			String overlay = (String) i.next();
			icon = ImageProcessor.compose(icon, ImageLoader.loadIcon(overlay).getImage());
		}
		setPropertyValue("icon", (new ImageIcon(icon)));
	}
	
	public void removeBreakpoint() {
		setBreakpoint(Boolean.FALSE);
	}


	public Boolean getBreakpoint() {
		return breakpoint;
	}

	public void setBreakpoint(Boolean breakpoint) {
		this.breakpoint = breakpoint;
		if (breakpoint.equals(Boolean.TRUE)) {
			addOverlay("images/karajan/breakpoint-overlay.png");
		}
		else {
			removeOverlay("images/karajan/breakpoint-overlay.png");
		}
	}
}
