// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 15, 2005
 */
package org.globus.cog.karajan.arguments;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.globus.cog.karajan.arguments.Arg.Channel;

public class Arguments {
	private Object type;
	private NamedArguments named;
	private VariableArguments vargs;
	private Map channels = Collections.EMPTY_MAP;

	private static final VariableArguments EMPTYVARGS = new VariableArgumentsImpl();
	private static final NamedArguments EMPTYNARGS = new NamedArgumentsImpl();

	public boolean equals(Object other) {
		if (other instanceof Arguments) {
			Arguments o = (Arguments) other;
			if (type != null) {
				if (!type.equals(o.type)) {
					return false;
				}
			}
			else {
				if (o.type != null) {
					return false;
				}
			}
			if (named == null) {
				if (o.named != null) {
					return false;
				}
			}
			else {
				if (!named.equals(o.named)) {
					return false;
				}
			}

			if (vargs == null) {
				if (o.vargs != null) {
					return false;
				}
			}
			else {
				if (!vargs.equals(o.vargs)) {
					return false;
				}
			}

			if (!channels.equals(o.channels)) {
				return false;
			}

			return true;
		}
		return false;
	}

	public int hashCode() {
		int hash = getClass().hashCode();
		if (type != null) {
			hash += type.hashCode();
		}
		if (named != null) {
			hash <<= 2;
			hash += named.hashCode();
		}
		if (vargs != null) {
			hash <<= 2;
			hash += vargs.hashCode();
		}
		if (channels != null) {
			hash <<= 2;
			hash += channels.hashCode();
		}
		return hash;
	}

	public Arguments copy() {
		Arguments args = new Arguments();
		args.type = type;
		args.named = named.copy();
		args.vargs = vargs.copy();
		if (!channels.isEmpty()) {
			args.channels = new HashMap(channels.size());
			Iterator i = channels.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry e = (Map.Entry) i.next();
				String name = (String) e.getKey();
				args.channels.put(name, ((VariableArguments) e.getValue()).copy());
			}
		}
		return args;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (named != null) {
			buf.append(named.toString());
			buf.append('\n');
		}
		if (vargs != null) {
			buf.append(vargs.toString());
			buf.append('\n');
		}
		Iterator i = channels.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry) i.next();
			buf.append(e.getKey());
			buf.append(": ");
			buf.append(e.getValue());
		}
		return buf.toString();
	}

	public NamedArguments getNamed() {
		if (named == null) {
			named = new NamedArgumentsImpl();
		}
		return named;
	}

	public void setNamed(NamedArguments named) {
		this.named = named;
	}

	public VariableArguments getVargs() {
		if (vargs == null) {
			vargs = new VariableArgumentsImpl();
		}
		return vargs;
	}

	public void setVargs(VariableArguments vargs) {
		this.vargs = vargs;
	}

	public Map getChannels() {
		return channels;
	}

	public void setType(Object type) {
		this.type = type;
	}

	public void addChannel(Channel channel, VariableArguments channelArguments) {
		if (channels.size() == 0) {
			channels = new HashMap(8);
		}
		channels.put(channel, channelArguments);
	}
}