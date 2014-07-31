/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.about;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.globus.cog.gui.util.UITools;
import org.globus.cog.util.ImageLoader;
import org.globus.cog.util.TextFileLoader;

public class CoGAbout extends JDialog 
    implements ActionListener, Serializable{

    JButton close;
    String text;
    ImageIcon image;
    JTextPane tp;
    JLabel lImage;
    JScrollPane jsp;
    
    public CoGAbout(){
	super();
	setupComponents();
    }
    
    public CoGAbout(Frame parent, boolean modal){
	super(parent, modal);
	setupComponents();
	text = null;
	image = null;
    }
    
    private void setupComponents(){
	JPanel main = new JPanel(new BorderLayout());
	JPanel left = new JPanel();
	left.setBorder(BorderFactory.createEtchedBorder());
	lImage = new JLabel();
	left.add(lImage);
	main.add(left, BorderLayout.WEST);
	JPanel right = new JPanel(new BorderLayout());
	main.add(right, BorderLayout.CENTER);
	tp = new AATextPane();
	tp.setEditable(false);
	jsp = new JScrollPane(tp);
	right.add(jsp, BorderLayout.CENTER);
	right.setBorder(BorderFactory.createEtchedBorder());
	JPanel sp = new JPanel(new FlowLayout(FlowLayout.CENTER));
	sp.setBorder(BorderFactory.createEtchedBorder());
	close = new JButton("Close");
	close.addActionListener(this);
	sp.add(close);
	main.add(sp, BorderLayout.SOUTH);
	getContentPane().add(main);
    }
	
    public void setText(String text){
	this.text = text;
    }
    
    public void setTextResource(String resName){
	TextFileLoader tl = new TextFileLoader();
	text = tl.loadFromResource(resName);
    }
    
    public void setImage(ImageIcon image){
	this.image = image;
    }
    
    public void setImageResource(String resName){
	ImageLoader il = new ImageLoader();
	image = il.loadImage(resName);
    }
    
    public void show(){
	if (image == null){
	    setImageResource("images/logos/about.png");
	}
	if (text == null){
	    setTextResource("text/license.html");
	}
	lImage.setIcon(image);
	tp.setContentType("text/html");
	tp.setText(text);
	//Scrolls to top of text
	tp.setCaretPosition(0);
	int ih = image.getIconHeight();
	jsp.setPreferredSize(new Dimension(ih,ih));
	pack();
	UITools.center(getParent(), this);
	super.show();
	
    }
    
    public void actionPerformed(ActionEvent e){
	if (e.getSource() == close){
	    hide();
	    dispose();
	}
    }
    
	
    
    public static void main(String[] args){
	CoGAbout ca = new CoGAbout(null, true);
	ca.show();
	System.exit(0);
    }
	
    public class AATextPane extends JTextPane{
	public void paint(Graphics g){
	    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					      RenderingHints.VALUE_ANTIALIAS_ON);
			super.paint(g);
		}
	}
}

