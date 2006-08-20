//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 14, 2005
 */
package org.globus.cog.karajan.workflow;

import java.io.File;
import java.rmi.server.UID;

import org.globus.cog.karajan.util.Cache;
import org.globus.cog.karajan.workflow.nodes.FlowElement;
import org.globus.cog.karajan.workflow.nodes.UIDMap;

public class ElementTree {
	private UIDMap UIDMap;
	private FlowElement root;
	private String name;
	private transient String basedir;
	private Cache cache;

	public Cache getCache() {
		return cache;
	}

	public ElementTree() {
		this(new UIDMap());
	}

	private ElementTree(UIDMap UIDMap) {
		this.UIDMap = UIDMap;
		cache = new Cache();
	}

	private ElementTree(ElementTree other) {
		this.UIDMap = other.UIDMap;
		this.root = other.root;
		this.name = other.name;
	}

	public FlowElement getRoot() {
		return root;
	}

	public void setRoot(FlowElement root) {
		this.root = root;
	}

	public UIDMap getUIDMap() {
		return UIDMap;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		if (name == null) {
			//be safe
			setBasedir("/tmp/" + new UID().toString());
		}
		else {
			File f = new File(name);
			setBasedir(f.getAbsoluteFile().getParent());
		}
	}

	public ElementTree copy() {
		ElementTree copy = new ElementTree(this);
		return copy;
	}

	public String getBasedir() {
		if (this.basedir == null) {
			this.basedir = ".";
		}
		return this.basedir;
	}

	public void setBasedir(String string) {
		this.basedir = string;
	}

	public File resolveFile(File f) {
		if (f.isAbsolute()) {
			return f;
		}
		else {
			return new File(getBasedir() + File.separator + f.getPath());
		}
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}
}
