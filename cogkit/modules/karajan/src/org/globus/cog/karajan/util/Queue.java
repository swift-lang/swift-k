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

// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.util;

/**
 * A queue implementation which allows traversal (using a cursor)
 * which remains valid even when elements are added or removed from
 * the queue. The queue is implemented as a doubly-linked list.
 * 
 * @author Mihael Hategan
 *
 */
public final class Queue<T> {
	private Entry<T> head;
	private int size;

	public Queue() {
		head = new Entry<T>(null, null, null);
		head.prev = head;
		head.next = head;
		size = 0;
	}

	public synchronized void enqueue(T o) {
		Entry<T> e = new Entry<T>(o, head.prev, head);
		head.prev.next = e;
		head.prev = e;
		size++;
	}
	
	public synchronized Object dequeue() { 
		Object o = head.next.obj;
		head.next.next.prev = head;
		head.next = head.next.next;
		size--;
		return o;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int size() {
		return size;
	}

	public Cursor<T> cursor() {
		return new C();
	}
	
	public String toString() {
	    StringBuffer sb = new StringBuffer();
	    sb.append('[');
	    Cursor<T> c = cursor();
	    while (c.hasNext()) {
	        sb.append(c.next());
	        if (c.hasNext()) {
	            sb.append(", ");
	        }
	    }
	    sb.append(']');
	    return sb.toString();
	}

	private class Entry<S> {
		private final S obj;
		private Entry<S> next, prev;

		public Entry(S obj, Entry<S> prev, Entry<S> next) {
			this.obj = obj;
			this.next = next;
			this.prev = prev;
		}

		public String toString() {
			return "Entry(" + obj + ")";
		}
	}

	public interface Cursor<T> {
		boolean hasNext();

		void remove();

		T next();

		void reset();
	}

	private class C implements Cursor<T> {
		private Entry<T> crt;

		public C() {
			reset();
		}

		public boolean hasNext() {
			return crt.next != head;
		}

		public T next() {
			synchronized(Queue.this) {
				crt = crt.next;
				return crt.obj;
			}
		}

		public void remove() {
			synchronized(Queue.this) {
				remove(crt);
			}
		}

		private void remove(Entry<T> e) {
			size--;
			e.next.prev = e.prev;
			e.prev.next = e.next;
		}

		public void reset() {
			crt = head;
		}
	}

	public static void main(String[] args) {
		Queue<String> q = new Queue<String>();
		q.enqueue("a");
		q.enqueue("b");
		q.enqueue("c");
		Cursor<String> c = q.cursor();
		System.err.println("" + c.next() + c.next() + c.next());
		c.reset();
		c.next();
		c.next();
		c.remove();
		System.err.println("" + c.next());
		c.reset();
		System.err.println("" + c.next() + c.next());
		c.reset();
		System.err.println("" + c.next() + c.next() + c.next());
	}
}
