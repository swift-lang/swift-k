/*
 * Created on Jun 30, 2006
 */
package org.griphyn.vdl.mapping;

import java.util.HashMap;
import java.util.Map;

import org.griphyn.vdl.mapping.file.AirsnMapper;
import org.griphyn.vdl.mapping.file.ConcurrentMapper;
import org.griphyn.vdl.mapping.file.FixedArrayFileMapper;
import org.griphyn.vdl.mapping.file.FixedFileMapper;
import org.griphyn.vdl.mapping.file.ROIMapper;
import org.griphyn.vdl.mapping.file.SimpleFileMapper;

public class MapperFactory {
	private static Map mappers = new HashMap();

	static {
		registerMapper("simple_mapper", SimpleFileMapper.class);
		registerMapper("vdl:fixed_mapper", FixedFileMapper.class);
		registerMapper("fixed_mapper", FixedFileMapper.class);
		registerMapper("array_mapper", FixedArrayFileMapper.class);
        registerMapper("temp_mapper", SimpleFileMapper.class);
        registerMapper("concurrent_mapper", ConcurrentMapper.class);
        registerMapper("airsn_mapper", AirsnMapper.class);
        registerMapper("roi_mapper", ROIMapper.class);
	}

	public synchronized static Mapper getMapper(String type, Map params) throws InvalidMapperException {
		Class cls = (Class) mappers.get(type);
		if (cls == null) {
			throw new InvalidMapperException(type);
		}
		try {
			Mapper mapper = (Mapper) cls.newInstance();
			mapper.setParams(params);
            return mapper;
		}
		catch (Exception e) {
			throw new InvalidMapperException(type, e);
		}
	}

	public static void registerMapper(String type, String cls) throws ClassNotFoundException {
		registerMapper(type, MapperFactory.class.getClassLoader().loadClass(cls));
	}

	public synchronized static void registerMapper(String type, Class cls) {
		mappers.put(type, cls);
	}

}
