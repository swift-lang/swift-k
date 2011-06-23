package org.griphyn.vdl.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a path (into a DSHandle?) and contains helper methods and member
 * classes.
 */

public class Path {
	public static final Path EMPTY_PATH = new EmptyPath();
	public static final Path CHILDREN = Path.parse("*");

	/** A list of Path.Entry that represents the path. */
	private List<Path.Entry> entries;

	/** True if any element of the Path contains a wildcard. */
	private boolean wildcard;
	
	/** Cached string representation
	    @see toString 
	 */
	private String tostrcached;

	public static class EmptyPath extends Path {
		public EmptyPath() {
			super();
		}

		public String toString() {
			return "$";
		}
	}

	/** Represents a component of a path. */
	public static class Entry {

		/**
		 * Indicates whether this path component is an array index. If so, then
		 * name will be the index into the array. If not, then name will be the
		 * name of the subelement.
		 */
		private boolean index;

		/**
		 * The name of this path entry (either the array offset or the type
		 * member name.
		 */
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
            return false;
		}

		public int hashCode() {
			return name.hashCode() + (index ? 0 : 123);
		}

		public String toString() {
			if (index) {
				return '[' + name + ']';
			}
            return name;
		}
	}

	public static Path parse(String path) {
		if (path == null || path.equals("") || path.equals("$")) {
			return Path.EMPTY_PATH;
		}
		ArrayList<Entry> list = new ArrayList<Entry>();
		StringBuilder sb = new StringBuilder();
		Entry e = new Entry();
		boolean wildcard = false;
		for (int i = 0; i < path.length(); i++) {
			char c = path.charAt(i);
			switch (c) {
				case '[': {
					if (sb.length() == 0) {
						continue; // TODO: what case does this capture?
						// attempt to use multidim arrays?
						//[m] it may simply be incomplete/incorrect
					}
				}
				case '.': {
					e.name = sb.toString();
					list.add(e);
					e = new Entry();
					sb.setLength(0);
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

	private Path(List<Entry> entries, boolean wildcard) {
		this.entries = entries;
		this.wildcard = wildcard;
	}

	private Path(List<Entry> entries) {
		this(entries, false);
		for (Entry e : entries) {
			if (e.isWildcard()) {
				this.wildcard = true;
				return;
			}
		}
	}

	private Path(Path other) {
		this.entries = new ArrayList<Entry>(other.entries);
		this.wildcard = other.wildcard;
	}

	/** 
	   Create an empty Path
	 */
	private Path() { 
	    this.entries = Collections.emptyList();
	}
	
	public String getElement(int index) {
		return entries.get(index).name;
	}

	public int size() {
		return entries == null ? 0 : entries.size();
	}

	public String getFirst() {
		return entries.get(0).name;
	} 
	
	public String getLast() {
		return entries.get(entries.size() - 1).name;
	}

	public boolean isEmpty() {
		return entries == null || entries.size() == 0;
	}

	public Path butFirst() {
		return subPath(1);
	}

	public Path subPath(int fromIndex) {
		return subPath(fromIndex, entries.size());
	}

	public Path subPath(int fromIndex, int toIndex) {
		return new Path(entries.subList(fromIndex, toIndex));
	}

	public Path addFirst(String element, boolean index) {
		Path p = new Path(this);
		Entry e;
		p.entries.add(0, e = new Entry(element, index));
		if (e.isWildcard()) {
			this.wildcard = true;
		}
		return p;
	}

	public Path addFirst(String element) {
		return addFirst(element, false);
	}

	public Path addLast(String element, boolean index) {
		Path p = new Path(this);
		Entry e = new Entry(element, index);
		p.entries.add(e);
		if (e.isWildcard()) {
			p.wildcard = true;
		}
		return p;
	}

	public Path addLast(String element) {
		return addLast(element, false);
	}

	/**
	 * Returns a string representation of this path. This method guarantees that
	 * <code>Path.parse(somePath.stringForm()).equals(somePath)</code>, for
	 * any legal Path instances. However, there is no guarantee that
	 * <code>someString.equals(Path.parse(someString).stringForm())</code>.
	 */
	public String stringForm() {
		StringBuilder sb = new StringBuilder();
		Iterator<Entry> i = entries.iterator();
		while (i.hasNext()) {
			sb.append(i.next().name);
			if (i.hasNext()) {
				sb.append('.');
			}
		}
		return sb.toString();
	}

	/**
	 * Returns a human readable string representation of this path. The string
	 * returned by this method <strong>will not</strong> correctly be parsed
	 * into the same path by {@link Path.parse}. In other words no guarantee is
	 * made that <code>Path.parse(somePath.toString()).equals(somePath)</code>.
	 * For a consistent such representation of this path use {@link stringForm}.
	 */
	public synchronized String toString() {
	    if (tostrcached != null) {
	        return tostrcached;
	    }
		StringBuffer sb = new StringBuffer();
		
		for (Entry e : entries) {
			
			if (e.isIndex()) {
				sb.append('[');
				sb.append(e.getName());
				sb.append(']');
			}
			else {
				sb.append('.');
				sb.append(e.getName());
			}
		}
		return tostrcached = sb.toString();
	}

	public Iterator<Entry> iterator() {
		return entries.iterator();
	}

	public boolean isArrayIndex(int pathIndex) {
		Entry e = entries.get(pathIndex);
		return e.index;
	}

	public boolean isWildcard(int pathIndex) {
		Entry e = entries.get(pathIndex);
		return e.isWildcard();
	}

	public boolean hasWildcards() {
		return wildcard;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Path) {
			Path other = (Path) obj;
			if (entries.size() != other.size()) {
				return false;
			}
			Iterator<Entry> i = entries.iterator();
			Iterator<Entry> o = other.entries.iterator();
			while (i.hasNext()) {
				if (!i.next().equals(o.next())) {
					return false;
				}
			}
			return true;
		}
        return false;
	}

	public int hashCode() {
		int hash = 0;
		for (Entry e : entries) { 
		    hash <<= 1;
			hash += e.hashCode();
		}
		return hash;
	}

	public Path append(Path path) {
		Path p = new Path(this);
		p.entries.addAll(path.entries);
		return p;
	}

}
