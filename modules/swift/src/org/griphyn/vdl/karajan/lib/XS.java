//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jun 1, 2014
 */
package org.griphyn.vdl.karajan.lib;

import java.util.Arrays;
import java.util.List;

import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.type.DuplicateFieldException;
import org.griphyn.vdl.type.NoSuchTypeException;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;
import org.griphyn.vdl.type.impl.TypeImpl;
import org.griphyn.vdl.type.impl.UnresolvedType;

/**
 * Type declarations in Swift have no dynamic components. So 
 * everything can be done at compile time
 */
public class XS {
    
    public static class Schema extends InternalFunction {        
        @Override
        protected Signature getSignature() {
            return new Signature(params());
        }

        @Override
        protected void runBody(LWThread thr) {
            // do nothing
        }

        @Override
        protected Node compileBody(WrapperNode w, Scope argScope, Scope scope) throws CompilationException {
            try {
                Types.resolveTypes();
            }
            catch (NoSuchTypeException e) {
                throw new CompilationException(w, "Cannot resolve types", e);
            }
            return null;
        }
    }
    
    public static class SimpleType extends InternalFunction {
        private ArgRef<String> name;
        private ArgRef<String> type;
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("name", "type"));
        }

        @Override
        protected void runBody(LWThread thr) {
            // do nothing
        }

        @Override
        protected Node compileBody(WrapperNode w, Scope argScope, Scope scope) throws CompilationException {            
            Type t = new TypeImpl(name.getValue());
            try {
                t.setBaseType(Types.getType(type.getValue()));
            }
            catch (NoSuchTypeException e) {
                throw new CompilationException(w, "Unknown type: '" + type.getValue() + "'");
            }
            
            Types.addType(t);
            
            return null;
        }
    }
    
    public static class Restriction extends InternalFunction {
        private ArgRef<String> base;
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("base"));
        }

        @Override
        protected void runBody(LWThread thr) {
            // do nothing
        }
        
        /*
         * type = last(split(base, ":"))
         */
        @Override
        protected Node compileBody(WrapperNode w, Scope argScope, Scope scope) throws CompilationException {
            String base = this.base.getValue();
            
            int ix = base.lastIndexOf(':');
            
            Var ret = scope.parent.lookupParam("type");
            ret.setValue(base.substring(ix + 1));
            
            return null;
        }
    }
    
    public static class ComplexType extends InternalFunction {
        private ArgRef<String> name;
        private ChannelRef<Object> c_vargs;
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("name", "..."));
        }

        @Override
        protected void runBody(LWThread thr) {
            // do nothing
        }

        /*
         * node := newComplexNode(name)
         *   for(field, ...) {
         *       (name, type) := each(field)
         *       addField(node, name, type)
         *   }
         *   addNode(node)
         */
        @Override
        protected Node compileBody(WrapperNode w, Scope argScope, Scope scope) throws CompilationException {
            List<Object> args = argScope.lookupChannel("...").getAll();
            
            Type t = new TypeImpl(name.getValue());
            
            for (Object o : args) {
                @SuppressWarnings("unchecked")
                List<Object> l = (List<Object>) o;
                
                String fname = (String) l.get(0);
                Type type = (Type) l.get(1);
                try {
                    t.addField(fname, type);
                }
                catch (DuplicateFieldException e) {
                    throw new CompilationException(w, "Field '" + fname + "' is alread defined for type '" + name + "'");
                }
            }
            
            Types.addType(t);
            
            return null;
        }
    }
    

    public static class Sequence extends InternalFunction {
        private ArgRef<String> minOccurs;
        private ArgRef<String> maxOccurs;
        private ChannelRef<Object> c_vargs;
        
        @Override
        protected Signature getSignature() {
            return new Signature(params(optional("minOccurs", "0"), optional("maxOccurs", "0"), "..."));
        }

        @Override
        protected void runBody(LWThread thr) {
            // do nothing
        }

        /*
         * (name, type) := each(first(...))
         *   if (maxOccurs == "unbounded") {
         *       list(name, UnresolvedType(type, true))
         *   }
         *   else {
         *       each(...)
         *   }
         */
        @Override
        protected Node compileBody(WrapperNode w, Scope argScope, Scope scope) throws CompilationException {
            List<Object> args = argScope.lookupChannel("...").getAll();
            
            Var.Channel crv = scope.parent.lookupChannel("...");
                        
            if (maxOccurs.getValue().equals("unbounded")) {
                @SuppressWarnings("unchecked")
                List<Object> first = (List<Object>) args.get(0);
                String name = (String) first.get(0);
                String type = (String) first.get(1);
                crv.append(Arrays.asList(name, new UnresolvedType(type)));
            }
            else {
                for (Object o : args) {
                    crv.append(o);
                }
            }
            
            return null;
        }
    }


    public static class Element extends InternalFunction {
        private ArgRef<String> name;
        private ArgRef<String> type;
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("name", "type"));
        }

        @Override
        protected void runBody(LWThread thr) {
            // do nothing
        }

        /*
         * type := last(split(type, ":"))
         * list(name, UnresolvedType(type, false))
         */
        @Override
        protected Node compileBody(WrapperNode w, Scope argScope, Scope scope) throws CompilationException {
            String type = this.type.getValue();
            String name = this.name.getValue();
            Var.Channel crv = scope.parent.lookupChannel("...");
            
            int ix = type.lastIndexOf(':');
            
            crv.append(Arrays.asList(name, new UnresolvedType(type.substring(ix + 1))));
            
            return null;
        }
    }

}
