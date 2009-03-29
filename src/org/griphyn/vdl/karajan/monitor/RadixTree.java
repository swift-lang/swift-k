/*
 * Created on Sep 22, 2007
 */
package org.griphyn.vdl.karajan.monitor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RadixTree {
	private Node root;
	private int size;

	public RadixTree() {
		root = new Node(null, null);
	}

	public Object put(String key, Object value) {
	    if (key == null) {
	        throw new IllegalArgumentException("Key is null");
	    }
	    else if (key.equals("")) {
	        throw new IllegalArgumentException("Key is \"\"");
	    }
		return add(root, key, 0, value);
	}
	
	public Object remove(String key) {
		return remove(root, key, 0);
	}
	
	public Object get(String key) {
		return get(root, key, 0);
	}
	
	public String find(String key) {
		return find(root, key, 0);
	}

	private Object add(Node n, String key, int pos, Object value) {
		if (n.nodes != null) {
			Iterator i = n.nodes.iterator();
			while (i.hasNext()) {
				Node sn = (Node) i.next();
				String sk = sn.key;
				if (key.charAt(pos) == sk.charAt(0)) {
					int j = 1;
					while (j < sk.length() && j + pos < key.length() && key.charAt(j + pos) == sk.charAt(j)) {
						j++;
					}
					if (j == sk.length()) {
						if (j + pos == key.length()) {
							Object old = sn.value;
							sn.value = value;
							return old;
						}
						else {
							return add(sn, key, pos + j, value);
						}
					}
					else {
						String common = sk.substring(0, j);
						String left = sk.substring(j);
						String right = key.substring(pos + j);
						sn.key = common;
						Node ln = new Node(left, sn.value);
						ln.nodes = sn.nodes;
						sn.nodes = null;
						sn.value = null;
						Node rn = new Node(right, value);
						sn.addNode(ln);
						sn.addNode(rn);
						size++;
						return null;
					}
				}
			}
		}
		n.addNode(new Node(key.substring(pos), value));
		size++;
		return null;
	}
	
	private Object remove(Node n, String key, int pos) {
		if (n.nodes != null) {
			Iterator i = n.nodes.iterator();
			while (i.hasNext()) {
				Node sn = (Node) i.next();
				String sk = sn.key;
				if (key.charAt(pos) == sk.charAt(0)) {
					int j = 1;
					while (j < sk.length() && j + pos < key.length() && key.charAt(j + pos) == sk.charAt(j)) {
						j++;
					}
					if (j == sk.length()) {
						Object old;
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
	
	private Object get(Node n, String key, int pos) {
		if (n.nodes != null) {
			Iterator i = n.nodes.iterator();
			while (i.hasNext()) {
				Node sn = (Node) i.next();
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
	
	private String find(Node n, String key, int pos) {
		if (n.nodes != null) {
			Iterator i = n.nodes.iterator();
			while (i.hasNext()) {
				Node sn = (Node) i.next();
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
	
	public List getAll() {
		ArrayList l = new ArrayList();
		traverse(l, root);
		return l;
	}
	
	private void traverse(List l, Node n) {
		if (n.value != null) {
			l.add(n.value);
		}
		List nodes = n.nodes;
		if (nodes != null) {
			Iterator i = nodes.iterator();
			while (i.hasNext()) {
				traverse(l, (Node) i.next());
			}
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		toString(sb, root);
		return sb.toString();
	}
	
	private void toString(StringBuffer sb, Node n) {
		n.toString(sb);
	}

	private class Node {
		private String key;
		private Object value;
		private List nodes;

		public Node(String key, Object value) {
			this.key = key;
			this.value = value;
		}

		public void addNode(Node n) {
			if (nodes == null) {
				nodes = new LinkedList();
			}
			nodes.add(n);
		}
		
		public void removeNode(Node n) {
			nodes.remove(n);
			if (nodes.size() == 0) {
				nodes = null;
			}
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			toString(sb);
			return sb.toString();
		}
		
		private void toString(StringBuffer sb) {
			if (key != null) {
				sb.append('\'');
				sb.append(key);
				sb.append('\'');
			}
			sb.append('(');
			sb.append(value);
			if (nodes != null) {
				sb.append(", ");
				sb.append(nodes);
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
		RadixTree rt = new RadixTree();
		p(rt);
		rt.put("the dog", "0");
		p(rt);
		rt.put("the cat", "1");
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
