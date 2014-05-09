package org.griphyn.vdl.mapping.file;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.MappingParamSet;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;


public class SingleFileMapperParams extends MappingParamSet {

	public static final List<String> NAMES = Arrays.asList("file");

	private Object file;

	@Override
	public Collection<String> getNames() {
		return NAMES;
	}

	public AbsFile getFile() {
		return (AbsFile) file;
	}

	@Override
	protected boolean set0(String name, Object value) {
		if (name.equals("file")) {
			this.file = value;
		}
		else {
			return super.set0(name, value);
		}
		return true;
	}

	@Override
	public AbstractDataNode getFirstOpen() {
		if (checkOpen(file)) {
			return (AbstractDataNode) file;
		}
		else {
			return super.getFirstOpen();
		}
	}

	@Override
	public void toString(StringBuilder sb) {
		addParam(sb, "file", file);
		super.toString(sb);
	}

	@Override
	public void unwrapPrimitives() {
		if (file == null) {
			throw new IllegalArgumentException("Missing required argument 'file'");
		}
		file = new AbsFile((String) unwrap(file, String.class));
		super.unwrapPrimitives();
	}


}
