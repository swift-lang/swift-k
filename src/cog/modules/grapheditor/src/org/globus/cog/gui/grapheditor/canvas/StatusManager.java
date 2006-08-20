
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Jan 23, 2004
 */
package org.globus.cog.gui.grapheditor.canvas;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.globus.cog.util.ImageLoader;

/**
 * A status manager is used to display the status of various operations
 * that are being performed. The manager is stack based. The information on the
 * top of the stack will be the one displayed. An optional icon can be used as
 * a visual representation of the operation that is taking place. A status manager
 * can also be used to display a progress indicator.
 */
public interface StatusManager {
	public static ImageIcon BUSY_ICON = ImageLoader.loadIcon("images/16x16/co/status-busy.png");
	
	/**
	 * Sets the text displayed when there's nothing else to display
	 * This may or may not make sense, depending on the target
	 */
	public void setDefaultText(String text);
	
	/**
	 * Pushes a message on the top of the stack
	 */
	public void push(String msg);
	
	/**
	 * Pushes a message on the top of the stack, together with an icon
	 */
	public void push(String msg, Icon icon);

	/**
	 * Pops a status message from the stack
	 */
	public void pop();
	
	/**
	 * Initializes (starts displaying) a progress indicator, for which
	 * the maximum value is <i>size</i>
	 */
	public void initializeProgress(int size);
		
	/**
	 * Sets the value of the progress indicator. The value is relative to 
	 * the one specified initially.
	 */
	public void setProgress(int size);

	/**
	 * Increases the value that the progress indicator represents
	 */
	public void stepProgress();

	/**
	 * Removes the progress indicator. In other words, it signifies to
	 * the manager that the operation for which the progress indicator was
	 * used has completed.
	 */
	public void removeProgress();
	
	public void error(String message, Exception details);
	
	public void warning(String message, Exception details);
	
	public void info(String message);
	
	public void debug(String message);
	
	public void out(String message);
	
	public void err(String message);
}
