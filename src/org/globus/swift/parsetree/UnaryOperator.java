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

public class UnaryOperator extends AbstractNode implements Expression {    
    private Type type;
    private Expression arg;
    
    public UnaryOperator(Type type, Expression arg) {
        this.type = type;
        this.arg = arg;
    }
    
    public UnaryOperator(String name, Expression arg) {
        this.type = Type.fromOperator(name);
        this.arg = arg;
    }

    public Type getType() {
        return type;
    }
    
    public Type getExpressionType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Expression getArg() {
        return arg;
    }

    public void setArg(Expression arg) {
        this.arg = arg;
    }
    
    @Override
    public List<? extends Node> getSubNodes() {
        return list(arg);
    }
    
    @Override
    public String getNodeName() {
        return type + " operator";
    }
}
