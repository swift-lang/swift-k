//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 25, 2015
 */
package org.griphyn.vdl.karajan.lib.swiftscript.v2;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractFunction;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.karajan.lib.SwiftFunction;
import org.griphyn.vdl.karajan.lib.swiftscript.ReadData;
import org.griphyn.vdl.karajan.lib.swiftscript.ReadStructured;
import org.griphyn.vdl.karajan.lib.swiftscript.SwiftDeserializer;
import org.griphyn.vdl.karajan.lib.swiftscript.SwiftSerializer;
import org.griphyn.vdl.karajan.lib.swiftscript.WriteData;
import org.griphyn.vdl.karajan.lib.swiftscript.v2.serialization.ReadPrimitive;
import org.griphyn.vdl.karajan.lib.swiftscript.v2.serialization.WritePrimitive;
import org.griphyn.vdl.karajan.lib.swiftscript.v2.serialization.WriteStructured;
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DataDependentException;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.PhysicalFormat;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class IO {
    
    private static final DSHandle STR_NONE = NodeFactory.newRoot(Types.STRING, "None");
    
    private static Map<String, Object> unwrapOptions(AbstractDataNode options, Node who) {
        if (options == null) {
            return Collections.emptyMap();
        }
        SwiftFunction.waitDeep(LWThread.currentThread(), options, who);
        Map<String, Object> m = new HashMap<String, Object>();
        for (Map.Entry<Comparable<?>, DSHandle> e : options.getArrayValue().entrySet()) {
            m.put((String) e.getKey(), e.getValue().getValue());
        }
        return m;
    }
    
    private static abstract class AbstractSerializationFunction extends AbstractFunction {

    	protected String getPath(DSHandle h, String name) {
            Type t = h.getType();
            if (t.equals(Types.STRING)) {
                return (String) h.getValue();
            }
            else if (t.isPrimitive() || t.isComposite()) {
                throw new ExecutionException(this, "invalid argument of type '" + t + 
                "' passed to " + name + "(): must be a string or a mapped type");
            }
            else {
                PhysicalFormat pf = h.map();
                if (pf instanceof AbsFile) {
                    AbsFile af = (AbsFile) pf;
                    if (!af.getProtocol("file").equals("file")) {
                        throw new ExecutionException(this, name + "() only supports local files");
                    }
                    return af.getPath();
                }
                else {
                    throw new ExecutionException(this, name + "() only supports reading from files");
                }
            }
        }

    }
    
    private static final Map<String, SwiftSerializer> WRITERS;
    
    static {
        WRITERS = new HashMap<String, SwiftSerializer>();
        WRITERS.put("CSV", new WriteData());
        WRITERS.put("FieldAndValue", new WriteStructured());
        WRITERS.put("FV", new WriteStructured());
        WRITERS.put("None", new WritePrimitive());
    }

    public static class Write extends AbstractSerializationFunction {
        private ArgRef<DSHandle> f;
        private ArgRef<AbstractDataNode> src;
        private ArgRef<AbstractDataNode> format;
        private ArgRef<AbstractDataNode> options;
        
        @Override
        protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
                throws CompilationException {
            DSHandle format = this.format.getValue();
            DSHandle src = this.src.getValue();
            if (format != null && format.isClosed()) {
                String sFormat = (String) format.getValue();
                if (!WRITERS.containsKey(sFormat)) {
                    throw new CompilationException(this, 
                        "Unsupported serialization format: '" + sFormat + "'");
                }
                if (src != null) {
                    SwiftSerializer d = WRITERS.get(sFormat);
                    d.checkParamType(src.getType(), this);
                }
            }
            return super.compileBody(w, argScope, scope);
        }

        @Override
        public Object function(Stack stack) {
            DSHandle f = this.f.getValue(stack);
            try {
                AbstractDataNode src = this.src.getValue(stack);
                SwiftFunction.waitDeep(LWThread.currentThread(), src, this);
                AbstractDataNode format = this.format.getValue(stack);
                format.waitFor(this);
                AbstractDataNode options = this.options.getValue(stack);
                Map<String, Object> opts = unwrapOptions(options, this);
                
                String sFormat = (String) format.getValue();
                if (!WRITERS.containsKey(sFormat)) {
                    throw new ExecutionException(this, 
                        "Unsupported de-serialization format: '" + sFormat + "'");
                }
                SwiftSerializer d = WRITERS.get(sFormat);
                d.checkParamType(src.getType(), this);
                String path = getPath(f, "write");
                mkdirs(path);
                d.writeData(path, src, this, opts);
                f.setValue(AbstractDataNode.FILE_VALUE);
            }
            catch (DependentException e) {
                f.setValue(new DataDependentException(f, e));
            }
            return null;
        }
        
        private void mkdirs(String path) {
            File f = new File(path);
            synchronized (Write.class) {
                f.getParentFile().mkdirs();
            }
        }

        @Override
        protected Signature getSignature() {
            return new Signature(params("f", "src", 
                optional("format", STR_NONE), optional("options", null)));
        }
    }
    
    private static final Map<String, SwiftDeserializer> READERS;
    
    static {
        READERS = new HashMap<String, SwiftDeserializer>();
        READERS.put("CSV", new ReadData());
        READERS.put("FieldAndValue", new ReadStructured());
        READERS.put("FV", new ReadStructured());
        READERS.put("None", new ReadPrimitive());
    }
    
    public static class Read extends AbstractSerializationFunction {
        private ArgRef<DSHandle> out;
        private ArgRef<AbstractDataNode> f;
        private ArgRef<AbstractDataNode> format;
        private ArgRef<AbstractDataNode> options;
        
        @Override
        protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
                throws CompilationException {
            DSHandle format = this.format.getValue();
            DSHandle out = this.out.getValue();
            if (format != null && format.isClosed()) {
                String sFormat = (String) format.getValue();
                if (!READERS.containsKey(sFormat)) {
                    throw new CompilationException(this, 
                        "Unsupported de-serialization format: '" + sFormat + "'");
                }
                if (out != null) {
                    SwiftDeserializer d = READERS.get(sFormat);
                    d.checkReturnType(out.getType(), this);
                }
            }
            return super.compileBody(w, argScope, scope);
        }

        @Override
        public Object function(Stack stack) {
            DSHandle out = this.out.getValue(stack);
            try {
                AbstractDataNode f = this.f.getValue(stack);
                f.waitFor(this);
                AbstractDataNode format = this.format.getValue(stack);
                format.waitFor(this);
                AbstractDataNode options = this.options.getValue(stack);
                Map<String, Object> opts = unwrapOptions(options, this);
                
                String sFormat = (String) format.getValue();
                if (!READERS.containsKey(sFormat)) {
                    throw new ExecutionException(this, 
                        "Unsupported de-serialization format: '" + sFormat + "'");
                }
                SwiftDeserializer d = READERS.get(sFormat);
                d.checkReturnType(out.getType(), this);
                d.readData(out, getPath(f, "read"), this, opts);
                out.closeDeep();
            }
            catch (DependentException e) {
                out.setValue(new DataDependentException(out, e));
            }
            return null;
        }
        
        @Override
        protected Signature getSignature() {
            return new Signature(params("out", "f", 
                optional("format", STR_NONE), optional("options", null)));
        }
    }

    public static class GetEnv extends FTypes.StringPString {
        @Override
        protected String v(String arg) {
            String val = System.getenv(arg);
            if (val == null) {
                return "";
            }
            else {
                return val;
            }
        }
    }

}
