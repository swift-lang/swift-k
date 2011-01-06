// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.interfaces;

import java.io.Serializable;

/**
 * Every Grid Task has an associated <code>Specification</code> that dictates
 * the objective of the task and the environment required to achieve the
 * objective. The {@link TaskHandler} manages the tasks based on the parameters
 * specified in the specification. The interpretation of the parameters in the
 * specification is handler specific.
 */

public interface Specification extends Serializable, Cloneable {
    public static final int JOB_SUBMISSION = 1;
    public static final int FILE_TRANSFER = 2;
    public static final int FILE_OPERATION = 3;
    public static final int WS_INVOCATION = 4;

    public void setType(int type);

    public int getType();

    public void setSpecification(String specification);

    public String getSpecification();
    
    public Object clone();
}
