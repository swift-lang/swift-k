//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 30, 2007
 */
package k.rt;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.globus.cog.karajan.util.TypeUtil;

public abstract class StdSink extends Sink<Object> {
	private PrintStream ps;
	
	protected StdSink(PrintStream ps) {
		this.ps = ps;
	}

	@Override
	public synchronized boolean add(Object value) {
		ps.print(TypeUtil.toString(value));
		return true;
	}
	
	@Override
	public boolean addAll(Collection<? extends Object> values) {
		for (Object o : values) {
			add(o);
		}
		return true;
	}
	
	@Override
    public List<Object> getAll() {
        return Collections.emptyList();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Object removeFirst() {
        throw new UnsupportedOperationException();
    }


    public static class StdoutSink extends StdSink {
		public StdoutSink() {
			super(System.out);
		}
		
		public String toString() {
		    return ">STDOUT";
		}
	}
    
    public static class StdoutNLSink extends StdSink {
        public StdoutNLSink() {
            super(System.out);
        }
        
        
        
        @Override
        public Object get(int index) {
            throw new UnsupportedOperationException("get");
        }

        @Override
        public boolean add(Object value) {
            super.add(": ");
            super.add(value);
            super.add("\n");
            return true;
        }

        public String toString() {
            return ">STDOUT";
        }
    }
	
	public static class StderrSink extends StdSink {
		public StderrSink() {
			super(System.err);
		}
		
		public String toString() {
            return ">STDERR";
        }
	}
}
