
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

    
package org.globus.cog.gui.grapheditor.util.graphics;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.Iterator;

public class ImageProcessor {
	private static final int OP_DESATURATE = 0x10000;
	private static final int OP_HIGHLIGHT  = 0x20000;
	private static final int OP_ADJUSTHSV  = 0x30000;
	private static final int OP_COMPOSE	 = 0x40000;
	private static final int MAX_CACHE_SIZE = 100;
	private static int CACHE_ENTRIES = 0;
	private static Hashtable cache = new Hashtable();
	
	private static float[] hsb = new float[3];
	public static Image desaturate(Image source, double amount){
		Operation op = new Operation(OP_DESATURATE, source, null, amount, 0.0, 0.0);
		if (cache.containsKey(op)){
			return getFromCache(op);
		}
		BufferedImage bi = new BufferedImage(source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		bi.createGraphics().drawImage(source, null, null);
		for (int y = 0; y < bi.getHeight(); y++){
			for (int x = 0; x < bi.getWidth(); x++){
				int v = bi.getRGB(x, y);
				int a = v & 0xff000000;
				int r = ((v >> 16) & 0xff);
				int g = ((v >>  8) & 0xff);
				int b = ((v >>  0) & 0xff);
				Color.RGBtoHSB(r, g, b, hsb);
				hsb[1] = (float) amount*hsb[1];
				hsb[2] = (float) amount*hsb[2];
				v = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
				v = (v & 0x00ffffff) | a;
				bi.setRGB(x, y, v);
			}
		}
		addToCache(op, bi);
		return bi;
	}
	
	public static Image highlight(Image source, double amount){
		Operation op = new Operation(OP_HIGHLIGHT, source, null, amount, 0.0, 0.0);
		if (cache.containsKey(op)){
			return getFromCache(op);
		}
		BufferedImage bi = new BufferedImage(source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		bi.createGraphics().drawImage(source, null, null);
		for (int y = 0; y < bi.getHeight(); y++){
			for (int x = 0; x < bi.getWidth(); x++){
				int v = bi.getRGB(x, y);
				int a = v & 0xff000000;
				int r = ((v >> 16) & 0xff);
				int g = ((v >>  8) & 0xff);
				int b = ((v >>  0) & 0xff);
				r = r + (int)((255 - r) * amount);
				g = g + (int)((255 - g) * amount);
				b = b + (int)((255 - b) * amount);
				v = (r << 16) + (g << 8) + b;
				v = (v & 0x00ffffff) | a;
				bi.setRGB(x, y, v);
			}
		}
		addToCache(op, bi);
		return bi;
	}
	
	public static Image adjustHSV(Image source, double dh, double ds, double dv){
		Operation op = new Operation(OP_ADJUSTHSV, source, null, dh, ds, dv);
		if (cache.containsKey(op)){
			return getFromCache(op);
		}
		BufferedImage bi = new BufferedImage(source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		bi.createGraphics().drawImage(source, null, null);
		for (int y = 0; y < bi.getHeight(); y++){
			for (int x = 0; x < bi.getWidth(); x++){
				int v = bi.getRGB(x, y);
				int a = v & 0xff000000;
				int r = ((v >> 16) & 0xff);
				int g = ((v >>  8) & 0xff);
				int b = ((v >>  0) & 0xff);
				Color.RGBtoHSB(r, g, b, hsb);
				hsb[0] = (float) hsb[0] - (float) dh;
				hsb[1] = (float) ds * hsb[1];
				if (hsb[1] > 1){
					hsb[1] = 1;
				}
				hsb[2] = (float) dv * hsb[2];
				if (hsb[2] > 1){
					hsb[2] = 1;
				}
				v = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
				v = (v & 0x00ffffff) | a;
				bi.setRGB(x, y, v);
			}
		}
		addToCache(op, bi);
		return bi;
	}
	
	public static Image compose(Image i1, Image i2){
		Operation op = new Operation(OP_COMPOSE, i1, i2, 0.0, 0.0, 0.0);
		if (cache.containsKey(op)){
			return getFromCache(op);
		}
		BufferedImage bi = new BufferedImage(i1.getWidth(null), i1.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		bi.createGraphics().drawImage(i1, null, null);
		bi.createGraphics().drawImage(i2, null, null);
		addToCache(op, bi);
		return bi;
	}
	
	private static void addToCache(Operation op, Image im){
		if (CACHE_ENTRIES == MAX_CACHE_SIZE){
			//clean cache
			Operation min = null;
			int rank = 999999999; 
			Iterator i = cache.keySet().iterator();
			while (i.hasNext()){
				Operation key = (Operation) i.next();
				CacheEntry entry = (CacheEntry) cache.get(key);
				entry.recent++;
				int crank = entry.hits - entry.recent;
				if (crank < rank){
					rank = crank;
					min = key;
				}
			}
			cache.remove(min);
			CACHE_ENTRIES--;
		}
		cache.put(op, new CacheEntry(im));
	}
	
	private static Image getFromCache(Operation op){
		CacheEntry entry = (CacheEntry) cache.get(op);
		entry.recent = 0;
		entry.hits++;
		return entry.image;
	}
	
	private static class Operation{
		public  int type;
		public Image im1, im2;
		public double am1, am2, am3;
		
		public Operation(int type, Image im1, Image im2, double am1, double am2, double am3){
			this.type = type;
			this.im1 = im1;
			this.im2 = im2;
			this.am1 = am1;
			this.am2 = am2;
			this.am3 = am3;
		}
		
		public boolean equals(Object o){
			if (!(o instanceof Operation)){
				return false;
			}
			if (o == null){
				return false;
			}
			Operation c = (Operation) o;
			if ((type == c.type) && (im1 == c.im1) && (im2 == c.im2) && (am1 == c.am1) &&
			(am2 == c.am2) && (am3 == c.am3)){
				return true;
			}
			return false;
		}
		
		public int hashCode(){
			int r = type;
			if (im1 != null){
				r+=im1.hashCode();
			}
			if (im2 != null){
				r+=im2.hashCode();
			}
			r+=(int) (am1*1000);
			r+=(int) (am2*1000);
			r+=(int) (am3*1000);
			return r;
		}
	}
	
	private static class CacheEntry{
		public Image image;
		public int recent;
		public int hits;
		
		public CacheEntry(Image image){
			this.image = image;
			this.recent = 0;
			this.hits = 1;
		}
	}
}

