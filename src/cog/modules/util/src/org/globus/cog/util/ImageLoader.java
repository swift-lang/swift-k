// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

/**
 * Loads an image from a file/resource Hopefully it works regardless of the
 * packaging. It also caches the icons, returning only one instance of the same
 * icon.
 */
public class ImageLoader {
	private static Logger logger = Logger.getLogger(ImageLoader.class);

	private static Hashtable icons;

	private static ImageIcon nullIcon = new ImageIcon();

	private static Hashtable imageMappings;

	static {
		imageMappings = new Hashtable();
		loadMap("images.map");
	}

	public static void loadMap(String name) {
		TextFileLoader l = new TextFileLoader();
		if (!l.exists(name)) {
			return;
		}
		String map = l.loadFromResource(name);
		String[] lines = map.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String[] pair = lines[i].split("=");
			if (pair.length != 2) {
				logger.debug("Invalid line in " + name + ": " + lines[i]);
				continue;
			}
			imageMappings.put(pair[0].trim(), pair[1].trim());
		}
	}

	/**
	 * Creates a new image loader. This is needed in order to get a reference to
	 * the correct class loader, which is needed for loading images from jars
	 * that are not local (webstart). This will not affect the cache, which is
	 * shared between all instances of this class.
	 */
	public ImageLoader() {
		if (icons == null) {
			icons = new Hashtable();
		}
	}

	/**
	 * Loads an image
	 * 
	 * @param name
	 *            The URI of the image to be loaded
	 * @return An ImageIcon with the loaded image or null if the image was not
	 *         found
	 */
	public ImageIcon loadImage(String name) {
		ImageIcon i = (ImageIcon) icons.get(name);
		if (i != null) {
			return i;
		}
		String mappedName = (String) imageMappings.get(name);
		if (mappedName == null) {
			mappedName = name;
			logger.debug("Loading image " + name);
		}
		else {
			logger.debug("Loading image " + name + " (mapped to " + mappedName + ")");
		}

		URL ImageURL = getClass().getClassLoader().getResource(mappedName);

		if (ImageURL != null) {
			ImageIcon ii = new ImageIcon(ImageURL);
			icons.put(name, ii);
			return ii;
		}
		else {
			File ImageFile = new File(name);
			FileInputStream is;

			try {
				is = new FileInputStream(ImageFile);

				byte[] ImageBytes = new byte[(int) ImageFile.length()];

				is.read(ImageBytes, 0, (int) ImageFile.length());
				ImageIcon icon = new ImageIcon(ImageBytes);
				icons.put(name, icon);
				return icon;
			}
			catch (FileNotFoundException e) {
				logger.error("Image not found: " + name);
				return nullIcon;
			}
			catch (IOException e) {
				logger.error("Error reading from file " + name);
				return nullIcon;
			}
		}
	}

	/**
	 * Convenience method that automatically creates an ImageLoader instance
	 * 
	 * @param name
	 *            The URI of the image to be loaded
	 * @return An ImageIcon with the loaded image or null if the image was not
	 *         found
	 */
	public static ImageIcon loadIcon(String name) {
		ImageLoader il = new ImageLoader();
		return il.loadImage(name);
	}
}