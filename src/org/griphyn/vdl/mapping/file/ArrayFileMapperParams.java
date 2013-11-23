package org.griphyn.vdl.mapping.file;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.MappingParamSet;


public class ArrayFileMapperParams extends MappingParamSet {

	public static final List<String> NAMES = Arrays.asList("files");

	private Object files;

	@Override
	public Collection<String> getNames() {
		return NAMES;
	}

	public void setFiles(Object files) {
		this.files = files;
	}

	public Object getFiles() {
		return files;
	}

	@Override
	protected boolean set0(String name, Object value) {
		if (name.equals("files")) {
			this.files = value;
		}
		else {
			return super.set0(name, value);
		}
		return true;
	}

	@Override
	public AbstractDataNode getFirstOpen() {
		if (checkOpen(files)) {
			return (AbstractDataNode) files;
		}
		else {
			return super.getFirstOpen();
		}
	}

	@Override
	public void toString(StringBuilder sb) {
		addParam(sb, "files", files);
		super.toString(sb);
	}

	@Override
	public void unwrapPrimitives() {
		if (files == null) {
			throw new IllegalArgumentException("Missing required argument 'files'");
		}
		files = unwrap(files);
		super.unwrapPrimitives();
	}


}
