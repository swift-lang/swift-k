/*
 * Created on Jun 30, 2006
 */
package org.griphyn.vdl.mapping;

import java.util.HashMap;
import java.util.Map;

import org.griphyn.vdl.mapping.file.AirsnMapper;
import org.griphyn.vdl.mapping.file.CSVMapper;
import org.griphyn.vdl.mapping.file.ConcurrentMapper;
import org.griphyn.vdl.mapping.file.FileSystemArrayMapper;
import org.griphyn.vdl.mapping.file.FixedArrayFileMapper;
import org.griphyn.vdl.mapping.file.SingleFileMapper;
import org.griphyn.vdl.mapping.file.ROIMapper;
import org.griphyn.vdl.mapping.file.RegularExpressionMapper;
import org.griphyn.vdl.mapping.file.SimpleFileMapper;

public class MapperFactory {
	private static Map mappers = new HashMap();

	static {

		// the following are general purpose file mappers
		registerMapper("simple_mapper", SimpleFileMapper.class);
		registerMapper("single_file_mapper", SingleFileMapper.class);
		registerMapper("fixed_array_mapper", FixedArrayFileMapper.class);
		registerMapper("concurrent_mapper", ConcurrentMapper.class);
		registerMapper("filesys_mapper", FileSystemArrayMapper.class);
		registerMapper("regexp_mapper", RegularExpressionMapper.class);
		registerMapper("csv_mapper", CSVMapper.class);

		// the following are application-specific mappers
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
		catch (InvalidMappingParameterException e) {
			throw new InvalidMapperException(type + ": " + e.getMessage(), e);
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
