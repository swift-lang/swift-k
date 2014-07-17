//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 1, 2014
 */
package org.griphyn.vdl.karajan.lib;

import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractFunction;
import org.globus.cog.karajan.parser.WrapperNode;
import org.globus.cog.karajan.util.Pair;
import org.griphyn.vdl.mapping.GenericMappingParamSet;

public class Mapping {
    public static class Cons extends InternalFunction {
    	private ArgRef<String> descriptor;
    	private ChannelRef<Pair<String, Object>> c_vargs;
    	private VarRef<GenericMappingParamSet> r_mapping;
    	
        @Override
        protected Signature getSignature() {
            return new Signature(params("descriptor", "..."), returns("mapping"));
        }
        
        @Override
        protected void runBody(LWThread thr) {
            Stack stack = thr.getStack();
            
            String descriptor = this.descriptor.getValue(stack);
            GenericMappingParamSet mp = new GenericMappingParamSet(descriptor);
            
            for (Pair<String, Object> param : c_vargs.get(stack)) {
            	mp.addParam(param);
            }
            
            r_mapping.setValue(stack, mp);
        }
        
        @Override
        protected Node compileBody(WrapperNode w, Scope argScope, Scope scope) throws CompilationException {
            String descriptor = this.descriptor.getValue();
            Var.Channel args = argScope.lookupChannel("...");
            
            Var ret = scope.parent.lookupParam("mapping");
            
            if (descriptor == null || args.isDynamic()) {
            	return this;
            }
            
            
            GenericMappingParamSet mp = new GenericMappingParamSet(descriptor);
            for (Object o : args.getAll()) {
            	@SuppressWarnings("unchecked")
                Pair<String, Object> param = (Pair<String, Object>) o;
            	mp.addParam(param);
            }
            
            ret.setValue(mp);
            return null;
        }
    }
    
    public static class Parameter extends AbstractFunction {
        private ArgRef<String> name;
        private ArgRef<String> value;
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("name", "value"));
        }

        @Override
        public Object function(Stack stack) {
            return new Pair<String, Object>(name.getValue(stack), value.getValue(stack));
        }
        
        @Override
        protected Node compileBody(WrapperNode w, Scope argScope, Scope scope) throws CompilationException {
        	Var.Channel crv = scope.parent.lookupChannel("...");
            if (name.getValue() == null || value.getValue() == null) {
            	crv.appendDynamic();
            	return this;
            }
            else {
            	crv.append(new Pair<String, Object>(name.getValue(), value.getValue()));
            	return null;
            }
        }
    }
}
