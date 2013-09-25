//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 21, 2005
 */
package org.globus.cog.karajan.translator.atoms;

import java.util.HashMap;
import java.util.Map;

public class Transliterator {
	private static Map transliterations;
	
	static {
		transliterations = new HashMap();
		transliterations.put(new MutableCharacter('?'), "__q_");
		transliterations.put(new MutableCharacter('#'), "__hash_");
		transliterations.put(new MutableCharacter('$'), "__dollar_");
		transliterations.put(new MutableCharacter('%'), "__percent_");
		transliterations.put(new MutableCharacter('&'), "__amp_");
		transliterations.put(new MutableCharacter('@'), "__at_");
		transliterations.put(new MutableCharacter('+'), "__plus_");
		transliterations.put(new MutableCharacter('-'), "__minus_");
		transliterations.put(new MutableCharacter('*'), "__times_");
		transliterations.put(new MutableCharacter('/'), "__fwslash_");
		transliterations.put(new MutableCharacter('\\'), "__backslash_");
		transliterations.put(new MutableCharacter('\''), "__quote_");
		transliterations.put(new MutableCharacter('|'), "__pipe_");
		transliterations.put(new MutableCharacter('<'), "__lt_");
		transliterations.put(new MutableCharacter('>'), "__gt_");
		transliterations.put(new MutableCharacter('='), "__eq_");
		transliterations.put(new MutableCharacter('.'), "__dot_");
		transliterations.put(new MutableCharacter('!'), "__bang_");
	}
	
	public static String transliterate(final String source) {
		final MutableCharacter mc = new MutableCharacter();
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < source.length(); i++) {
			mc.setC(source.charAt(i));
			if (transliterations.containsKey(mc)) {
				sb.append((String) transliterations.get(mc));
			}
			else {
				sb.append(mc.getC());
			}
		}
		return sb.toString();
	}
	
	private static final class MutableCharacter {
		private char c;
		
		public MutableCharacter(char c) {
			this.c = c;
		}
		
		public MutableCharacter() {
			this((char) 0);
		}
		
		public void setC(char c) {
			this.c = c;
		}
		
		public char getC() {
			return c;
		}

		public boolean equals(Object obj) {
			if (obj instanceof MutableCharacter) {
				return ((MutableCharacter) obj).c == this.c;
			}
			return false;
		}

		public int hashCode() {
			return (int) c;
		}
	}
}
