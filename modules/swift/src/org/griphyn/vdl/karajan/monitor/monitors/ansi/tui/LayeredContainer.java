/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Jan 31, 2007
 */
package org.griphyn.vdl.karajan.monitor.monitors.ansi.tui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LayeredContainer extends Container {
	protected List<Component>[] layers;

	@SuppressWarnings("unchecked")
    public LayeredContainer() {
		layers = new List[3];
	}

	public void add(Component comp) {
		int layer = comp.getLayer();
		if (layers[layer] == null) {
			layers[layer] = new ArrayList<Component>();
		}
		layers[layer].add(comp);
		comp.setParent(this);
		invalidate();
	}

	public void remove(Component comp) {
		int layer = comp.getLayer();
		if (layers[layer] != null) {
			layers[layer].remove(comp);
		}
		if (focused == comp) {
			focused = null;
		}
		invalidate();
	}

	public List<Component>[] getLayers() {
		return layers;
	}

	public void removeAll() {
		for (int i = BOTTOM_LAYER; i <= TOP_LAYER; i++) {
			layers[i] = null;
		}
		invalidate();
	}

	protected void drawTree(ANSIContext context) throws IOException {
		super.drawTree(context);
		drawTree(context, BOTTOM_LAYER);
		drawTree(context, NORMAL_LAYER);
		drawTree(context, TOP_LAYER);

	}

	protected void drawTree(ANSIContext context, int layer) throws IOException {
		if (layers[layer] == null) {
			return;
		}
		Iterator<Component> i = layers[layer].iterator();
		while (i.hasNext()) {
			Component c = i.next();
			if (c.isVisible()) {
				c.drawTree(context);
			}
		}
	}

	protected void validate() {
		if (isValid()) {
			return;
		}
		validate(BOTTOM_LAYER);
		validate(NORMAL_LAYER);
		validate(TOP_LAYER);
		super.validate();
	}

	protected void validate(int layer) {
		if (layers[layer] == null) {
			return;
		}
		Iterator<Component> i = layers[layer].iterator();
		boolean focus = false;
		while (i.hasNext()) {
			Component c = i.next();
			if (c.hasFocus() && !hasFocus()) {
				focus();
			}
			c.validate();
		}
	}

	public boolean childFocused(Component component) {
	    oldFocused = focused;
		boolean f = true;
		if (focused != null) {
			f = focused.unfocus();
			if (f) {
				focused.focusLost();
			}
		}
		if (f) {
			focused = component;
			focused.focusGained();
		}
		return f;
	}

	public boolean keyboardEvent(Key key) {
		if (key.modALT() || key.isFunctionKey()) {
			return keyboardEvent(key, TOP_LAYER) || keyboardEvent(key, NORMAL_LAYER)
					|| keyboardEvent(key, BOTTOM_LAYER);
		}
		else if (key.equals(Key.TAB)) {
		    if (focused == null) {
		        focusFirst();
		    }
		    else {
		    	if (focused.keyboardEvent(key)) {
		    	    return true;
		    	}
		    	else {
		    		focused.focusNext();
		    	}
		    }
		    return true;
		}
		else if (focused != null) {
			return focused.keyboardEvent(key);
		}
		else {
			return false;
		}
	}

	public boolean focusFirst() {
	    for (int i = 0; i < layers.length; i++) {
	        if (layers[i] != null) {
	            Iterator<Component> j = layers[i].iterator();
	            if (j.hasNext()) {
	                Component comp = j.next();
	                if (j.next().focusFirst()) {
	                    return true;
	                }
	            }
	        }
	    }
	    return false;
    }
	
	
    public boolean focusNext() {
        boolean found = false;
        if (focused == null) {
            return focusFirst();
        }
        else if (focused.focusNext()) {
            return true;
        }
        
        for (int i = 0; i < layers.length; i++) {
            if (layers[i] != null) {
                Iterator<Component> j = layers[i].iterator();
                while (j.hasNext()) {
                    if (found) {
                        if (j.next().focusFirst()) {
                            return true;
                        }
                    }
                    if (j.next() == focused) {
                        found = true;
                    }
                }
            }
        }
        return false;
    }

    protected boolean keyboardEvent(Key key, int layer) {
		if (layers[layer] == null) {
			return false;
		}
		Iterator<Component> i = layers[layer].iterator();
		while (i.hasNext()) {
			if (i.next().keyboardEvent(key)) {
				return true;
			}
		}
		return false;
	}
}
