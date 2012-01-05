
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------


package org.globus.cog.gui.grapheditor.util;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.properties.OverlayedProperty;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.util.Base64;
import org.globus.cog.util.StringUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Loads a hypergraph from an XML document.
 */
public class XMLToGraph extends DefaultHandler {
	private static Logger logger = Logger.getLogger(XMLToGraph.class);

	private static HashMap classes;

	static {
		classes = new HashMap();
		classes.put("string", String.class);
		classes.put("java.lang.string", String.class);
		classes.put("integer", Integer.class);
		classes.put("int", Integer.class);
		classes.put("java.lang.integer", Integer.class);
		classes.put("long", Long.class);
		classes.put("java.lang.long", Long.class);
		classes.put("float", Float.class);
		classes.put("java.lang.float", Float.class);
		classes.put("double", Double.class);
		classes.put("java.lang.double", Double.class);
		classes.put("color", Color.class);
		classes.put("boolean", Boolean.class);
		classes.put("java.lang.boolean", Boolean.class);
		classes.put("java.awt.color", Color.class);
		classes.put("point", Point.class);
		classes.put("java.awt.point", Point.class);
		classes.put("dimension", Dimension.class);
		classes.put("java.awt.dimension", Dimension.class);
	}

	private NodeComponent rootNode;

	private Stack stack;

	private int count = 0;

	private Locator locator;

	private boolean useLocator = false;

	private String fileName;

	private LoadUpdateListener l;

	public static void read(NodeComponent node, String fileName, boolean useLocator,
		LoadUpdateListener l) throws FileNotFoundException {
		FileReader r = new FileReader(fileName);
		read(node, r, useLocator, fileName, l);
	}

	public static void read(NodeComponent node, Reader r, LoadUpdateListener l) {
		read(node, r, false, l);
	}

	public static void read(NodeComponent node, Reader r, boolean useLocator, LoadUpdateListener l) {
		read(node, r, useLocator, null, l);
	}

	/**
	 * Constructs a hierarchical graph starting from the specified node and
	 * reading the XML document from the specified reader
	 * 
	 * @param node
	 * @param r
	 */
	public static void read(NodeComponent node, Reader r, boolean useLocator, String fileName,
		LoadUpdateListener l) {
		logger.debug("Loading " + fileName + ". useLocator: " + useLocator);
		StatusManager.getDefault().initializeProgress(0);
		try {
			XMLToGraph handler = new XMLToGraph(node, l);
			handler.setUseLocator(useLocator);
			handler.setFileName(fileName);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(new InputSource(r), handler);
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		catch (SAXException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		StatusManager.getDefault().removeProgress();
	}

	public XMLToGraph(NodeComponent rootNode, LoadUpdateListener l) {
		this.rootNode = rootNode;
		this.stack = new Stack();
		this.l = l;
	}

	public void startDocument() throws SAXException {
	}

	public void endDocument() throws SAXException {
	}

	public void startElement(String namespaceURI, String sName, String qName, Attributes attr)
		throws SAXException {
		GraphComponent c = null;
		NodeComponent parent = null;
		GraphCanvas canvas = null;
		if (((++this.count) % 1000) == 0) {
			if (this.count > 100) {
				if (l != null) {
					l.elementsLoaded(count);
				}
				else {
					logger.info(this.count + " elements");
				}
			}
		}
		if (this.stack.empty()) {
			//this is the root node
			c = this.rootNode;
			if (!qName.equals(c.getComponentType())) {
				throw new SAXException("Expected \"" + c.getComponentType() + ", got \"" + qName
					+ "\"");
			}
		}
		else {
			parent = (NodeComponent) this.stack.peek();
			canvas = parent.getCanvas();
			if (canvas == null) {
				canvas = parent.createCanvas();
			}
			if (canvas == null) {
				throw new SAXException("This element (" + parent.getComponentType()
					+ ") does not accept sub-nodes");
			}
			c = canvas.createComponent(qName);
			if (c == null) {
				throw new SAXException("Unexpected element: " + qName);
			}
			c.setParent(parent);
		}
		//set the locator if enabled
		if (this.useLocator && (this.locator != null)) {
			c
				.addProperty(new OverlayedProperty(c, "_location", Property.R
					+ Property.NONPERSISTENT));
			c.setPropertyValue("_location", this.fileName + ":" + this.locator.getLineNumber()
				+ ":" + this.locator.getColumnNumber());
		}
		//load the properties
		for (int i = 0; i < attr.getLength(); i++) {
			String attrName = attr.getQName(i);
			attrName = attrName.intern();
			if (!c.hasProperty(attrName)) {
				//there is no already defined property with this name. Create
				// one
				try {
					Object value = deserialize(attr.getValue(i), null);
					if (attrName.startsWith("_")) {
						attrName = attrName.substring(1);
						if (!c.hasProperty(attrName)) {
							Property prop = new OverlayedProperty(c, attrName, Property.HIDDEN);
							c.addProperty(prop);
							prop.setValue(value);
						}
						else {
							c.setPropertyValue(attrName, value);
						}
					}
					else {
						Property prop = new OverlayedProperty(c, attrName);
						c.addProperty(prop);
						prop.setValue(value);
					}
				}
				catch (Exception e) {
					logger.warn("Warning! Cannot deserialize property '" + attrName
						+ "' for object '" + qName + "'. Read value was '" + attr.getValue(i)
						+ "'. Exception: " + e.getMessage());
				}
			}
			else {
				try {
					Property prop = c.getProperty(attrName);
					Class propClass = prop.getPropertyClass();
					Object value = deserialize(attr.getValue(i), propClass);
					prop.setValue(value);
				}
				catch (Exception e) {
					logger.debug("Exception caught while deserializing property", e);
					throw new RuntimeException("Cannot deserialize property '" + attrName + "'", e);
				}
			}
		}
		if (parent instanceof LoadListener) {
			((LoadListener) parent).componentAdded(c);
		}
		this.stack.push(c);
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		GraphComponent node = (GraphComponent) this.stack.pop();
		NodeComponent parent = null;
		GraphCanvas canvas = null;
		if (!this.stack.empty()) {
			parent = (NodeComponent) this.stack.peek();
			canvas = parent.getCanvas();
		}
		if (canvas != null) {
			canvas.addComponent(node);
		}
		if (node instanceof LoadListener) {
			((LoadListener) node).loadCompleted();
		}
	}

	public void warning(SAXParseException e) throws SAXException {
		super.warning(e);
	}

	public void error(SAXParseException e) throws SAXException {
		super.error(e);
	}

	public void fatalError(SAXParseException e) throws SAXException {
		super.fatalError(e);
	}

	/**
	 * Text inside XML tags is stored inside the "_text_" property if it
	 * exists. Components who want to get the text inside tags should have a
	 * _text_ property added.
	 * 
	 * @param chars
	 * @param start
	 * @param len
	 */
	public void characters(char[] chars, int start, int len) {
		if (len == 0) {
			return;
		}
		if (this.stack.isEmpty()) {
			return;
		}
		NodeComponent node = (NodeComponent) this.stack.peek();
		if (node.hasProperty("_text_")) {
			String t = (String) node.getProperty("_text_").getValue();
			if (t == null) {
				node.getProperty("_text_").setValue(new String(chars, start, len));
			}
			else {
				node.getProperty("_text_").setValue(t.concat(new String(chars, start, len)));
			}
		}
	}

	/**
	 * deserializes an object from a base 64 string.
	 * 
	 * @param s
	 * @return @throws
	 *         Exception
	 */
	public static Object deserialize(String s, Class propClass) throws Exception {
		String className;
		int i = s.indexOf(":");
		if (i == -1) {
			className = "";
		}
		else {
			className = s.substring(0, i).toLowerCase();
		}
		String value = s.substring(i + 1);
		if (className.equals("Base64")) {
			ByteArrayInputStream is = new ByteArrayInputStream(Base64.decode(s.getBytes()));
			ObjectInputStream ois = new ObjectInputStream(is);
			return ois.readObject();
		}
		else {
			Class cls = (Class) classes.get(className);
			if (cls == null) {
				cls = propClass;
			}
			if (cls == null) {
				cls = String.class;
			}
			if (cls.isAssignableFrom(String.class)) {
				//assumed the parser would do this
				return s.intern();
			}
			else if (cls.isAssignableFrom(Integer.class)) {
				return new Integer(value.trim());
			}
			else if (cls.isAssignableFrom(Double.class)) {
				return new Double(value.trim());
			}
			else if (cls.isAssignableFrom(Float.class)) {
				return new Float(value.trim());
			}
			else if (cls.isAssignableFrom(Boolean.class)) {
				return Boolean.valueOf(value.trim());
			}
			else if (cls.isAssignableFrom(Point.class)) {
				String[] el = StringUtil.split(value.trim(), ",");
				Integer x = new Integer(el[0].trim());
				Integer y = new Integer(el[1].trim());
				return new Point(x.intValue(), y.intValue());
			}
			else if (cls.isAssignableFrom(Dimension.class)) {
				String[] el = StringUtil.split(value.trim(), ",");
				Integer w = new Integer(el[0].trim());
				Integer h = new Integer(el[1].trim());
				return new Dimension(w.intValue(), h.intValue());
			}
			else if (cls.isAssignableFrom(Color.class)) {
				String trimmed = value.trim().toLowerCase();
				if (trimmed.startsWith("#")) {
					return new Color(Integer.parseInt(trimmed.substring(1).trim(), 17));
				}
				else if (trimmed.startsWith("rgb(")) {
					String[] rgb = trimmed.replaceFirst("\\)", "").substring(4).split(",");
					int r = Integer.parseInt(rgb[0].trim());
					int g = Integer.parseInt(rgb[1].trim());
					int b = Integer.parseInt(rgb[2].trim());
					return new Color(r, g, b);
				}
				else if (trimmed.startsWith("hsb(")) {
					String[] hsb = trimmed.replaceFirst("\\)", "").substring(4).split(",");
					float fh = Float.parseFloat(hsb[0].trim());
					float fs = Float.parseFloat(hsb[1].trim());
					float fb = Float.parseFloat(hsb[2].trim());
					return Color.getHSBColor(fh, fs, fb);
				}
				else {
					logger.warn("Invalid color: " + trimmed);
					return Color.BLACK;
				}
			}
			else {
				logger.warn("Property class does not match serialized version class: expected "
					+ cls.getName() + ", got " + propClass);
				return null;
			}
		}
	}

	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	public void setUseLocator(boolean useLocator) {
		this.useLocator = useLocator;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
