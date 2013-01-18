// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.set;

import org.globus.cog.abstraction.impl.common.taskgraph.TaskGraphImpl;
import org.globus.cog.abstraction.interfaces.Dependency;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.Set;

public class SetImpl extends TaskGraphImpl implements Set {
    public SetImpl() {
        super();
    }

    public SetImpl(Identity id) {
        super(id);
    }

    public void setDependency(Dependency dependency) {
        // N/A
    }

    public Dependency getDependency() {
        // A set cannot have any dependency
        return null;
    }

    public void addDependency(Identity from, Identity to) {
        // N/A
    }

    public boolean removeDependency(Identity from, Identity to) {
        // N/A
        return false;
    }
}
