
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.ant;


import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.targets.swing.views.SwingView;

public class TextEditorView extends SwingView implements CaretListener{
    JTextArea textArea;
    JScrollPane jsp;

    public TextEditorView(){
        textArea = new JTextArea();
        textArea.addCaretListener(this);
        jsp = new JScrollPane(textArea);
        setComponent(jsp);
        setName("Text editor");
    }

    public void invalidate() {
        try{
            NodeComponent node = getCanvas().getOwner();
            textArea.setText((String) node.getPropertyValue("_text_"));
        }
        catch (Exception e){
        }
    }

    public void caretUpdate(CaretEvent e) {
        NodeComponent node = getCanvas().getOwner();
        node.setPropertyValue("_text_", ((JTextArea)e.getSource()).getText());
    }
}
