// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jan 22, 2004
 */
package org.globus.cog.gui.grapheditor;

public class ClassTargetPair {
	public Class cls;
	public String target;

	public ClassTargetPair(Class cls, String target) {
		this.cls = cls;
		this.target = target;
	}

	public boolean equals(Object obj) {
		if (obj instanceof ClassTargetPair) {
			return this.cls.equals(((ClassTargetPair) obj).cls)
					&& this.target.equals(((ClassTargetPair) obj).target);
		}
		return false;
	}

	public int hashCode() {
		return cls.hashCode() + target.hashCode();
	}

	public String toString() {
		return cls.toString() + ":" + target;
	}

}
