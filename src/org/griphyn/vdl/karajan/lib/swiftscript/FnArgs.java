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
 * Created on Sep 28, 2006
 */
package org.griphyn.vdl.karajan.lib.swiftscript;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import k.rt.Context;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.karajan.lib.SwiftFunction;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.RootClosedMapDataNode;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Types;
import org.griphyn.vdl.type.impl.TypeImpl.Array;


public class FnArgs extends SwiftFunction {
    private ArgRef<AbstractDataNode> format;
    private VarRef<Context> ctx;
    
	@Override
    protected Signature getSignature() {
        return new Signature(params("format"));
    }
	
	private static final Field STRING_STRING_ARRAY = 
	    Field.Factory.getImmutableField("?", new Array(Types.STRING, Types.STRING));
	
	@Override
    protected Field getReturnType() {
        return STRING_STRING_ARRAY;
    }

    @Override
    protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
            throws CompilationException {
        ctx = scope.getVarRef("#context");
        AbstractDataNode format = this.format.getValue();
        if (format != null) {
            if (format.isClosed()) {
                String fmt = (String) format.getValue();
                AbstractDataNode parsed = processArgs(fmt, ctx.getValue().getArguments());
                if (staticReturn(scope, parsed)) {
                    return null;
                }
            }
        }
        return super.compileBody(w, argScope, scope);
    }
    
    @Override
    public Object function(Stack stack) {
        AbstractDataNode format = this.format.getValue(stack);
        return processArgs((String) format.getValue(), ctx.getValue().getArguments());
    }

    private enum OptType {
        STRING, INT, FLOAT, BOOLEAN;
        
        public static OptType fromString(String str) {
            return OptType.valueOf(str.toUpperCase());
        }
    }
    
    private static class Opt {
        public String name;
        public boolean flag, mandatory;
        public String defValue;
        public OptType type;
        public char sep;
        
        @Override
        public String toString() {
            return "{name: " + name + ", flag: " + flag + ", mandatory: " + 
            mandatory + ", type: " + type + ", defValue: " + defValue + ", sep: " + sep + "}";
        }
    }

    /**
     * fmt := arg, arg, ... arg
     * arg := optional | mandatory
     * optional = '[' mandatory ']'
     * mandatory := key [sep [typespec]]
     * key := ('-' | '/' | '_' | '0'...'9' | 'a'...'z' | 'A...Z')+
     * sep := ' ' | '=' | ':'
     * typespec := '<' type [ ':' value ] '>'
     * type := 'int' | 'string' | 'boolean' | 'float' | 'help'
     * value := string
     */
    private AbstractDataNode processArgs(String fmt, List<String> argv) {
        Map<String, Opt> opts = buildOptMap(fmt);
        
        Map<String, String> parsed = parseArgs(opts, argv);
        
        return buildSwiftArray(parsed);
    }

    private AbstractDataNode buildSwiftArray(Map<String, String> parsed) {
        Field f = Field.Factory.getImmutableField("?", new Array(Types.STRING, Types.STRING));
        return new RootClosedMapDataNode(f, parsed, null);
    }

    private Map<String, String> parseArgs(Map<String, Opt> opts, List<String> argv) {
        Map<String, String> parsed = new HashMap<String, String>();
        
        boolean key = true;
        Opt o = null;
        
        for (String arg : argv) {
            if (key) {
                String k = arg;
                boolean found = false;
                // if it's a key, it cannot contain ':' or '=', so stop when we hit those
                out:
                for (int i = 0; i < arg.length(); i++) {
                    char c = arg.charAt(i);
                    switch (c) {
                        case ':':
                        case '=':
                            k = arg.substring(0, i);
                            o = checkKey(opts, parsed, k, c);
                            parsed.put(o.name, checkValue(o, arg.substring(i + 1)));
                            found = true;
                            o = null;
                            break out;
                    }
                }
                if (!found) {
                    // the whole thing must be the key
                    o = checkKey(opts, parsed, k, ' ');
                    if (o.flag) {
                        parsed.put(o.name, "true");
                    }
                    else {
                        // value in next arg
                        key = false;
                    }
                }
            }
            else {
                // value
                parsed.put(o.name, checkValue(o, arg));
                // next arg must be a key
                key = true;
                o = null;
            }
        }
        
        if (o != null) {
            throw new IllegalArgumentException("Missing value for option '" + o.name + "'");
        }
        fillDefaults(opts, parsed);
        checkMissing(opts, parsed);
        
        return parsed;
    }

    private void checkMissing(Map<String, Opt> opts, Map<String, String> parsed) {
        for (Map.Entry<String, Opt> e : opts.entrySet()) {
            if (e.getValue().mandatory && !parsed.containsKey(e.getKey())) {
                throw new IllegalArgumentException("Missing mandatory option '" + e.getKey() + "'");
            }
        }
    }

    private void fillDefaults(Map<String, Opt> opts, Map<String, String> parsed) {
        for (Map.Entry<String, Opt> e : opts.entrySet()) {
            String defValue = e.getValue().defValue;
            if (defValue != null && !parsed.containsKey(e.getKey())) {
                parsed.put(e.getKey(), defValue);
            }
        }
    }

    private String checkValue(Opt o, String arg) {
        if (checkType(o.type, arg)) {
            return arg;
        }
        else {
            throw new IllegalArgumentException("Invalid value '" + 
                arg + "' for option '" + o.name + "'. Must be " + getAllowedValuesRepr(o.type));
        }
    }

    private String getAllowedValuesRepr(OptType type) {
        switch (type) {
            case INT:
                return "an integer";
            case FLOAT:
                return "a number";
            case BOOLEAN:
                return "either 'true' or 'false'";
            default:
                return "a string";
        }
    }

    private Opt checkKey(Map<String, Opt> opts, Map<String, String> parsed, String k, char sep) {
        if (parsed.containsKey(k)) {
            throw new IllegalArgumentException("Duplicate option '" + k + "'");
        }
        Opt o = opts.get(k);
        if (o == null) {
            throw new IllegalArgumentException("Invalid option: '" + k + "'");
        }
        if (o.sep != sep) {
            throw new IllegalArgumentException("Invalid separator ('" + 
                sep + "' for '" + k + "'. Expected '" + o.sep + "'");
        }
        return o;
    }

    private Map<String, Opt> buildOptMap(String fmt) {
        Map<String, Opt> opts = new HashMap<String, Opt>();
        String[] fmts = fmt.split("\\s*,\\s*");
        for (int i = 0; i < fmts.length; i++) {
            Opt o = makeOpt(fmts[i]);
            opts.put(o.name, o);
        }
        return opts;
    }

    private Opt makeOpt(String s) {
        String crt = s;
        Opt o = new Opt();
        if (s.startsWith("[")) {
            if (!s.endsWith("]")) {
                throw new IllegalArgumentException("Invalid option specification: '" + s + "'. Should end with ']'");
            }
            o.mandatory = false;
            crt = s.substring(1, s.length() - 1);
        }
        else {
            o.mandatory = true;
        }
        
        int sepIndex = -1;
        out: 
        for (int i = 0; i < crt.length(); i++) {
            char c = crt.charAt(i);
            switch (c) {
                case ' ':
                case ':':
                case '=':
                    sepIndex = i;
                    o.sep = c;
                    break out;
            }
        }
        if (sepIndex == -1) {
            o.sep = ' ';
            o.name = crt;
            if (o.mandatory) {
                throw new IllegalArgumentException("Missing required option type in '" + s + "'");
            }
            else {
                o.flag = true;
                o.type = OptType.BOOLEAN;
            }
        }
        else {
            o.name = crt.substring(0, sepIndex);
            String typespec = crt.substring(sepIndex + 1).trim();
            if (typespec.isEmpty()) {
                throw new IllegalArgumentException("Missing required option type in '" + s + "'");
            }
            if (!typespec.startsWith("<") || !typespec.endsWith(">")) {
                throw new IllegalArgumentException("Invalid option type specification: '" + typespec + 
                    "'. Missing '<' and '>'");
            }
            typespec = typespec.substring(1, typespec.length() - 1);
            int ic = typespec.indexOf(':');
            if (ic != -1) {
                o.defValue = typespec.substring(ic + 1);
                typespec = typespec.substring(0, ic);
            }
            o.type = OptType.fromString(typespec);
            if (!checkType(o.type, o.defValue)) {
                throw new IllegalArgumentException("Invalid default value for option '" + o.name + 
                    "': '" + o.defValue + "'");
            }
        }
        return o;
    }

    private boolean checkType(OptType type, String v) {
        if (v == null) {
            return true;
        }
        switch (type) {
            case INT:
                try {
                    Integer.parseInt(v);
                    return true;
                }
                catch (NumberFormatException e) {
                    return false;
                }
            case FLOAT:
                try {
                    Double.parseDouble(v);
                    return true;
                }
                catch (NumberFormatException e) {
                    return false;
                }
            case BOOLEAN:
                return v.equals("true") || v.equals("false");
            case STRING:
            default:
                return true;
        }
    }
}
