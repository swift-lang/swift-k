/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
	public static final Path CHILDREN = Path.parse("[*]");

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
		private Comparable<?> key;

		public Entry(Comparable<?> key, boolean index) {
		    this.key = key;
			this.index = index;
			if (key == null) {
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
		    return false;
		}

		public Comparable<?> getKey() {
			return key;
		}

		public boolean equals(Object obj) {
			if (obj instanceof Entry) {
				Entry other = (Entry) obj;
				if (isWildcard()) {
				    return other.isWildcard();
				}
				return key.equals(other.key);
			}
            return false;
		}

		public int hashCode() {
		    return key.hashCode() + (index ? 0 : 123);
		}

		public String toString() {
			if (index) {
				return '[' + String.valueOf(key) + ']';
			}
			else {
			    return String.valueOf(key);
			}
		}
		
		/*
		 * equals() and hashCode() cannot be used to
		 * properly test equality between a wildcard and 
		 * another entry
		 */
		public static class Wildcard extends Entry {
            @Override
            public boolean isIndex() {
                return true;
            }

            @Override
            public boolean isWildcard() {
                return true;
            }
            
            
		}
	}

	public static Path parse(String path) {
		return new PathParser(path).parse();
	}

	private Path(List<Entry> entries, boolean wildcard) {
		this.entries = entries;
		this.wildcard = wildcard;
	}

	protected Path(List<Entry> entries) {
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
	
	public Comparable<?> getElement(int index) {
		return entries.get(index).key;
	}
	
	public Comparable<?> getKey(int index) {
        return entries.get(index).key;
    }
	
	public Entry getEntry(int index) {
	    return entries.get(index);
	}

	public int size() {
		return entries == null ? 0 : entries.size();
	}

	public Comparable<?> getFirst() {
		return entries.get(0).key;
	} 
	
	public Comparable<?> getLast() {
		return entries.get(entries.size() - 1).key;
	}

	public boolean isEmpty() {
		return entries == null || entries.size() == 0;
	}

	public Path butFirst() {
		return subPath(1);
	}
	
	public Path butLast() {
	    return subPath(0, entries.size() - 1);
	}

	public Path subPath(int fromIndex) {
		return subPath(fromIndex, entries.size());
	}

	public Path subPath(int fromIndex, int toIndex) {
		return new Path(entries.subList(fromIndex, toIndex));
	}

	public Path addFirst(Comparable<?> element, boolean index) {
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

	public Path addLast(Comparable<?> element, boolean index) {
		Path p = new Path(this);
		Entry e = new Entry(element, index);
		p.entries.add(e);
		if (e.isWildcard()) {
			p.wildcard = true;
		}
		return p;
	}
	
	protected void addLast(Entry e) {
	    if (entries.isEmpty()) {
	        entries = new ArrayList<Entry>();
	    }
	    entries.add(e);
	}

	public Path addLast(Comparable<?> element) {
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
		boolean first = true;
		while (i.hasNext()) {
		    Entry e = i.next();
		    if (e.isWildcard()) {
		        sb.append("[*]");
		    }
		    else if (e.isIndex()) {
		        sb.append('[');
		        sb.append(stringForm(e.key));
		        sb.append(']');
		    }
		    else {
		        if (!first) {
		            sb.append('.');
		        }
		        sb.append(e.key);
		    }
		    first = false;
		}
		return sb.toString();
	}

	private String stringForm(Comparable<?> key) {
        if (key instanceof String) {
            return "\"" + escape((String) key) + '"';
        }
        else if (key instanceof Number) {
            return key.toString();
        }
        else {
            throw new IllegalArgumentException("Unsupported key type: " + key);
        }
    }

	/**
	 * TODO: match or use the actual language valid escapes
	 */
    private String escape(String key) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (c == '\\' || c == '"') {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
	 * Returns a human readable string representation of this path. The string
	 * returned by this method <strong>will not necessarily</strong> correctly be parsed
	 * into the same path by {@link Path.parse}. In other words no guarantee is
	 * made that <code>Path.parse(somePath.toString()).equals(somePath)</code>.
	 * For a consistent such representation of this path use {@link stringForm}.
	 */
	public synchronized String toString() {
	    if (tostrcached != null) {
	        return tostrcached;
	    }
		StringBuffer sb = new StringBuffer();
		
		boolean first = true;
		for (Entry e : entries) {
		    if (e.isWildcard()) {
                sb.append("[*]");
            }
		    else if (e.isIndex()) {
				sb.append('[');
				sb.append(e.getKey());
				sb.append(']');
			}
			else {
			    if (!first) {
			        sb.append('.');
			    }
				sb.append(e.getKey());
			}
		    first = false;
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
