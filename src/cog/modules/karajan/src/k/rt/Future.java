//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 17, 2008
 */
package k.rt;

public interface Future {
    void addListener(FutureListener l, ConditionalYield y);
}
