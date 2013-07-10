//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 18, 2008
 */
package k.rt;

public class Futures {
    public static Object futureCheck(final Object o) {
        if (o instanceof FutureValue) {
            return ((FutureValue) o).getValue();
        }
        else {
            return o;
        }
    }
}
