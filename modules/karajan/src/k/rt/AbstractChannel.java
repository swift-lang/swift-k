//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 4, 2008
 */
package k.rt;


public abstract class AbstractChannel<T> implements Channel<T> {

    @Override
    public void addAll(Channel<? extends T> c) {
        addAll(c.getAll());
    }

    @SuppressWarnings("unchecked")
	@Override
    public T[] toArray() {
        return (T[]) getAll().toArray();
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public <S> S[] toArray(S[] a) {
        return (S[]) getAll().toArray();
    }

    @Override
    public void close() {
    }

    @Override
    public boolean isClosed() {
        return true;
    }

    @Override
    public Channel<T> subChannel(int fromIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Channel<T> subChannel(int fromIndex, int size) {
        throw new UnsupportedOperationException();
    }    
}
