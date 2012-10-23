import("sys.k")
import("task.k")

							
element(pool, [handle, optional(gridlaunch), sysinfo, optional(lrc), ..., channel(properties)]
	list(handle, each(...))
)
				
element(lrc, [url]
)
				
element(gridftp, [url, storage, major, minor, patch]
	if(
		url == "local://localhost" 
		list("file", "local", "")
		list("file", "gsiftp", url)
	)
)
				
element(jobmanager, [universe, url, major, minor, patch]
	provider := if(
		major == "4" "GT4"
		major == "2" "GT2"
		url == "local://localhost" "local"
		throw("Unknown job manager version: major = {major}, minor = {minor}, patch = {patch}")
	)
	list("execution", provider, url)
)
			
element(profile, [namespace, key, value]
)
				
element(workdirectory, [dir]
)

element(forkify, [url]
	concat(first(split(url, "/")), "/jobmanager-fork")
)
		
sitesFile := try(first(...), throw("Usage cog-workflow checksites.k <filename>"))

sites := list(executeFile(sitesFile))

for(site, sites
	
	name := first(site)
	services := butFirst(site)
	
	print("Checking {name}... ")
	
	for(service, services
		[type, provider, url] := each(service)
	
		print("    {provider}/{type}: ", nl=false)
	
		race(
			try(
				sequential(
					if(
						type == "file" discard(file:exists("blabla", host=url, provider=provider))
						type == "execution" execute("/bin/date", provider=provider, host=forkify(url))
					)
					print("[OK]")
				)
				print(exception)
			)
			sequential(
				wait(delay=10000)
				print("[timeout]")
			)
		)
	)
)

print("All done")