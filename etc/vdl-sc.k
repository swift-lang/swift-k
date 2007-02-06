import("sys.k")

namespace("vdl"
	export(siteCatalog
		element([file]
	
			import("task.k")
							
			element(pool, [handle, optional(gridlaunch), sysinfo, optional(lrc), ..., channel(properties)]
				host(name = handle
					each(...)
					to(properties, each(properties))
				)
			)
				
			element(lrc, [url]
				//not handled yet
			)
				
			element(gridftp, [url, storage, major, minor, patch]
				if(
					url == "local://localhost" 
						service(type="file", provider="local")
					service(type="file", provider="gsiftp", url=url)
				)
			)
				
			element(jobmanager, [universe, url, major, minor, patch]
				provider := if(
					major == "4" "GT4"
					major == "2" "GT2"
					url == "local://localhost" "local"
					throw("Unknown job manager version: major = {major}, minor = {minor}, patch = {patch}")
				)
				service(type="execution", provider=provider, url=url)
			)
			
			element(profile, [namespace, key, value]
				property("{namespace}::{key}", value)
			)
				
			element(workdirectory, [dir]
				property(name="workdir", value = dir)
			)
			
			resources(
				executeFile(file)
			)
		)
	)
)