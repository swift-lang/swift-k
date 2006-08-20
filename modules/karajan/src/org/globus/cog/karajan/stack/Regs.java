//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 29, 2005
 */
package org.globus.cog.karajan.stack;

public final class Regs {
	private int iA, iB;
	private boolean bA, bB;

	public boolean getBA() {
		return bA;
	}

	public void setBA(boolean ba) {
		this.bA = ba;
	}

	public boolean getBB() {
		return bB;
	}

	public void setBB(boolean bb) {
		this.bB = bb;
	}

	public int getIA() {
		return iA;
	}

	public void setIA(int ia) {
		this.iA = ia;
	}

	public int getIB() {
		return iB;
	}

	public void setIB(int ib) {
		this.iB = ib;
	}

	public int preIncIA() {
		return ++iA;
	}
	
	public int postIncIA() {
		return iA++;
	}

	public int preIncIB() {
		return ++iB;
	}
	
	public int preDecIA() {
		return --iA;
	}

	public int preDecIB() {
		return --iB;
	}
	
	public String toString() {
		return "[iA = " + iA + ", iB = " + iB + ", bA = " + bA + ", bB = " + bB + "]";
	}
}
