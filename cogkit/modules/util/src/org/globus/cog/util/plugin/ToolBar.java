/*
 *
 */
package org.globus.cog.util.plugin;

import java.awt.Component;
import java.awt.Insets;

import javax.swing.JToolBar;

/**
 * http://www.apl.jhu.edu/~hall/java/Swing-Tutorial/Swing-Tutorial-JToolBar.html
 */
public class ToolBar extends JToolBar {
  private ToolBarButton[] buttons;
  private static final Insets margins = new Insets(0, 0, 0, 0);
  
  public ToolBar() {}
  
  public ToolBar(String[] imageResources,String[] labels) {
      buttons = new ToolBarButton[imageResources.length];
      for (int i = 0; i < imageResources.length; i++) {
            ToolBarButton button = new ToolBarButton(imageResources[i]);
            buttons[i] = button;
            button.setToolTipText(labels[i]);
            button.setMargin(margins);
            add(button);
    }
  }
  
  public ToolBarButton getButtonAt(int index) {
      return buttons[index];
  }
  public int buttonCount() {
      return buttons.length;
  }

  public void setTextLabels(boolean labelsAreEnabled) {
    Component c;
    int i = 0;
    while((c = getComponentAtIndex(i++)) != null) {
      ToolBarButton button = (ToolBarButton)c;
      if (labelsAreEnabled)
        button.setText(button.getToolTipText());
      else
        button.setText(null);
    }
  }
}