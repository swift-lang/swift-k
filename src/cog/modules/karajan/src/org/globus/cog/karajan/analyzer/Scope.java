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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.globus.cog.karajan.analyzer.Var.Channel;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.compiled.nodes.user.CBFInvocationWrapper;
import org.globus.cog.karajan.compiled.nodes.user.Function;
import org.globus.cog.karajan.compiled.nodes.user.InvocationWrapper;
import org.globus.cog.karajan.parser.WrapperNode;

public class Scope {
    public static class Checkpoint {
        protected final ArrayList<Boolean> map;

		public Checkpoint(ArrayList<Boolean> map) {
		    this.map = new ArrayList<Boolean>(map);
		}
    }
    
	public abstract static class Def {
		public abstract Node newInstance();
	}
	
	public static class JavaDef extends Def {
		private Class<? extends Node> cls;
		
		public JavaDef(Class<? extends Node> cls) {
			this.cls = cls;
		}

		@Override
		public Node newInstance() {
			try {
				return cls.newInstance();
			}
			catch (Exception e) {
				throw new RuntimeException("Could not instantiate class " + cls.getName(), e);
			}
		}
	}
	
	public static class UserDef extends JavaDef {
		protected final Function fn;
		private boolean recursive;
		private List<InvocationWrapper> recursiveInvocations;
		
		public UserDef(Function fn) {
			super(InvocationWrapper.class);
			this.fn = fn;
		}
				
		@Override
		public Node newInstance() {
			return new InvocationWrapper(fn);
		}
		
		public Function getFunction() {
			return fn;
		}
	}
	
	public static class CBFUserDef extends UserDef {
		public CBFUserDef(Function fn) {
			super(fn);
		}

		@Override
		public Node newInstance() {
			return new CBFInvocationWrapper(fn);
		}
	}
	
	public static class RecursiveUserDef extends JavaDef {
		private final Function fn;
		
		public RecursiveUserDef(Function fn) {
			super(InvocationWrapper.class);
			this.fn = fn;
		}
		
		@Override
		public Node newInstance() {
			return new InvocationWrapper(fn);
		}
	}
	
	public static final String VARGS = "-";
	
	public Scope parent;
	protected WrapperNode owner;
	protected Map<String, Var> vars;
	private Map<String, Map<String, Def>> defs;
	private List<ArgRef<?>> args;
	private boolean params;
	
	public Scope() {
	}
	
	public Scope(Scope scope) {
		this.parent = scope;
	}
	
	public Scope(WrapperNode owner, Scope scope) {
		this.parent = scope;
		this.owner = owner;
	}
	
	protected ContainerScope getContainerScope() {
		return parent.getContainerScope();
	}
	
	public Checkpoint checkpoint() {
	    return getContainerScope().checkpoint();
	}
	
	public void restore(Checkpoint c) {
	    getContainerScope().restore(c);
	}
	
	public boolean hasParams() {
		return params;
	}

	public void setParams(boolean params) {
		this.params = params;
	}

	public Var.Channel addChannel(String name) {
		return addChannel(name, new StaticChannel());
	}
	
	public Var.Channel addChannel(String name, Object value) {
		Var.Channel v = new Var.Channel(name);
		if (value instanceof SingleValuedChannel) {
			v.setSingleValued(true);
		}
		v.setValue(value);
		addVar(v);
		return v;
	}
	
	public Var addOptionalArg(String name) {
		Var v = new Var(name);
		v.setCanBeNull(false);
		addVar(v);
		return v;
	}
	
	public Var addVar(String name) {
		return addVar(name, null);
	}
		
	public Var addVar(String name, Object value) {
	    Var v = new Var(name);
	    v.setValue(value);
	    if (value != null) {
	    	addVarStatic(v);
	    }
	    else {
	    	addVar(v);
	    }
		return v;
	}
				
	public void addVar(Var ref) {
		if (vars == null) {
			vars = new HashMap<String, Var>();
		}
		int index;
		if (vars.containsKey(ref.name)) {
			index = vars.get(ref.name).getIndex();
			if (index == -1) {
			    index = getContainerScope().allocate(ref);
			}
		}
		else {
			index = getContainerScope().allocate(ref);
		}
		if (CompilerSettings.DEBUG_STACK_ALLOCATION) {
		    System.out.println("\t+" + ref.name + " - " + index);
		}
		ref.setIndex(index);
		vars.put(ref.name, ref);
	}
	
	public void addVarNoIndex(Var ref) {
        if (vars == null) {
            vars = new HashMap<String, Var>();
        }
        vars.put(ref.name, ref);
    }
	
	public void removeVar(String name) {
		if (vars == null) {
			return;
		}
		Var existing = vars.remove(name);
		if (existing != null) {
			getContainerScope().releaseVars(Collections.singletonList(existing));
		}
	}
	
	public void addVarStatic(Var ref) {
		if (vars == null) {
			vars = new HashMap<String, Var>();
		}
		vars.put(ref.name, ref);
	}
	
	public void addVar(Var ref, int index) {
		if (vars == null) {
			vars = new HashMap<String, Var>();
		}
		if (CompilerSettings.DEBUG_STACK_ALLOCATION) {
		    System.out.println("\t+" + ref.name + " - " + index);
		}
		ref.setIndex(index);
		vars.put(ref.name, ref);
	}

	public void close() {
		if (vars != null) {
			releaseVars(vars.values());
		}
	}
		
	protected void releaseVars(Collection<Var> c) {
		if (parent != null) {
			parent.releaseVars(c);
		}
		else {
			throw new IllegalStateException("Failed to release vars");
		}
	}

	public Node resolve(String name) {
		Def def = findDefNoNs(name);
		if (def != null) {
			return def.newInstance();
		}
		else {
			int c = name.indexOf(':');
			if (c != -1) {
				def = findDef(name.substring(0, c), name.substring(c + 1));
				if (def != null) {
					return def.newInstance();
				}
			}
		}
		
		throw new RuntimeException("Unknown function: " + name);
	}
	
	private Def findDef(String ns, String name) {
		if (defs != null) {
			Map<String, Def> m = defs.get(name);
			if (m != null && m.containsKey(ns)) {
				return m.get(ns);
			}
		}
		if (parent != null) {
			return parent.findDef(ns, name);
		}
		return null;
	}

	private Def findDefNoNs(String name) {
		List<String> l = new LinkedList<String>();
		getAllNamespaces(name, l);
		for (String ns : l) {
			if (ns == null) {
				return findDef(null, name);
			}
		}
		if (l.size() > 1) {
			throw new RuntimeException("Ambiguous function reference '" + name + "'. Valid choices: " + l);
		}
		return findDefNoNsRecursive(name);
	}

	private void getAllNamespaces(String name, List<String> l) {
		if (defs != null) {
			Map<String, Def> m = defs.get(name);
			if (m != null) {
				l.addAll(m.keySet());
			}
		}
		if (parent != null) {
			parent.getAllNamespaces(name, l);
		}
	}

	private Def findDefNoNsRecursive(String name) {
		if (defs != null) {
			Map<String, Def> m = defs.get(name);
			if (m != null) {
				return m.values().iterator().next();
			}
		}
		if (parent != null) {
			return parent.findDefNoNsRecursive(name);
		}
		else {
			return null;
		}
	}

	public void addDef(String ns, String name, Def def) {
		if (defs == null) {
			defs = new HashMap<String, Map<String, Def>>();
		}
		Map<String, Def> nss = defs.get(name);
		if (nss == null) {
			nss = new HashMap<String, Def>();
			defs.put(name, nss);
		}
		nss.put(ns, def);
	}
	
	public Var.Channel lookupChannel(String name) {
	    return lookupChannelRecursive(name, null);
	}
	
	public Var.Channel lookupChannel(String name, Node src) {
	    return lookupChannelRecursive(name, src);
	}
	
	public Var lookupParam(String name) {
	    return lookupParamRecursive(name, null);
	}
	
	public Var lookupParam(Param p) {
	    return lookupParamRecursive(p.name, null);
	}
	
	public Var lookupParam(String name, Node src) {
	    return lookupParamRecursive(name, src);
	}
	
	public Var lookupParam(Param p, Node src) {
	    return lookupParamRecursive(p.name, src);
	}
	
	protected Var lookupParamRecursive(String var, Node src) {
        if (vars != null) {
            Var existing = vars.get("#param#" + var);
            if (existing != null) {
            	return existing;
            }
        }
        
        if (params) {
        	throw new IllegalArgumentException(owner + " does not have a parameter named '" + var + "'");
        }
        
        if (parent != null) {
            return parent.lookupParamRecursive(var, src);
        }
        else {
            throw new IllegalArgumentException("Parameter not found: " + var);
        }
	}
	
	public Var lookup(String name) {
	    return lookup(name, 0);
	}
				
	public Var.Channel lookupChannel(Param p) {
	    return lookupChannelRecursive(p.name, null);
	}
	
	public Var.Channel lookupChannel(Param p, Node src) {
	    return lookupChannelRecursive(p.name, src);
	}
	
	protected Var.Channel lookupChannelRecursive(String var, Node src) {
        if (vars != null) {
            Var existing = vars.get("#channel#" + var);
            if (existing != null && !((Var.Channel) existing).isDisabled()) {
            	return (Var.Channel) existing;
            }
        }
        
        if (parent != null) {
            return parent.lookupChannelRecursive(var, src);
        }
        else {
            throw new IllegalArgumentException("Channel not found: " + var);
        }
	}
		
    protected Var lookup(String var, int frame) {
        if (vars != null) {
            Var existing = vars.get(var);
            if (existing != null) {
            	return existing;
            }
        }
        
        if (parent != null) {
            return parent.lookup(var, frameBump(frame));
        }
        else {
            throw new VariableNotFoundException(var);
        }
	}
	
	protected int frameBump(int frame) {
		return frame;
	}

	public <T> VarRef<T> getVarRef(String name) {
		return getVarRef(name, 0);
	}
	
	public <T> VarRef<T> getVarRef(Var var) {
		return getVarRef(var.name, 0);
	}
	
	@SuppressWarnings("unchecked")
	protected <T> VarRef<T> getVarRef(String name, int frame) {
        if (vars != null) {
            Var existing = vars.get(name);
            if (existing != null) {
            	if (!existing.isDynamic() && existing.getValue() != null) {
            		return new VarRef.Static<T>((T) existing.getValue());
            	}
            	else {
            		if (existing.getCanBeNull()) {
            			if (frame == 0) {
            				return new VarRef.DynamicLocal<T>(name, existing.getIndex());
            			}
            			else {
            				return new VarRef.Dynamic<T>(name, frame, existing.getIndex());
            			}
            		}
            		else {
            			return new VarRef.DynamicNotNull<T>(name, frame, existing.getIndex());
            		}
            	}
            }
        }
        
        if (parent != null) {
            return parent.getVarRef(name, frameBump(frame));
        }
        else {
            throw new RuntimeException("Variable not found: " + name);
        }
    }
	
	public <T> ChannelRef<T> getChannelRef(Var.Channel var) {
		return getChannelRefRecursive(var.name, 0);
	}
	
	protected <T> ChannelRef<T> getChannelRefRecursive(String name, int frame) {
        if (vars != null) {
            Var.Channel existing = (Channel) vars.get(name);
            if (existing != null) {
            	if (existing.isSingleValued()) {
            		return new ChannelRef.ReturnSingleValued<T>(frame, existing.getIndex());
            	}
            	else {
            		return new ChannelRef.Return<T>(frame, existing.getIndex());
            	}
            }
        }
        
        if (parent != null) {
            return parent.getChannelRefRecursive(name, frameBump(frame));
        }
        else {
            throw new RuntimeException("Variable not found: " + name);
        }
    }
	
	public <T> VarRef<T> addLocal(String name) {
		addVar(name);
		return getVarRef(name);
	}
	
	public void addAlias(Var exc, String name) {
		Var v = new Var(name);
		v.setValue(exc.getValue());
		v.setIndex(exc.getIndex());
		if (vars == null) {
			vars = new HashMap<String, Var>();
		}
		vars.put(name, v);
	}
	
	public void addPositionalParams(List<Param> args) {
		if (args == null || args.isEmpty()) {
			return;
		}
		for (Param p : args) {
			Var v = new ParamWrapperVar.Positional(p);
			addVarNoIndex(v);
		}
	}
	
	public void allocatePositionalParams(List<Param> args) {
		if (args == null || args.isEmpty()) {
			return;
		}
		int index = getContainerScope().allocateContiguous(args.size(), owner);
		for (Param p : args) {
			Var v = new ParamWrapperVar.Positional(p);
			addVar(v, index++);
		}
	}
	
	public void addOptionalParams(List<Param> args) {
		if (args == null || args.isEmpty()) {
			return;
		}
		
		for (Param p : args) {
			Var v = new ParamWrapperVar.Optional(p, null);
			addVarNoIndex(v);
		}
	}
	
	public ParamWrapperVar.IndexRange allocateOptionalParams(List<Param> args) {
		if (args == null || args.isEmpty()) {
			return null;
		}
		
		int index = getContainerScope().allocateContiguous(args.size(), owner);
		ParamWrapperVar.IndexRange ir = new ParamWrapperVar.IndexRange(index, args.size());
		for (Param p : args) {
			Var v = new ParamWrapperVar.Optional(p, ir);
			addVarNoIndex(v);
		}
		return ir;
	}
	
	public void addChannelParams(List<Param> channels) {
		for (Param c : channels) {
			Var.Channel v = new Var.Channel(c.name);
			v.setValue(c.value);
			addVar(v);
		}
	}
	
	public int addParams(List<Param> channels, List<Param> optional, List<Param> positional) {
		return addParams(channels, optional, positional, null);
	}
	
	public int addParams(List<Param> channels, List<Param> optional, List<Param> positional, Object owner) {
		int index = getContainerScope().allocateContiguous(channels.size() + optional.size() + positional.size(), owner);
		int first = index;
		for (Param c : channels) {
			Var.Channel v = new Var.Channel(c.name);
			v.setValue(c.value);
			addVar(v, index++);
		}
		for (Param o :  positional) {
			Var v = new ParamWrapperVar.Positional(o);
			addVar(v, index++);
		}
		for (Param o :  optional) {
            Var v = new ParamWrapperVar.UDFOptional(o);
            addVar(v, index++);
        }
		return first;
	}
	
	public LinkedList<Var> addVarsContiguous(LinkedList<String> vars) {
		LinkedList<Var> l = new LinkedList<Var>();
		int index = getContainerScope().allocateContiguous(vars.size());
		for (String name : vars) {
			Var v = new Var(name);
			addVar(v, index++);
			l.add(v);
		}
		return l;
	}
	
	public int reserveRange(int size) {
		return getContainerScope().allocateContiguous(size);
	}
	
	public void releaseRange(int first, int last) {
		if (first <= last) {
			getContainerScope().releaseRange(first, last);
		}
	}
	
	public void reorderContiguous(LinkedList<Var> initial, LinkedList<Var> vars) {
		if (initial.size() == vars.size()) {
			return;
		}
		int firstIndex = initial.getFirst().getIndex();
		int lastInitialIndex = initial.getLast().getIndex();
		int index = firstIndex;
		for (Var v : vars) {
			v.setIndex(index);
			index++;
		}
		for (Var v : initial) {
			if (!vars.contains(v)) {
				this.vars.remove(v.name);
			}
		}
		getContainerScope().releaseRange(index, lastInitialIndex);
	}
	
	public List<ArgRef<?>> getArgs() {
		if (args != null) {
			return args;
		}
		if (parent != null) {
			return parent.getArgs();
		}
		return null;
	}

	public int size() {
		if (vars == null) {
			return 0;
		}
		else {
			return vars.size();
		}
	}
	
	public String toString() {
		String type = getType();
		if (vars == null) {
			if (parent == null) {
				return type + fmt(Collections.emptyList(), owner);
			}
			else {
				return type + fmt(Collections.emptyList(), owner) + "\n" + parent.toString();
			}
		}
		else {
			if (parent == null) {
				return type + fmt(vars.values(), owner);
			}
			else {
				return type + fmt(vars.values(), owner) + "\n" + parent.toString();
			}
		}
	}
	
	private String fmt(Collection<?> l, WrapperNode owner) {
		StringBuilder sb = new StringBuilder();
		sb.append(" - ");
		sb.append(owner);
		for (Object o : l) {
		    sb.append("\n\t\t");
		    sb.append(o);
		}
		return sb.toString();
	}

	protected String getType() {
		return "S";
	}

	public RootScope getRoot() {
		if (parent != null) {
			return parent.getRoot();
		}
		else {
			return null;
		}
	}

	public Collection<String> getAllChannelNames() {
		Set<String> s = new HashSet<String>();
		getAllChannelNamesRecursive(s);
		return s;
	}

	protected void getAllChannelNamesRecursive(Set<String> s) {
		if (vars != null) {
			for (Var v : vars.values()) {
				if (v instanceof Var.Channel) {
					s.add(v.name.substring("#channel#".length()));
				}
			}
		}
		if (parent != null) {
			parent.getAllChannelNamesRecursive(s);
		}
	}
}
