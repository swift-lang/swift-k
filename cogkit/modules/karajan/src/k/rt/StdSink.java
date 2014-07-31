/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
