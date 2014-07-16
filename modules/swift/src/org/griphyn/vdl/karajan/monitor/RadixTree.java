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


/*
 * Created on Sep 22, 2007
 */
package org.griphyn.vdl.karajan.monitor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RadixTree<T> {
	private Node<T> root;
	private int size;

	public RadixTree() {
		root = new Node<T>(null, null);
	}

	public T put(String key, T value) {
	    if (key == null) {
	        throw new IllegalArgumentException("Key is null");
	    }
	    else if (key.equals("")) {
	        throw new IllegalArgumentException("Key is \"\"");
	    }
		return add(root, key, 0, value);
	}
	
	public T remove(String key) {
		return remove(root, key, 0);
	}
	
	public T get(String key) {
		return get(root, key, 0);
	}
	
	public String find(String key) {
		return find(root, key, 0);
	}

	private T add(Node<T> n, String key, int pos, T value) {
		if (n.nodes != null) {
		    for (Node<T> sn : n.nodes) {
				String sk = sn.key;
				if (key.charAt(pos) == sk.charAt(0)) {
					int j = 1;
					while (j < sk.length() && j + pos < key.length() && key.charAt(j + pos) == sk.charAt(j)) {
						j++;
					}
					if (j == sk.length()) {
						if (j + pos == key.length()) {
							T old = sn.value;
							sn.value = value;
							return old;
						}
						else {
							return add(sn, key, pos + j, value);
						}
					}
					else if (j + pos == key.length()) {
					    String common = sk.substring(0, j);
					    String left = sk.substring(j);
					    Node<T> ln = new Node<T>(left, sn.value);
                        ln.nodes = sn.nodes;
                        sn.key = common;
                        sn.nodes = null;
                        sn.value = value;
                        sn.addNode(ln);
                        return null;
					}
					else {
						String common = sk.substring(0, j);
						String left = sk.substring(j);
						String right = key.substring(pos + j);
						sn.key = common;
						Node<T> ln = new Node<T>(left, sn.value);
						ln.nodes = sn.nodes;
						sn.nodes = null;
						sn.value = null;
						Node<T> rn = new Node<T>(right, value);
						sn.addNode(ln);
						sn.addNode(rn);
						size++;
						return null;
					}
				}
			}
		}
		n.addNode(new Node<T>(key.substring(pos), value));
		size++;
		return null;
	}
	
	private T remove(Node<T> n, String key, int pos) {
		if (n.nodes != null) {
		    for (Node<T> sn : n.nodes) {
				String sk = sn.key;
				if (key.charAt(pos) == sk.charAt(0)) {
					int j = 1;
					while (j < sk.length() && j + pos < key.length() && key.charAt(j + pos) == sk.charAt(j)) {
						j++;
					}
					if (j == sk.length()) {
						T old;
						if (j + pos == key.length()) {
							old = sn.value;
							size--;
							sn.value = null;
						}
						else {
							old = remove(sn, key, pos + j);
						}
						if (sn.nodes == null) {
							n.removeNode(sn);
						}
						return old;
					}
					else {
						//no exact match
						return null;
					}
				}
			}
		}
		return null;
	}
	
	private T get(Node<T> n, String key, int pos) {
		if (n.nodes != null) {
		    for (Node<T> sn : n.nodes) {
				String sk = sn.key;
				if (key.charAt(pos) == sk.charAt(0)) {
					int j = 1;
					while (j < sk.length() && j + pos < key.length() && key.charAt(j + pos) == sk.charAt(j)) {
						j++;
					}
					if (j == sk.length()) {
						if (j + pos == key.length()) {
							return sn.value;
						}
						else {
							return get(sn, key, pos + j);
						}
					}
					else {
						return null;
					}
				}
			}
		}
		return null;
	}
	
	private String find(Node<T> n, String key, int pos) {
		if (n.nodes != null) {
		    for (Node<T> sn : n.nodes) {
				String sk = sn.key;
				if (key.charAt(pos) == sk.charAt(0)) {
					int j = 1;
					while (j < sk.length() && j + pos < key.length() && key.charAt(j + pos) == sk.charAt(j)) {
						j++;
					}
					if (j == sk.length()) {
						if (j + pos == key.length()) {
							return key;
						}
						else {
							return find(sn, key, pos + j);
						}
					}
					else {
						return key.substring(0, j + pos);
					}
				}
			}
		}
		return key.substring(0, pos);
	}
	
	public int size() {
		return size;
	}
	
	public List<T> getAll() {
		ArrayList<T> l = new ArrayList<T>();
		traverse(l, root);
		return l;
	}
	
	private void traverse(List<T> l, Node<T> n) {
		if (n.value != null) {
			l.add(n.value);
		}
		List<Node<T>> nodes = n.nodes;
		if (nodes != null) {
		    for (Node<T> sn : nodes) {
				traverse(l, sn);
			}
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		toString(sb, root);
		return sb.toString();
	}
	
	private void toString(StringBuffer sb, Node<T> n) {
		n.toString(sb, 0);
	}

	private class Node<S> {
		private String key;
		private S value;
		private List<Node<S>> nodes;

		public Node(String key, S value) {
			this.key = key;
			this.value = value;
		}

		public void addNode(Node<S> n) {
			if (nodes == null) {
				nodes = new LinkedList<Node<S>>();
			}
			nodes.add(n);
		}
		
		public void removeNode(Node<S> n) {
			nodes.remove(n);
			if (nodes.size() == 0) {
				nodes = null;
			}
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			toString(sb, 0);
			return sb.toString();
		}
		
		private void toString(StringBuffer sb, int level) {
		    sb.append('\n');
		    for (int i = 0; i < level; i++) {
		        sb.append('\t');
		    }
			if (key != null) {
				sb.append('\'');
				sb.append(key);
				sb.append('\'');
			}
			sb.append('(');
			sb.append(value);
			if (nodes != null) {
				sb.append(", ");
				for (Node<S> n : nodes) {
				    n.toString(sb, level + 1);
				}
			}
			sb.append(')');
		}
	}
	
	private static void p(Object x) {
		if (x instanceof String) {
			System.out.println("'" + x + "'");
		}
		else {
			System.out.println(x);
		}
	}
	
	public static void main(String[] args) {
		RadixTree<String> rt = new RadixTree<String>();
		p(rt);
		rt.put("the dog", "0");
		p(rt);
		rt.put("the cats", "1");
		p(rt);
		rt.put("the cat", "3");
		p(rt);
		rt.put("the dog barks", "4");
		p(rt);
		rt.put("the dollar", "2");
		p(rt);
		rt.remove("the cat");
		p(rt);
		p(rt.find("who?"));
		p(rt.get("the dog"));
		p(rt.find("the dog ate the cat"));
	}
}
