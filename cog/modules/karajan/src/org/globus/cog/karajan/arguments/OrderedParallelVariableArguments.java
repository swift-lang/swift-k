//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 2, 2005
 */
package org.globus.cog.karajan.arguments;

import java.util.ArrayList;
import java.util.List;

import org.globus.cog.karajan.util.TypeUtil;

public class OrderedParallelVariableArguments extends AbstractWriteOnlyVariableArguments {
	private final VariableArguments dest;
	private final OrderedParallelVariableArguments prev;
	private OrderedParallelVariableArguments next;
	private List buffer;
	private boolean closed, prevClosed;

	public OrderedParallelVariableArguments(VariableArguments dest,
			OrderedParallelVariableArguments prev) {
		this.dest = dest;
		this.prev = prev;
		if (prev != null) {
			prev.next = this;
		}
	}

	private synchronized boolean isPrevClosed() {
		if (prev == null) {
			return true;
		}
		else {
			return prevClosed;
		}
	}

	private void initBuffer() {
		if (buffer == null) {
			buffer = new ArrayList();
		}
	}

	public final boolean isClosed() {
		return closed && isPrevClosed();
	}

	public synchronized void close() {
		boolean prevClosed = isPrevClosed();

		if (prevClosed) {
			flushBuffer();
		}

		if (next != null && prevClosed) {
			next.prevClosed();
		}
		this.closed = true;
	}

	private void flushBuffer() {
		if (buffer != null) {
			dest.appendAll(buffer);
			buffer = null;
		}
	}

	protected synchronized void prevClosed() {
        OrderedParallelVariableArguments i = this;
        while (i != null) {
        	i.flushBuffer();
            i.prevClosed = true;
            if (i.closed) {
                i = i.next;
            }
            else {
                break;
            }
        };
	}

	public void merge(VariableArguments args) {
		appendAll(args.getAll());
	}

	public synchronized void append(Object value) {
		if (isPrevClosed()) {
			dest.append(value);
		}
		else {
			initBuffer();
			buffer.add(value);
		}
	}

	public synchronized void appendAll(List args) {
		if (isPrevClosed()) {
			dest.appendAll(args);
		}
		else {
			initBuffer();
			buffer.addAll(args);
		}
	}

	public String toString() {
		if (buffer != null) {
			return "-" + TypeUtil.listToString(buffer) + "-";
		}
		else {
			return "-||-";
		}
	}

	public OrderedParallelVariableArguments getNext() {
		return next;
	}

	public boolean isCommutative() {
		return dest.isCommutative();
	}
}
