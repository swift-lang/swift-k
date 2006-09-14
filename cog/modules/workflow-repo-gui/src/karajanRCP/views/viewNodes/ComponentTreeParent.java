package karajanRCP.views.viewNodes;

import java.util.ArrayList;

public class ComponentTreeParent extends ComponentTreeObject {
	private ArrayList children;
	public ComponentTreeParent(String name) {
		super(name);
		children = new ArrayList();
	}
    
    public ComponentTreeParent(String name, String type) {
        super(name, type);
        children = new ArrayList();
    }
    
	public void addChild(ComponentTreeObject child) {
		children.add(child);
		child.setParent(this);
	}
	public void removeChild(ComponentTreeObject child) {
		children.remove(child);
		child.setParent(null);
	}
	public ComponentTreeObject [] getChildren() {
		return (ComponentTreeObject [])children.toArray(new ComponentTreeObject[children.size()]);
	}
	public boolean hasChildren() {
		return children.size()>0;
	}
}
