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


package org.griphyn.vdl.karajan.lib.swiftscript;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import k.rt.Channel;
import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractSingleValuedFunction;
import org.globus.cog.karajan.util.TypeUtil;
import org.griphyn.vdl.karajan.lib.SwiftFunction;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.RootArrayDataNode;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.type.Types;
import org.griphyn.vdl.util.VDL2Config;

public class Misc {

	private static final Logger logger = Logger.getLogger(Misc.class);
	
	public static final boolean PROVENANCE_ENABLED;
	
	static {
		boolean v;
		try {
            v = VDL2Config.getConfig().getProvenanceLog();
        }
        catch (IOException e) {
            v = false;
        }
        PROVENANCE_ENABLED = v;
	}

	private static final Logger traceLogger = Logger.getLogger("org.globus.swift.trace");
	
	public static class Trace extends InternalFunction {
		private ChannelRef<AbstractDataNode> c_vargs;

        @Override
        protected Signature getSignature() {
            return new Signature(params("..."));
        }

        @Override
        protected void runBody(LWThread thr) {
        	Channel<AbstractDataNode> vargs = c_vargs.get(thr.getStack());
            SwiftFunction.waitForAll(this, vargs);

            StringBuilder buf = new StringBuilder();
            buf.append("SwiftScript trace: ");
            boolean first = true;
            for (AbstractDataNode n : vargs) {
                if (!first) {
                    buf.append(", ");
                }
                else {
                	first = false;
                }
                //buf.append(v == null ? args[i] : v);
                prettyPrint(buf, n);
            }
            traceLogger.warn(buf);
        }
	}
	
	private static void prettyPrint(StringBuilder buf, DSHandle h) {
        Object o = h.getValue();
        if (o == null) {
            buf.append(h);
        }
        else {
            if (h.getType().isPrimitive()) {
                if (h.getType().equals(Types.INT)) {
                    buf.append(((Number) o).intValue());
                }
                else {
                    buf.append(o);
                }
            }
            else if (h.getType().isArray()) {
                buf.append('{');
                boolean first = true;
                for (Map.Entry<Comparable<?>, DSHandle> e : h.getArrayValue().entrySet()) {
                    if (first) {
                        first = false;
                    }
                    else {
                        buf.append(", ");
                    }
                    buf.append(e.getKey());
                    buf.append(" = ");
                    prettyPrint(buf, e.getValue());
                }
                buf.append('}');
            }
            else {
                buf.append(h);
            }
        }
    }
	
	public static class StrCat extends AbstractSingleValuedFunction {
        private ChannelRef<AbstractDataNode> c_vargs;

        @Override
        protected Signature getSignature() {
            return new Signature(params("..."));
        }
        
        @Override
        public Object function(Stack stack) {
            Channel<AbstractDataNode> vargs = c_vargs.get(stack);
            Channel<Object> args = SwiftFunction.unwrapAll(this, vargs);
            
            StringBuffer buf = new StringBuffer();
            
            for (Object o : args) {
                buf.append(TypeUtil.toString(o));
            }
            
            DSHandle handle = new RootDataNode(Types.STRING, buf.toString());
    
            if (PROVENANCE_ENABLED) {
            	int provid = SwiftFunction.nextProvenanceID();
            	int index = 0;
                for (AbstractDataNode dn : vargs) {
                    SwiftFunction.logProvenanceParameter(provid, dn, String.valueOf(index++));
                }
                SwiftFunction.logProvenanceResult(provid, handle, "strcat");
            }
            return handle;
        }
	}
	
	public static class Exists extends AbstractSingleValuedFunction {
        private ArgRef<AbstractDataNode> file;

        @Override
        protected Signature getSignature() {
            return new Signature(params("file"));
        }
        
        @Override
        public Object function(Stack stack) {
        	AbstractDataNode dn = file.getValue(stack);
            String filename = SwiftFunction.unwrap(this, dn);

            AbsFile file = new AbsFile(filename);
            if (logger.isDebugEnabled()) {
                logger.debug("exists: " + file);
            }
            DSHandle handle = new RootDataNode(Types.BOOLEAN, file.exists());
    
            if (PROVENANCE_ENABLED) {
            	int provid = SwiftFunction.nextProvenanceID();
            	SwiftFunction.logProvenanceParameter(provid, dn, "file");
                SwiftFunction.logProvenanceResult(provid, handle, "exists");
            }
    
            return handle;
        }
    }
	
	public static class StrCut extends AbstractSingleValuedFunction {
        private ArgRef<AbstractDataNode> input;
        private ArgRef<AbstractDataNode> pattern;

        @Override
        protected Signature getSignature() {
            return new Signature(params("input", "pattern"));
        }
        
        @Override
        public Object function(Stack stack) {
        	AbstractDataNode hinput = this.input.getValue(stack);
        	String input = SwiftFunction.unwrap(this, hinput);
        	AbstractDataNode hpattern = this.pattern.getValue(stack);
            String pattern = SwiftFunction.unwrap(this, hpattern);

            if (logger.isDebugEnabled()) {
                logger.debug("strcut will match '" + input + "' with pattern '" + pattern + "'");
            }

            String group;
            try {
                Pattern p = Pattern.compile(pattern);
                // TODO probably should memoize this?
    
                Matcher m = p.matcher(input);
                m.find();
                group = m.group(1);
            }
            catch (IllegalStateException e) {
                throw new ExecutionException("@strcut could not match pattern " + pattern
                        + " against string " + input, e);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("strcut matched '" + group + "'");
            }
            DSHandle handle = new RootDataNode(Types.STRING, group);
    
            if (PROVENANCE_ENABLED) {
            	int provid = SwiftFunction.nextProvenanceID();
                SwiftFunction.logProvenanceResult(provid, handle, "strcut");
                SwiftFunction.logProvenanceParameter(provid, hinput, "input");
                SwiftFunction.logProvenanceParameter(provid, hpattern, "pattern");
            }
            return handle;
        }
	}
	
	public static class StrStr extends AbstractSingleValuedFunction {
        private ArgRef<AbstractDataNode> input;
        private ArgRef<AbstractDataNode> pattern;

        @Override
        protected Signature getSignature() {
            return new Signature(params("input", "pattern"));
        }
        
        @Override
        public Object function(Stack stack) {
            AbstractDataNode hinput = this.input.getValue(stack);
            String input = SwiftFunction.unwrap(this, hinput);
            AbstractDataNode hpattern = this.pattern.getValue(stack);
            String pattern = SwiftFunction.unwrap(this, hpattern);

            if (logger.isDebugEnabled()) {
                logger.debug("strstr will search '" + input + "' for pattern '" + pattern + "'");
            }
            
            DSHandle result = new RootDataNode(Types.INT, input.indexOf(pattern));

            
            if (PROVENANCE_ENABLED) {
                int provid = SwiftFunction.nextProvenanceID();
                SwiftFunction.logProvenanceResult(provid, result, "strstr");
                SwiftFunction.logProvenanceParameter(provid, hinput, "input");
                SwiftFunction.logProvenanceParameter(provid, hpattern, "pattern");
            }
            return result;
        }
    }
	
	public static class StrSplit extends AbstractSingleValuedFunction {
        private ArgRef<AbstractDataNode> input;
        private ArgRef<AbstractDataNode> pattern;

        @Override
        protected Signature getSignature() {
            return new Signature(params("input", "pattern"));
        }
        
        @Override
        public Object function(Stack stack) {
            AbstractDataNode hinput = this.input.getValue(stack);
            String input = SwiftFunction.unwrap(this, hinput);
            AbstractDataNode hpattern = this.pattern.getValue(stack);
            String pattern = SwiftFunction.unwrap(this, hpattern);

            String[] split = input.split(pattern);

            DSHandle handle = new RootArrayDataNode(Types.STRING.arrayType());
            for (int i = 0; i < split.length; i++) {
                DSHandle el;
                try {
                    el = handle.getField(i);
                    el.setValue(split[i]);
                }
                catch (NoSuchFieldException e) {
                    throw new ExecutionException(this, e);
                }
            }
            handle.closeDeep();
                       
            if (PROVENANCE_ENABLED) {
                int provid = SwiftFunction.nextProvenanceID();
                SwiftFunction.logProvenanceResult(provid, handle, "strsplit");
                SwiftFunction.logProvenanceParameter(provid, hinput, "input");
                SwiftFunction.logProvenanceParameter(provid, hpattern, "pattern");
            }
            return handle;
        }
    }
	
	/**
	 * StrJoin (@strjoin) - Combine elements of an array into a single string with a specified delimiter
	 * @param stack
	 * @return DSHandle representing the resulting string
	 * @throws ExecutionException
	 */
	public static class StrJoin extends AbstractSingleValuedFunction {
        private ArgRef<AbstractDataNode> array;
        private ArgRef<AbstractDataNode> delim;

        @Override
        protected Signature getSignature() {
            return new Signature(params("array", "delim"));
        }
        
        @Override
        public Object function(Stack stack) {
            AbstractDataNode harray = this.array.getValue(stack);
            Map<Comparable<?>, DSHandle> arrayValues = SwiftFunction.waitForArray(this, harray);
            AbstractDataNode hdelim = this.delim.getValue(stack);
            String delim = SwiftFunction.unwrap(this, hdelim);

            StringBuilder result = new StringBuilder();
            
            boolean first = true;
            for (DSHandle h : arrayValues.values()) {
            	if (first) {
            		first = false;
            	}
            	else {
            		result.append(delim);
            	}
            	result.append(h.getValue());
            }

            DSHandle handle = new RootDataNode(Types.STRING, result.toString());
                       
            if (PROVENANCE_ENABLED) {
                int provid = SwiftFunction.nextProvenanceID();
                SwiftFunction.logProvenanceResult(provid, handle, "strjoin");
                SwiftFunction.logProvenanceParameter(provid, harray, "array");
                SwiftFunction.logProvenanceParameter(provid, hdelim, "delim");
            }
            return handle;
        }
    }
    
	public static class Regexp extends AbstractSingleValuedFunction {
        private ArgRef<AbstractDataNode> input;
        private ArgRef<AbstractDataNode> pattern;
        private ArgRef<AbstractDataNode> transform;

        @Override
        protected Signature getSignature() {
            return new Signature(params("input", "pattern", "transform"));
        }
        
        @Override
        public Object function(Stack stack) {
            AbstractDataNode hinput = this.input.getValue(stack);
            String input = SwiftFunction.unwrap(this, hinput);
            AbstractDataNode hpattern = this.pattern.getValue(stack);
            String pattern = SwiftFunction.unwrap(this, hpattern);
            AbstractDataNode htransform = this.transform.getValue(stack);
            String transform = SwiftFunction.unwrap(this, htransform);

            if (logger.isDebugEnabled()) {
                logger.debug("regexp will match '" + input + "' with pattern '" + pattern + "'");
            }
    
            String group;
            try {
                Pattern p = Pattern.compile(pattern);
    
                Matcher m = p.matcher(input);
                m.find();
                group = m.replaceFirst(transform);
            }
            catch (IllegalStateException e) {
                throw new ExecutionException("@regexp could not match pattern " + pattern
                        + " against string " + input, e);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("regexp replacement produced '" + group + "'");
            }
            DSHandle handle = new RootDataNode(Types.STRING);
            handle.setValue(group);
            handle.closeShallow();

            if (PROVENANCE_ENABLED) {
                int provid = SwiftFunction.nextProvenanceID();
                SwiftFunction.logProvenanceResult(provid, handle, "regexp");
                SwiftFunction.logProvenanceParameter(provid, hinput, "input");
                SwiftFunction.logProvenanceParameter(provid, hpattern, "pattern");
                SwiftFunction.logProvenanceParameter(provid, htransform, "transform");
            }
            return handle;
        }
    }
	
	public static class ToInt extends AbstractSingleValuedFunction {
        private ArgRef<AbstractDataNode> str;

        @Override
        protected Signature getSignature() {
            return new Signature(params("str"), returns(channel("...", 1)));
        }
        
        @Override
        public Object function(Stack stack) {
            AbstractDataNode hstr = str.getValue(stack);
            String str = SwiftFunction.unwrap(this, hstr);
            
            DSHandle handle = new RootDataNode(Types.INT, Integer.valueOf(str));

            if (PROVENANCE_ENABLED) {
                int provid = SwiftFunction.nextProvenanceID();
                SwiftFunction.logProvenanceParameter(provid, hstr, "str");
                SwiftFunction.logProvenanceResult(provid, handle, "toint");
            }
    
            return handle;
        }
    }
	
	public static class ToFloat extends AbstractSingleValuedFunction {
        private ArgRef<AbstractDataNode> str;

        @Override
        protected Signature getSignature() {
            return new Signature(params("str"));
        }
        
        @Override
        public Object function(Stack stack) {
            AbstractDataNode hstr = str.getValue(stack);
            Object obj = SwiftFunction.unwrap(this, hstr);
            
            DSHandle handle;
            
            if (obj instanceof String) {
                handle = new RootDataNode(Types.FLOAT, Double.valueOf((String) obj));
            }
            else if (obj instanceof Number) {
                handle = new RootDataNode(Types.FLOAT, ((Number) obj).doubleValue());
            }
            else {
                throw new ExecutionException("Expected a string or int. Got " + obj);
            }
            

            if (PROVENANCE_ENABLED) {
                int provid = SwiftFunction.nextProvenanceID();
                SwiftFunction.logProvenanceParameter(provid, hstr, "str");
                SwiftFunction.logProvenanceResult(provid, handle, "tofloat");
            }
    
            return handle;
        }
    }

	/*
	 * Takes in a float and formats to desired precision and returns a string
	 */
	public static class Format extends AbstractSingleValuedFunction {
        private ArgRef<AbstractDataNode> format;
        private ArgRef<AbstractDataNode> value;

        @Override
        protected Signature getSignature() {
            return new Signature(params("format", "value"));
        }
        
        @Override
        public Object function(Stack stack) {
            AbstractDataNode hformat = this.format.getValue(stack);
            String format = SwiftFunction.unwrap(this, hformat);
            AbstractDataNode hvalue = this.value.getValue(stack);
            Double value = SwiftFunction.unwrap(this, hvalue);
            
            DSHandle handle = new RootDataNode(Types.STRING, 
            		String.format("%." + format + "f", value));

            if (PROVENANCE_ENABLED) {
                int provid = SwiftFunction.nextProvenanceID();
                SwiftFunction.logProvenanceResult(provid, handle, "format");
                SwiftFunction.logProvenanceParameter(provid, hformat, "format");
                SwiftFunction.logProvenanceParameter(provid, hvalue, "value");
            }
            return handle;
        }
    }
	
	/*
	 * Takes in an int and pads zeros to the left and returns a string
	 */
	public static class Pad extends AbstractSingleValuedFunction {
        private ArgRef<AbstractDataNode> size;
        private ArgRef<AbstractDataNode> value;

        @Override
        protected Signature getSignature() {
            return new Signature(params("size", "value"));
        }
        
        @Override
        public Object function(Stack stack) {
            AbstractDataNode hsize = this.size.getValue(stack);
            Integer size = SwiftFunction.unwrap(this, hsize);
            AbstractDataNode hvalue = this.value.getValue(stack);
            Integer value = SwiftFunction.unwrap(this, hvalue);
            
            DSHandle handle = new RootDataNode(Types.STRING, 
                    String.format("%0" + size + "d", value));

            if (PROVENANCE_ENABLED) {
                int provid = SwiftFunction.nextProvenanceID();
                SwiftFunction.logProvenanceResult(provid, handle, "pad");
                SwiftFunction.logProvenanceParameter(provid, hsize, "size");
                SwiftFunction.logProvenanceParameter(provid, hvalue, "value");
            }
            return handle;
        }
    }
	
	public static class ToString extends AbstractSingleValuedFunction {
        private ArgRef<AbstractDataNode> value;

        @Override
        protected Signature getSignature() {
            return new Signature(params("value"));
        }
        
        @Override
        public Object function(Stack stack) {
            AbstractDataNode hvalue = this.value.getValue(stack);
            hvalue.waitFor(this);
            
            StringBuilder sb = new StringBuilder();
            prettyPrint(sb, hvalue);
            DSHandle handle = new RootDataNode(Types.STRING, sb.toString());

            if (PROVENANCE_ENABLED) {
                int provid = SwiftFunction.nextProvenanceID();
                SwiftFunction.logProvenanceParameter(provid, hvalue, "value");
                SwiftFunction.logProvenanceResult(provid, handle, "tostring");
            }
    
            return handle;
        }
    }
	
	/*
     * This is copied from swiftscript_dirname.
     * Both the functions could be changed to be more readable.
     * Returns length of an array.
     * Good for debugging because array needs to be closed
     *   before the length is determined
     */
	public static class Dirname extends AbstractSingleValuedFunction {
        private ArgRef<AbstractDataNode> file;

        @Override
        protected Signature getSignature() {
            return new Signature(params("file"));
        }
        
        @Override
        public Object function(Stack stack) {
            AbstractDataNode dn = file.getValue(stack);
            String name = SwiftFunction.filename(dn)[0];

            String result = new AbsFile(name).getDirectory();
            DSHandle handle = new RootDataNode(Types.STRING, result);
    
            if (PROVENANCE_ENABLED) {
                int provid = SwiftFunction.nextProvenanceID();
                SwiftFunction.logProvenanceParameter(provid, dn, "file");
                SwiftFunction.logProvenanceResult(provid, handle, "dirname");
            }
    
            return handle;
        }
    }
		
	public static class Length extends AbstractSingleValuedFunction {
        private ArgRef<AbstractDataNode> array;

        @Override
        protected Signature getSignature() {
            return new Signature(params("array"));
        }
        
        @Override
        public Object function(Stack stack) {
            AbstractDataNode harray = this.array.getValue(stack);
            harray.waitFor(this);
            
            DSHandle handle = new RootDataNode(Types.INT, Integer.valueOf(harray.getArrayValue().size()));
                       
            if (PROVENANCE_ENABLED) {
                int provid = SwiftFunction.nextProvenanceID();
                SwiftFunction.logProvenanceResult(provid, handle, "length");
                SwiftFunction.logProvenanceParameter(provid, harray, "array");
            }
            return handle;
        }
    }
}

/*
 * Local Variables:
 *  c-basic-offset: 4
 * End:
 *
 * vim: ft=c ts=8 sts=4 sw=4 expandtab
 */
