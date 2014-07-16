//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 13, 2007
 */
package k.rt;

import java.util.Collection;
import java.util.List;


public interface Channel<T> extends Collection<T> {
	public T get(int index);
	
	public void addAll(Channel<? extends T> c);

    public List<T> getAll();
    
    public T removeFirst();
    
    public boolean isClosed();
    
    public void close();

    public Channel<T> subChannel(int fromIndex);
    
    public Channel<T> subChannel(int fromIndex, int size);
}
