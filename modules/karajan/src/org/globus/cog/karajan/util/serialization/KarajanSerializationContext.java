// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jan 12, 2005
 */
package org.globus.cog.karajan.util.serialization;

import java.util.HashMap;
import java.util.Map;

import org.globus.cog.karajan.workflow.ElementTree;
import org.globus.cog.karajan.workflow.nodes.FlowElement;

import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.converters.ConverterLookup;

public class KarajanSerializationContext {
	private ElementTree tree;
	private boolean source;
	private FlowElement parent;
	private ClassMapper classMapper;
	private String fileName;
	private ConverterLookup converterLookup;
	private Map sourceElements;
	private boolean kmode;
	private boolean detachedSource;
	private ElementMarshallingPolicy elementMarshallingPolicy;
	
	public KarajanSerializationContext() {
		this(new ElementTree());
	}

	public KarajanSerializationContext(ElementTree tree) {
		this.tree = tree;
		source = true;
		sourceElements = new HashMap();
	}

	public boolean isSource() {
		return source;
	}

	public void setSource(boolean source) {
		this.source = source;
	}

	public void setParent(FlowElement node) {
		this.parent = node;
	}

	public FlowElement getParent() {
		return parent;
	}

	public ClassMapper getClassMapper() {
		return classMapper;
	}

	public void setClassMapper(ClassMapper classMapper) {
		this.classMapper = classMapper;
	}

	public void setFileName(String project) {
		this.fileName = project;
	}

	public String getFileName() {
		return fileName;
	}

	public ConverterLookup getConverterLookup() {
		return converterLookup;
	}

	public void setConverterLookup(ConverterLookup converterLookup) {
		this.converterLookup = converterLookup;
	}

	public Map getSourceElements() {
		return sourceElements;
	}

	public void setSourceElements(Map sourceElements) {
		this.sourceElements = sourceElements;
	}

	public void setKmode(boolean lineNumbers) {
		this.kmode = lineNumbers;
	}

	public boolean isKmode() {
		return kmode;
	}

	public void setDetachedSource(boolean b) {
		this.detachedSource = b;
	}

	public boolean getDetachedSource() {
		return detachedSource;
	}

	public ElementTree getTree() {
		return tree;
	}

	public ElementMarshallingPolicy getElementMarshallingPolicy() {
		if (elementMarshallingPolicy == null) {
			if (source) {
				setElementMarshallingPolicy(new SourceElementMarshallingPolicy());
			}
			else {
				setElementMarshallingPolicy(new StateElementMarshallingPolicy());
			}
		}
		return elementMarshallingPolicy;
	}

	public void setElementMarshallingPolicy(ElementMarshallingPolicy elementMarshallingPolicy) {
		this.elementMarshallingPolicy = elementMarshallingPolicy;
		elementMarshallingPolicy.setKContext(this);
	}
}