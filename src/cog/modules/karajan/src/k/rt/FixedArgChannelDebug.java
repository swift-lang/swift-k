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

public class FixedArgChannelDebug<T> extends FixedArgChannel<T> {
	private List<String> names;
	
	public FixedArgChannelDebug(Frame f, int startIndex, int endIndex) {
		super(f, startIndex, endIndex);
	}
	
	public void setNames(List<String> names) {
		this.names = names;
	}
	

	@Override
	public synchronized boolean add(T value) {
		getFrame().setName(getIndex(), names.get(getIndex() - (getBounds() >> 16)));
		return super.add(value);
	}

	@Override
	public synchronized boolean addAll(Collection<? extends T> values) {
		int index = getIndex();
		int bounds = getBounds();
		Frame f = getFrame();
		for (Object o : values) {
			f.setName(index, names.get(index - (bounds >> 16)));
			index++;
		}
		return super.addAll(values);
	}

	@Override
	public synchronized void addAll(Channel<? extends T> c) {
	    int index = getIndex();
        int bounds = getBounds();
        Frame f = getFrame();
		for (Object o : c) {
			f.setName(index, names.get(index - (bounds >> 16)));
			index++;
        }
		super.addAll(c);
	}	
}
