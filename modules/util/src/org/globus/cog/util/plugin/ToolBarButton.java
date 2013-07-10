/*
 * 
 */
package org.globus.cog.util.plugin;

import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JButton;

import org.globus.cog.util.ImageLoader;

/**
 * http://www.apl.jhu.edu/~hall/java/Swing-Tutorial/Swing-Tutorial-JToolBar.html
 */
public class ToolBarButton extends JButton {
    private static final Insets margins =
      new Insets(0, 0, 0, 0);

    public ToolBarButton(Icon icon) {
      super(icon);
      setMargin(margins);
      setVerticalTextPosition(BOTTOM);
      setHorizontalTextPosition(CENTER);
    }

    public ToolBarButton(String imageFile) {
      this(imageFile,null);
    }

    public ToolBarButton(String imageFile, String text) {
      this(new ImageLoader().loadImage(imageFile));      
      setText(text);
    }
  }
