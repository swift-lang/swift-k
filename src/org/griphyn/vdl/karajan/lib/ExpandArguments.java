package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.Map;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.Set;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;

import org.griphyn.vdl.mapping.ArrayDataNode;
import org.griphyn.vdl.mapping.DSHandle;

public class ExpandArguments extends VDLFunction {
	public static final Logger logger = Logger.getLogger(ExpandArguments.class);

	static {
		setArguments(ExpandArguments.class, new Arg[] { Arg.VARGS });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		ArrayList l = new ArrayList();
		Object[] items = Arg.VARGS.asArray(stack);
		for (int i = 0; i < items.length; i++) {
			Object item = items[i];
			if(!(item instanceof DSHandle)) {
				throw new RuntimeException("Cannot handle argument implemented by "+item.getClass());
			}

			if(item instanceof ArrayDataNode) {
				ArrayDataNode array = (ArrayDataNode) item;
				Map m=array.getArrayValue();
				Set keySet = m.keySet();
				TreeSet sortedKeySet = new TreeSet(new StringsAsIntegersComparator());
				sortedKeySet.addAll(keySet);
				Iterator it = sortedKeySet.iterator();
				while(it.hasNext()) {
					Object key = it.next();
					l.add(m.get(key));
				}
			} else {
                       		l.add(item);
			}
			// TODO this does not correctly handle structs or
			// externals - at the moment, probably neither of
			// those should be usable as a string. It also
			// does not handle nested arrays. However, none of
			// those should get here in normal operation due
			// to static type-checking
                }
		return l;
	}

	class StringsAsIntegersComparator implements Comparator {
		public int compare(Object l, Object r) {
			Integer lnum = new Integer((String)l);
			Integer rnum = new Integer((String)r);
			return lnum.compareTo(rnum);
		}
	}

}

