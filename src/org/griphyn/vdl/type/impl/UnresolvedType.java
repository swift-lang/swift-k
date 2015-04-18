/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Aug 13, 2007
 */
package org.griphyn.vdl.type.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.namespace.QName;

import org.griphyn.vdl.type.DuplicateFieldException;
import org.griphyn.vdl.type.Field;
import org.griphyn.vdl.type.Type;
import org.griphyn.vdl.type.Types;

public class UnresolvedType implements Type {
	private String name;
	private URI namespaceURI;
	
	public UnresolvedType(String namespace, String name) {
		this.setNamespace(namespace);
		this.name = name;
	}
	
	public UnresolvedType(URI namespace, String name) {
		this.setNamespace(namespace);
		this.name = name;
	}
	
	public UnresolvedType(String name) {
		this.name = name;
	}

	public void addField(Field field) throws DuplicateFieldException {
		throw new UnsupportedOperationException();
	}

	public void addField(String name, Type type) throws DuplicateFieldException {
		throw new UnsupportedOperationException();
	}

	public Type arrayType() {
		throw new UnsupportedOperationException();
	}
	
	public Type arrayType(Type keyType) {
        throw new UnsupportedOperationException();
    }
	
	public Type itemType() {
		throw new UnsupportedOperationException();
	}
	
	public Type keyType() {
	    throw new UnsupportedOperationException();
	}

	public Type getBaseType() {
		throw new UnsupportedOperationException();
	}

	public Field getField(String name) throws NoSuchFieldException {
		throw new UnsupportedOperationException();
	}

	public List getFieldNames() {
		throw new UnsupportedOperationException();
	}

	public List getFields() {
		throw new UnsupportedOperationException("addField");
	}
	
	@Override
    public int getFieldIndex(String name) throws NoSuchFieldException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasMappedComponents() {
        throw new UnsupportedOperationException();
    }
	
	@Override
    public boolean hasArrayComponents() {
        throw new UnsupportedOperationException();
    }

    public String getName() {
		return name;
	}

	public String getNamespace() {
		return namespaceURI.toString();
	}

	public URI getNamespaceURI() {
		return namespaceURI;
	}

	public QName getQName() {
		return new QName(namespaceURI.toString(), name);
	}

	public boolean isPrimitive() {
		throw new UnsupportedOperationException();
	}

	public boolean isArray() {
		return this.name.endsWith("[]");
	}
	
	public boolean isComposite() {
        return isArray();
    }

    public void setBaseType(Type base) {
		throw new UnsupportedOperationException();
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNamespace(String namespace) {
		if (namespace != null) {
			try {
				this.namespaceURI = new URI(namespace);
			}
			catch (URISyntaxException e) {
				throw new IllegalArgumentException(
						"The supplied namespace is not a valid URI string");
			}
		}
		else {
			this.namespaceURI = null;
		}
	}

	public void setNamespace(URI namespace) {
		this.namespaceURI = namespace;
	}

	public void setPrimitive() {
		throw new UnsupportedOperationException();
	}

	public void setQName(QName name) {
		this.name = name.getLocalPart();
		try {
			this.namespaceURI = new URI(name.getNamespaceURI());
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException("The supplied namespace is not a valid URI string");
		}
	}
	
	public boolean equals(Object other) {
		if (other instanceof Type) {
			Type ot = (Type) other;
			URI ons = ot.getNamespaceURI();
			if ((namespaceURI == null || ons == null) && namespaceURI != ons) {
				return false;   
			}
			return ot.getName().equals(name);
		}
		else if (other instanceof String) {
		    throw new RuntimeException("Internal error. Testing type equality with a string.");
		}
		else {
			return false;
		}
	}

	public int hashCode() {
		return name.hashCode();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (namespaceURI != null) {
			sb.append(namespaceURI.toString());
			sb.append(':');
		}
		sb.append(name);
		return sb.toString();
	}

    @Override
    public boolean canBeAssignedTo(Type type) {
        if (type.equals(Types.ANY)) {
            return true;
        }
        else {
            return this.equals(type);
        }
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        if (this.equals(Types.ANY)) {
            return true;
        }
        else {
            return this.equals(type);
        }
    }
}
