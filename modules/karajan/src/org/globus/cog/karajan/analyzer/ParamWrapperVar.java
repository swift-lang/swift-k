//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 10, 2012
 */
package org.globus.cog.karajan.analyzer;


public class ParamWrapperVar extends Var {
	protected Param param;
	
	protected ParamWrapperVar(Param p) {
		super(p.varName());
		this.param = p;
	}
	
	@Override
	public Object getValue() {
		return param.getValue();
	}

	@Override
	public void setValue(Object value) {
		param.setValue(value);
	}

	@Override
	public void setDynamic() {
		super.setDynamic();
		param.setDynamic();
	}

	public static class Positional extends ParamWrapperVar {
		public Positional(Param p) {
		    super(p);
		}
		
		@Override
		public void setIndex(int index) {
			super.setIndex(index);
			param.setIndex(index);
		}

		@Override
		public boolean isSettableByName() {
			return false;
		}
	}
	
	public static class UDFOptional extends Positional {
		public UDFOptional(Param p) {
		    super(p);
		}
		
		@Override
		public boolean isSettableByName() {
			return true;
		}
	}
	
	public static class Optional extends ParamWrapperVar {
		private IndexRange ir;
		
		public Optional(Param p, IndexRange ir) {
		    super(p);
		    this.ir = ir;
		}
		
		@Override
        public void setIndex(int index) {
            super.setIndex(index);
            param.setIndex(index);
        }
		
		@Override
		public void setDynamic() {
			super.setDynamic();
			setIndex(ir.nextIndex());
		}
	}
	
	public static class IndexRange {
	    private int crt;
	    private final int last, first;
	    
	    public IndexRange(int crt, int size) {
	        this.first = crt;
	        this.crt = crt;
	        this.last = crt + size - 1;
	    }
	    
	    public int nextIndex() {
	        return crt++;
	    }
	    
	    public int currentIndex() {
	        return crt;
	    }
	    
	    public int lastIndex() {
	        return last;
	    }
	    
	    public int firstIndex() {
	        return first;
	    }

		public boolean isUsed() {
			return crt != first;
		}
	}
}
