// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 7, 2004
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import java.util.HashMap;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;

public class Map extends FunctionsCollection {
	
	public static class Entry implements java.util.Map.Entry {
		public Object key;
		public Object value;
		
		public Object getKey() {
			return key;
		}
		public Object getValue() {
			return value;
		}
		public Object setValue(Object value) {
			Object old = this.value;
			this.value = value;
			return old;
		}
	}

	static {
		setArguments("map_map", new Arg[] { Arg.VARGS });
	}

	public Object map_map(VariableStack stack) throws ExecutionException {
		java.util.Map map = new HashMap();
		Object[] items = Arg.VARGS.asArray(stack);
		for (int i = 0; i < items.length; i++) {
			if (items[i] instanceof java.util.Map.Entry) {
				java.util.Map.Entry entry = (java.util.Map.Entry) items[i];
				map.put(entry.getKey(), entry.getValue());
			}
			else if (items[i] instanceof java.util.Map) {
				map.putAll((java.util.Map) items[i]);
			}
			else {
				throw new ExecutionException("Invalid argument (must be map:entry or map): "
						+ items[i] + "(class " + items[i].getClass() + ")");
			}
		}
		return map;
	}
	
	public static final Arg PA_MAP = new Arg.TypedPositional("map", java.util.Map.class, "map");

	static {
		setArguments("map_put", new Arg[] { PA_MAP, Arg.VARGS });
	}

	public Object map_put(VariableStack stack) throws ExecutionException {
		java.util.Map map = (java.util.Map) PA_MAP.getValue(stack);
		Object[] items = Arg.VARGS.asArray(stack);
		for (int i = 0; i < items.length; i++) {
			if (items[i] instanceof java.util.Map.Entry) {
				java.util.Map.Entry entry = (java.util.Map.Entry) items[i];
				map.put(entry.getKey(), entry.getValue());
			}
			else if (items[i] instanceof java.util.Map) {
				map.putAll((java.util.Map) items[i]);
			}
			else {
				Object[] items2 = Arg.VARGS.asArray(stack);
				throw new ExecutionException("Invalid argument (must be map or map:entry): "
						+ items[i]);
			}
		}
		return null;
	}
	
	public static final Arg PA_KEY = new Arg.Positional("key");
	public static final Arg PA_VALUE = new Arg.Positional("value");

	static {
		setArguments("map_entry", new Arg[] { PA_KEY, PA_VALUE });
	}

	public Object map_entry(VariableStack stack) throws ExecutionException {
		Entry entry = new Entry();
		entry.key = PA_KEY.getValue(stack);
		entry.value = PA_VALUE.getValue(stack);
		return entry;
	}

	static {
		setArguments("map_get", new Arg[] { PA_KEY, PA_MAP });
	}

	public Object map_get(VariableStack stack) throws ExecutionException {
		Object key = PA_KEY.getValue(stack);
		java.util.Map map = (java.util.Map) PA_MAP.getValue(stack);
		if (!map.containsKey(key)) {
			throw new ExecutionException("Invalid key: " + key + ". Map contents: " + map);
		}
		return map.get(key);
	}

	static {
		setArguments("map_delete", new Arg[] { PA_KEY, PA_MAP });
	}

	public Object map_delete(VariableStack stack) throws ExecutionException {
		Object key = PA_KEY.getValue(stack);
		java.util.Map map = (java.util.Map) PA_MAP.getValue(stack);
		if (!map.containsKey(key)) {
			throw new ExecutionException("Invalid key: " + key);
		}
		return map.remove(key);
	}

	static {
		setArguments("map_size", new Arg[] { PA_MAP });
	}

	public int map_size(VariableStack stack) throws ExecutionException {
		java.util.Map map = (java.util.Map) PA_MAP.getValue(stack);
		return map.size();
	}

	static {
		setArguments("map_contains", new Arg[] { PA_KEY, PA_MAP });
	}

	public boolean map_contains(VariableStack stack) throws ExecutionException {
		Object key = PA_KEY.getValue(stack);
		java.util.Map map = (java.util.Map) PA_MAP.getValue(stack);
		return map.containsKey(key);
	}
}