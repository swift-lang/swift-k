
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.globus.cog.gui.grapheditor.GraphComponent;
import org.globus.cog.gui.grapheditor.edges.EdgeComponent;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.properties.OverlayedProperty;
import org.globus.cog.gui.grapheditor.properties.Property;
import org.globus.cog.util.Base64;
import org.globus.cog.util.graph.Edge;
import org.globus.cog.util.graph.GraphInterface;
import org.globus.cog.util.graph.Node;


/**
 * Saves a hypergraph into an XML file
 */
public class GraphToXML {

    /**
     *
     * @param c The topmost component of the hypergraph
     * @param writer The writer for the XML to go to
     * @param indentation the level of indentation (generates easy to read XML files... if there is such a thing)
     * @param hidden indicates whether hidden properties should be saved
     * @throws IOException
     */
    public static void write(GraphComponent c, Writer writer, int indentation, boolean hidden) throws IOException {
        NodeComponent node = null;
        String tagName = c.getComponentType();

        writer.write(repeat('\t', indentation));
        writer.write("<" + tagName);
        boolean validcanvas = false;
        if (c instanceof NodeComponent) {
            //hope shortcut evaluation works
            node = (NodeComponent) c;
            validcanvas = (node.getCanvas() != null) && (node.getCanvas().getGraph().nodeCount() != 0);
        }


        String text = writeProperties(c, writer, hidden);
        if ((!validcanvas) && (text == null)) {
            writer.write("/>\n");
        }
        else {
            writer.write(">\n");
            if (validcanvas) {
                GraphInterface g = node.getCanvas().getGraph();
                if (g != null) {
                	int crtId = 0;
                	LinkedList ids = new LinkedList();
                    Iterator j = g.getNodesIterator();
                    while (j.hasNext()){
                    	Node n = (Node) j.next();
                    	NodeComponent nc = (NodeComponent) n.getContents();
						Object value = nc.getPropertyValue("nodeid");
						if (value != null) {
							ids.add(value.toString());
						}
                    }
                    j = g.getNodesIterator();
                    while (j.hasNext()) {
                        Node n = (Node) j.next();
                        NodeComponent nc = (NodeComponent) n.getContents();
						Object value = nc.getPropertyValue("nodeid");
                        if (value == null){
                        	while (ids.contains(String.valueOf(crtId))){
                        		crtId++;
                        	}
                        	nc.setPropertyValue("nodeid", String.valueOf(crtId));
                        	crtId++;
                        }
                        write((GraphComponent) n.getContents(), writer, indentation + 1, hidden);
                    }
                    if (hidden) {
                        //write the edges
                        Iterator k = g.getEdgesIterator();
                        while (k.hasNext()) {
                            Edge e = (Edge) k.next();
                            EdgeComponent ec = (EdgeComponent) e.getContents();
                            Node from = e.getFromNode();
                            Node to = e.getToNode();
                          	NodeComponent fromc = (NodeComponent) from.getContents();
                          	NodeComponent toc = (NodeComponent) to.getContents();
                            ec.getProperty("from").setValue(fromc.getPropertyValue("nodeid"));
                            ec.getProperty("to").setValue(toc.getPropertyValue("nodeid"));
                            write(ec, writer, indentation + 1, hidden);
                        }
                    }
                }
            }
            if (text != null) {
                writer.write(text);
            }
            writer.write(repeat('\t', indentation) + "</" + tagName + ">\n");
        }
    }

    public static String writeProperties(GraphComponent c, Writer writer, boolean hidden) throws IOException {
    	GraphComponent copy = c.newInstance();
        Collection properties = new LinkedList(c.getProperties());        
        Iterator i = properties.iterator();
        String text = null;
        while (i.hasNext()) {
            Property p = (Property) i.next();
            if (p.getName().equals("_text_")) {
                text = (String) p.getValue();
                continue;
            }
            if (!hidden && p.hasAccess(Property.HIDDEN)) {
                continue;
            }
            if (p.getValue() == null) {
                continue;
            }
            if (p.hasAccess(Property.X)) {
                continue;
            }
            if (p.hasAccess(Property.NONPERSISTENT)) {
                continue;
            }
            Object value = p.getValue();
            if (value != null) {
            	if (value.equals(copy.getPropertyValue(p.getName()))) {
            		//it's the default value, so no need to write it
            		continue;
            	}
            }
			try {
				if ((p instanceof OverlayedProperty) && (p.hasAccess(Property.HIDDEN))) {
					writer.write(" _" + p.getName() + "=\"" + XMLEntities.encodeString(serialize(p.getValue())) + "\"");
				}
				else {
					writer.write(" " + p.getName() + "=\"" + XMLEntities.encodeString(serialize(p.getValue())) + "\"");
				}
			}
			catch (Exception e) {
				throw new RuntimeException("Cannot serialize property '" + p.getName() + "': " + e.getMessage());
			}
        }
        return text;
    }

    public static String repeat(char c, int count) {
        StringBuffer r = new StringBuffer();
        for (; count > 0; count--) {
            r.append(c);
        }
        return r.toString();
    }

    /**
     * Serializes an arbitrary object to a Base 64 string.
     * @param o
     * @return
     * @throws Exception
     */
    public static String serialize(Object o) throws Exception {
		Class oc = o.getClass();
		if (oc.equals(String.class)) {
			return (String) o;
		}
		else if (oc.equals(Integer.class)) {
			return "Integer: " + o.toString();
		}
		else if (oc.equals(Double.class)) {
			return "Double: " + o.toString();
		}
		else if (oc.equals(Float.class)) {
			return "Float: " + o.toString();
		}
		else if (oc.equals(Boolean.class)) {
			return "Boolean: " + o.toString();
		}
		else if (oc.equals(Point.class)) {
			Point p = (Point) o;
			return "Point: " + p.x + ", " + p.y;
		}
		else if (oc.equals(Dimension.class)) {
			Dimension d = (Dimension) o;
			return "Dimension: " + d.width + ", " + d.height;
		}
		else if (oc.equals(Color.class)) {
			Color c = (Color) o;
			return "Color: rgb("+c.getRed()+", "+c.getGreen()+", "+c.getBlue()+")";
		}
		else {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(o);
			oos.close();
			return "Base64: " + new String(Base64.encode(os.toByteArray()), "US-ASCII");
		}
    }
}
