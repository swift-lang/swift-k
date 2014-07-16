// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.abstraction.impl.common.taskgraph;

import java.util.Enumeration;
import java.util.Vector;

import org.globus.cog.abstraction.interfaces.Dependency;
import org.globus.cog.abstraction.interfaces.ExecutableObject;

public class DependencyImpl implements Dependency {
    private int type = Dependency.NONE;
    private Vector dependencyList = null;

    public DependencyImpl() {
        this.dependencyList = new Vector();
    }

    public DependencyImpl(int type) {
        this.type = type;
        this.dependencyList = new Vector();
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public Enumeration elements() {
        return this.dependencyList.elements();
    }

    public Enumeration getDependents(ExecutableObject executableObject) {
        Vector dependentList = new Vector();
        Enumeration en = this.dependencyList.elements();
        while (en.hasMoreElements()) {
            DependencyPair pair = (DependencyPair) en.nextElement();
            ExecutableObject from = pair.getFrom();
            if (from.equals(executableObject)) {
                dependentList.add(pair.getTo());
            }
        }
        return dependentList.elements();
    }

    public Enumeration getDependsOn(ExecutableObject executableObject) {
        Vector dependsOnList = new Vector();
        Enumeration en = this.dependencyList.elements();
        while (en.hasMoreElements()) {
            DependencyPair pair = (DependencyPair) en.nextElement();
            ExecutableObject to = pair.getTo();
            if (to.equals(executableObject)) {
                dependsOnList.add(pair.getFrom());
            }
        }
        return dependsOnList.elements();
    }

    public void add(ExecutableObject from, ExecutableObject to) {
        DependencyPair pair = new DependencyPair(from, to);
        this.dependencyList.add(pair);
    }

    public boolean remove(ExecutableObject from, ExecutableObject to) {
        // deletes only the first instance of the "pair"
        DependencyPair pair = new DependencyPair(from, to);
        Enumeration en = this.dependencyList.elements();
        boolean returnValue = false;

        while (en.hasMoreElements()) {
            DependencyPair newPair = (DependencyPair) en.nextElement();
            if (pair.equals(newPair)) {
                returnValue = this.dependencyList.remove(newPair);
                return returnValue;
            }
        }
        return returnValue;
    }

    public boolean removeAllDependents(ExecutableObject executableObject) {
        Enumeration en = this.dependencyList.elements();
        boolean returnValue = false;

        while (en.hasMoreElements()) {
            DependencyPair newPair = (DependencyPair) en.nextElement();
            if (executableObject.equals(newPair.getFrom())) {
                this.dependencyList.remove(newPair);
                returnValue = true;
            }
        }
        return returnValue;
    }

    public boolean removeAllDependsOn(ExecutableObject executableObject) {
        Enumeration en = this.dependencyList.elements();
        boolean returnValue = false;

        while (en.hasMoreElements()) {
            DependencyPair newPair = (DependencyPair) en.nextElement();
            if (executableObject.equals(newPair.getTo())) {
                this.dependencyList.remove(newPair);
                returnValue = true;
            }
        }
        return returnValue;
    }

    public boolean hasDependents(ExecutableObject executableObject) {
        Enumeration en = this.dependencyList.elements();

        while (en.hasMoreElements()) {
            DependencyPair newPair = (DependencyPair) en.nextElement();
            if (executableObject.equals(newPair.getFrom())) {
                return true;
            }
        }
        return false;
    }

    public boolean isDependent(ExecutableObject executableObject) {
        Enumeration en = this.dependencyList.elements();

        while (en.hasMoreElements()) {
            DependencyPair newPair = (DependencyPair) en.nextElement();
            if (executableObject.equals(newPair.getTo())) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(ExecutableObject from, ExecutableObject to) {
        DependencyPair pair = new DependencyPair(from, to);
        Enumeration en = this.dependencyList.elements();

        while (en.hasMoreElements()) {
            DependencyPair newPair = (DependencyPair) en.nextElement();
            if (pair.equals(newPair)) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        return this.dependencyList.size();
    }
}