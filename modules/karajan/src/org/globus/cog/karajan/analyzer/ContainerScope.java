//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 5, 2012
 */
package org.globus.cog.karajan.analyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.globus.cog.karajan.parser.WrapperNode;

public class ContainerScope extends Scope {
	private ArrayList<Boolean> map;
	
	public ContainerScope() {
		this(null, null);
	}
	
	public ContainerScope(WrapperNode owner, Scope parent) {
		super(owner, parent);
		map = new ArrayList<Boolean>();
	}

	@Override
	protected ContainerScope getContainerScope() {
		return this;
	}

	@Override
	public Checkpoint checkpoint() {
	    if (CompilerSettings.DEBUG_STACK_ALLOCATION) {
	        List<Integer> l = Collections.emptyList();
            dump("checkpoint", l);
        }
		return new Checkpoint(map);
	}

	@Override
	public void restore(Checkpoint c) {
		this.map = c.map;
		if (CompilerSettings.DEBUG_STACK_ALLOCATION) {
            List<Integer> l = Collections.emptyList();
            dump("restore", l);
        }
	}

	public int allocate(Var ref) {
		int index = findUnused();
		if (index == -1) {
			// extend by one
			index = map.size();
		}
		set(index);
		if (CompilerSettings.DEBUG_STACK_ALLOCATION) {
			dump("allocate", Collections.singletonList(index));
		}
		return index;
	}
	
	private void set(int index) {
		while (map.size() <= index) {
			map.add(Boolean.FALSE);
		}
		map.set(index, Boolean.TRUE);
	}
	
	private void unset(int index) {
		map.set(index, Boolean.FALSE);
	}
	
	private int findUnused() {
		for (int i = 0; i < map.size(); i++) {
			if (!map.get(i)) {
				return i;
			}
		}
		return -1;
	}
	
	private void dump(String id, List<Integer> changed) {
		System.out.println("C - " + owner + " - " + id + ":");
		System.out.println(map("\t", changed));
	}
	
	protected String map(String header, Collection<Integer> changed) {
		StringBuilder sb = new StringBuilder();
		sb.append(header);
		for (int i = 0; i < size(); i++) {
			if (i % 10 == 0) {
				sb.append("|");
			}
			if (map.get(i)) {
				if (changed.contains(i)) {
					sb.append("+");
				}
				else {
					sb.append("#");
				}
			}
			else {
				if (changed.contains(i)) {
					sb.append("-");
				}
				else {
					sb.append(".");
				}
			}
		}
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		List<Integer> l = Collections.emptyList();
		return "C - " + owner + "\n" + map("\t", l) + (parent == null ? "" : "\n" + parent.toString());
	}

	/**
	 * Allocate index at the end of the of the frame 
	 */
	public int allocateFixed(Var ref) {
		int index = map.size();
		set(index);
		if (CompilerSettings.DEBUG_STACK_ALLOCATION) {
			dump("allocateFixed", Collections.singletonList(index));
		}
		return index;
	}

	public void releaseAll(Collection<Var> vars) {
		List<Integer> l = new ArrayList<Integer>();
		for (Var v : vars) {
			int index = v.getIndex();
			if (index == -1) {
				continue;
			}
			if (CompilerSettings.DEBUG_STACK_ALLOCATION) {
				l.add(index);
				System.out.println("\t-" + v.name + " - " + index);
			}
			vars.remove(index);
			unset(index);
		}
		if (CompilerSettings.DEBUG_STACK_ALLOCATION) {
			dump("releaseAll", l);
		}
	}
	
	public void releaseRange(int first, int last) {
		for (int i = first; i <= last; i++) {
			unset(i);
		}
		if (CompilerSettings.DEBUG_STACK_ALLOCATION) {
			dump("releaseRange", range(first, last));
		}
	}

	private List<Integer> range(int first, int last) {
		List<Integer> l = new ArrayList<Integer>();
		for (int i = first; i <= last; i++) {
			l.add(i);
		}
		return l;
	}

	@Override
	protected int frameBump(int frame) {
		return frame + 1;
	}

	public int allocateContiguous(int size) {
		return allocateContiguous(size, null);
	}
	
	public int allocateContiguous(int size, Object who) {
        int pos = findHole(size);
        if (pos != -1) {
            if (CompilerSettings.DEBUG_STACK_ALLOCATION) {
                dump("allocateContiguous(" + size + ") - " + who, range(pos, pos + size - 1));
            }
            return pos;
        }
        else {
            int tailFree = map.size();
            for (int i = map.size() - 1; i >= 0; i--) {
                if (map.get(i)) {
                    break;
                }
                else {
                    tailFree = i;
                }
            }
            for (int i = 0; i < size; i++) {
                set(i + tailFree);
            }
            if (CompilerSettings.DEBUG_STACK_ALLOCATION) {
                dump("allocateContiguous(" + size + ") - " + who, range(tailFree, tailFree + size - 1));
            }
            return tailFree;
        }
    }
	
	private int findHole(int size) {
		int index = 0;
		while (index + size < map.size()) {
			if (!map.get(index)) {
				boolean found = true;
				int i;
				for (i = index; i < index + size; i++) {
					if (map.get(i)) {
						found = false;
						break;
					}
				}
				if (found) {
					for (i = index; i < index + size; i++) {
						set(i);
					}
					return index;
				}
				index = i;
			}
			index++;
		}
		return -1;
	}
	
	public int size() {
		return map.size();
	}

	@Override
	public void close() {
	}
	
	@Override
	protected void releaseVars(Collection<Var> c) {
		releaseAll(c);
	}

	@Override
	protected String getType() {
		return "C";
	}
}
