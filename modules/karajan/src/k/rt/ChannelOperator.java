//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 23, 2012
 */
package k.rt;

import java.util.Collection;


public abstract class ChannelOperator<T, S> extends Sink<T> {
	private S value;
	
	public ChannelOperator(S initialValue) {
		this.value = initialValue;
	}

	@Override
	public boolean add(T value) {
		this.value = update(this.value, value);
		return true;
	}
	
	protected abstract S update(S crt, T value);

	@Override
	public boolean addAll(Collection<? extends T> values) {
		for (T v : values) {
			add(v);
		}
		return true;
	}
	
	public S getValue() {
		return value;
	}
	
	public static abstract class Double<P> extends Sink<P> {
		private double value;
		
		public Double(double initialValue) {
			this.value = initialValue;
		}
	
		@Override
		public synchronized boolean add(P value) {
			this.value = update(this.value, value);
			return true;
		}
		
		protected abstract double update(double crt, P value);
	
		@Override
		public synchronized boolean addAll(Collection<? extends P> values) {
			for (P v : values) {
				this.value = update(this.value, v);
			}
			return true;
		}
	
		public synchronized Object getValue() {
			return value;
		}
	}
}
