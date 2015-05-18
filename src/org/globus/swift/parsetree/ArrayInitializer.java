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
 * Created on Apr 17, 2015
 */
package org.globus.swift.parsetree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ArrayInitializer extends AbstractNode implements Expression {
    public enum Type {
        RANGE, ITEMS;
    }
    
    public static class Range {
        private Expression from;
        private Expression to;
        private Expression step;
        
        public Expression getFrom() {
            return from;
        }
        
        public void setFrom(Expression from) {
            this.from = from;
        }
        
        public Expression getTo() {
            return to;
        }
        
        public void setTo(Expression to) {
            this.to = to;
        }
        
        public Expression getStep() {
            return step;
        }
        
        public void setStep(Expression step) {
            this.step = step;
        }
    }
    
    private Type type;
    private Range range;
    private List<Expression> items;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }
    
    public void addItem(Expression item) {
        if (items == null) {
            items = new ArrayList<Expression>();
        }
        items.add(item);
    }
    
    public List<Expression> getItems() {
        if (items == null) {
            return Collections.emptyList();
        }
        else {
            return items;
        }
    }
    
    

    @Override
    public List<? extends Node> getSubNodes() {
        if (type == Type.RANGE) {
            return Arrays.asList(range.from, range.to, range.step);
        }
        else {
            return getItems();
        }
    }
    
    @Override
    public String getNodeName() {
        return "array initializer";
    }

    @Override
    public Expression.Type getExpressionType() {
        if (type == Type.RANGE) {
            return Expression.Type.RANGE_EXPRESSION;
        }
        else {
            return Expression.Type.ARRAY_EXPRESSION;
        }
    }
}
