package org.griphyn.vdl.mapping.file;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.MappingParamSet;


public class AbstractFileMapperParams extends MappingParamSet {

	public static final List<String> NAMES = Arrays.asList("prefix", "suffix", "pattern", "location");

	private Object prefix = null;
	private Object suffix = null;
	private Object pattern = null;
	private Object location = null;

	@Override
	public Collection<String> getNames() {
		return NAMES;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix() {
		return (String) prefix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getSuffix() {
		return (String) suffix;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getPattern() {
		return (String) pattern;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getLocation() {
		return (String) location;
	}

	@Override
	protected boolean set0(String name, Object value) {
		if (name.equals("prefix")) {
			this.prefix = value;
		}
		else if (name.equals("suffix")) {
			this.suffix = value;
		}
		else if (name.equals("pattern")) {
			this.pattern = value;
		}
		else if (name.equals("location")) {
			this.location = value;
		}
		else {
			return super.set0(name, value);
		}
		return true;
	}

	@Override
	public AbstractDataNode getFirstOpen() {
		if (checkOpen(prefix)) {
			return (AbstractDataNode) prefix;
		}
		else if (checkOpen(suffix)) {
			return (AbstractDataNode) suffix;
		}
		else if (checkOpen(pattern)) {
			return (AbstractDataNode) pattern;
		}
		else if (checkOpen(location)) {
			return (AbstractDataNode) location;
		}
		else {
			return super.getFirstOpen();
		}
	}

	@Override
	public void toString(StringBuilder sb) {
		addParam(sb, "prefix", prefix);
		addParam(sb, "suffix", suffix);
		addParam(sb, "pattern", pattern);
		addParam(sb, "location", location);
		super.toString(sb);
	}

	@Override
	public void unwrapPrimitives() {
		prefix = unwrap(prefix, String.class);
		suffix = unwrap(suffix, String.class);
		pattern = unwrap(pattern, String.class);
		location = unwrap(location, String.class);
		super.unwrapPrimitives();
	}


}
