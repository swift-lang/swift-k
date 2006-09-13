import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JFrame;

import org.globus.cog.gui.grapheditor.RendererFactory;
import org.globus.cog.gui.grapheditor.RootContainer;
import org.globus.cog.gui.grapheditor.canvas.AbstractCanvas;
import org.globus.cog.gui.grapheditor.canvas.GraphCanvas;
import org.globus.cog.gui.grapheditor.generic.RootCanvas;
import org.globus.cog.gui.grapheditor.generic.RootNode;
import org.globus.cog.gui.grapheditor.nodes.NodeComponent;
import org.globus.cog.gui.grapheditor.targets.swing.CanvasFrame;
import org.globus.cog.gui.grapheditor.targets.swing.CanvasPanel;
import org.globus.cog.gui.grapheditor.targets.swing.GraphFrame;
import org.globus.cog.gui.grapheditor.targets.swing.SwingCanvasRenderer;
import org.globus.cog.gui.grapheditor.targets.swing.SwingRootCanvasRenderer;

public class swingTest {

    public static void main(String[] args){
        try{
            //GraphViewer.addNode(GraphViewer.getGraph(), "check");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        /*NodeComponent p = new RootNode();
        GraphCanvas c = p.getCanvas();
        if (c == null) {
            c = p.createCanvas();
        }
        RendererFactory.setCurrentTarget("swing");
        RendererFactory.addClassRenderer(AbstractCanvas.class, "swing", SwingCanvasRenderer.class);
        RendererFactory.addClassRenderer(RootCanvas.class, "swing", SwingRootCanvasRenderer.class);
        SwingCanvasRenderer canvasRenderer = (SwingCanvasRenderer) c.newRenderer("swing");
        canvasRenderer.initialize();
        CanvasPanel cPane = canvasRenderer.getPanel();
        CanvasFrame canvasFrame = new CanvasFrame();
        JFrame frame1 = new JFrame() ;
        //canvasRenderer.setRootContainer((RootContainer) frame1);
        frame1.getContentPane().add(canvasRenderer.getComponent());
        frame1.show();
        
        */
        
    }
}
