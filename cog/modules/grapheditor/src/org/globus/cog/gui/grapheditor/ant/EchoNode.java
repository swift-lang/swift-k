
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant;

import org.globus.cog.gui.grapheditor.properties.ComponentProperty;
import org.globus.cog.gui.grapheditor.properties.Property;

/**
 * An echo node. It captures the <echo>...</echo> text
 */
public class EchoNode extends TaskNode{
    private String text;
    private String message;

    public EchoNode(){
        super();
        text = null;
        setComponentType("echo");
        loadIcon("images/ant-echo.png");
        setCanvasType(EchoCanvas.class);
        //add the _text_ property to receive the text inside the tags
        addProperty(new ComponentProperty(this, "_text_", Property.HIDDEN));
        //also support for the <echo message="..."/> form
        addProperty(new ComponentProperty(this, "message"));
    }

    public void set_text_(String text){
        this.text = text;
    }

    public String get_text_(){
        return text;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
	
	public boolean supportsType(String type) {
		return type.equals("echo");
	}
}
