/*
 * Created on Jun 30, 2006
 */
package org.griphyn.vdl.mapping;

import java.util.HashMap;
import java.util.Map;

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

public class MapperFactory {
	private static Map<String,Class<Mapper>> mappers = 
	    new HashMap<String,Class<Mapper>>();

	static {

		// the following are general purpose file mappers
		registerMapper("simple_mapper", SimpleFileMapper.class);
		registerMapper("single_file_mapper", SingleFileMapper.class);
		registerMapper("fixed_array_mapper", FixedArrayFileMapper.class);
		registerMapper("concurrent_mapper", ConcurrentMapper.class);
		registerMapper("filesys_mapper", FileSystemArrayMapper.class);
		registerMapper("regexp_mapper", RegularExpressionMapper.class);
		registerMapper("structured_regexp_mapper",
			StructuredRegularExpressionMapper.class);
		registerMapper("csv_mapper", CSVMapper.class);
		registerMapper("array_mapper", ArrayFileMapper.class);

		// the following are application-specific mappers
		registerMapper("airsn_mapper", AirsnMapper.class);
		registerMapper("roi_mapper", ROIMapper.class);
		registerMapper("ext", ExternalMapper.class);
	}

	public synchronized static Mapper getMapper(String type, 
	                                            Map<String,Object> params) 
	throws InvalidMapperException {
		Class<Mapper> cls = mappers.get(type);
		if (cls == null) {
			throw new InvalidMapperException("no such mapper: "+type);
		}
		try {
			Mapper mapper = cls.newInstance();
			mapper.setParams(params);
			return mapper;
		}
		catch (Exception e) {
			throw new InvalidMapperException(type + ": " + e.getMessage(), e);
		}
	}

    public static void registerMapper(String type, String cls) 
	throws ClassNotFoundException {
	    registerMapper(type, MapperFactory.class.getClassLoader().loadClass(cls));
	}

	@SuppressWarnings("unchecked")
    public synchronized static void registerMapper(String type, 
	                                               Class<?> cls) {
		mappers.put(type, (Class<Mapper>) cls);
	}
}
