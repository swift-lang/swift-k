//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 8, 2012
 */
package k.rt;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class NullSink extends Sink<Object> {
	private static final Iterator<Object> emptyIt = Collections.emptyList().iterator();
	
	@Override
	public boolean add(Object value) {
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends Object> values) {
		return false;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public Iterator<Object> iterator() {
		return emptyIt;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public List<Object> getAll() {
		return Collections.emptyList();
	}	
}
