/*
 * Created on Mar 14, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.globus.cog.gridshell.ctrl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

/**
 * 
 */
public abstract class AbstractActionBinder {
	private static final Logger logger = Logger.getLogger(AbstractActionBinder.class);

	// used to restore bindings
	private Map restoreInputMap = new HashMap();
	private Map restoreActionMap = new HashMap();
	
	
   	/**
     * Use to save mappings you are overriding and then add command mapping 
     * @param keyStroke
     * @param key
     * @param action
     */
    protected void addMapping(JComponent mappedComponent, KeyStroke keyStroke, Object key, Action action) {
    	if(key == null || mappedComponent == null) {
    		return;
    	}
    	if(!restoreActionMap.containsKey(mappedComponent)) {
    		logger.debug("initialized restoreActionMap for: "+mappedComponent);
    		restoreActionMap.put(mappedComponent,new HashMap());
    	}
    	if(!restoreInputMap.containsKey(mappedComponent)) {
    		logger.debug("initialized restoreInputMap for: "+mappedComponent);
    		restoreInputMap.put(mappedComponent,new HashMap());
    	}
    	
    	// mappings for this mappedComponent
    	Map thisInputMap = (Map)restoreInputMap.get(mappedComponent);
    	Map thisActionMap = (Map)restoreActionMap.get(mappedComponent);
    	
    	// always save the old values in order to restore
    	Object oldInputValue = mappedComponent.getInputMap().get(keyStroke);
    	if(!thisInputMap.containsKey(keyStroke)) {
        	thisInputMap.put(keyStroke,oldInputValue);
    	}else {
    		throw new RuntimeException("Already received new binding for "+mappedComponent+" inputMap keyStroke="+keyStroke+" - Hint: Ensure to call createBindings is called only once for each time restoreBindings is called.");
    	}    	
    	logger.debug("keyStroke="+keyStroke+" oldInputValue="+oldInputValue);
    	
    	Object oldActionValue = mappedComponent.getActionMap().get(key);
    	if(!thisActionMap.containsKey(key)) {
    		thisActionMap.put(key,oldActionValue);
    	}else {
    		throw new RuntimeException("Already received new binding for "+mappedComponent+" actionMap key="+key+" - Hint: Ensure to call createBindings is called only once for each time restoreBindings is called.");
    	}
    	
    	logger.debug("key="+key+" oldActionValue="+oldActionValue);
    	    	    	
    	// now add them
    	mappedComponent.getInputMap().put(keyStroke,key);
        mappedComponent.getActionMap().put(key,action);
        
        if(logger.isDebugEnabled()) {
        	logger.debug("thisInputMap="+thisInputMap);
        	logger.debug("thisActionMap="+thisActionMap);
        }
    }
	/**
	 * Restores the bindings
	 */
	public void restoreBindings() {
		logger.debug("restoring inputMap");
		Map currentMap = restoreInputMap;
		Iterator iMappings = currentMap.keySet().iterator();
		while(iMappings.hasNext()) {			
			JComponent mappedComponent = (JComponent)iMappings.next();
			logger.debug("resoring for mappedComponent: "+mappedComponent);
			
			Map thisInputMap = (Map)currentMap.get(mappedComponent);
			logger.debug("thisInputMap="+thisInputMap);
			
			Iterator iMapping = thisInputMap.keySet().iterator();
			while(iMapping.hasNext()) {				
				KeyStroke keyStroke = (KeyStroke) iMapping.next();
				Object value = thisInputMap.get(keyStroke);
				mappedComponent.getInputMap().put(keyStroke,value);
				
				logger.debug("restored keyStoke: "+keyStroke+" to "+value);
			}
		}
		
		logger.debug("restoring actionMap");
		currentMap = restoreActionMap;
		iMappings = currentMap.keySet().iterator();
		while(iMappings.hasNext()) {
			JComponent mappedComponent = (JComponent)iMappings.next();			
			logger.debug("resoring for mappedComponent: "+mappedComponent);
			
			Map thisActionMap = (Map)currentMap.get(mappedComponent);
			logger.debug("thisActionMap="+thisActionMap);
			Iterator iMapping = thisActionMap.keySet().iterator();
			while(iMapping.hasNext()) {
				Object key = iMapping.next();
				Action value = (Action) thisActionMap.get(key);
				mappedComponent.getActionMap().put(key,value);
				
				logger.debug("restored key: "+key+" to "+value);
			}
		}
	}	
	/**
	 * Should use addMapping method to add mappings to components, 
	 * then when restoreBindings is called the bindings will be restored
	 * to the state that they were before the components were bound
	 */	
	abstract public void createBindings();
}
