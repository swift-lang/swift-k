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

import java.util.Arrays;

import k.rt.Channel;
import k.rt.ExecutionException;
import k.rt.Stack;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.griphyn.vdl.karajan.FileNameResolver;
import org.griphyn.vdl.karajan.FileNameResolver.MultiMode;
import org.griphyn.vdl.karajan.FileNameResolver.Transform;
import org.griphyn.vdl.karajan.lib.SwiftFunction;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.ArrayHandle;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Types;

/**
    Formatted string generation. <br>
    Example: sprintf("\t%s\n", "hello"); <br>
    Differences from trace(): 
    1) respects \t, \n and \\;
    2) allows for typechecked format specifiers; 
    3) allows for consumption of variables without display (%k) 
    Format specifiers: <br>
      %%: % sign. <br>
      %M: Filename output: waits for close
      %p: Not typechecked, output as in trace(). <br>
      %b: Typechecked boolean output. <br>
      %f: Typechecked float output. <br>
      %i: Typechecked int output. <br>
      %s: Typechecked string output. <br>
      %k: Variable sKipped, no output. <br>
      %q: Array output
 */
public class Sprintf extends SwiftFunction {

    private static final Logger logger = Logger.getLogger(Sprintf.class);
    
    private ArgRef<AbstractDataNode> spec;
    private ChannelRef<AbstractDataNode> c_vargs;

    @Override
    protected Signature getSignature() {
        return new Signature(params("spec", "..."));
    }

    @Override
    protected Field getReturnType() {
        return Field.GENERIC_STRING;
    }

    @Override
    public Object function(Stack stack) {
    	AbstractDataNode hspec = this.spec.getValue(stack);
        hspec.waitFor(this);
        Channel<AbstractDataNode> args = c_vargs.get(stack);
        waitForAll(this, args);
        String spec = (String) hspec.getValue();
        String msg = format(spec, args);
        if (logger.isDebugEnabled()) {
            logger.debug("generated: " + msg);
        }
        
        return NodeFactory.newRoot(Field.GENERIC_STRING, msg);
    }
    
    public static String format(String spec, Channel<AbstractDataNode> args) {
        logger.debug("spec: " + spec);
        StringBuilder output = new StringBuilder();
        format(spec, args, output);        
        return output.toString();
    }
    
    /** 
       This method can be targeted as a helper function 
       (by @sprintf(), etc.)
     */
    public static void format(String spec, Channel<AbstractDataNode> vars, StringBuilder output) {
        int i = 0; 
        int arg = 0; 
        while (i < spec.length()) {
            char c = spec.charAt(i);
            if (c == '%') {
                char d = spec.charAt(++i); 
                arg = append(d, arg, vars, output);
            }
            else if (c == '\\') {
                char d = spec.charAt(++i);
                escape(i, spec, d, output); 
            }
            else {
                output.append(c);
            }
            i++;
        }
    }
       
    private static int append(char c, int arg, Channel<AbstractDataNode> vars, StringBuilder output) {
        if (c == '%') {
            output.append('%');
            return arg;
        }
        if (arg >= vars.size()) {
            throw new IllegalArgumentException("tracef(): too many specifiers!");
        }

        if (c == 'M') {
            append_M(vars.get(arg), output);
        }
        else if (c == 'b') { 
            append_b(vars.get(arg), output);
        }
        else if (c == 'f') {
            append_f(vars.get(arg), output);
        }
        else if (c == 'i') {
            append_i(vars.get(arg), output);
        }
        else if (c == 'd') {
            append_i(vars.get(arg), output);
        }
        else if (c == 'p') {
            output.append(vars.get(arg).toString());
        }
        else if (c == 's') {
            append_s(vars.get(arg), output);
        }
        else if (c == 'q') {
            append_q(vars.get(arg), output);
        }
        else if (c == 'k') {
            ;
        }
        else {
            throw new IllegalArgumentException("tracef(): Unknown format: %" + c);
        }
        return arg+1;
    }

    private static void append_M(DSHandle arg, StringBuilder output) {
        try {
            synchronized (arg.getRoot()) { 
                String[] names = new FileNameResolver(arg).getURLsAsArray();
                if (names.length > 1) {
                    output.append(Arrays.asList(names));
                }
                else { 
                    output.append(names[0]);
                }
            }
        }
        catch (Exception e) { 
            throw new IllegalArgumentException("tracef(%M): Could not lookup: " + arg); 
        }
    }
    
    private static void append_b(DSHandle arg, StringBuilder output) 
    throws ExecutionException {
        if (arg.getType() == Types.BOOLEAN) {
            output.append(arg.getValue());
        }
        else {
            throw new IllegalArgumentException("tracef(): %b requires a boolean! " 
            		+ dshandleDescription(arg));
        }
    }
    
    private static void append_f(DSHandle arg, StringBuilder output) {
        if (arg.getType() == Types.FLOAT) {
            output.append(arg.getValue());
        }
        else {
            throw new IllegalArgumentException("tracef(): %f requires a float! " 
            		+ dshandleDescription(arg));
        }
    }

    private static void append_i(DSHandle arg, StringBuilder output) {
        if (arg.getType() == Types.INT) {
        	Integer d = (Integer) arg.getValue();
            output.append(d);
        }
        else {
            throw new IllegalArgumentException("tracef(): %i requires an int! " 
            		+ dshandleDescription(arg));
        }
    }
    
    private static void append_q(DSHandle arg, StringBuilder output) {
        if (arg instanceof ArrayHandle) {
            ArrayHandle node = (ArrayHandle) arg;
            output.append("[");
            try {
                int size = node.arraySize();
                for (int i = 0; i < size; i++) {
                    String entry = "["+i+"]"; 
                    DSHandle handle = 
                        node.getField(Path.parse(entry));
                    output.append(handle.getValue());
                    if (i < size-1)
                        output.append(",");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new IllegalArgumentException("trace(%q): Could not get children of: " 
                		+ arg);
            }
            output.append("]");
        }
        else {
            throw new IllegalArgumentException("tracef(): %q requires an array! " 
            		+ dshandleDescription(arg));
        }        
    }
    
    private static void append_s(DSHandle arg, StringBuilder output) {
        output.append(String.valueOf(arg.getValue()));
    }
    
    /**      
     * @param i Only used for error messages
     * @param spec Only used for error messages
     */
    private static void escape(int i, String spec, 
    		                   char c, StringBuilder output) 
    throws ExecutionException {
        if (c == '\\') {
            output.append('\\');
        }
        else if (c == 'n') {
            output.append('\n');
        }
        else if (c == 't') {
            output.append('\t');
        }
        else {
            throw new IllegalArgumentException("tracef(): unknown backslash escape sequence! " + 
            		    "(\\" + c + ")\n" + 
            		    "\t in " + spec + " character: " + i);
        }
    }
    
    /**
     * Return String containing variable name and type of a given DSHandle
     */
    private static String dshandleDescription(DSHandle dshandle) {
        String variableName = (dshandle.getRoot().toString().split(":")[0]);
        return "Variable \"" + variableName + "\" is a " + dshandle.getType();
    }
}
