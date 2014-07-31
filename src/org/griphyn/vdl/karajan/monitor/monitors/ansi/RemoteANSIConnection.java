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


/*
 * Created on Jan 30, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.util.Date;

public class RemoteANSIConnection extends AbstractANSIDisplay {

	public static final int SB = 250;
	public static final int SE = 240;
	public static final int IP = 244;
	public static final int IAC = 255;
	public static final int WILL = 251;
	public static final int WONT = 252;
	public static final int DO = 253;
	public static final int DONT = 254;
	public static final int ECHO = 1;
	public static final int TERMINAL_TYPE = 24;
	public static final int SUPPRESS_GOAHEAD = 3;
	public static final int LINEMODE = 34;
	public static final int BINARY = 0;

	private Socket socket;
	private PushbackInputStream is;
	private OutputStream os;
	private OutputStreamWriter osw;

	public RemoteANSIConnection(ANSIMonitor m, Socket socket) throws IOException {
		super(m.getState(), socket.getInputStream(), socket.getOutputStream());
		this.socket = socket;
	}

	protected void command(int a) throws IOException {
		os.write(IAC);
		os.write(a);
		os.flush();
	}

	protected void command(int a, int b) throws IOException {
		os.write(IAC);
		os.write(a);
		os.write(b);
		os.flush();
	}

	protected void command(int a, int b, int c) throws IOException {
		os.write(IAC);
		os.write(a);
		os.write(b);
		os.write(c);
		os.flush();
	}

	protected void expect(int x) throws IOException {
		int c = is.read();
		if (c != x) {
			throw new RuntimeException("Invalid reply. Expected " + x + ", got " + c);
		}
	}

	protected void processCommand() throws IOException {
		int cmd = is.read();
		switch (cmd) {
			case IP: {
				socket.close();
				break;
			}
		}
	}

	protected void expectReply(int a, int b) throws IOException {
		expect(IAC);
		expect(a);
		expect(b);
	}

	protected void printReply(int count) throws IOException {
		for (int i = 0; i < count; i++) {
			int c = is.read();
			System.out.println(c);
		}
	}

	protected boolean willDo(int what) throws IOException {
		expect(IAC);
		int c = is.read();
		expect(what);
		return c == WILL;
	}

	protected void skipUntil(int what) throws IOException {
		int c = 0;
		while (true) {
			c = is.read();
			if (c == IAC) {
				c = is.read();
				if (c != IAC) {
					is.unread(c);
					break;
				}
			}
		}
		expect(what);
	}

	protected String readSubNegotiationReply(int what) throws IOException {
		expect(IAC);
		expect(SB);
		expect(what);
		expect(0);
		StringBuffer sb = new StringBuffer();
		int c;
		do {
			c = is.read();
			if (c != IAC) {
				sb.append((char) c);
			}
		} while (c != IAC);
		expect(SE);
		return sb.toString();
	}

	protected void negotiateStuff() throws IOException {
		command(DONT, ECHO);
		System.err.println("> DONT ECHO");
		command(WILL, ECHO);
		System.err.println("> WILL ECHO");
		expectReply(DO, ECHO);
		System.err.println("< DO ECHO");
		command(DO, LINEMODE);
		System.err.println("> DO LINEMODE");
		expectReply(DO, SUPPRESS_GOAHEAD);
		System.err.println("< DO SUPRESS_GOAHEAD");
		expectReply(WILL, LINEMODE);
		System.err.println("< WILL LINEMODE");
		expectReply(SB, LINEMODE);
		System.err.println("< SB LINEMODE");
		skipUntil(SE);
		System.err.println("< SE");
		command(DO, BINARY);
		System.err.println("> DO BINARY");
		expectReply(WILL, BINARY);
		System.err.println("< WILL BINARY");
		command(WILL, BINARY);
		System.err.println("> WILL BINARY");
		expectReply(DO, BINARY);
		System.err.println("< DO BINARY");
		command(DO, TERMINAL_TYPE);
		if (willDo(TERMINAL_TYPE)) {
			command(SB, TERMINAL_TYPE, 1);
			command(SE);
			String termtype = readSubNegotiationReply(TERMINAL_TYPE);
			System.out.println("Remote terminal is " + termtype);
		}
	}

	public void run() {
		try {
			is = new PushbackInputStream(socket.getInputStream());
			os = socket.getOutputStream();
			osw = new OutputStreamWriter(os);
			if (is.available() > 0) {
				osw.write("HTTP/1.1 200 OK\n");
				osw.write("Date: " + new Date() + "\n");
				osw.write("Content-Length: -1\n");
				osw.write("Connection: close\n");
				osw.write("Content-Type: text/html;\n");
				osw.write("\n");
				osw.write("\n");
				osw.write("<html><head></head><body>Hello!</body></html>\n");
				osw.flush();
			}
			else {
				negotiateStuff();

				/*
				 * ANSIContext context = new ANSIContext(os, is); Screen screen =
				 * new Screen(context); screen.init(); Label text = new
				 * Label("Swift System Monitor"); text.setBgColor(ANSI.BLUE);
				 * text.setFgColor(ANSI.WHITE); text.setLocation(0, 0);
				 * text.setJustification(Label.CENTER);
				 * text.setSize(screen.getWidth(), 1);
				 * 
				 * CharacterMap map = new CharacterMap(); map.setLocation(10,
				 * 10);
				 * 
				 * screen.add(text); screen.add(map); screen.redraw();
				 * context.run();
				 */
				super.run();
			}
			socket.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
