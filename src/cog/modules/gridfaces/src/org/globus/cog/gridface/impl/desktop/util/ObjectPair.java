//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.gridface.impl.desktop.util;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.globus.cog.gridface.impl.desktop.interfaces.AccessPreferences;

public class ObjectPair extends Dictionary implements AccessPreferences {
	
	private Vector pairs = null;
	
	private LocalEnumerator elemEnum=null;
	private LocalEnumerator keyEnum=null;

	public ObjectPair() {
		super();
		pairs = new Vector();
		elemEnum = new LocalEnumerator(LocalEnumerator.ENUM_VALUES);
		keyEnum= new LocalEnumerator(LocalEnumerator.ENUM_KEYS);
	}
	public Enumeration elements() {
		elemEnum.resetIndex();
	
		return elemEnum;
	}
	public Object[] elementsAsArray(){
		Object[] elementsArray = new Object[size()];	
		for (int i = 0; i < pairs.size(); i++) {
			elementsArray[i] = ((Pair)pairs.get(i)).getValue();
		}
		return elementsArray;
	}
	public Object get(Object key) {
		for (Iterator iter = pairs.iterator(); iter.hasNext();) {
			Pair element = (Pair) iter.next();
			if(element.getKey().equals(key)){
				return element.getValue();
			}
		}
		return null;
	}
	public boolean isEmpty() {
		return pairs.isEmpty();
	}
	public Enumeration keys() {
		keyEnum.resetIndex();
		return keyEnum;
	}
	public Object[] keysAsArray(){
		Object[] keysArray = new Object[size()];	
		for (int i = 0; i < pairs.size(); i++) {
			keysArray[i] = ((Pair)pairs.get(i)).getKey();
		}
		return keysArray;
	}
	public boolean pairsAreOnlyOfClassString(){
		for (Iterator iter = pairs.iterator(); iter.hasNext();) {
			Pair element = (Pair) iter.next();
			//Right we will just check if all the values are of class String
			//if( element.getKey().toString().indexOf(String.class.getName()) != -1) return false;
			if(!(element.getValue() instanceof String)){
				return false;
			}
		}
		return true;
	}
	public Object put(Object key, Object value) {
		Pair pair = new Pair(key,value);
		pairs.add(pair);
		return pair;
	}
	public Object remove(Object key) {
		int tmpIndex=0;
		Object objRemoved=null;
		
		for (Iterator iter = pairs.iterator(); iter.hasNext();) {
			Pair element = (Pair) iter.next();
			if(element.getKey().equals(key)) {
				break;
			}
			tmpIndex++;
		}
		pairs.remove(tmpIndex);

		return objRemoved;
	}
	public int size() {
		return pairs.size();
	}
	
	public String toString() {
		if(pairsAreOnlyOfClassString()){
			StringBuffer valuesToString=new StringBuffer();
			boolean firstValue=true;
			for (Iterator iter = pairs.iterator(); iter.hasNext();) {
				Pair element = (Pair) iter.next();
				//Skip the delimeter if its the first value
				//being put in the string buffer
				if(firstValue){
					valuesToString.append(element.getValue());
					firstValue=false;
				}else{
					valuesToString.append(" "+element.getValue());
				}
			}
			return valuesToString.toString();
		}
		return null;
	}
	
	public void loadPreferences(Preferences startNode) throws Exception {
		try{
			String[] children = startNode.keys();
			
			//Object pairs are only stored in pairs.. of Arg[0,1..] , Value[0,1..]
			for (int i = 0; i < children.length / 2; i++) {
				//If Value[i] is not String then dont load
				if(!startNode.get("Value"+i,"NotFound").equals(NO_TOSTRING)){
					this.put(startNode.get("Arg"+i,"NotFound"),
							startNode.get("Value"+i,"NotFound"));
				}
			}
		}catch(BackingStoreException be){}
		
		

	}
	//TODO currently ObjectPair is only used to Store Class,Object pairs
	public void savePreferences(Preferences startNode) {
		if (pairs != null) {
		Object[] classArray = keysAsArray();
		Object[] objArray = elementsAsArray();
		
		for (int j = 0; j < classArray.length; j++) {
			String objString = classArray[j].toString();
			//If key is a Class object then we must strip out
			//the prefix "class " or "interface " from it
			if(classArray[j] instanceof Class){
				String classPrefix = "class ";
				String interfacePrefix = "interface ";
				if(objString.startsWith(classPrefix)){
					objString = objString.split(classPrefix)[1];
				}else if(objString.startsWith(interfacePrefix)){
					objString = objString.split(interfacePrefix)[1];
				}
			}
			startNode.put("Arg" + j, objString);
			
			//indicate in Preferences whether value is String or not
			if(objArray[j] instanceof String){
				startNode.put("Value"+j,objArray[j].toString());
			}else{
				startNode.put("Value"+j,NO_TOSTRING);
			}
			
		}
	}

	}
	private class Pair {
		private Object key;
		private Object value;
		
		public Pair(Object k, Object v){
			this.key = k;
			this.value = v;
		}
		public Object getKey(){
			return this.key;
		}
		public Object getValue(){
			return this.value;
		}
	}
	private class LocalEnumerator implements Enumeration {
		static final int ENUM_KEYS = 0;
		static final int ENUM_VALUES = 1;
		
		int type;
		
		protected int index=0;
		
		public LocalEnumerator(int type){
			this.type = type;
		}
		public void resetIndex(){
			index=0;
		}
		public boolean hasMoreElements() {
			return (pairs.size()  > index);
		}
		public Object nextElement() {
			if(type == ENUM_KEYS){
				return ((Pair)pairs.get(index++)).getKey();
			}else if(type == ENUM_VALUES){
				return ((Pair)pairs.get(index++)).getValue();
			}
			return null;
		}
}
	
}
