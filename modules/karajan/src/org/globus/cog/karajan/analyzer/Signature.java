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
 * Created on Dec 5, 2012
 */
package org.globus.cog.karajan.analyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Signature {
	private List<Param> params;
	private List<Param> channelParams;
	private List<Param> returns;
	private List<Param> channelReturns;
	
	private Signature() {
	}
		
	public Signature(Param[] params) {
		this(Arrays.asList(params));
	}
	
	public Signature(List<Param> params) {
		addParams(params);
	}

	public Signature(Param[] params, Param[] returns) {
		this(params);
		addReturns(returns);
	}
	
	public void addParams(List<Param> params) {
	    for (Param p : params) {
            if (p.type == Param.Type.CHANNEL) {
                channelParams = add(channelParams, p);
            }
            else {
                this.params = add(this.params, p);
            }
        }
	}
	
	public void addReturns(Param[] returns) {
        if (returns != null) {
            for (Param p : returns) {
                addReturn(p);                
            }
        }
	}
	
	public void addReturn(Param p) {
	    if (p.type == Param.Type.CHANNEL) {
            channelReturns = add(channelReturns, p);
        }
        else {
            returns = add(returns, p);
        }
	}

	private <T> List<T> add(List<T> l, T p) {
		if (l == null) {
			l = new ArrayList<T>();
		}
		l.add(p);
		return l;
	}

	public List<Param> getParams() {
		return emptyIfNull(params);
	}

	private <T> List<T> emptyIfNull(List<T> p) {
		if (p == null) {
			return Collections.emptyList();
		}
		else {
			return p;
		}
	}

	public List<Param> getChannelParams() {
		return emptyIfNull(channelParams);
	}

	public List<Param> getReturns() {
		return emptyIfNull(returns);
	}

	public List<Param> getChannelReturns() {
		return emptyIfNull(channelReturns);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		boolean first = true;
		if (params != null) {
			first = appendAll(sb, params, first);
		}
		if (channelParams != null) {
			first = appendAll(sb, channelParams, first);
		}
		sb.append(") -> (");
		first = true;
		if (returns != null) {
			first = appendAll(sb, returns, first);
		}
		if (channelReturns != null) {
			first = appendAll(sb, channelReturns, first);
		}
		sb.append(")");
		return sb.toString();
	}

	private boolean appendAll(StringBuilder sb, List<Param> l, boolean first) {
		for (Param p : l) {
			if (!first) sb.append(", "); else first = false;
			sb.append(p);
		}
		return first;
	}

	public Signature copy() {
		Signature sig = new Signature();
		sig.params = copyParams(params);
		sig.channelParams = copyParams(channelParams);
		sig.returns = copyParams(returns);
		sig.channelReturns = copyParams(channelReturns);
		
		return sig;
	}

	private List<Param> copyParams(List<Param> l) {
		if (l == null) {
			return null;
		}
		List<Param> c = new LinkedList<Param>();
		for (Param p : l) {
			c.add(p.copy());
		}
		return c;
	}
}
