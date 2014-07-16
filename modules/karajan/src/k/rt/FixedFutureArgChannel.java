//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 6, 2012
 */
package k.rt;

import java.util.Collection;
import java.util.List;

import org.globus.cog.karajan.analyzer.CompilerSettings;

public class FixedFutureArgChannel<T> extends Sink<T> {
	private final Frame f;
	private int index, end, start;
	private List<String> names;
	
	public FixedFutureArgChannel(Frame f, int startIndex, int endIndex) {
		this.f = f;
		this.start = startIndex;
		this.index = startIndex;
		this.end = endIndex;
		setFutures();
    }
    
    private void setFutures() {
    	for (int i = start; i <= end; i++) {
    		f.set(i, new FutureObject());
    	}
	}
	
	public void setNames(List<String> names) {
		this.names = names;
	}
	

	@Override
	public synchronized boolean add(T value) {
		if (index > end) {
			throw new IndexOutOfBoundsException();
		}
		if (CompilerSettings.DEBUG) {
			f.setName(index, names.get(index - start));
		}
		((FutureObject) f.get(index++)).setValue(value);
		return true;
	}

	@Override
	public synchronized boolean addAll(Collection<? extends T> values) {
		if (index + values.size() - 1 > end) {
			throw new IndexOutOfBoundsException();
		}
		for (Object o : values) {
			if (CompilerSettings.DEBUG) {
				f.setName(index, names.get(index - start));
			}
			((FutureObject) f.get(index++)).setValue(o);
		}
		return true;
	}

	@Override
	public synchronized void addAll(Channel<? extends T> c) {
		if (index + c.size() - 1 > end) {
			throw new IndexOutOfBoundsException();
		}
		for (Object o : c) {
			if (CompilerSettings.DEBUG) {
				f.setName(index, names.get(index - start));
			}
            ((FutureObject) f.get(index++)).setValue(o);
        }
	}
	
	public String toString() {
		return "F<" + index + ", " + end + ">";
	}
}
