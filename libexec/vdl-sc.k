import("sys.k")

namespace("vdl"
	export(siteCatalog
		element([file]
	
			import("task.k")
							
			element(pool, [handle, optional(gridlaunch), optional(sysinfo), optional(lrc), ..., channel(properties)]
				host(name = handle
					each(...)
					to(properties
						each(properties)
						maybe(property("gridlaunch", gridlaunch))
					)
				)
			)
				
			element(gridftp, [url, optional(storage), optional(major), optional(minor), optional(patch)]
				if(
					url == "local://localhost" 
						service(type="file", provider="local")
					service(type="file", provider="gsiftp", url=url)
				)
			)
				
			element(jobmanager, [url, major, optional(universe), optional(minor), optional(patch)]
				provider := if(
					url == "local://localhost" "local"
					url == "pbs://localhost" "pbs"
					major == "4" "GT4"
					major == "2" "GT2"
					throw("Unknown job manager version: major = {major}, minor = {minor}, patch = {patch}, url = {url}")
				)
				service(type="execution", provider=provider, url=url)
			)
			
			element(execution, [provider, url, optional(jobManager)]
				service(type="execution", provider=provider, maybe(url=url), maybe(jobManager=jobManager))
			)
			
			element(filesystem, [provider, url, optional(storage)]
				service(type="file", provider=provider, url=url)
			)
			
			element(profile, [namespace, key, value]
				if(
					namespace == "karajan"
					property("{key}", value)
					property("{namespace}:{key}", value)
				)
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
