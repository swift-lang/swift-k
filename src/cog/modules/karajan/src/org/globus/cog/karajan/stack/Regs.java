//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 29, 2005
 */
package org.globus.cog.karajan.stack;

import org.globus.cog.karajan.workflow.events.EventListener;

public final class Regs {
	public static final int BA = 0x00000001;
	public static final int BB = 0x00000002;
	public static final int BARRIER = 0x00000004;
	public static final int NBA = 0xfffffffe;
	public static final int NBB = 0xfffffffd;
	public static final int NBARRIER = 0xfffffffb;
	
	private int iA, iB;
	private int b;
	private EventListener caller;

	public boolean getBA() {
		return (b & BA) != 0;
	}

	public void setBA(boolean ba) {
		if (ba) {
			b |= BA;
		}
		else {
			b &= NBA;
		}
	}

	public boolean getBB() {
		return (b & BB) != 0;
	}

	public void setBB(boolean bb) {
		if (bb) {
			b |= BB;
		}
		else {
			b &= NBB;
		}
	}
	
	public boolean getBarrier() {
		return (b & BARRIER) != 0;
	}

	public void setBarrier(boolean bc) {
		if (bc) {
			b |= BARRIER;
		}
		else {
			b &= NBARRIER;
		}
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
		return "[iA = " + iA + ", iB = " + iB + ", bA = " + getBA() + ", bB = " + getBB() + "]";
	}

	public synchronized EventListener getCaller() {
		return caller;
	}

	public synchronized void setCaller(EventListener caller) {
		this.caller = caller;
	}
}
