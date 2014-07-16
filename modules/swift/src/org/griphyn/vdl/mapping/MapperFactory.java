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
 * Created on Jun 30, 2006
 */
package org.griphyn.vdl.mapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.griphyn.vdl.mapping.file.AirsnMapper;
import org.griphyn.vdl.mapping.file.ArrayFileMapper;
import org.griphyn.vdl.mapping.file.CSVMapper;
import org.griphyn.vdl.mapping.file.ConcurrentMapper;
import org.griphyn.vdl.mapping.file.ExternalMapper;
import org.griphyn.vdl.mapping.file.FileSystemArrayMapper;
import org.griphyn.vdl.mapping.file.FixedArrayFileMapper;
import org.griphyn.vdl.mapping.file.ROIMapper;
import org.griphyn.vdl.mapping.file.RegularExpressionMapper;
import org.griphyn.vdl.mapping.file.SimpleFileMapper;
import org.griphyn.vdl.mapping.file.SingleFileMapper;
import org.griphyn.vdl.mapping.file.StructuredRegularExpressionMapper;
import org.griphyn.vdl.mapping.file.TestMapper;

public class MapperFactory {
	private static Map<String, Class<? extends Mapper>> mappers;
	
	private static Map<String, Set<String>> validParams;
	private static Set<String> deprecated;
	private static Set<String> warned;

	static {
	    mappers = new HashMap<String, Class<? extends Mapper>>();
	    deprecated = new HashSet<String>();
	    validParams = new HashMap<String, Set<String>>();
	    warned = new HashSet<String>();
	    
		// the following are general purpose file mappers
		registerMapper("SimpleMapper", "simple_mapper", SimpleFileMapper.class);
		registerMapper("SingleFileMapper", "single_file_mapper", SingleFileMapper.class);
		registerMapper("FixedArrayMapper", "fixed_array_mapper", FixedArrayFileMapper.class);
		registerMapper("ConcurrentMapper", "concurrent_mapper", ConcurrentMapper.class);
		registerMapper("FilesysMapper", "filesys_mapper", FileSystemArrayMapper.class);
		registerMapper("RegexpMapper", "regexp_mapper", RegularExpressionMapper.class);
		registerMapper("StructuredRegexpMapper", "structured_regexp_mapper",
			StructuredRegularExpressionMapper.class);
		registerMapper("CSVMapper", "csv_mapper", CSVMapper.class);
		registerMapper("ArrayMapper", "array_mapper", ArrayFileMapper.class);

		// the following are application-specific mappers
		registerMapper("AIRSNMapper", "airsn_mapper", AirsnMapper.class);
		registerMapper("ROIMapper", "roi_mapper", ROIMapper.class);
		registerMapper("Ext", "ext", ExternalMapper.class);
		registerMapper("TestMapper", "test_mapper", TestMapper.class);
	}

	public synchronized static Mapper getMapper(String type) throws InvalidMapperException {
		Class<? extends Mapper> cls = mappers.get(type);
		if (cls == null) {
			throw new InvalidMapperException("No such mapper: "+type);
		}
		try {
			return cls.newInstance();
		}
		catch (Exception e) {
			throw new InvalidMapperException(type + ": " + e.getMessage(), e);
		}
	}
	
	public synchronized static boolean isValidMapperType(String type) {
	    return mappers.containsKey(type);
	}
	
    @SuppressWarnings("unchecked")
    public static void registerMapper(String type, String deprecatedType, String cls) throws ClassNotFoundException {
	    registerMapper(type, deprecatedType, 
	        (Class<? extends Mapper>) MapperFactory.class.getClassLoader().loadClass(cls));
	}

    public synchronized static void registerMapper(String type, String deprecatedType, Class<? extends Mapper> cls) {
		mappers.put(type, cls);
		if (deprecatedType != null) {
		    deprecated.add(deprecatedType);
		    mappers.put(deprecatedType, cls);
		}
		try {
		    Mapper m = cls.newInstance();
		    Set<String> params = m.getSupportedParamNames();
		    validParams.put(type, params);
		    if (deprecatedType != null) {
		        validParams.put(deprecatedType, params);
		    }
		}
		catch (Exception e) {
		    throw new RuntimeException("Cannot instantiate a '" + type + "'", e);
		}
	}

    
    public static Set<String> getValidParams(String type) {
        return validParams.get(type);
    }
    
    public static void main(String[] args) {
        for (String name : mappers.keySet()) {
            try {
                Mapper m = getMapper(name);
                if (!m.isStatic()) {
                    System.out.println(name);
                }
            }
            catch (InvalidMapperException e) {
                e.printStackTrace();
            }
        }
    }
}
