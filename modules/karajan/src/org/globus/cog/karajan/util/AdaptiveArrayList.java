//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2006
 */
package org.globus.cog.karajan.util;

import java.util.ArrayList;

/**
 * <p>An implementation of a {@link java.util.List} that tracks
 * typical required capacities and initializes new instances
 * with average sizes inferred from historical information.</p>
 * 
 * <p>The constructor allows initialization with different contexts,
 * each of which represents a different set of historical 
 * information</p>
 *  
 * 
 * @author Mihael Hategan
 *
 */
public class AdaptiveArrayList<T> extends ArrayList<T> {
	private final Context context;
	
	public AdaptiveArrayList(Context context) {
		super(context.getSize());
		this.context = context;
		context.incLists();
	}
	
	public boolean add(T o) {
		context.incItems();
		return super.add(o);
	}
	
	public static class Context {
		private int lists, items;
		
		public Context() {
			lists = 1;
			items = 2;
		}

		public Context(int lists, int items) {
			this.lists = lists;
			this.items = items;
		}
		
		public int getSize() {
			return items / lists + 2;
		}
		
		public synchronized void incLists() {
			lists++;
		}
		
		public synchronized void incItems() {
			items++;
		}
	}
}
