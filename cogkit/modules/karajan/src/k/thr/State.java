/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 29, 2007
 */
package k.thr;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public final class State {
	public static final byte[] BIT_COUNT = new byte[256];
	public static final int[] MAX_VAL = new int[33];
	
	// how many bits do I need to store 0... n
	// clearly i = 0 isn't useful, so BIT_COUNT[0] corresponds to i == 1
	// to store 0-1, we need 1 bit
	// --''--   0-2, we need 2 bits
	private static void setBitCount(int n, int b) {
		if (n < 1 || n > BIT_COUNT.length) {
			return;
		}
		BIT_COUNT[n - 1] = (byte) b;
	}
	
	private static byte getBitCount(int i) {
		return BIT_COUNT[i - 1];
	}
	
	static {
		int ix = 1;
		for (int b = 1; b < 9; b++) {
			int limit = (1 << b);
			while (ix < limit) {
				setBitCount(ix, b);
				ix++;
			}
		}
		for (int i = 0; i < MAX_VAL.length; i++) {
			MAX_VAL[i] = (int) (((long) 1) << i) - 1;
		}
	}
	
	private long[] compacted; 
	private List<Object> state;
	private List<Object> trace;
	private byte cindex, crtBit;
	
	public State() {
		state = new ArrayList<Object>();
	}
		
	public State(State s) {
		state = new ArrayList<Object>();
		if (s != null) {
			state.addAll(s.state);
			if (s.compacted != null) {
				compacted = new long[s.compacted.length];
				System.arraycopy(s.compacted, 0, compacted, 0, s.compacted.length);
				cindex = s.cindex;
				crtBit = s.crtBit;
			}
		}
	}
	
	public void pushInt(int value, int max) {
		byte bits;
		if (max > 255) {
			bits = 32;
		}
		else {
			bits = getBitCount(max);
			if (value == Integer.MAX_VALUE) {
				value = MAX_VAL[bits];
			}
			else {
				if (value > max) {
					throw new IllegalArgumentException("value > max");
				}
			}
		}
		pushInt0(value, bits);
	}
	
	private void pushInt0(int value, byte bits) {
		//System.out.println(System.identityHashCode(this) + " + " + value + " / " + bits);
		if (compacted == null) {
			compacted = new long[1];
		}
		int endBits = bits + crtBit;
		if (endBits >= 64) {
			// store lower
			compacted[cindex++] |= (((long) value) << crtBit);
			extend();
			if (endBits == 64) {
				crtBit = 0;
			}
			else {
				crtBit = (byte) (endBits - 64);
				compacted[cindex] |= (value >> (bits - crtBit));
			}
			
		}
		else {
			compacted[cindex] |= (((long) value) << crtBit);
			crtBit = (byte) endBits;
		}
	}
	
	private static String zeroPad(String s, int width) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < (width - s.length()); i++) {
			sb.append('0');
		}
		sb.append(s);
		return sb.toString();
	}
	
	public int popInt(int max) {
		byte bits;
		if (max > 255) {
			bits = 32;
		}
		else {
			bits = getBitCount(max);
		}
		int value = popInt0(bits);
		return value;
	}
	
	private int popInt0(byte bits) {
		if (compacted == null) {
			return Integer.MIN_VALUE;
		}
		int value = 0;
		int endBits = crtBit - bits;
		if (endBits < 0) {
			if (crtBit > 0) {
				value = (int) ((compacted[cindex--] & MAX_VAL[bits + endBits]) << (-endBits));
			}
			else {
				cindex--;
			}
			crtBit = (byte) (64 + endBits);
			value |= (int) ((compacted[cindex] >> crtBit) & MAX_VAL[-endBits]);
			crtBit = (byte) (64 + endBits);
		}
		else {
			value = (int) (compacted[cindex] >> endBits) & MAX_VAL[bits];
			crtBit = (byte) endBits;
		}
		//System.out.println(System.identityHashCode(this) + " - " + value + " / " + bits);
		if (value == MAX_VAL[bits]) {
			value = Integer.MAX_VALUE;
		}
		if (cindex == 0 && crtBit == 0) {
			compacted = null;
		}
		return value;
	}

	private void extend() {
		long[] tmp = new long[compacted.length + 1];
		System.arraycopy(compacted, 0, tmp, 0, compacted.length);
		compacted = tmp;
	}

	public int popInt() {
		return popInt(256);
	}
	
	public synchronized Object pop() {
		if (state.size() == 0) {
			return null;
		}
		else {
			return state.remove(state.size() - 1);
		}
	}
		
	public void push(int i, int maxValue) {
		pushInt(i, maxValue);
	}

	public void push(int i) {
		pushInt0(i, (byte) 32);
	}
	
	public void push(Object o) {
		state.add(o);
	}
	
	public void push(boolean b) {
		pushInt0(b ? 1 : 0, (byte) 1);
	}
	
	public boolean popBoolean() {
		return popInt0((byte) 1) != 0;
	}
	
	private static final NumberFormat NF = new DecimalFormat("00000000");
		
	public String toString() {
		return NF.format(System.identityHashCode(this)) + ": " + state.toString();
	}
		
	public boolean isEmpty() {
	    return state.isEmpty() && compacted == null;
	}
		
	public synchronized void addTraceElement(Object n) {
	    if (trace == null) {
	        trace = new ArrayList<Object>(3);
	    }
	    trace.add(n);
	}
	
	public synchronized List<Object> getTrace() {
	    if (trace == null) {
	        return null;
	    }
	    else {
	    	return new ArrayList<Object>(trace);
	    }
	}
	
	public static void main(String[] args) {
		State s = new State();
		s.push(2);
		s.push(5, 250);
		s.push(5, 250);
		s.push(5, 250);
		s.push(5, 100);
		s.push(5, 100);
		System.out.println(s.popInt(100));
		System.out.println(s.popInt(100));
		System.out.println(s.popInt(250));
		System.out.println(s.popInt(250));
		System.out.println(s.popInt(250));
		System.out.println(s.popInt());
	}
}
