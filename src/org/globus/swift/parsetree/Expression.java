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

import java.util.HashMap;
import java.util.Map;

public interface Expression extends Node {
    public enum Type {
        OR("||"), AND("&&"), NOT("!"),
        // fishy; this works because of the ordering
        NEGATION("-"), 
        PLUS("+"), MINUS("-"), MUL("*"), FDIV("/"), IDIV("%/"), MOD("%"),
        EQ("=="), NE("!="), LT("<"), LE("<="), GT(">"), GE(">="), 
        BOOLEAN_CONSTANT, FLOAT_CONSTANT, INTEGER_CONSTANT, STRING_CONSTANT, 
        ARRAY_SUBSCRIPT_EXPRESSION, ARRAY_EXPRESSION, STRUCT_EXPRESSION, STRUCT_MEMBER_REFERENCE, VARIABLE_REFERENCE,
        RANGE_EXPRESSION,
        CALL_EXPRESSION, FUNCTION_EXPRESSION, 
        STAR_EXPRESSION;
        
        String operator;
        
        Type(String operator) {
            this.operator = operator;
        }
        
        Type() {
            this.operator = null;
        }
        
        public String getOperator() {
            return operator;
        }
        
        
        private static Map<String, Type> ops;
        static {
            ops = new HashMap<String, Type>();
            for (Type t : values()) {
                ops.put(t.getOperator(), t);
            }
        }
        
        public static Type fromOperator(String name) {
            Type t = ops.get(name);
            
            if (t == null) {
                throw new IllegalArgumentException("Unknown operator: " + name);
            }
            
            return t;
        }
    }
    
    Type getExpressionType();
}
