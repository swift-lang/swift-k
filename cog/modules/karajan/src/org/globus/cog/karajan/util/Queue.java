
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public final class Queue {
	private final List list;
	private int size;

	public Queue() {
		list = new ArrayList();
		size = 0;
	}

	public void enqueue(Object o) {
		list.add(o);
		size++;
	}

	public Object dequeue() {
		size--;
		return list.remove(0);
	}

	public Object peek(){
		if (size >= 1){
			return list.get(0);
		}
		else{
			return null;
		}
	}
	public boolean isEmpty() {
		return size==0;
	}

	public int size() {
		return size;
	}
	
	public Object remove(int index){
		size--;
	    return list.remove(index);
	}
	
	public Object get(int index){
	    return list.get(index);
	}
	
	public Collection getAll() {
		return list;
	}
}
