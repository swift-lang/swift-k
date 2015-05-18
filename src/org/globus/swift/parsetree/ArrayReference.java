/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Created on Apr 19, 2015
 */
package org.globus.swift.parsetree;

import java.util.List;

public class ArrayReference extends AbstractNode implements Expression, LValue {
    private LValue base;
    private Expression index;
    
    public ArrayReference(LValue base, Expression index) {
        this.base = base;
        this.index = index;
    }

    public LValue getBase() {
        return base;
    }

    public void setBase(LValue base) {
        this.base = base;
    }

    public Expression getIndex() {
        return index;
    }

    public void setIndex(Expression index) {
        this.index = index;
    }

    @Override
    public List<? extends Node> getSubNodes() {
        return list(index);
    }
    
    @Override
    public String getNodeName() {
        return "array reference";
    }
    
    @Override
    public Expression.Type getExpressionType() {
        return Expression.Type.ARRAY_SUBSCRIPT_EXPRESSION;
    }
}
