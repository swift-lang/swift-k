package com.thoughtworks.xstream.ext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.thoughtworks.xstream.converters.ErrorWriter;
import com.thoughtworks.xstream.core.util.FastStack;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.StreamException;

public class MXppReader implements HierarchicalStreamReader {

	private final XmlPullParser parser;
	private final FastStack elementStack = new FastStack(16);
	private final IntQueue lookaheadQueue = new IntQueue(8);
	private Reader reader;

	private boolean hasMoreChildrenCached;
	private boolean hasMoreChildrenResult;

	public MXppReader(Reader reader) {
		try {
			this.reader = reader;
			parser = createParser();
			parser.setInput(new BufferedReader(reader));
			moveDown();
		}
		catch (XmlPullParserException e) {
			throw new StreamException(e);
		}
	}

	protected final XmlPullParser createParser() {
		// WARNING, read comment in getValue() before switching
		// to a different parser.
		return new MXParser();
	}

	public boolean hasMoreChildren() {
		if (hasMoreChildrenCached) {
			return hasMoreChildrenResult;
		}
		lookaheadMark();
		while (true) {
			switch (lookahead()) {
				case XmlPullParser.START_TAG:
					hasMoreChildrenCached = true;
					hasMoreChildrenResult = true;
					lookaheadReset();
					return true;
				case XmlPullParser.END_TAG:
				case XmlPullParser.END_DOCUMENT:
					hasMoreChildrenCached = true;
					hasMoreChildrenResult = false;
					lookaheadReset();
					return false;
				default:
					continue;
			}
		}
	}
	
	private void lookaheadMark() {
		lookaheadQueue.mark();
	}
	
	private void lookaheadReset() {
		lookaheadQueue.reset();
	}

	private int lookahead() {
		try {
			if (lookaheadQueue.isEmpty()) {
				lookaheadQueue.write(parser.next());
			}
			return lookaheadQueue.read();
		}
		catch (XmlPullParserException e) {
			throw new StreamException(e);
		}
		catch (IOException e) {
			throw new StreamException(e);
		}
	}

	private int next() {
		if (!lookaheadQueue.isEmpty()) {
			return lookaheadQueue.read();
		}
		else {
			try {
				return parser.next();
			}
			catch (XmlPullParserException e) {
				throw new StreamException(e);
			}
			catch (IOException e) {
				throw new StreamException(e);
			}
		}
	}

	public void moveDown() {
		hasMoreChildrenCached = false;
		final int currentDepth = elementStack.size();
		while (elementStack.size() <= currentDepth) {
			read();
			if (elementStack.size() < currentDepth) {
				throw new RuntimeException(
						"Attempted to read unexisting child element"); // sanity
																			 // check
			}
		}
	}

	public void moveUp() {
		hasMoreChildrenCached = false;
		final int currentDepth = elementStack.size();
		while (elementStack.size() >= currentDepth) {
			read();
		}
	}

	public String getNodeName() {
		return (String) elementStack.peek();
	}

	public boolean hasValue() {
		return lookahead() == XmlPullParser.TEXT;
	}

	public String getValue() {
		// MXP1 (pull parser) collapses all text into a single
		// text event. This allows us to only need to lookahead
		// one step. However if using a different pull parser
		// impl, you may need to look ahead further.
		lookaheadMark();
		if (lookahead() == XmlPullParser.TEXT) {
			final String text = parser.getText();
			lookaheadReset();
			return text == null ? "" : text;
		}
		else {
			lookaheadReset();
			return "";
		}
	}

	public String getAttribute(final String name) {
		return parser.getAttributeValue(null, name);
	}

	public Object peekUnderlyingNode() {
		throw new UnsupportedOperationException();
	}

	private void read() {
		switch (next()) {
			case XmlPullParser.START_TAG:
				elementStack.push(parser.getName());
				break;
			case XmlPullParser.END_TAG:
			case XmlPullParser.END_DOCUMENT:
				elementStack.pop();
				break;
		}
	}

	public void appendErrors(final ErrorWriter errorWriter) {
		errorWriter.add("line number", String.valueOf(parser.getLineNumber()));
	}

	public String getAttributeName(final int i) {
		return parser.getAttributeName(i);
	}

	public String getAttributeValue(final int i) {
		return parser.getAttributeValue(i);
	}

	public int getAttributeCount() {
		return parser.getAttributeCount();
	}

	public int getLineNumber() {
		return parser.getLineNumber();
	}

	public void close() {
		try {
			reader.close();
		}
		catch (IOException e) {
			throw new StreamException(e);
		}
	}

	public HierarchicalStreamReader underlyingReader() {
		return null;
	}
}