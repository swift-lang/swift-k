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
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.compiled.nodes.functions.BinaryOp;
import org.globus.cog.karajan.compiled.nodes.functions.UnaryOp;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.RootDataNode;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;
import org.griphyn.vdl.util.VDL2Config;

public class Operators {
	
	public static final boolean PROVENANCE_ENABLED;
	
	static {
		boolean v;
		try {
		    v = VDL2Config.getConfig().getProvenanceLog();
		}
		catch (Exception e) {
			v = false;
		}
		PROVENANCE_ENABLED = v;
	}

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
			throw new ExecutionException(n, "Internal error. Expected an int: " + h);
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
		
	public static class Sum extends BinaryOp<DSHandle, DSHandle> {
        @Override
        protected DSHandle value(DSHandle v1, DSHandle v2) {
            Type t = type(v1, v2);
            DSHandle r;
            if (t == Types.STRING) {
            	r = new RootDataNode(Types.STRING, (String.valueOf(v1.getValue()) + String.valueOf(v2.getValue())));
            }
            else if (t == Types.INT) {
            	r = new RootDataNode(Types.INT, getInt(this, v1) + getInt(this, v2));
            }
            else {
            	r = new RootDataNode(Types.FLOAT, getFloat(this, v1) + getFloat(this, v2));
            }
            logBinaryProvenance("sum", v1, v2, r);
            return r;
        }
	}
	
	public static class Difference extends BinaryOp<DSHandle, DSHandle> {
        @Override
        protected DSHandle value(DSHandle v1, DSHandle v2) {
            Type t = type(v1, v2);
            DSHandle r;
            if (t == Types.INT) {
                r = new RootDataNode(Types.INT, getInt(this, v1) - getInt(this, v2));
            }
            else {
                r = new RootDataNode(Types.FLOAT, getFloat(this, v1) - getFloat(this, v2));
            }
            logBinaryProvenance("difference", v1, v2, r);
            return r;
        }
    }
	
	public static class Product extends BinaryOp<DSHandle, DSHandle> {
        @Override
        protected DSHandle value(DSHandle v1, DSHandle v2) {
            Type t = type(v1, v2);
            DSHandle r;
            if (t == Types.INT) {
                r = new RootDataNode(Types.INT, getInt(this, v1) * getInt(this, v2));
            }
            else {
                r = new RootDataNode(Types.FLOAT, getFloat(this, v1) * getFloat(this, v2));
            }
            logBinaryProvenance("product", v1, v2, r);
            return r;
        }
    }
	
	public static class FQuotient extends BinaryOp<DSHandle, DSHandle> {
        @Override
        protected DSHandle value(DSHandle v1, DSHandle v2) {
            DSHandle r = new RootDataNode(Types.FLOAT, getFloat(this, v1) / getFloat(this, v2));
            logBinaryProvenance("fquotient", v1, v2, r);
            return r;
        }
    }
	
	public static class Quotient extends FQuotient {
	}

	public static class IQuotient extends BinaryOp<DSHandle, DSHandle> {
        @Override
        protected DSHandle value(DSHandle v1, DSHandle v2) {
            DSHandle r = new RootDataNode(Types.INT, getInt(this, v1) / getInt(this, v2));
            logBinaryProvenance("iquotient", v1, v2, r);
            return r;
        }
    }

	public static class Remainder extends BinaryOp<DSHandle, DSHandle> {
        @Override
        protected DSHandle value(DSHandle v1, DSHandle v2) {
            Type t = type(v1, v2);
            DSHandle r;
            if (t == Types.INT) {
                r = new RootDataNode(Types.INT, getInt(this, v1) % getInt(this, v2));
            }
            else {
                r = new RootDataNode(Types.FLOAT, getFloat(this, v1) % getFloat(this, v2));
            }
            logBinaryProvenance("remainder", v1, v2, r);
            return r;
        }
    }
	
	public static class LE extends BinaryOp<DSHandle, DSHandle> {
        @Override
        protected DSHandle value(DSHandle v1, DSHandle v2) {
            DSHandle r = new RootDataNode(Types.BOOLEAN, getFloat(this, v1) <= getFloat(this, v2));
            logBinaryProvenance("le", v1, v2, r);
            return r;
        }
    }
	
	public static class GE extends BinaryOp<DSHandle, DSHandle> {
        @Override
        protected DSHandle value(DSHandle v1, DSHandle v2) {
            DSHandle r = new RootDataNode(Types.BOOLEAN, getFloat(this, v1) >= getFloat(this, v2));
            logBinaryProvenance("ge", v1, v2, r);
            return r;
        }
    }
	
	public static class LT extends BinaryOp<DSHandle, DSHandle> {
        @Override
        protected DSHandle value(DSHandle v1, DSHandle v2) {
            DSHandle r = new RootDataNode(Types.BOOLEAN, getFloat(this, v1) < getFloat(this, v2));
            logBinaryProvenance("lt", v1, v2, r);
            return r;
        }
    }
	
	public static class GT extends BinaryOp<DSHandle, DSHandle> {
        @Override
        protected DSHandle value(DSHandle v1, DSHandle v2) {
            DSHandle r = new RootDataNode(Types.BOOLEAN, getFloat(this, v1) > getFloat(this, v2));
            logBinaryProvenance("gt", v1, v2, r);
            return r;
        }
    }
	
	public static class EQ extends BinaryOp<DSHandle, DSHandle> {
        @Override
        protected DSHandle value(DSHandle v1, DSHandle v2) {
            DSHandle r = new RootDataNode(Types.BOOLEAN, v1.getValue().equals(v2.getValue()));
            logBinaryProvenance("eq", v1, v2, r);
            return r;
        }
    }
	
	public static class NE extends BinaryOp<DSHandle, DSHandle> {
        @Override
        protected DSHandle value(DSHandle v1, DSHandle v2) {
            DSHandle r = new RootDataNode(Types.BOOLEAN, !v1.getValue().equals(v2.getValue()));
            logBinaryProvenance("ne", v1, v2, r);
            return r;
        }
    }
	
	public static class And extends BinaryOp<DSHandle, DSHandle> {
        @Override
        protected DSHandle value(DSHandle v1, DSHandle v2) {
            DSHandle r = new RootDataNode(Types.BOOLEAN, getBool(this, v1) && getBool(this, v2));
            logBinaryProvenance("and", v1, v2, r);
            return r;
        }
    }

	public static class Or extends BinaryOp<DSHandle, DSHandle> {
        @Override
        protected DSHandle value(DSHandle v1, DSHandle v2) {
            DSHandle r = new RootDataNode(Types.BOOLEAN, getBool(this, v1) || getBool(this, v2));
            logBinaryProvenance("or", v1, v2, r);
            return r;
        }
    }
	
	public static class Not extends UnaryOp<DSHandle, DSHandle> {
        @Override
        protected DSHandle value(DSHandle v) {
            DSHandle r = new RootDataNode(Types.BOOLEAN, !getBool(this, v));
            logUnaryProvenance("not", v, r);
            return r;
        }
    }

	private static void logBinaryProvenance(String name, DSHandle v1, DSHandle v2, DSHandle result) throws ExecutionException {
		if (PROVENANCE_ENABLED) {
			String thread = LWThread.currentThread().getName();
			String lhsid = v1.getIdentifier();
			String rhsid = v2.getIdentifier();
			String rid = result.getIdentifier();
			provenanceLogger.info("OPERATOR thread=" + thread + " operator=" + name + 
					" lhs=" + lhsid + " rhs=" + rhsid + " result=" + rid);
		}
	}

	private static void logUnaryProvenance(String name, DSHandle v, DSHandle r) throws ExecutionException {
		if (PROVENANCE_ENABLED) {
			String thread = LWThread.currentThread().getName();
			String vid = v.getIdentifier();
			String rid = r.getIdentifier();
			provenanceLogger.info("UNARYOPERATOR thread=" + thread + " operator=" + name + 
					" operand=" + vid + " result=" + rid);
		}
	}
}

