import("sys.k")

export(siteCatalog
	element([file]

		import("task.k")
						
		element(pool, [handle, optional(gridlaunch), sysinfo, optional(lrc), ..., channel(properties)]
			print("Adding host {handle}")
			host(name = handle
				each(...)
				to(properties, each(properties))
			)
		)
			
		element(lrc, [url]
			//not handled yet
		)
			
		element(gridftp, [url, storage, major, minor, patch]
			print("Adding file service {url}")
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
				generateError("Unknown job manager version: major = {major}, minor = {minor}, patch = {patch}")
			)
			print("Adding {provider} execution service {url}")
			service(type="execution", provider=provider, url=url)
		)
			
		element(workdirectory, [dir]
			property(name="workdir", value = dir)
		)
		
		resources(	
			executeFile(file)
		)
	)
)
