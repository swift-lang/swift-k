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


package org.griphyn.vdl.karajan.lib;

import k.rt.ExecutionException;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.util.StringCache;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;
import org.griphyn.vdl.util.SwiftConfig;

public class Operators {
	
	public static final boolean PROVENANCE_ENABLED = SwiftConfig.getDefault().isProvenanceEnabled();

	public static final Logger provenanceLogger = Logger.getLogger("org.globus.swift.provenance.operators");

	private static Type type(DSHandle l, DSHandle r) throws ExecutionException {
		Type tl = l.getType();
		Type tr = r.getType();
		if (Types.STRING.equals(tl) || Types.STRING.equals(tr)) {
			return Types.STRING;
		}
		else if (Types.FLOAT.equals(tl) || Types.FLOAT.equals(tr)) {
			return Types.FLOAT;
		}
		else {
			return Types.INT;
		}
	}

	private static int getInt(Node n, DSHandle h) {
		waitFor(n, h);
		Object v = h.getValue();
		if (v instanceof Integer) {
			return ((Integer) v).intValue();
		}
		else {
			throw new ExecutionException(n, "Internal error. Expected an int: " + v + " (" + h + ")");
		}
	}
	
	private static void waitFor(Node n, DSHandle h) {
        ((AbstractDataNode) h).waitFor(n);
    }

    private static double getFloat(Node n, DSHandle h) {
		waitFor(n, h);
        Object v = h.getValue();
        if (v instanceof Number) {
            return ((Number) v).doubleValue();
        }
        else {
            throw new ExecutionException(n, "Internal error. Expected float: " + h);
        }
    }
	
	private static boolean getBool(Node n, DSHandle h) {
		waitFor(n, h);
        Object v = h.getValue();
        if (v instanceof Boolean) {
            return ((Boolean) v).booleanValue();
        }
        else {
            throw new ExecutionException(n, "Internal error. Expected float: " + h);
        }
    }
		
	public static class Sum extends SwiftBinaryOp {
        @Override
        protected DSHandle value(AbstractDataNode v1, AbstractDataNode v2) {
            Type t = type(v1, v2);
            DSHandle r;
            try {
                if (t == Types.STRING) {
                	r = NodeFactory.newRoot(Field.GENERIC_STRING, StringCache.intern((String.valueOf(v1.getValue()) + String.valueOf(v2.getValue()))));
                }
                else if (t == Types.INT) {
                	r = NodeFactory.newRoot(Field.GENERIC_INT, getInt(this, v1) + getInt(this, v2));
                }
                else {
                	r = NodeFactory.newRoot(Field.GENERIC_FLOAT, getFloat(this, v1) + getFloat(this, v2));
                }
                logBinaryProvenance("sum", v1, v2, r);
                return r;
            }
            catch (DependentException e) {
                return NodeFactory.newRoot(Field.Factory.getImmutableField("?", t), e);
            }
        }
	}
	
	public static class Difference extends SwiftBinaryOp {
        @Override
        protected DSHandle value(AbstractDataNode v1, AbstractDataNode v2) {
            Type t = type(v1, v2);
            DSHandle r;
            try {
                if (t == Types.INT) {
                    r = NodeFactory.newRoot(Field.GENERIC_INT, getInt(this, v1) - getInt(this, v2));
                }
                else {
                    r = NodeFactory.newRoot(Field.GENERIC_FLOAT, getFloat(this, v1) - getFloat(this, v2));
                }
                logBinaryProvenance("difference", v1, v2, r);
                return r;
            }
            catch (DependentException e) {
                return NodeFactory.newRoot(Field.Factory.getImmutableField("?", t), e);
            }
        }
    }
	
	public static class Product extends SwiftBinaryOp {
        @Override
        protected DSHandle value(AbstractDataNode v1, AbstractDataNode v2) {
            Type t = type(v1, v2);
            DSHandle r;
            try {
                if (t == Types.INT) {
                    r = NodeFactory.newRoot(Field.GENERIC_INT, getInt(this, v1) * getInt(this, v2));
                }
                else {
                    r = NodeFactory.newRoot(Field.GENERIC_FLOAT, getFloat(this, v1) * getFloat(this, v2));
                }
                logBinaryProvenance("product", v1, v2, r);
                return r;
            }
            catch (DependentException e) {
                return NodeFactory.newRoot(Field.Factory.getImmutableField("?", t), e);
            }
        }
    }
	
	public static class FQuotient extends SwiftBinaryOp {
        @Override
        protected DSHandle value(AbstractDataNode v1, AbstractDataNode v2) {
            DSHandle r = NodeFactory.newRoot(Field.GENERIC_FLOAT, getFloat(this, v1) / getFloat(this, v2));
            logBinaryProvenance("fquotient", v1, v2, r);
            return r;
        }
    }
	
	public static class Quotient extends FQuotient {
	}

	public static class IQuotient extends SwiftBinaryOp {
        @Override
        protected DSHandle value(AbstractDataNode v1, AbstractDataNode v2) {
            DSHandle r = NodeFactory.newRoot(Field.GENERIC_INT, getInt(this, v1) / getInt(this, v2));
            logBinaryProvenance("iquotient", v1, v2, r);
            return r;
        }
    }

	public static class Remainder extends SwiftBinaryOp {
        @Override
        protected DSHandle value(AbstractDataNode v1, AbstractDataNode v2) {
            Type t = type(v1, v2);
            DSHandle r;
            try {
                if (t == Types.INT) {
                    r = NodeFactory.newRoot(Field.GENERIC_INT, getInt(this, v1) % getInt(this, v2));
                }
                else {
                    r = NodeFactory.newRoot(Field.GENERIC_FLOAT, getFloat(this, v1) % getFloat(this, v2));
                }
                logBinaryProvenance("remainder", v1, v2, r);
                return r;
            }
            catch (DependentException e) {
                return NodeFactory.newRoot(Field.Factory.getImmutableField("?", t), e);
            }
        }
    }
	
	public static class LE extends SwiftBinaryOp {
        @Override
        protected Field getReturnType() {
            return Field.GENERIC_BOOLEAN;
        }

        @Override
        protected DSHandle value(AbstractDataNode v1, AbstractDataNode v2) {
            DSHandle r = NodeFactory.newRoot(Field.GENERIC_BOOLEAN, getFloat(this, v1) <= getFloat(this, v2));
            logBinaryProvenance("le", v1, v2, r);
            return r;
        }
    }
	
	public static class GE extends SwiftBinaryOp {
	    @Override
        protected Field getReturnType() {
            return Field.GENERIC_BOOLEAN;
        }

        @Override
        protected DSHandle value(AbstractDataNode v1, AbstractDataNode v2) {
            DSHandle r = NodeFactory.newRoot(Field.GENERIC_BOOLEAN, getFloat(this, v1) >= getFloat(this, v2));
            logBinaryProvenance("ge", v1, v2, r);
            return r;
        }
    }
	
	public static class LT extends SwiftBinaryOp {
	    @Override
        protected Field getReturnType() {
            return Field.GENERIC_BOOLEAN;
        }
	    
        @Override
        protected DSHandle value(AbstractDataNode v1, AbstractDataNode v2) {
            DSHandle r = NodeFactory.newRoot(Field.GENERIC_BOOLEAN, getFloat(this, v1) < getFloat(this, v2));
            logBinaryProvenance("lt", v1, v2, r);
            return r;
        }
    }
	
	public static class GT extends SwiftBinaryOp {
	    @Override
        protected Field getReturnType() {
            return Field.GENERIC_BOOLEAN;
        }
        
        @Override
        protected DSHandle value(AbstractDataNode v1, AbstractDataNode v2) {
            DSHandle r = NodeFactory.newRoot(Field.GENERIC_BOOLEAN, getFloat(this, v1) > getFloat(this, v2));
            logBinaryProvenance("gt", v1, v2, r);
            return r;
        }
    }
	
	public static class EQ extends SwiftBinaryOp {
	    @Override
        protected Field getReturnType() {
            return Field.GENERIC_BOOLEAN;
        }
        
        @Override
        protected DSHandle value(AbstractDataNode v1, AbstractDataNode v2) {
            DSHandle r = NodeFactory.newRoot(Field.GENERIC_BOOLEAN, equals(v1.getValue(), v2.getValue()));
            logBinaryProvenance("eq", v1, v2, r);
            return r;
        }

        private boolean equals(Object v1, Object v2) {
            if (v1 instanceof Integer && v2 instanceof Integer) {
                return v1.equals(v2);
            }
            else if (v1 instanceof Number) {
                if (v2 instanceof Number) {
                    return ((Number) v1).doubleValue() == ((Number) v2).doubleValue();
                }
                else {
                    return false;
                }
            }
            else {
                return v1.equals(v2);
            }
        }
    }
	
	public static class NE extends SwiftBinaryOp {
	    @Override
        protected Field getReturnType() {
            return Field.GENERIC_BOOLEAN;
        }
        
        @Override
        protected DSHandle value(AbstractDataNode v1, AbstractDataNode v2) {
            DSHandle r = NodeFactory.newRoot(Field.GENERIC_BOOLEAN, !v1.getValue().equals(v2.getValue()));
            logBinaryProvenance("ne", v1, v2, r);
            return r;
        }
    }
	
	public static class And extends SwiftBinaryOp {
	    @Override
        protected Field getReturnType() {
            return Field.GENERIC_BOOLEAN;
        }
        
        @Override
        protected DSHandle value(AbstractDataNode v1, AbstractDataNode v2) {
            DSHandle r = NodeFactory.newRoot(Field.GENERIC_BOOLEAN, getBool(this, v1) && getBool(this, v2));
            logBinaryProvenance("and", v1, v2, r);
            return r;
        }
    }

	public static class Or extends SwiftBinaryOp {
	    @Override
        protected Field getReturnType() {
            return Field.GENERIC_BOOLEAN;
        }
        
        @Override
        protected DSHandle value(AbstractDataNode v1, AbstractDataNode v2) {
            DSHandle r = NodeFactory.newRoot(Field.GENERIC_BOOLEAN, getBool(this, v1) || getBool(this, v2));
            logBinaryProvenance("or", v1, v2, r);
            return r;
        }
    }
	
	public static class Not extends SwiftUnaryOp {
	    @Override
        protected Field getReturnType() {
            return Field.GENERIC_BOOLEAN;
        }
        
        @Override
        protected DSHandle value(AbstractDataNode v) {
            DSHandle r = NodeFactory.newRoot(Field.GENERIC_BOOLEAN, !getBool(this, v));
            logUnaryProvenance("not", v, r);
            return r;
        }
    }
	
	public static class Inc extends SwiftUnaryOp {
	    @Override
        protected Field getReturnType() {
            return Field.GENERIC_INT;
        }
        
        @Override
        protected DSHandle value(AbstractDataNode v) {
            DSHandle r = NodeFactory.newRoot(Field.GENERIC_INT, getInt(this, v) + 1);
            return r;
        }
    }

	private static void logBinaryProvenance(String name, DSHandle v1, DSHandle v2, DSHandle result) throws ExecutionException {
		if (PROVENANCE_ENABLED) {
			String thread = SwiftFunction.getThreadPrefix();
			String lhsid = v1.getIdentifier();
			String rhsid = v2.getIdentifier();
			String rid = result.getIdentifier();
			provenanceLogger.info("OPERATOR thread=" + thread + " operator=" + name + 
					" lhs=" + lhsid + " rhs=" + rhsid + " result=" + rid);
		}
	}

	private static void logUnaryProvenance(String name, DSHandle v, DSHandle r) throws ExecutionException {
		if (PROVENANCE_ENABLED) {
			String thread = SwiftFunction.getThreadPrefix();
			String vid = v.getIdentifier();
			String rid = r.getIdentifier();
			provenanceLogger.info("UNARYOPERATOR thread=" + thread + " operator=" + name + 
					" operand=" + vid + " result=" + rid);
		}
	}
}

