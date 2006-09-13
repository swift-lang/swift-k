package karajanRCP;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.plugin.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.osgi.framework.BundleContext;

import java.net.URL;
import java.util.*;

/**
 * The main plugin class to be used in the desktop.
 */
public class KarajanRCPPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static KarajanRCPPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	private FormColors formColors;
	
	public static final String IMG_FORM_BG = "formBg"; 
	public static final String IMG_LARGE = "large"; 
	public static final String IMG_HORIZONTAL = "horizontal";
	public static final String IMG_VERTICAL = "vertical"; 
	public static final String IMG_SAMPLE = "sample"; 
	public static final String IMG_WIZBAN = "wizban"; 
	public static final String IMG_LINKTO_HELP = "linkto_help";
	public static final String IMG_HELP_TOPIC = "help_topic"; 
	public static final String IMG_CLOSE = "close";

	
	
	/**
	 * The constructor.
	 */
	public KarajanRCPPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle
					.getBundle("org.cogkit.repository.KarajanPluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}
	

	protected void initializeImageRegistry(ImageRegistry registry) {
		registerImage(registry, IMG_FORM_BG, "form_banner.gif");
		registerImage(registry, IMG_LARGE, "large_image.gif");
		registerImage(registry, IMG_HORIZONTAL, "th_horizontal.gif"); 
		registerImage(registry, IMG_VERTICAL, "th_vertical.gif");
		registerImage(registry, IMG_SAMPLE, "sample.gif");
		registerImage(registry, IMG_WIZBAN, "newprj_wiz.gif"); 
		registerImage(registry, IMG_LINKTO_HELP, "linkto_help.gif");
		registerImage(registry, IMG_HELP_TOPIC, "help_topic.gif"); 
		registerImage(registry, IMG_CLOSE, "close_view.gif"); 
	}

	private void registerImage(ImageRegistry registry, String key,
			String fileName) {
		try {
			IPath path = new Path("icons/" + fileName); 
			URL url = find(path);
			if (url!=null) {
				ImageDescriptor desc = ImageDescriptor.createFromURL(url);
				registry.put(key, desc);
			}
		} catch (Exception e) {
		}
	}


	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		resourceBundle = null;
	}
    
	public FormColors getFormColors(Display display) {
		if (formColors == null) {
			formColors = new FormColors(display);
			formColors.markShared();
		}
		return formColors;
	} 

	/**
	 * Returns the shared instance.
	 */
	public static KarajanRCPPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = KarajanRCPPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle.getBundle("karajanRCP.KarajanRCPPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("KarajanRCP", path);
	}
	
	
	
	public Image getImage(String key) {
		return getImageRegistry().get(key);
	}
	
}
