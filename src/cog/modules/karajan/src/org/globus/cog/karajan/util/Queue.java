// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.util;

public final class Queue {
	private Entry head;
	private int size;

	public Queue() {
		head = new Entry(null, null, null);
		head.prev = head;
		head.next = head;
		size = 0;
	}

	public synchronized void enqueue(Object o) {
		Entry e = new Entry(o, head.prev, head);
		head.prev.next = e;
		head.prev = e;
		size++;
	}
	
	public synchronized Object dequeue() { 
		Object o = head.next.obj;
		head.next.next.prev = head;
		head.next = head.next.next;
		return o;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int size() {
		return size;
	}

	public Cursor cursor() {
		return new C();
	}

	private class Entry {
		private final Object obj;
		private Entry next, prev;

		public Entry(Object obj, Entry prev, Entry next) {
			this.obj = obj;
			this.next = next;
			this.prev = prev;
		}

		public String toString() {
			return "Entry(" + obj + ")";
		}
	}

	public interface Cursor {
		boolean hasNext();

		void remove();

		Object next();

		void reset();
	}

	private class C implements Cursor {
		private Entry crt;

		public C() {
			reset();
		}

		public boolean hasNext() {
			return crt.next != head;
		}

		public synchronized Object next() {
			crt = crt.next;
			return crt.obj;
		}

		public synchronized void remove() {
			remove(crt);
		}

		private void remove(Entry e) {
			size--;
			e.next.prev = e.prev;
			e.prev.next = e.next;
		}

		public void reset() {
			crt = head;
		}
	}

	public static void main(String[] args) {
		Queue q = new Queue();
		q.enqueue("a");
		q.enqueue("b");
		q.enqueue("c");
		Cursor c = q.cursor();
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
