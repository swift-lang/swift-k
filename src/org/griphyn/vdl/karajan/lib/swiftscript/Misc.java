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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
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
import org.globus.cog.karajan.util.TypeUtil;
import org.griphyn.vdl.karajan.FileNameExpander;
import org.griphyn.vdl.karajan.lib.StringCache;
import org.griphyn.vdl.karajan.lib.SwiftFunction;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Types;
import org.griphyn.vdl.util.SwiftConfig;

public class Misc {

	private static final Logger logger = Logger.getLogger(Misc.class);
	
	public static final boolean PROVENANCE_ENABLED = SwiftConfig.getDefault().isProvenanceEnabled();

	private static final Logger traceLogger = Logger.getLogger("org.globus.swift.trace");
	
    public static class Print extends InternalFunction {
       private ChannelRef<AbstractDataNode> c_vargs;
       
       @Override
       protected Signature getSignature() {
           return new Signature(params("..."));
       }
       
       @Override
       protected void runBody(LWThread thr) {
           Channel<AbstractDataNode> vargs = c_vargs.get(thr.getStack());
           StringBuilder buf = new StringBuilder();
           try {
               Printf.waitForAll(this, vargs);
               buf.append("SwiftScript print: ");
               boolean first = true;
               for (AbstractDataNode n : vargs) {
                   if (!first) {
                       buf.append(", ");
                   }
                   else {
                       first = false;
                   }
                   prettyPrint(buf, n);
               }
           }
           catch (DependentException e) {
               buf.append("SwiftScript print: <exception>");
           }
           traceLogger.warn(buf);
       }
   }
   

	public static class Trace extends InternalFunction {
		private ChannelRef<AbstractDataNode> c_vargs;

        @Override
        protected Signature getSignature() {
            return new Signature(params("..."));
        }

        @Override
        protected void runBody(LWThread thr) {
        	Channel<AbstractDataNode> vargs = c_vargs.get(thr.getStack());
        	StringBuilder buf = new StringBuilder();
        	try {
        	    Tracef.waitForAll(this, vargs);
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
        	}
        	catch (DependentException e) {
        	    buf.append("SwiftScript trace: <exception>");
        	}

            traceLogger.warn(buf);
        }
	}
	
	private static void prettyPrint(StringBuilder buf, DSHandle h) {
	    Object o;
	    try {
	        o = h.getValue();
	    }
	    catch (DependentException e) {
	        buf.append("<exception>");
	        return;
	    }
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
	
	public static class StrCat extends AbstractSingleValuedSwiftFunction {
        private ChannelRef<AbstractDataNode> c_vargs;

        @Override
        protected Signature getSignature() {
            return new Signature(params("..."));
        }
        
        @Override
        protected Field getFieldType() {
            return Field.GENERIC_STRING;
        }

        @Override
        public Object function(Stack stack) {
            Channel<AbstractDataNode> vargs = c_vargs.get(stack);
            Channel<Object> args = SwiftFunction.unwrapAll(this, vargs);
            
            StringBuffer buf = new StringBuffer();
            
            for (Object o : args) {
                buf.append(TypeUtil.toString(o));
            }
            
            DSHandle handle = NodeFactory.newRoot(Field.GENERIC_STRING, buf.toString());
    
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
	
	public static class Exists extends AbstractSingleValuedSwiftFunction {
        private ArgRef<AbstractDataNode> file;

        @Override
        protected Signature getSignature() {
            return new Signature(params("file"));
        }
        
        @Override
        protected Field getFieldType() {
            return Field.GENERIC_BOOLEAN;
        }
        
        @Override
        public Object function(Stack stack) {
        	AbstractDataNode dn = file.getValue(stack);
        	String filename;
        	if (dn.getType().equals(Types.STRING)) {
        	    filename = SwiftFunction.unwrap(this, dn);
        	}
        	else {
        	    filename = new FileNameExpander(dn).getSingleLocalPath();
        	}

            AbsFile file = new AbsFile(filename);
            if (logger.isDebugEnabled()) {
                logger.debug("exists: " + file);
            }
            DSHandle handle = NodeFactory.newRoot(Field.GENERIC_BOOLEAN, file.exists());
    
            if (PROVENANCE_ENABLED) {
            	int provid = SwiftFunction.nextProvenanceID();
            	SwiftFunction.logProvenanceParameter(provid, dn, "file");
                SwiftFunction.logProvenanceResult(provid, handle, "exists");
            }
    
            return handle;
        }
    }
	
	public static class StrCut extends AbstractSingleValuedSwiftFunction {
        private ArgRef<AbstractDataNode> input;
        private ArgRef<AbstractDataNode> pattern;

        @Override
        protected Signature getSignature() {
            return new Signature(params("input", "pattern"));
        }
        
        @Override
        protected Field getFieldType() {
            return Field.GENERIC_STRING;
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
            DSHandle handle = NodeFactory.newRoot(Field.GENERIC_STRING, group);
    
            if (PROVENANCE_ENABLED) {
            	int provid = SwiftFunction.nextProvenanceID();
                SwiftFunction.logProvenanceResult(provid, handle, "strcut");
                SwiftFunction.logProvenanceParameter(provid, hinput, "input");
                SwiftFunction.logProvenanceParameter(provid, hpattern, "pattern");
            }
            return handle;
        }
	}
	
	public static class StrStr extends AbstractSingleValuedSwiftFunction {
        private ArgRef<AbstractDataNode> input;
        private ArgRef<AbstractDataNode> pattern;

        @Override
        protected Signature getSignature() {
            return new Signature(params("input", "pattern"));
        }
        
        @Override
        protected Field getFieldType() {
            return Field.GENERIC_STRING;
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
            
            DSHandle result = NodeFactory.newRoot(Field.GENERIC_INT, input.indexOf(pattern));

            
            if (PROVENANCE_ENABLED) {
                int provid = SwiftFunction.nextProvenanceID();
                SwiftFunction.logProvenanceResult(provid, result, "strstr");
                SwiftFunction.logProvenanceParameter(provid, hinput, "input");
                SwiftFunction.logProvenanceParameter(provid, hpattern, "pattern");
            }
            return result;
        }
    }


    public static class ExecSystem extends AbstractSingleValuedSwiftFunction {
        private ArgRef<AbstractDataNode> input;
 
        @Override
	    protected Signature getSignature() {
            return new Signature(params("input"));
	    }

        @Override
   	    public Object function(Stack stack) {
    		AbstractDataNode hinput = this.input.getValue(stack);
    		String input     = SwiftFunction.unwrap(this, hinput);
    		
    		DSHandle handle = NodeFactory.newOpenRoot(Field.GENERIC_STRING_ARRAY, null);
    
    		StringBuffer out = new StringBuffer();
    		Process proc;
    		int i = 0;
    
    		try {
    		    proc = Runtime.getRuntime().exec(new String[] {"bash", "-c", input});
    		    proc.waitFor();
                int exitcode = proc.exitValue();
                // If the shell returned a non-zero exit code, attempt to print stderr
                if ( exitcode != 0 ) {
                    BufferedReader reader = new BufferedReader( new InputStreamReader(proc.getErrorStream()) );
                    String line = "";
                    StringBuffer stderr = new StringBuffer();
                    while ( (line = reader.readLine()) != null ) {
                        stderr.append(line);
                    }
                    logger.warn("swift:system returned exitcode :" + exitcode);
                    logger.warn("swift:system stderr:\n " + stderr );
                }
    		    BufferedReader reader = new BufferedReader( new InputStreamReader(proc.getInputStream()) );
    		    String line = "";
                while ( (line = reader.readLine()) != null ) {
                    DSHandle el;
                    el = handle.getField(i++);
                    el.setValue(line);
                }
    		} catch (Exception e) {
    		    e.printStackTrace();
    		}
    		handle.closeDeep();
    
    		if (PROVENANCE_ENABLED) {
    		    int provid = SwiftFunction.nextProvenanceID();
    		    SwiftFunction.logProvenanceResult(provid, handle, "system");
    		    SwiftFunction.logProvenanceParameter(provid, hinput, "input");
    		}
    		return handle;
	    }
	}

    public static class StrSplit extends AbstractSingleValuedSwiftFunction {

        private ArgRef<AbstractDataNode> input;
        private ArgRef<AbstractDataNode> pattern;

        @Override
        protected Signature getSignature() {
            return new Signature(params("input", "pattern"));
        }
        
        @Override
        protected Field getFieldType() {
            return Field.GENERIC_STRING_ARRAY;
        }
        
        @Override
        public Object function(Stack stack) {
            AbstractDataNode hinput = this.input.getValue(stack);
            String input = SwiftFunction.unwrap(this, hinput);
            AbstractDataNode hpattern = this.pattern.getValue(stack);
            String pattern = SwiftFunction.unwrap(this, hpattern);

            String[] split = input.split(pattern);

            DSHandle handle = NodeFactory.newRoot(Field.GENERIC_STRING_ARRAY, Arrays.asList(split));
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
	public static class StrJoin extends AbstractSingleValuedSwiftFunction {
        private ArgRef<AbstractDataNode> array;
        private ArgRef<AbstractDataNode> delim;

        @Override
        protected Signature getSignature() {
            return new Signature(params("array", "delim"));
        }
        
        @Override
        protected Field getFieldType() {
            return Field.GENERIC_STRING;
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

            DSHandle handle = NodeFactory.newRoot(Field.GENERIC_STRING, result.toString());
                       
            if (PROVENANCE_ENABLED) {
                int provid = SwiftFunction.nextProvenanceID();
                SwiftFunction.logProvenanceResult(provid, handle, "strjoin");
                SwiftFunction.logProvenanceParameter(provid, harray, "array");
                SwiftFunction.logProvenanceParameter(provid, hdelim, "delim");
            }
            return handle;
        }
    }
    
	public static class Regexp extends AbstractSingleValuedSwiftFunction {
        private ArgRef<AbstractDataNode> input;
        private ArgRef<AbstractDataNode> pattern;
        private ArgRef<AbstractDataNode> transform;

        @Override
        protected Signature getSignature() {
            return new Signature(params("input", "pattern", "transform"));
        }
        
        @Override
        protected Field getFieldType() {
            return Field.GENERIC_STRING;
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
            DSHandle handle = NodeFactory.newRoot(Field.GENERIC_STRING, group);

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
	
	public static class ToInt extends AbstractSingleValuedSwiftFunction {
        private ArgRef<AbstractDataNode> str;

        @Override
        protected Signature getSignature() {
            return new Signature(params("str"), returns(channel("...", 1)));
        }
        
        @Override
        protected Field getFieldType() {
            return Field.GENERIC_INT;
        }
        
        @Override
        public Object function(Stack stack) {
            AbstractDataNode hstr = str.getValue(stack);
            String str = SwiftFunction.unwrap(this, hstr);
            
            DSHandle handle = NodeFactory.newRoot(Field.GENERIC_INT, Integer.valueOf(str));

            if (PROVENANCE_ENABLED) {
                int provid = SwiftFunction.nextProvenanceID();
                SwiftFunction.logProvenanceParameter(provid, hstr, "str");
                SwiftFunction.logProvenanceResult(provid, handle, "toint");
            }
    
            return handle;
        }
    }
	
	public static class ToFloat extends AbstractSingleValuedSwiftFunction {
        private ArgRef<AbstractDataNode> str;

        @Override
        protected Signature getSignature() {
            return new Signature(params("str"));
        }
        
        @Override
        protected Field getFieldType() {
            return Field.GENERIC_FLOAT;
        }
        
        @Override
        public Object function(Stack stack) {
            AbstractDataNode hstr = str.getValue(stack);
            Object obj = SwiftFunction.unwrap(this, hstr);
            
            DSHandle handle;
            
            if (obj instanceof String) {
                handle = NodeFactory.newRoot(Field.GENERIC_FLOAT, Double.valueOf((String) obj));
            }
            else if (obj instanceof Number) {
                handle = NodeFactory.newRoot(Field.GENERIC_FLOAT, ((Number) obj).doubleValue());
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
	public static class Format extends AbstractSingleValuedSwiftFunction {
	    private ChannelRef<AbstractDataNode> c_vargs;

        @Override
        protected Signature getSignature() {
            return new Signature(params("..."));
        }
        
        @Override
        protected Field getFieldType() {
            return Field.GENERIC_STRING;
        }
        
        @Override
        public Object function(Stack stack) {
            Channel<AbstractDataNode> vargs = c_vargs.get(stack);
            Channel<Object> args = SwiftFunction.unwrapAll(this, vargs);
            
            if (args.size() == 0) {
                throw new ExecutionException(this, "Missing format specification");
            }
            
            String format = (String) args.get(0);
            Object[] a = args.subChannel(1).toArray();
            
            DSHandle handle = NodeFactory.newRoot(Field.GENERIC_STRING, String.format(format, a));

            if (PROVENANCE_ENABLED) {
                int provid = SwiftFunction.nextProvenanceID();
                SwiftFunction.logProvenanceResult(provid, handle, "format");
            }
            return handle;
        }
    }
	
	/*
	 * Takes in an int and pads zeros to the left and returns a string
	 */
	public static class Pad extends AbstractSingleValuedSwiftFunction {
        private ArgRef<AbstractDataNode> size;
        private ArgRef<AbstractDataNode> value;

        @Override
        protected Signature getSignature() {
            return new Signature(params("size", "value"));
        }
        
        @Override
        protected Field getFieldType() {
            return Field.GENERIC_STRING;
        }
        
        @Override
        public Object function(Stack stack) {
            AbstractDataNode hsize = this.size.getValue(stack);
            Integer size = SwiftFunction.unwrap(this, hsize);
            AbstractDataNode hvalue = this.value.getValue(stack);
            Integer value = SwiftFunction.unwrap(this, hvalue);
            
            DSHandle handle = NodeFactory.newRoot(Field.GENERIC_STRING, 
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
	
	public static class ToString extends AbstractSingleValuedSwiftFunction {
        private ArgRef<AbstractDataNode> value;

        @Override
        protected Signature getSignature() {
            return new Signature(params("value"));
        }
        
        @Override
        protected Field getFieldType() {
            return Field.GENERIC_STRING;
        }
        
        @Override
        public Object function(Stack stack) {
            AbstractDataNode hvalue = this.value.getValue(stack);
            hvalue.waitFor(this);
            
            StringBuilder sb = new StringBuilder();
            prettyPrint(sb, hvalue);
            DSHandle handle = NodeFactory.newRoot(Field.GENERIC_STRING, StringCache.get(sb.toString()));

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
	public static class Dirname extends AbstractSingleValuedSwiftFunction {
        private ArgRef<AbstractDataNode> file;

        @Override
        protected Signature getSignature() {
            return new Signature(params("file"));
        }
        
        @Override
        protected Field getFieldType() {
            return Field.GENERIC_STRING;
        }
        
        @Override
        public Object function(Stack stack) {
            AbstractDataNode dn = file.getValue(stack);
            String name = new FileNameExpander(dn).getSingleLocalPath();

            String result = new AbsFile(name).getDirectory();
            DSHandle handle = NodeFactory.newRoot(Field.GENERIC_STRING, result);
    
            if (PROVENANCE_ENABLED) {
                int provid = SwiftFunction.nextProvenanceID();
                SwiftFunction.logProvenanceParameter(provid, dn, "file");
                SwiftFunction.logProvenanceResult(provid, handle, "dirname");
            }
    
            return handle;
        }
    }

	public static class Length extends AbstractSingleValuedSwiftFunction {
        private ArgRef<AbstractDataNode> array;

        @Override
        protected Signature getSignature() {
            return new Signature(params("array"));
        }
        
        @Override
        protected Field getFieldType() {
            return Field.GENERIC_INT;
        }
        
        @Override
        public Object function(Stack stack) {
            AbstractDataNode harray = this.array.getValue(stack);
            harray.waitFor(this);
            
            DSHandle handle = NodeFactory.newRoot(Field.GENERIC_INT, Integer.valueOf(harray.getArrayValue().size()));
                       
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
