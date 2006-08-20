// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 29, 2005
 */
package org.globus.cog.karajan.benchmarks;

import java.util.StringTokenizer;

import org.globus.cog.karajan.stack.DefaultStackFrame;
import org.globus.cog.karajan.stack.StackFrame;

public class FrameBenchmark {
	public static final int COUNT = 100000;
	
	public static final String text = "Twas brillig, and the slithy toves"+
									"Did gyre and gimble in the wabe:"+
									"All mimsy were the borogoves,"+
									"And the mome raths outgrabe."+

									"Beware the Jabberwock, my son!"+
									"The jaws that bite, the claws that catch!"+
									"Beware the Jubjub bird, and shun"+
									"The frumious Bandersnatch!"+

									"He took his vorpal sword in hand:"+
									"Long time the manxome foe he sought"+
									"So rested he by the Tumtum tree,"+
									"And stood awhile in thought.";

	private static String[] words;
	private static Object[] objects;
	
	static {
		StringTokenizer st = new StringTokenizer(text, " ");
		words = new String[st.countTokens()];
		objects = new Object[words.length];
		for (int i = 0; i < words.length; i++) {
			words[i] = st.nextToken();
			objects[i] = new Object();
		}
	}

	public static void main(String[] args) {
		put();
		put_get();
		put_key();
		put_key_get();
		put_remove();
	}
	
	public static void put() {
		System.out.println("Put benchmark:");
		long start, end, start2, end2;
		
		System.out.println("\tDefaultStackFrame: ");
		start2 = System.currentTimeMillis();
		for (int size = 4; size < 32; size+=4) {
			System.out.print("\t\t"+size+" variables ");
			start = System.currentTimeMillis();
			for (int i = 0; i < COUNT; i++) {
				StackFrame sf = new DefaultStackFrame();
				for (int j = 0; j < size; j++) {
					sf.setVar(words[j], objects[j]);
				}
			}
			end = System.currentTimeMillis();
			System.out.println((end - start) +"ms");
		}
		end2 = System.currentTimeMillis();
		System.out.println("\tTotal: "+(end2 - start2) +"ms");
		
		System.out.println("\tFastStackFrame: ");
		start2 = System.currentTimeMillis();
		for (int size = 4; size < 32; size+=4) {
			System.out.print("\t\t"+size+" variables ");
			start = System.currentTimeMillis();
			for (int i = 0; i < COUNT; i++) {
				StackFrame sf = new DefaultStackFrame();
				for (int j = 0; j < size; j++) {
					sf.setVar(words[j], objects[j]);
				}
			}
			end = System.currentTimeMillis();
			System.out.println((end - start) +"ms");
		}
		end2 = System.currentTimeMillis();
		System.out.println("\tTotal: "+(end2 - start2) +"ms");
	}
	
	public static void put_get() {
		System.out.println("Put-get benchmark:");
		long start, end, start2, end2;
		
		System.out.println("\tDefaultStackFrame: ");
		start2 = System.currentTimeMillis();
		for (int size = 4; size < 32; size+=4) {
			System.out.print("\t\t"+size+" variables ");
			start = System.currentTimeMillis();
			for (int i = 0; i < COUNT; i++) {
				StackFrame sf = new DefaultStackFrame();
				for (int j = 0; j < size; j++) {
					sf.setVar(words[j], objects[j]);
				}
				for (int j = 0; j < size; j++) {
					sf.getVar(words[j]);
				}
			}
			end = System.currentTimeMillis();
			System.out.println((end - start) +"ms");
		}
		end2 = System.currentTimeMillis();
		System.out.println("\tTotal: "+(end2 - start2) +"ms");
		
		System.out.println("\tFastStackFrame: ");
		start2 = System.currentTimeMillis();
		for (int size = 4; size < 32; size+=4) {
			System.out.print("\t\t"+size+" variables ");
			start = System.currentTimeMillis();
			for (int i = 0; i < COUNT; i++) {
				StackFrame sf = new DefaultStackFrame();
				for (int j = 0; j < size; j++) {
					sf.setVar(words[j], objects[j]);
				}
				for (int j = 0; j < size; j++) {
					sf.getVar(words[j]);
				}
			}
			end = System.currentTimeMillis();
			System.out.println((end - start) +"ms");
		}
		end2 = System.currentTimeMillis();
		System.out.println("\tTotal: "+(end2 - start2) +"ms");
	}
	
	public static void put_key() {
		System.out.println("Put-containsKey benchmark:");
		long start, end, start2, end2;
		
		System.out.println("\tDefaultStackFrame: ");
		start2 = System.currentTimeMillis();
		for (int size = 4; size < 32; size+=4) {
			System.out.print("\t\t"+size+" variables ");
			start = System.currentTimeMillis();
			for (int i = 0; i < COUNT; i++) {
				StackFrame sf = new DefaultStackFrame();
				for (int j = 0; j < size; j++) {
					sf.setVar(words[j], objects[j]);
				}
				for (int j = 0; j < size; j++) {
					sf.isDefined(words[j]);
				}
			}
			end = System.currentTimeMillis();
			System.out.println((end - start) +"ms");
		}
		end2 = System.currentTimeMillis();
		System.out.println("\tTotal: "+(end2 - start2) +"ms");
		
		System.out.println("\tFastStackFrame: ");
		start2 = System.currentTimeMillis();
		for (int size = 4; size < 32; size+=4) {
			System.out.print("\t\t"+size+" variables ");
			start = System.currentTimeMillis();
			for (int i = 0; i < COUNT; i++) {
				StackFrame sf = new DefaultStackFrame();
				for (int j = 0; j < size; j++) {
					sf.setVar(words[j], objects[j]);
				}
				for (int j = 0; j < size; j++) {
					sf.isDefined(words[j]);
				}
			}
			end = System.currentTimeMillis();
			System.out.println((end - start) +"ms");
		}
		end2 = System.currentTimeMillis();
		System.out.println("\tTotal: "+(end2 - start2) +"ms");
	}
	
	public static void put_key_get() {
		System.out.println("Put-containsKey-get benchmark:");
		long start, end, start2, end2;
		
		System.out.println("\tDefaultStackFrame: ");
		start2 = System.currentTimeMillis();
		for (int size = 4; size < 32; size+=4) {
			System.out.print("\t\t"+size+" variables ");
			start = System.currentTimeMillis();
			for (int i = 0; i < COUNT; i++) {
				StackFrame sf = new DefaultStackFrame();
				for (int j = 0; j < size; j++) {
					sf.setVar(words[j], objects[j]);
				}
				for (int j = 0; j < size; j++) {
					sf.isDefined(words[j]);
					sf.getVar(words[j]);
				}
			}
			end = System.currentTimeMillis();
			System.out.println((end - start) +"ms");
		}
		end2 = System.currentTimeMillis();
		System.out.println("\tTotal: "+(end2 - start2) +"ms");
		
		System.out.println("\tFastStackFrame: ");
		start2 = System.currentTimeMillis();
		for (int size = 4; size < 32; size+=4) {
			System.out.print("\t\t"+size+" variables ");
			start = System.currentTimeMillis();
			for (int i = 0; i < COUNT; i++) {
				StackFrame sf = new DefaultStackFrame();
				for (int j = 0; j < size; j++) {
					sf.setVar(words[j], objects[j]);
				}
				for (int j = 0; j < size; j++) {
					sf.isDefined(words[j]);
					sf.getVar(words[j]);
				}
			}
			end = System.currentTimeMillis();
			System.out.println((end - start) +"ms");
		}
		end2 = System.currentTimeMillis();
		System.out.println("\tTotal: "+(end2 - start2) +"ms");
	}
	
	public static void put_remove() {
		System.out.println("Put-containsKey-get benchmark:");
		long start, end, start2, end2;
		
		System.out.println("\tDefaultStackFrame: ");
		start2 = System.currentTimeMillis();
		for (int size = 4; size < 32; size+=4) {
			System.out.print("\t\t"+size+" variables ");
			start = System.currentTimeMillis();
			for (int i = 0; i < COUNT; i++) {
				StackFrame sf = new DefaultStackFrame();
				for (int j = 0; j < size; j++) {
					sf.setVar(words[j], objects[j]);
				}
				for (int j = 0; j < size; j++) {
					sf.deleteVar(words[j]);
				}
			}
			end = System.currentTimeMillis();
			System.out.println((end - start) +"ms");
		}
		end2 = System.currentTimeMillis();
		System.out.println("\tTotal: "+(end2 - start2) +"ms");
		
		System.out.println("\tFastStackFrame: ");
		start2 = System.currentTimeMillis();
		for (int size = 4; size < 32; size+=4) {
			System.out.print("\t\t"+size+" variables ");
			start = System.currentTimeMillis();
			for (int i = 0; i < COUNT; i++) {
				StackFrame sf = new DefaultStackFrame();
				for (int j = 0; j < size; j++) {
					sf.setVar(words[j], objects[j]);
				}
				for (int j = 0; j < size; j++) {
					sf.deleteVar(words[j]);
				}
			}
			end = System.currentTimeMillis();
			System.out.println((end - start) +"ms");
		}
		end2 = System.currentTimeMillis();
		System.out.println("\tTotal: "+(end2 - start2) +"ms");
	}
}
