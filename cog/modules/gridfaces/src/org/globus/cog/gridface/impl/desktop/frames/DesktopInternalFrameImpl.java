//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.impl.desktop.frames;
/*
 * Desktop internal frame.
 */

//Local imports
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.lang.reflect.Constructor;
import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JTextField;

import org.globus.cog.gridface.impl.desktop.interfaces.DesktopInternalFrame;
import org.globus.cog.gridface.impl.desktop.util.DesktopUtilities;
import org.globus.cog.gridface.impl.desktop.util.ObjectPair;

public class DesktopInternalFrameImpl extends JInternalFrame implements DesktopInternalFrame {

	//Before frame close confirm to save changes
	public boolean saveChanges = true;

	/**
	 * Creates a non-resizable, non-closable, non-maximizable,
	 * non-iconifiable <code>DesktopInternalFrame</code> with no title.
	 */
	public DesktopInternalFrameImpl() throws Exception{
		this(null, null, "", false, false, false, false,false);
	}

	/**
	 * Creates a non-resizable, non-closable, non-maximizable,
	 * non-iconifiable <code>DesktopInternalFrame</code> with the specified title.
	 * Note that passing in a <code>null</code> <code>title</code> results in
	 * unspecified behavior and possibly an exception.
	 *
	 * @param title  the non-<code>null</code> <code>String</code>
	 *     to display in the title bar
	 */
	public DesktopInternalFrameImpl(String title) throws Exception{
		this(null, null, title, false, false, false, false,false);
	}

	/**
	 * Creates a non-closable, non-maximizable, non-iconifiable
	 * <code>DesktopInternalFrame</code> with the specified title
	 * and resizability.
	 *
	 * @param title      the <code>String</code> to display in the title bar
	 * @param resizable  if <code>true</code>, the internal frame can be resized
	 */
	public DesktopInternalFrameImpl(String title, boolean resizable) throws Exception {
		this(null, null, title, resizable, false, false, false,false);
	}

	/**
	 * Creates a non-maximizable, non-iconifiable <code>DesktopInternalFrame</code>
	 * with the specified title, resizability, and
	 * closability.
	 *
	 * @param title      the <code>String</code> to display in the title bar
	 * @param resizable  if <code>true</code>, the internal frame can be resized
	 * @param closable   if <code>true</code>, the internal frame can be closed
	 */
	public DesktopInternalFrameImpl(String title, boolean resizable, boolean closable) throws Exception{
		this(null, null, title, resizable, closable, false, false,false);
	}

	/**
	 * Creates a non-iconifiable <code>DesktopInternalFrame</code>
	 * with the specified title,
	 * resizability, closability, and maximizability.
	 *
	 * @param title       the <code>String</code> to display in the title bar
	 * @param resizable   if <code>true</code>, the internal frame can be resized
	 * @param closable    if <code>true</code>, the internal frame can be closed
	     * @param maximizable if <code>true</code>, the internal frame can be maximized
	 */
	public DesktopInternalFrameImpl(
		String title,
		boolean resizable,
		boolean closable,
		boolean maximizable) throws Exception{
		this(null, null, title, resizable, closable, maximizable, false,false);
	}

	/**
	 * Creates a <code>DesktopInternalFrame</code> with the specified 
	 * Application container, Hashtable of Objects and their defining
	 * classes accepted by the container constructors, title,
	 * resizability, closability, maximizability, and iconifiability.
	 * All <code>DesktopInternalFrame</code> constructors use this one.
	 *
	 * @param title       the <code>String</code> to display in the title bar
	 * @param resizable   if <code>true</code>, the internal frame can be resized
	 * @param closable    if <code>true</code>, the internal frame can be closed
	     * @param maximizable if <code>true</code>, the internal frame can be maximized
	     * @param iconifiable if <code>true</code>, the internal frame can be iconified
	 */
	public DesktopInternalFrameImpl(
		String appClassName,
		ObjectPair arguments,
		String title,
		boolean resizable,
		boolean closable,
		boolean maximizable,
		boolean iconifiable,
		boolean saveChanges)
		throws Exception {
			
			super(title, resizable, closable, maximizable, iconifiable);
		if (appClassName != null) {
			Class[] argClassArray = null;
			Component appComponent = null;
			Class appClass = Class.forName(appClassName);
			
			if (arguments != null) {
				argClassArray = new Class[arguments.size()];
				Object[] argObjects = new Object[arguments.size()];

				int tmpIndex = 0;
				for (Enumeration e = arguments.keys(); e.hasMoreElements();) {
					//argClass[tmpIndex] = (Class) e.nextElement();
					argClassArray[tmpIndex] = Class.forName((String)e.nextElement());
					
					tmpIndex++;
				}
				int tmpIndex2 = 0;
				for (Enumeration e = arguments.elements();
					e.hasMoreElements();
					) {
					argObjects[tmpIndex2] = e.nextElement();
					tmpIndex2++;
				}
				
				Constructor argsConstructor = appClass.getConstructor(argClassArray);
				
				Object obj = DesktopUtilities.createObject(
						argsConstructor,
						argObjects);
				if(obj instanceof Component){
					appComponent = (Component)obj;
				}else{
					appComponent = new JTextField("Class is not an instance of java.awt.Component");
				}

			} else {
				try {
					//appComponent = (Component) appClass.newInstance();
					Object obj = appClass.newInstance();
					if(obj instanceof Component){
						appComponent = (Component)obj;
					}else{
						appComponent = new JTextField("Class is not an instance of java.awt.Component");
					}
					
					
				} catch (IllegalAccessException exp) {
					exp.printStackTrace();
				} catch (InstantiationException exp) {
					exp.printStackTrace();
				}
			}

			this.getContentPane().add(appComponent);
			this.pack();
			
		} else {
			if (arguments != null) {
				throw new Exception("in Frame: Cannot pass constructor arguments without application class!");
			}
			this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		}
		this.saveChanges = saveChanges;
	}
	public DesktopInternalFrameImpl(
			Component childComponent,
			String title,
			int width,
			int height,
			boolean resizable,
			boolean closable,
			boolean maximizable,
			boolean iconifiable,
			boolean saveChanges){
				super(title, resizable, closable, maximizable, iconifiable);
				if(childComponent!=null){
					this.getContentPane().add(childComponent);
					if(width != -1 && height != -1){
						this.setPreferredSize(new Dimension(width,height));
					}
				}
				
				this.pack();
				this.saveChanges = saveChanges;
			}
	public DesktopInternalFrameImpl(
		Component childComponent,
		String title,
		boolean resizable,
		boolean closable,
		boolean maximizable,
		boolean iconifiable,
		boolean saveChanges){
		this(childComponent,title,-1,-1,
				resizable,closable,maximizable,iconifiable,saveChanges);
			
		}
	/**
	 * @return
	 */
	public boolean isSaveChanges() {
		return this.saveChanges;
	}

	/**
	 * @param b
	 */
	public void setSaveChanges(boolean saveChange) {
		this.saveChanges = saveChange;
	}
	
	public void setIconImage(Image image) {
	    this.setFrameIcon(new ImageIcon(image));
	}

}
