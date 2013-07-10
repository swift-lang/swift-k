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

public class FixedArgChannel<T> extends Sink<T> {
	private final Frame f;
	private int index;
	private final int bounds;
	private List<String> names;
	
	public FixedArgChannel(Frame f, int startIndex, int endIndex) {
		this.f = f;
		this.index = startIndex;
		this.bounds = endIndex + (startIndex << 16);
	}
	
	public void setNames(List<String> names) {
		this.names = names;
	}

	@Override
	public synchronized boolean add(T value) {
		if (index > (bounds & 0x0000ffff)) {
			throw new IllegalExtraArgumentException(value);
		}
		if (CompilerSettings.DEBUG) {
			f.setName(index, names.get(index - (bounds >> 16)));
		}
		f.set(index++, value);
		return true;
	}

	@Override
	public synchronized boolean addAll(Collection<? extends T> values) {
		if (index + values.size() - 1 > (bounds & 0x0000ffff)) {
			throw new IndexOutOfBoundsException();
		}
		for (Object o : values) {
			if (CompilerSettings.DEBUG) {
				f.setName(index, names.get(index - (bounds >> 16)));
			}
			f.set(index++, o);
		}
		return true;
	}

	@Override
	public synchronized void addAll(Channel<? extends T> c) {
		if (index + c.size() - 1 > (bounds & 0x0000ffff)) {
			throw new IndexOutOfBoundsException();
		}
		for (Object o : c) {
			if (CompilerSettings.DEBUG) {
				f.setName(index, names.get(index - (bounds >> 16)));
			}
            f.set(index++, o);
        }
	}
	
	public String toString() {
		return "<" + index + ", " + (bounds & 0x0000ffff) + ">";
	}
}
