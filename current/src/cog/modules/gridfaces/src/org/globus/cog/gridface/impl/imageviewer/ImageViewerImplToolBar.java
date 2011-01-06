
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.gridface.impl.imageviewer;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;

import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.interfaces.Identity;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.interfaces.StatusListener;
import org.globus.cog.gridface.impl.GridFaceImpl;
import org.globus.cog.gridface.impl.URIInputPanelImpl;
import org.globus.cog.gridface.impl.desktop.interfaces.AccessClose;
import org.globus.cog.gridface.impl.directorybrowser.FileTransferObjectImpl;
import org.globus.cog.gridface.impl.gcm.GridCommandManagerImpl;
import org.globus.cog.gridface.interfaces.ConnectionObject;
import org.globus.cog.gridface.interfaces.FileTransferObject;
import org.globus.cog.gridface.interfaces.GridCommand;
import org.globus.cog.gridface.interfaces.GridCommandManager;
import org.globus.cog.gridface.interfaces.GridFace;
import org.globus.cog.gridface.interfaces.ImageViewerPanel;
import org.globus.cog.util.ImageLoader;

public class ImageViewerImplToolBar extends JPanel implements 
						       ImageViewerPanel, 
						       AccessClose, 
						       StatusListener {
  //The background color for image rotations.
  final private Color backgroundColor = Color.WHITE;
  
  private GridFace myGridFace;

  //GUI declarations
  private imagePane mainImagePane;
  private URIInputPanelImpl inputPanel;
  private Window parentWindow;
  
  private Image origImage;
  private BufferedImage buffImage;
  private Graphics2D buffImageGraphics;
  
  private listener imageListener;
  private Hashtable submittedCommands = new Hashtable();
  
  //Keeps track of how much we've rotated the image so we can just rotate the 
  //origIamge to keep quality up
  private double rotateTracker;
  
  private JScrollPane scrollPane;
  private URI imageURI;
  private File tempFile =  null;
  private FileTransferObject fileTrans;
  private ConnectionObject connectObject;

  private ImageLoader imageLoader;
  
  public ImageViewerImplToolBar() throws Exception {
  	this(new GridCommandManagerImpl());
  }

  /**
   * The constructor calls the parent's constuctor (since the class
   * extends JPanel).
   */
  public ImageViewerImplToolBar(GridCommandManager gcm) {
  	super(new GridBagLayout());
  	GridBagConstraints c = new GridBagConstraints();
    imageListener = new listener();
    
    imageLoader = new ImageLoader();
    
    // The top panel that holds the uri, the go button, and the
    // toolbar button topPanel = new JPanel(new GridBagLayout());
    
    c.anchor = GridBagConstraints.LINE_START;
    
    // create a new GridFace
    myGridFace = new GridFaceImpl();

    // Create the go button
    JButton goButton = new JButton("Go");
    goButton.addActionListener(imageListener);
    goButton.setToolTipText("Load Image");
    
    // Create and add the URIInputPanel at the top
    inputPanel = new URIInputPanelImpl();
    c.gridx = 0;
    c.gridy = 1;
    this.add(inputPanel, c);
    // Add the go button
    c.gridx = 1;
    c.weightx = 1;
    this.add(goButton, c);
    // create and add the button panel
    c.gridx=0;
    c.gridy=0;
    c.gridwidth=2;
    this.add(createButtonPanel(), c);
    
   
    
    // Create the component that displays the images, put it in a
    // scrollpane
    mainImagePane = new imagePane(buffImage);
    scrollPane = new JScrollPane(mainImagePane);
    
    // Make the scrollpane transparent
    scrollPane.getViewport().setOpaque(false);
    scrollPane.setOpaque(false);
    scrollPane.setPreferredSize(new Dimension(inputPanel.getPreferredSize().width,inputPanel.getPreferredSize().width));
    c.gridx=0;
    c.gridy=2;
    c.gridwidth=2;
    c.weightx=2;
    c.weighty=2;
    c.fill=GridBagConstraints.BOTH;
    this.add(scrollPane,c);
    
    fileTrans = new FileTransferObjectImpl(this, gcm);
    
    // initialize the rotateTracker
    rotateTracker = 0;
  }
  
  public ImageViewerImplToolBar(GridCommandManager gcm, URI imageURI){
  	this(gcm);
  	this.imageURI = imageURI;
  	this.inputPanel.set(imageURI);
  	this.goButtonPushed();
  }
  
  public ImageViewerImplToolBar(URI imageURI) throws Exception {
  	this(new GridCommandManagerImpl(), imageURI);
  }
  
  
  private JPanel createButtonPanel() {
  	JPanel buttonPanel = new JPanel();
  	
  	// These are loaded from the cog-resources Jar file: this
  	// should be converted to use the image loader 

	// gvl: we introduced at one pont a defualt icon size that
	// should be used to determine the name of the
	// directory. E.g. 32x32 should be assambled by "ICONSIZE =
	// "x" + ICONSIZE. THis way its easier to switch icon
	// sizes. for now its ok. However this need to cahnge also all
	// over the modules. This should be documented in the
	// programmers guide.

  	JButton saveButton = new JButton(imageLoader.loadImage("images/32x32/co/filesave.png"));
  	//Button saveAsButton = new JButton(imageLoader.loadImage("images/32x32/co/filesaveas.png"));
    JButton rotateButton = new JButton(imageLoader.loadImage("images/32x32/co/arrow-reload.png"));
    JButton flipHorizontalButton = new JButton(imageLoader.loadImage("images/32x32/co/arrow-forward.png"));
    JButton flipVerticalButton = new JButton(imageLoader.loadImage("images/32x32/co/arrow-down.png"));
    JButton changeSizeButton = new JButton(imageLoader.loadImage("images/32x32/co/window-new.png"));
    
    saveButton.setPressedIcon(imageLoader.loadImage("images/32x32/bw/filesave.png"));
    //saveAsButton.setPressedIcon(imageLoader.loadImage("images/32x32/bw/filesaveas.png"));
    rotateButton.setPressedIcon(imageLoader.loadImage("images/32x32/bw/arrow-reload.png"));
    flipHorizontalButton.setPressedIcon(imageLoader.loadImage("images/32x32/bw/arrow-forward.png"));
    flipVerticalButton.setPressedIcon(imageLoader.loadImage("images/32x32/bw/arrow-down.png"));
    changeSizeButton.setPressedIcon(imageLoader.loadImage("images/32x32/bw/window-new.png"));
  	
    saveButton.setBorderPainted(false);
    //saveAsButton.setBorderPainted(false);
    rotateButton.setBorderPainted(false);
    flipHorizontalButton.setBorderPainted(false);
    flipVerticalButton.setBorderPainted(false);
    changeSizeButton.setBorderPainted(false);
    
    saveButton.setMargin(new Insets(0,0,0,0));
    //saveAsButton.setMargin(new Insets(0,0,0,0));
    rotateButton.setMargin(new Insets(0,0,0,0));
    flipHorizontalButton.setMargin(new Insets(0,0,0,0));
    flipVerticalButton.setMargin(new Insets(0,0,0,0));
    changeSizeButton.setMargin(new Insets(0,0,0,0));
    
    saveButton.addActionListener(imageListener);
    //saveAsButton.addActionListener(imageListener);
    rotateButton.addActionListener(imageListener);
    flipHorizontalButton.addActionListener(imageListener);
    flipVerticalButton.addActionListener(imageListener);
    changeSizeButton.addActionListener(imageListener);
    
    saveButton.setToolTipText("Save");
   // saveAsButton.setToolTipText("Save As");
    rotateButton.setToolTipText("Rotate");
    flipHorizontalButton.setToolTipText("Flip Horizontally");
    flipVerticalButton.setToolTipText("Flip Vertically");
    changeSizeButton.setToolTipText("Change Size");
    
    buttonPanel.add(saveButton);
    //buttonPanel.add(saveAsButton);
    buttonPanel.add(rotateButton);
    buttonPanel.add(flipHorizontalButton);
    buttonPanel.add(flipVerticalButton);
    buttonPanel.add(changeSizeButton);
    
    return buttonPanel;
  }
  
  public void alert(String message) {
  	JOptionPane.showMessageDialog(this,message,message,JOptionPane.PLAIN_MESSAGE);
  }
  
  private void  goButtonPushed() {
  	startWaiting();
	// sets the internal uri of the input panel to what's in the
	// text field
	inputPanel.get(); 
	imageURI = inputPanel.getURI();
	String host = imageURI.getHost();
	
	
	// for something like the file implementations with no host
	if(host==null)
		host= "";
	
	String uriScheme = imageURI.getScheme();
	
	if(uriScheme != null && !(uriScheme.equals("gridftp")||uriScheme.equals("gsiftp")||uriScheme.equals("file"))){
		this.createUsernameDialog();
	}
			
//	try {
//		fileTrans.connect(imageURI.getScheme(), host, String.valueOf(imageURI.getPort()));
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
	
	

	String tempPath = imageURI.getPath();
  	String extension=null;
  	try {
  		extension = tempPath.substring(tempPath.lastIndexOf("."));
  		tempFile = File.createTempFile("tempImage", extension);
  	} catch (Exception e) {
  		this.popLoadError();
  		this.stopWaiting();
  		return;
  	}  	
  	this.stopWaiting();
  	load(tempPath);
  }
  
	/** Create the dialog to be popped up asking the user for
	 * their username and password if they are not connecting to a
	 * gridftp server.
	 *
	 */
	protected void createUsernameDialog() {
		final JDialog usernameDialog = new JDialog((JFrame) null, "Username & Password", true);
		//final JDialog usernameDialog = new JDialog();
		
		final JTextField usernameField = new JTextField(20);
		final JPasswordField passwordField = new JPasswordField(20);
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileTrans.setUsername(usernameField.getText());
				fileTrans.setPassword(new String(passwordField.getPassword()));
				usernameDialog.dispose();
			}
		});
		
		usernameDialog.getContentPane().setLayout(new GridLayout(3,2));
		usernameDialog.getContentPane().add(new JLabel("Username: "));
		usernameDialog.getContentPane().add(usernameField);
		usernameDialog.getContentPane().add(new JLabel("Password: "));
		usernameDialog.getContentPane().add(passwordField);
		usernameDialog.getContentPane().add(okButton);
		
		//SpringUtilities.makeCompactGrid(usernameDialog.getContentPane(),3, 2, 6, 6, 6, 6);

		usernameDialog.pack();
		usernameDialog.setVisible(true);
	}
  
  /**
   * Gets the angle of rotation desired and calls the <code>rotate(angle)</code>
   * method accordingly.
   */
  private void rotateButtonPushed() {
  	String angleString = (String) JOptionPane.showInputDialog(null,
			"Angle (in degrees):", "Rotate", JOptionPane.QUESTION_MESSAGE);
	int angle;
	try {
		angle = Integer.parseInt(angleString);
	}
	catch (NumberFormatException e) {
		// If they've given us a bad number do nothing:
		return;
	}
	rotate(angle);
  }
  
  /**
   * Gets the percentage by which to change the size, sets the
   * <code>codeSizeFactor</code> accordingly and calls the
   * <code>changeSize</code> method.
   */
  private void changeSizeButtonPushed() {
	String sizeString = (String) JOptionPane.showInputDialog(null,
			"Percentage by which to resize image:", "Change Size", JOptionPane.QUESTION_MESSAGE);
	double size;
	try {
		size = Double.parseDouble(sizeString);
	}
	catch (Exception e) {
		// If they've given us a bad number do nothing:
		return;
	}
	size = size / 100;
	changeSize(size);
  }

  private void startWaiting() {
  	setCursor(new Cursor(Cursor.WAIT_CURSOR));
  }
  
  private void stopWaiting() {
  	setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }
  
  /**
   * Paints the background of buffImage with a color.
   *
   * @param myWidth int - width of area to be cleared
   * @param myHeight int - height of area to be cleared
   */
  private void paintBuffImageBackground(int myWidth, int myHeight) {
    buffImageGraphics.setBackground(backgroundColor);
    buffImageGraphics.clearRect(0, 0, myWidth, myHeight);
  }

  
  /**
   * Determine whether or not there is an image loaded.
   * @return boolean Whether or not the image is loaded.
   */
  private boolean isImageLoaded() {
    if(buffImage == null) {
      return false;
    } else {
      return true;
    }
  }
  
  
  private void popLoadError() {
  	JOptionPane.showMessageDialog(null, "Could not open file.\nIt may be corrupt or in an unrecognizable format.",
            "File Error",
            JOptionPane.WARNING_MESSAGE);
  }
  

  public void load() { load(tempFile.getPath()); }
  
  /**
   * Loads up an image, either the first one or a new one.
   */
  public void load(String imagePath) {
  	startWaiting();
  	
    // Get the URI and load up corresponding image, the tempfile
    // should have already been created when the go button was pushed.
    origImage = this.getToolkit().getImage(imagePath);

    // Wait for the image to get loaded before we can do anything.
      MediaTracker tracker = new MediaTracker(this);
      tracker.addImage(origImage, 0);
      try {
        tracker.waitForID(0);
      }
      catch (InterruptedException ex) {
        origImage = null;
        return;
      }

    try {
      buffImage = new BufferedImage(origImage.getWidth(this),
                                    origImage.getHeight(this),
                                    BufferedImage.TYPE_INT_RGB);
    }
    catch (IllegalArgumentException e) {
      //If we get here the given path is bad. Popup a window saying so.
      origImage = null;
      buffImage = null;
      buffImageGraphics = null;
      popLoadError();
      stopWaiting();
      return;
    }
    
    // Create the graphics context so that we can draw things, then clear background & draw
    buffImageGraphics = buffImage.createGraphics();
    paintBuffImageBackground(origImage.getWidth(this), origImage.getHeight(this));
    buffImageGraphics.drawImage(origImage, 0, 0, null);

    // update the mainImagePane (updates the display)
    mainImagePane.setImageRef(buffImage, parentWindow);
    rotateTracker = 0;
    stopWaiting();
  }

 
  /**
   * Rotate the image by the given angle
   * @param angle int
   */
  public void rotate(int angle) {
  	setCursor(new Cursor(Cursor.WAIT_CURSOR));
    //add the angle to the rotateTracker
    rotateTracker += Math.toRadians((double) angle);

    int height = origImage.getHeight(this);
    int width = origImage.getWidth(this);

    //calculate the new height & width based on the angle of rotation...hmm geometry.
    int newheight = (int)(Math.abs(height*Math.cos(rotateTracker))+Math.abs(width*Math.sin(rotateTracker)));
    int newwidth  = (int)(Math.abs(height*Math.sin(rotateTracker))+Math.abs(width*Math.cos(rotateTracker)));

    buffImage = new BufferedImage(newwidth, newheight, BufferedImage.TYPE_INT_RGB);
    buffImageGraphics = buffImage.createGraphics();

    //Paint the background
    paintBuffImageBackground(newwidth, newheight);


    //Imagine the original image centered in a field of dimensions
    //newwidth and newheight.  These lines place the origin at the top
    //left corner of that original image in the new field.
    int xtranslate = (newwidth - width)/2;
    int ytranslate = (newheight - height)/2;
    buffImageGraphics.translate(xtranslate,ytranslate);

    //Set a rotation transformation, and draw on the original image.
    buffImageGraphics.rotate(rotateTracker,(double)width/2.0,(double)height/2.0);
    buffImageGraphics.drawImage(origImage, 0, 0, null);

    //update the mainImagePane (updates the display)
    mainImagePane.setImageRef(buffImage, parentWindow);
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }


  /**
   * Flip the image vertically.
   * An <code>AffineTransform</code> is used to do this since there is not operation in the
   * <code>Graphics2D</code> class for this.
   */
  public void flipVertical() {
  	setCursor(new Cursor(Cursor.WAIT_CURSOR));
    BufferedImage tempBuff = new BufferedImage(buffImage.getWidth(), buffImage.getHeight(),BufferedImage.TYPE_INT_RGB);
    Graphics2D tempBuffGraphics = tempBuff.createGraphics();
    AffineTransform trans = new AffineTransform(new double[] {1.0,0.0,0.0,-1.0});
    trans.translate(0.0, -buffImage.getHeight());

    tempBuffGraphics.transform(trans);
    tempBuffGraphics.drawImage(buffImage, 0, 0, null);

    // Set the origtImage to buffImage so we can roate it later.
    buffImage = tempBuff;
    origImage = buffImage;

    buffImageGraphics = tempBuffGraphics;
    mainImagePane.setImageRef(buffImage, parentWindow);
    rotateTracker=0;
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

  }


  /**
   * Flip the image horizontally.
   */
  public void flipHorizontal() {
  	setCursor(new Cursor(Cursor.WAIT_CURSOR));
    BufferedImage tempBuff = new BufferedImage(buffImage.getWidth(), buffImage.getHeight(),BufferedImage.TYPE_INT_RGB);
    Graphics2D tempBuffGraphics = tempBuff.createGraphics();

    AffineTransform trans = new AffineTransform(new double[] {-1.0,0.0,0.0,1.0});
    trans.translate(-buffImage.getWidth(), 0.0);

    tempBuffGraphics.transform(trans);
    tempBuffGraphics.drawImage(buffImage, 0, 0, null);

    // Set the origtImage to buffImage so we can roate it later.
    buffImage = tempBuff;
    origImage = buffImage;

    buffImageGraphics = tempBuffGraphics;
    mainImagePane.setImageRef(buffImage, parentWindow);
    rotateTracker=0;
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }


  /**
   * Resizes the image by the factor specified in changeSizeFactor.
   */
  public void changeSize(double changeSizeFactor) {
  	setCursor(new Cursor(Cursor.WAIT_CURSOR));
    //Create a new buffered image & associated graphics of the proper width and height.
    BufferedImage tempBuff = new BufferedImage((int)(changeSizeFactor*buffImage.getWidth()),
                                               (int)(changeSizeFactor*buffImage.getHeight()), BufferedImage.TYPE_INT_RGB);
    Graphics2D tempGraphics = tempBuff.createGraphics();

    // Set a scale transformation & draw in the new image.
    tempGraphics.scale(changeSizeFactor,changeSizeFactor);
    tempGraphics.drawImage(buffImage, 0, 0, null);

    buffImage = tempBuff;
    buffImageGraphics = tempGraphics;
    origImage = buffImage;
    
    mainImagePane.setImageRef(buffImage, parentWindow);
    rotateTracker=0;
    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }


  /**
   * Save the changed image.
   */
  public void save() {
  	startWaiting();
    //Get the URI and the extension of the file
    URI u = inputPanel.getURI();
    String path = u.getPath();
    String extension = path.substring(path.lastIndexOf(".")+1);

    //see whether or not the file type is supported
    String types[] = ImageIO.getReaderFormatNames();
    boolean contains = false;
    for (int i = 0; i < types.length;i++) {
      if(types[i].equals(extension)) {
        contains = true;
        break;
      }
    }
    System.out.println(extension + " " + contains);
    if(!contains) {
      //if not, tell the user
      JOptionPane.showMessageDialog(null, "That filetype is not supported for saving.",
                                    "File Type Not Supported",
                                    JOptionPane.WARNING_MESSAGE);
      return;
    }

    //otherwise, save the file
    try {
      ImageIO.write(buffImage, extension, tempFile);
      URI tempFileURI = null;
      try {
      	tempFileURI = new URI("file", null, "///"+tempFile.toString(), null, null);
      	} catch (URISyntaxException e){
      	}
      
      GridCommand saveCommand = fileTrans.putFile(tempFileURI, imageURI);
      submittedCommands.put(saveCommand.getIdentity().toString(), "save");
      try{
      	fileTrans.execute(saveCommand, false);
      } catch (Exception e) {
      }
      rotateTracker = 0;
    }
    catch (Exception e) {
      //We'll probably get here only if there is a permissions problem
      JOptionPane.showMessageDialog(null, "Could not save image.",
                                    "Save Failed",
                                    JOptionPane.WARNING_MESSAGE);
    }
    stopWaiting();
  }

  
  /**
   * Save as a new image
   */
  public void saveAS() {
  	setCursor(new Cursor(Cursor.WAIT_CURSOR));
  	setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

  }

  
  /**
   * Registers a viewer for a particular protocol.
   *
   * @param mimetype a <code>String</code> the mimetype. For now we
   * use a combination of endings and
   * prefixes. E.g. gridftp://..../a.jpg. Will fetch and display in
   * an image in the viewer. The file will be downloaded to the
   * local computer.
   * @param viewer an <code>Object</code> The viewer the displays
   * the result in the view window.
   */
  public void register(String mimetype, Object viewer) {
  	
  }

  
  /**
   * Sets the default viewer to
   *
   * @param viewer an <code>Object</code> value
   */
  public void setDefault(Object viewer) {

  }

  
  /**
   * updates the displaed gridface.
   */
  public void update() {
    myGridFace.update();
  }

  
  /**
   * Records the last time the GridFace was updated. The default
   * value is the time of creation.
   *
   * @return <code>Date</code>, the last time when the GridFace was
   * updated.
   */
  public Date lastUpdateTime() {
    return myGridFace.lastUpdateTime();
  }

  
  /**
   * sets the name for a GridFace. Names are useful to distinguish
   * gridfaces from each other. A Name is supposed to be
   * unique. Default is null.
   *
   * @param name a <code>String</code> thatreturns a uniqe name.
   */
  public void setName(String name) {
    myGridFace.setName(name);
  }

  
  /**
   * sets a label for a GridFace. Label are useful to create
   * abbreviations for a GridFace for a graphical display. Labels
   * are ofthe used as a title if applicable. Defaukt is null.
   *
   * @param label a <code>String</code> that returns the assigned label.
   */
  public void setLabel(String label) {
    myGridFace.setLabel(label);
  }

  /**
   * Registers another GridFace to this GridFace. This will prevent
   * that gridfaces are unnecesarily killed.
   *
   * @param connection a <code>GridFace</code> value
   */
  public void register(GridFace connection) {
  	myGridFace.register(connection);
  }


    //gvl: it is not good programming style to have more than one
    //class in a file. also the name listener for a class is dubious.
    //is this an inner class? need to chacke this later when there is
    //more time.

  /**
   * Provides a generic action listener for all buttons and
   * distinguishes between them on the basis of their text.
   *
   */
public class listener implements ActionListener {
  /**
   * Determines what button was pressed and calls the appropriate method.
   *
   * This might need to be implemented differently, but this way
   * reduces the number of instantiated classes.
   *
   * @param actionEvent The ActionEvent that was performed.  This is
   * used to get the text of the button that was pushed.
   */
  public void actionPerformed(ActionEvent actionEvent) {
  	String buttonText = ( (JButton) actionEvent.getSource()).getToolTipText();
  	if (buttonText == "Rotate") {
  		//check to see if we have an image yet
  		if(!isImageLoaded()) return;
  		rotateButtonPushed();
  	}
  	else if (buttonText == "Flip Vertically") {
  		if(!isImageLoaded()) return;
  		flipVertical();
  	}
  	else if (buttonText == "Flip Horizontally") {
  		if(!isImageLoaded()) return;
  		flipHorizontal();
  	}
  	else if (buttonText == "Save As") {
      	if(!isImageLoaded()) return;
      	saveAS();
      }
      else if (buttonText == "Save") {
      	if(!isImageLoaded()) return;
      	save();
      }
      else if (buttonText == "Change Size") {
      	if(!isImageLoaded()) return;
      	changeSizeButtonPushed();
      }
      else if (buttonText == "Load Image"){
      	goButtonPushed();
      } 
    }
    

}

  /**
   * This class extends JComponent and displays the image.  It's
   * paintComponent method paints on the image if there is one.
   */
  public class imagePane extends JComponent implements Scrollable {
    BufferedImage imageRef;
    int height, width;
    Dimension preferredSize = new Dimension(0,0);
    
    /**
     * Initialize a reference to the BufferedImage
     * @param imageRef A reference to the BufferedImage so that it may be displayed.
     */
    public imagePane(BufferedImage imageRef) {
      this.imageRef = imageRef;
    }

    /**
     * Paints the image onto the display
     * @param g The Graphics context onto which we paint the image.
     */
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      try {
        g.drawImage(imageRef, 0, 0, imageRef.getWidth(), imageRef.getHeight(), this);
      } catch (NullPointerException e) {
      }
    }
    
    /**
     * Set the image reference and repaint this component.
     * @param imageRef A reference to the new image.
     * @param window JFrame
     */
    public void setImageRef(BufferedImage imageRef, Window window){
      this.imageRef = imageRef;
      this.repaint();
      Dimension imageDimension = new Dimension(imageRef.getWidth(), imageRef.getHeight());
      this.setPreferredSize(imageDimension);
      scrollPane.revalidate();
    }

	public Dimension getPreferredScrollableViewportSize() {
		return this.getPreferredSize();
	}

	public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
		return 0;
	}

	public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
		return 0;
	}


	public boolean getScrollableTracksViewportWidth() {
		return false;
	}


	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
  }

  public void statusChanged(StatusEvent event) {
  	Status status = event.getStatus();
  	//System.out.println("Command Status Changed to " + status.getStatusCode());
  	
  	GridCommand command = (GridCommand) event.getSource();
  	if ((status.getStatusCode() == Status.COMPLETED))
  	{
  		if (command.getCommand().equals("open"))
  		{
  			//open returns sessionid for future reference
  			fileTrans.setSessionId((Identity) command.getOutput());
  			//CHECK this for file sep stuff
  			SwingUtilities.invokeLater(new Runnable() {
  				public void run() {
  					URI tempFileURI = null;
  					try {
  						alert("HERE: "+tempFile);
  						tempFileURI = new URI("file", null, "//" + tempFile.toString(), null, null);
  					} catch (URISyntaxException e){
  					}
  					
  					GridCommand openCommand = fileTrans.getFile(imageURI, tempFileURI);
  					submittedCommands.put(openCommand.getIdentity().toString(), "open");
  					try {
  						fileTrans.execute(openCommand, true);
  					} catch (Exception e) { }
  				}});
  		} else if (command.getCommand().equals("getfile")) {
  			String saveOrOpen = (String) submittedCommands.get(command.getIdentity().toString());
  			submittedCommands.remove(command.getIdentity().toString());
  			if(saveOrOpen.equals("open")){
  				SwingUtilities.invokeLater(new Runnable() {
  					public void run() {
  						load();
  					}
  				});
  			}
  		}
  	}
  }

/* (non-Javadoc)
 * @see org.globus.cog.gridface.interfaces.ImageViewerPanel#close()
 */
	public boolean close() {
		return true;
	}

}
