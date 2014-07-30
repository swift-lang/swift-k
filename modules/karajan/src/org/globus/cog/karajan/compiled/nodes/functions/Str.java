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
 * Created on Mar 21, 2014
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import k.rt.Channel;
import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.globus.cog.karajan.util.TypeUtil;

public class Str {
	public static class Concat extends AbstractSingleValuedFunction {
        private ChannelRef<Object> c_vargs;
        
        @Override
        protected Param[] getParams() {
            return params("...");
        }

        @Override
        public Object function(Stack stack) {
            StringBuilder sb = new StringBuilder();
            for (Object o : c_vargs.get(stack)) {
                sb.append(TypeUtil.toString(o));
            }
            return sb.toString();
        }
    }
	
	public static class Join extends AbstractSingleValuedFunction {
        private ChannelRef<Object> c_vargs;
        
        @Override
        protected Param[] getParams() {
            return params("...");
        }

        @Override
        public Object function(Stack stack) {
            Channel<Object> args = c_vargs.get(stack);
            if (args.size() > 2) {
                throw new ExecutionException("Too many arguments: " + args.size());
            }
            if (args.isEmpty()) {
                throw new ExecutionException("Too few arguments");
            }
            @SuppressWarnings("unchecked")
			Collection<Object> items = (Collection<Object>) args.get(0);
            String delim = " ";
            if (args.size() == 2) {
                delim = (String) args.get(1);
            }
            StringBuilder sb = new StringBuilder();
            Iterator<Object> i = items.iterator();
            while (i.hasNext()) {
                sb.append(i.next());
                if (i.hasNext()) {
                    sb.append(delim);
                }
            }
            return sb.toString();
        }
    }

    
    public static class SubString extends AbstractSingleValuedFunction {
        private ArgRef<String> string;
        private ArgRef<Number> from;
        private ArgRef<Number> to;
        
        @Override
        protected Param[] getParams() {
            return params("string", "from", optional("to", Integer.MAX_VALUE));
        }

        @Override
        public Object function(Stack stack) {
            int to = this.to.getValue(stack).intValue();
            String str = string.getValue(stack);
            if (to == Integer.MAX_VALUE) {
                return str.substring(from.getValue(stack).intValue());
            }
            else if (to < 0) {
                to += str.length() - 1;
            }
            return str.substring(from.getValue(stack).intValue(), to);
        }
    }
    
    public static class Matches extends AbstractSingleValuedFunction {
        private ArgRef<Object> string;
        private ArgRef<String> pattern;
        
        @Override
        protected Param[] getParams() {
            return params("string", "pattern");
        }

        @Override
        public Object function(Stack stack) {
            Object o = string.getValue(stack);
            String str;
            if (o instanceof Exception) {
                str = ((Exception) o).getMessage();
            }
            else {
                str = String.valueOf(o);
            }
            if (str == null) {
                return false;
            }
            else {
                String pat = pattern.getValue(stack);
                Pattern p = Pattern.compile(pat, Pattern.DOTALL);
                return p.matcher(str).matches();
            }
        }
    }
    
    public static class Split extends AbstractSingleValuedFunction {
        private ArgRef<String> string;
        private ArgRef<String> separator;
        
        @Override
        protected Param[] getParams() {
            return params("string", "separator");
        }

        @Override
        public Object function(Stack stack) {
            String str = string.getValue(stack);
            String sep = separator.getValue(stack);

            List<String> list = new ArrayList<String>();
            int index = -1;
            int last = 0;
            do {
                index = str.indexOf(sep, index + sep.length());
                if (index > -1) {
                    if (last < index) {
                        list.add(str.substring(last, index));
                    }
                    last = index + sep.length();
                }
            } while (index != -1);
            list.add(str.substring(last, str.length()));
            return list;
        }
    }
    
    public static class Strip extends AbstractSingleValuedFunction {
        private ArgRef<String> string;
        
        @Override
        protected Param[] getParams() {
            return params("string");
        }

        @Override
        public Object function(Stack stack) {
            return string.getValue(stack).trim();
        }
    }

    public static class Chr extends AbstractSingleValuedFunction {
        private ArgRef<Number> code;
    
        @Override
        protected Param[] getParams() {
            return params("code");
        }
        
        @Override
        public Object function(Stack stack) {
            return (char) code.getValue(stack).intValue();
        }
    }
    
    public static class Quote extends AbstractSingleValuedFunction {
        private ArgRef<String> str;
    
        @Override
        protected Param[] getParams() {
            return params("str");
        }
        
        @Override
        public Object function(Stack stack) {
            return '"' + str.getValue(stack) + '"';
        }
    }
    
        public static class ParseNumber extends AbstractSingleValuedFunction {
        private ArgRef<String> str;
        
        @Override
        protected Param[] getParams() {
            return params("str");
        }
        
        @Override
        protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
                throws CompilationException {
            if (str.getValue() != null && staticReturn(scope, Double.parseDouble(str.getValue()))) {
                return null;
            }
            else {
                return super.compileBody(w, argScope, scope);
            }
        }

        @Override
        public Object function(Stack stack) {
            String str = this.str.getValue(stack);
            return Double.parseDouble(str);
        }
    }

}
