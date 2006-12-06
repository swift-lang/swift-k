/*
 * Created on Jun 9, 2006
 */
package org.griphyn.vdl.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Path {
	public static final Path EMPTY_PATH = new EmptyPath();
	public static final Path CHILDREN = Path.parse("*");

	private List elements;
	private boolean wildcard;

	public static class EmptyPath extends Path {
		public EmptyPath() {
			super(Collections.EMPTY_LIST);
		}

		public String toString() {
			return "$";
		}
	}

	public static class Entry {
		private boolean index;
		private String name;

		public Entry(String name, boolean index) {
			this.name = name;
			this.index = index;
			if (name == null) {
				throw new IllegalArgumentException(
						"Attempted to create a path entry with a null name");
			}
		}

		public Entry(String name) {
			this(name, false);
		}

		public Entry() {
		}

		public boolean isIndex() {
			return index;
		}

		public boolean isWildcard() {
			return (name.length() == 1) && (name.charAt(0) == '*');
		}

		public String getName() {
			return name;
		}

		public boolean equals(Object obj) {
			if (obj instanceof Entry) {
				Entry other = (Entry) obj;
				return name.equals(other.name) && (index == other.index);
			}
			else {
				return false;
			}
		}

		public int hashCode() {
			return name.hashCode() + (index ? 0 : 123);
		}

		public String toString() {
			if (index) {
				return '[' + name + ']';
			}
			else {
				return name;
			}
		}
	}

	public static Path parse(String path) {
		if (path == null || path.equals("") || path.equals("$")) {
			return Path.EMPTY_PATH;
		}
		ArrayList list = new ArrayList();
		StringBuffer sb = new StringBuffer();
		Entry e = new Entry();
		boolean wildcard = false;
		for (int i = 0; i < path.length(); i++) {
			char c = path.charAt(i);
			switch (c) {
				case '[': {
					if (sb.length() == 0) {
						continue;
					}
				}
				case '.': {
					e.name = sb.toString();
					list.add(e);
					e = new Entry();
					sb = new StringBuffer();
					break;
				}
				case ']': {
					e.index = true;
					break;
				}
				case '*': {
					wildcard = true;
				}
				default: {
					sb.append(c);
					break;
				}
			}
		}
		e.name = sb.toString();
		list.add(e);
		list.trimToSize();
		return new Path(list, wildcard);
	}

	private Path(List elements, boolean wildcard) {
		this.elements = elements;
		this.wildcard = wildcard;
	}

	private Path(List elements) {
		this(elements, false);
		Iterator i = elements.iterator();
		while (i.hasNext()) {
			Entry e = (Entry) i.next();
			if (e.isWildcard()) {
				this.wildcard = true;
				return;
			}
		}
	}

	private Path(Path other) {
		this.elements = new ArrayList(other.elements);
		this.wildcard = other.wildcard;
	}

	public String getElement(int index) {
		return ((Entry) elements.get(index)).name;
	}

	public int size() {
		return elements == null ? 0 : elements.size();
	}

	public String getFirst() {
		return ((Entry) elements.get(0)).name;
	}

	public boolean isEmpty() {
		return elements == null || elements.size() == 0;
	}

	public Path butFirst() {
		return subPath(1);
	}

	public Path subPath(int fromIndex) {
		return subPath(fromIndex, elements.size());
	}

	public Path subPath(int fromIndex, int toIndex) {
		return new Path(elements.subList(fromIndex, toIndex));
	}

	public Path addFirst(String element, boolean index) {
		Path p = new Path(this);
		Entry e;
		p.elements.add(0, e = new Entry(element, index));
		if (e.isWildcard()) {
			this.wildcard = true;
		}
		return p;
	}
	
	public Path addFirst(String element) {
		return addFirst(element, false);
	}

	// TODO must check if new entry is a wildcard or not and update the variable
	// properly
	public Path addLast(String element, boolean index) {
		Path p = new Path(this);
		Entry e;
		p.elements.add(e = new Entry(element, index));
		if (e.isWildcard()) {
			p.wildcard = true;
		}
		return p;
	}

	public Path addLast(String element) {
		return addLast(element, false);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		Iterator i = elements.iterator();
		while (i.hasNext()) {
			sb.append(((Entry) i.next()).name);
			if (i.hasNext()) {
				sb.append('.');
			}
		}
		return sb.toString();
	}

	public Iterator iterator() {
		return elements.iterator();
	}

	public boolean isArrayIndex(int pathIndex) {
		Entry e = (Entry) elements.get(pathIndex);
		return e.index;
	}

	public boolean isWildcard(int pathIndex) {
		Entry e = (Entry) elements.get(pathIndex);
		return e.isWildcard();
	}

	public boolean hasWildcards() {
		return wildcard;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Path) {
			Path other = (Path) obj;
			if (elements.size() != other.size()) {
				return false;
			}
			Iterator i = elements.iterator();
			Iterator o = other.elements.iterator();
			while (i.hasNext()) {
				if (!i.next().equals(o.next())) {
					return false;
				}
			}
			return true;
		}
		else {
			return false;
		}
	}

	public int hashCode() {
		int hash = 0;
		Iterator i = elements.iterator();
		while (i.hasNext()) {
			hash <<= 1;
			hash += i.next().hashCode();
		}
		return hash;
	}

}
