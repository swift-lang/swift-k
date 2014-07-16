package org.griphyn.vdl.mapping.file;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.MappingParamSet;
import org.griphyn.vdl.mapping.DSHandle;


public class CSVMapperParams extends MappingParamSet {

	public static final List<String> NAMES = Arrays.asList("file", "header", "skip", "hdelim", "delim");

	private Object file = null;
	private Object header = true;
	private Object skip = 0;
	private Object hdelim = null;
	private Object delim = " \t,";

	@Override
	public Collection<String> getNames() {
		return NAMES;
	}

	public void setFile(DSHandle file) {
		this.file = file;
	}

	public DSHandle getFile() {
		return (DSHandle) file;
	}

	public void setHeader(Boolean header) {
		this.header = header;
	}

	public Boolean getHeader() {
		return (Boolean) header;
	}

	public void setSkip(Integer skip) {
		this.skip = skip;
	}

	public Integer getSkip() {
		return (Integer) skip;
	}

	public void setHdelim(String hdelim) {
		this.hdelim = hdelim;
	}

	public String getHdelim() {
		return (String) hdelim;
	}

	public void setDelim(String delim) {
		this.delim = delim;
	}

	public String getDelim() {
		return (String) delim;
	}

	@Override
	protected boolean set0(String name, Object value) {
		if (name.equals("file")) {
			this.file = value;
		}
		else if (name.equals("header")) {
			this.header = value;
		}
		else if (name.equals("skip")) {
			this.skip = value;
		}
		else if (name.equals("hdelim")) {
			this.hdelim = value;
		}
		else if (name.equals("delim")) {
			this.delim = value;
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
		else if (checkOpen(header)) {
			return (AbstractDataNode) header;
		}
		else if (checkOpen(skip)) {
			return (AbstractDataNode) skip;
		}
		else if (checkOpen(hdelim)) {
			return (AbstractDataNode) hdelim;
		}
		else if (checkOpen(delim)) {
			return (AbstractDataNode) delim;
		}
		else {
			return super.getFirstOpen();
		}
	}

	@Override
	public void toString(StringBuilder sb) {
		addParam(sb, "file", file);
		addParam(sb, "header", header);
		addParam(sb, "skip", skip);
		addParam(sb, "hdelim", hdelim);
		addParam(sb, "delim", delim);
		super.toString(sb);
	}

	@Override
	public void unwrapPrimitives() {
		header = unwrap(header, Boolean.class);
		skip = unwrap(skip, Integer.class);
		hdelim = unwrap(hdelim, String.class);
		delim = unwrap(delim, String.class);
		super.unwrapPrimitives();
	}


}
