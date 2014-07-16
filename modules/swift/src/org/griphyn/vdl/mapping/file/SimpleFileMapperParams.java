package org.griphyn.vdl.mapping.file;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.MappingParamSet;


@SuppressWarnings("unused")
public class SimpleFileMapperParams extends AbstractFileMapperParams {

	public static final List<String> NAMES = Arrays.asList("padding");

	private Object padding = 4;

	@Override
	public Collection<String> getNames() {
		return NAMES;
	}

	public void setPadding(Integer padding) {
		this.padding = padding;
	}

	public Integer getPadding() {
		return (Integer) padding;
	}

	@Override
	protected boolean set0(String name, Object value) {
		if (name.equals("padding")) {
			this.padding = value;
		}
		else {
			return super.set0(name, value);
		}
		return true;
	}

	@Override
	public AbstractDataNode getFirstOpen() {
		if (checkOpen(padding)) {
			return (AbstractDataNode) padding;
		}
		else {
			return super.getFirstOpen();
		}
	}

	@Override
	public void toString(StringBuilder sb) {
		addParam(sb, "padding", padding);
		super.toString(sb);
	}

	@Override
	public void unwrapPrimitives() {
		padding = unwrap(padding, Integer.class);
		super.unwrapPrimitives();
	}


}
