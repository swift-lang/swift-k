/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 28, 2014
 */
package org.griphyn.vdl.mapping.nodes;

import java.util.List;

import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.DuplicateMappingChecker;
import org.griphyn.vdl.mapping.RootHandle;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;


public class NodeFactory {
    public static AbstractDataNode newNode(Field f, RootHandle root, AbstractDataNode parent) {
        AbstractDataNode n;
        Type t = f.getType();
        if (t.isPrimitive()) {
            n = new FuturePrimitiveDataNode(f, root, parent);
        }
        else if (!t.isComposite()) {
            n = new FutureMappedSingleDataNode(f, root, parent);
        }
        else if (t.isArray()) {
            n = new FutureArrayDataNode(f, root, parent);
        }
        else {
            n = new FutureStructDataNode(f, root, parent);
        }
        n.initialize();
        return n;
    }
    
    public static AbstractDataNode newNode(Field f, RootHandle root, AbstractDataNode parent, Object value) {
        AbstractDataNode n;
        Type t = f.getType();
        if (t.isPrimitive()) {
            return new ClosedPrimitiveDataNode(f, root, parent, value);
        }
        else {
            throw new IllegalArgumentException();
        }
    }
    
    public static AbstractDataNode newRoot(Field f, Object value) {
        Type t = f.getType();
        if (t.isPrimitive()) {
            return new RootClosedPrimitiveDataNode(f, value);
        }
        else if (t.isArray() && t.itemType().isPrimitive()){
            return new RootClosedArrayDataNode(f, (List<?>) value, null);
        }
        else {
        	throw new IllegalArgumentException();
        }
    }
    
    public static AbstractDataNode newRoot(Field f, DependentException e) {
        Type t = f.getType();
        if (t.isPrimitive()) {
            return new RootClosedPrimitiveDataNode(f, e);
        }
        else {
            throw new IllegalArgumentException();
        }
    }
    
    public static RootHandle newOpenRoot(Field f, DuplicateMappingChecker dmc) {
        Type t = f.getType();
        if (t.isPrimitive()) {
            return new RootFuturePrimitiveDataNode(f);
        }
        else if (t.isArray()) {
            return new RootFutureArrayDataNode(f, dmc);
        }
        else if (t.isComposite()) {
            return new RootFutureStructDataNode(f, dmc);
        }
        else {
            return new RootFutureMappedSingleDataNode(f, dmc);
        }
    }
    
    public static AbstractDataNode newRoot(Type type, Object value) {
        if (type.isPrimitive()) {
            return new RootClosedPrimitiveDataNode(Field.Factory.getImmutableField("?", type), value);
        }
        else {
            throw new IllegalArgumentException();
        }
    }
}
