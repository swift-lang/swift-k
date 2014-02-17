//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 27, 2006
 */
package org.globus.cog.karajan.compiled.nodes.grid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import k.rt.Context;
import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.Contact;

public class BDP extends InternalFunction {
	public static final String TCPBUFSZNONE = "-1";
	public static final String TCPBUFSZLATE = "";
	
	private ArgRef<Object> srchost;
	private ArgRef<Object> desthost;
	private ArgRef<String> provider;
	
	private VarRef<Object> r_tcpBufferSize;
	private VarRef<Context> context;

	@Override
	protected Signature getSignature() {
		return new Signature(params("srchost", "desthost", "provider"), returns("tcpBufferSize"));
	}

	@Override
	protected void addLocals(Scope scope) {
		super.addLocals(scope);
		context = scope.getVarRef("#context");
	}


	@Override
	public void runBody(LWThread thr) {
		Stack stack = thr.getStack();
		String provider = this.provider.getValue(stack);
		if (provider.equalsIgnoreCase("gsiftp") || provider.equalsIgnoreCase("gridftp")) {
			Map<String, Map<String, String>> bdp = getBDP(stack);
			
			Object bufsz = TCPBUFSZNONE;

			try {
				Object srchost = this.srchost.getValue(stack);
				String ssrchost = getSHost(srchost);
				Object desthost = this.desthost.getValue(stack);
				String sdesthost = getSHost(desthost);
				if (ssrchost != null && sdesthost != null) {
					bufsz = getBufferSize(bdp, ssrchost, sdesthost);
				}
				else {
					bufsz = bdp;
				}
				if (bufsz != null && !bufsz.equals(TCPBUFSZNONE)) {
					this.r_tcpBufferSize.setValue(stack, bufsz);
				}
			}
			catch (ExecutionException e) {
			}
		}
	}

	private Map<String, Map<String, String>> getBDP(Stack stack) {
		Context ctx = this.context.getValue(stack);
		synchronized (ctx) {
			@SuppressWarnings("unchecked")
			Map<String, Map<String, String>> bdp = (Map<String, Map<String, String>>) ctx.getAttribute("bdp.conf");
			if (bdp == null) {
				bdp = parseBDP();
				ctx.setAttribute("bdp.conf", bdp);
			}
			return bdp;
		}
	}

	public static String getBufferSize(Map<String, Map<String, String>> map, String src, String dest) {
		Map<String, String> destmap = match(map, src);
		if (destmap != null) {
			String value = match(destmap, dest);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	private String getSHost(Object host) throws ExecutionException {
		if (host instanceof Contact) {
			if (((Contact) host).isVirtual()) {
				return null;
			}
			else {
				BoundContact bc = (BoundContact) host;
				if (bc.hasService(Service.FILE_OPERATION, "gsiftp")) {
					return ((BoundContact) host).getService(Service.FILE_OPERATION, "gsiftp").getServiceContact().getContact();
				}
				else if (bc.hasService(Service.FILE_OPERATION, "gridftp")) {
					return ((BoundContact) host).getService(Service.FILE_OPERATION, "gridftp").getServiceContact().getContact();
				}
				else {
					throw new ExecutionException();
				}
			}
		}
		else if (host instanceof String) {
			return (String) host;
		}
		else {
			throw new ExecutionException("Unknown host parameter: " + host);
		}
	}

	private static <T> T match(Map<String, T> map, String str) {
		for (Map.Entry<String, T> e : map.entrySet()) {
			if (str.matches(e.getKey())) {
				return e.getValue();
			}
		}
		return null;
	}

	protected Map<String, Map<String, String>> parseBDP() throws ExecutionException {
		try {
			URL bdp = BDP.class.getClassLoader().getResource("bdp.conf");
			if (bdp == null) {
				throw new ExecutionException("bdp.conf not found in classpath");
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(bdp.openStream()));
			String line = br.readLine();
			Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
			while (line != null) {
				if (line.trim().length() == 0) {
					continue;
				}
				int s1 = line.indexOf(',');
				int s2 = line.indexOf("->");
				if (s1 == -1 || s2 == -1) {
					throw new ExecutionException("Invalid line in bdf.conf: " + line);
				}
				String src = ".*" + line.substring(0, s1).trim().replaceAll("\\.", "\\.")
						+ "(:.*)?";
				Map<String, String> sm = map.get(src);
				if (sm == null) {
					sm = new HashMap<String, String>();
					map.put(src, sm);
				}
				String dest = ".*" + line.substring(s1 + 1, s2).trim().replaceAll("\\.", "\\.")
						+ "(:.*)?";
				String sz = getSize(line.substring(s2 + 2).trim());
				sm.put(dest, sz);
				line = br.readLine();
			}
			return map;
		}
		catch (IOException e) {
			throw new ExecutionException("Error reading resource bdf.conf", e);
		}
	}

	protected String getSize(String sz) throws ExecutionException {
		char c = sz.charAt(sz.length() - 1);
		if (Character.isDigit(c)) {
			return sz;
		}
		int value = Integer.parseInt(sz.substring(0, sz.length() - 1));
		switch (c) {
			case 'K':
				return String.valueOf(value * 1024);
			case 'M':
				return String.valueOf(value * 1024 * 1024);
			case 'G':
				return String.valueOf(value * 1024 * 1024 * 1024);
			default:
				throw new ExecutionException("Invalid suffix: " + c);
		}
	}
}
