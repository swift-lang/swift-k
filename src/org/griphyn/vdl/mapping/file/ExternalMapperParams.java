package org.griphyn.vdl.mapping.file;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.griphyn.vdl.mapping.MappingParamSet;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;


public class ExternalMapperParams extends MappingParamSet {

	public static final List<String> NAMES = Arrays.asList("exec");

	private Object exec;
	private Map<String, Object> other = new HashMap<String, Object>();

	@Override
	public Collection<String> getNames() {
		return NAMES;
	}

	public void setExec(String exec) {
		this.exec = exec;
	}

	public String getExec() {
		return (String) exec;
	}
	
	@Override
	protected boolean set0(String name, Object value) {
		if (name.equals("exec")) {
			this.exec = value;
		}
		else {
			if (!super.set0(name, value)) {
			    other.put(name, value);
			}
		}
		return true;
	}

	@Override
	public AbstractDataNode getFirstOpen() {
		if (checkOpen(exec)) {
			return (AbstractDataNode) exec;
		}
		else {
		    for (Object o : other.values()) {
		        if (checkOpen(o)) {
		            return (AbstractDataNode) o;
		        }
		    }
			return super.getFirstOpen();
		}
	}

	@Override
	public void toString(StringBuilder sb) {
		addParam(sb, "exec", exec);
		for (Map.Entry<String, Object> e : other.entrySet()) {
		    addParam(sb, e.getKey(), e.getValue());
		}
		super.toString(sb);
	}
	@Override
	public void unwrapPrimitives() {
		exec = unwrap(exec, String.class);
		for (Map.Entry<String, Object> e : other.entrySet()) {
		    e.setValue(unwrap(e.getValue(), String.class));
		}
		super.unwrapPrimitives();
	}

	public Map<String, Object> getOtherParams() {
	    return other;
	}
}
