/*
 * Created on Jun 15, 2006
 */
package org.griphyn.vdl.mapping;

import org.griphyn.vdl.type.Field;


public class DataNode extends AbstractDataNode {
	private DSHandle root;
	private DSHandle parent;
	
	protected DataNode(Field field, DSHandle root, DSHandle parent) {
		super(field);
		if (parent != null && field.getId() == null) {
		    throw new IllegalArgumentException("Internal error: field id is null");
		}
		this.root = root;
		this.parent = parent;
	}
	
	public DSHandle getRoot() {
		return root;
	}
	
	public DSHandle getParent() {
		return parent;
	}
	
	public void setParent(DSHandle parent) {
		this.parent = parent;
	}

	public String getParam(String name) {
		return null;
	}
}
