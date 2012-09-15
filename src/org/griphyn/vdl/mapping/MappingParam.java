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


package org.griphyn.vdl.mapping;

import org.griphyn.vdl.type.Types;

/** The MappingParam class provides helper methods to deal with
  * parameters to mappers. The basic usage pattern is to
  * create a MappingParam class for each parameter of a particular
  * Mapper parameter, and then use various getTypeValue methods to
  * retrieve parameters in the mapper code.
  */
public class MappingParam {
    public static final MappingParam SWIFT_LINE = new MappingParam("swift#line");
    public static final MappingParam SWIFT_DBGNAME = new MappingParam("swift#dbgname");
    public static final MappingParam SWIFT_RESTARTID = new MappingParam("swift#restartid");
    public static final MappingParam SWIFT_WAITFOR = new MappingParam("swift#waitfor");
    public static final MappingParam SWIFT_BASEDIR = new MappingParam("swift#basedir");
    public static final MappingParam SWIFT_DESCRIPTOR = new MappingParam("swift#descriptor");
    public static final MappingParam SWIFT_INPUT = new MappingParam("swift#input");
    public static final MappingParam SWIFT_HANDLE = new MappingParam("swift#handle");
    
    
	private final String name;
	private Object defValue;
	private boolean defSet;

	/** Creates a mapper parameter with a default value to be used
	  * if none is specified in the SwiftScript program.
	  */
	public MappingParam(String name, Object defaultValue) {
		this.name = name;
		this.defValue = defaultValue;
		this.defSet = true;
	}

	/** Creates a mapper parameter with no default value. If no value
	  * is specified in the SwiftScript program for this parameter,
	  * then calls to the getValue methods will fail with an
	  * InvalidMappingParameterException.
	  */
	public MappingParam(String name) {
		this.name = name;
		this.defSet = false;
	}

	/** Returns the value of this parameter. If the value is a dataset,
	  * then the value will be converted to a string. If no value is
	  * specified in the SwiftScript program, then the default value
	  * will be returned.
	  */
	public Object getValue(Mapper mapper) {
		Object value = mapper.getParam(this);
		if (value instanceof AbstractDataNode) {
			AbstractDataNode handle = (AbstractDataNode) value;
			handle.waitFor();
			if (handle.getType().equals(Types.INT)) {
			    return Integer.valueOf(((Number) handle.getValue()).intValue());
			}
			else {
			    return handle.getValue().toString();
			}
		}
		else if (value == null) {
			if (!defSet) {
				throw new InvalidMappingParameterException("Missing required mapping parameter: "
						+ name);
			}
			else {
				return defValue;
			}
		}
		else {
			return value;
		}
	}
	
	public Object getValue(MappingParamSet params) {
        Object value = params.get(this);
        if (value instanceof AbstractDataNode) {
            AbstractDataNode handle = (AbstractDataNode) value;
            handle.waitFor();
            return handle.getValue().toString();
        }
        else if (value == null) {
            if (!defSet) {
                throw new InvalidMappingParameterException("Missing required mapping parameter: "
                        + name);
            }
            else {
                return defValue;
            }
        }
        else {
            return value;
        }
    }
	
	

	/** return the raw value of this parameter. Defaulting and type
	  * conversion will not occur. */
	public Object getRawValue(Mapper mapper) {
		return mapper.getParam(this);
	}

	/** Returns the mapper parameter as a String. Other data types will be
	    converted to a String as appropriate. */
	public String getStringValue(Mapper mapper) {
		Object value = getValue(mapper);
		if (value == null) {
			return null;
		}
		return String.valueOf(value);
	}
	
	public String getStringValue(MappingParamSet params) {
        Object value = getValue(params);
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

	public void setValue(Mapper mapper, Object value) {
		mapper.setParam(this, value);
	}

	public boolean isPresent(Mapper mapper) {
		return mapper.getParam(this) != null;
	}

	public boolean isPresent(MappingParamSet map) {
		return map.isPresent(this);
	}


	/** Returns the parameter value as a boolean. The native SwiftScript
	  * types for this parameter can be boolean (in which case that
	  * value is returned) or Strings "true" or "false". If none of these
	  * match, then 'false' is returned (note an exception is not thrown,
	  * unlike other getValue methods).
	  */
	public boolean getBooleanValue(Mapper mapper) {
		Object value = getValue(mapper);
		if (value instanceof String) {
			return Boolean.valueOf((String) value).booleanValue();
		}
		else if (value instanceof Boolean) {
			return ((Boolean) value).booleanValue();
		}
		else {
			return false;
		}
	}

	/** Returns the parameter value as an int. If the SwiftScript value 
	  * is a string, then the string will be parsed to an int. If the
	  * value is an int, then this will be passed through. Otherwise,
	  * a NumberFormatException will be thrown. */
	public int getIntValue(Mapper mapper) {
		Object value = getValue(mapper);
		if (value instanceof String) {
			return Integer.parseInt((String) value);
		}
		else if (value instanceof Integer) {
			return ((Integer) value).intValue();
		}
		else {
			throw new NumberFormatException(String.valueOf(value));
		}
	}

	public void setValue(MappingParamSet map, Object value) {
	    map.set(this, value);
	}

	public String toString() {
		return "mapping parameter " + name;
	}

    public String getName() {
        return name;
    }
}
