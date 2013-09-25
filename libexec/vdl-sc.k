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
						maybe(property("sysinfo", sysinfo))
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
			
			element(env, [name, value]
				property("env:{name}", value)
			)

			element(workdirectory, [dir]
				property("workdir", dir)
			)
			
			element(scratch, [dir]
				property("scratch", dir)
			)
			
			element(p, [name, value]
				print("	{name} = {value}")
				property(name, value)
			)
			
			element(intrepidCoasterParams, [optional(averageJobTime, blockSize)]
				print("Setting Intrepid coaster params:")
				p("scratch", "/scratch")
				p("jobsPerNode", "4")
				p("kernelprofile", "zeptoos")
				p("alcfbgpnat", "true")
				maybe(
					discard(averageJobTime)
					blockSize := try(blockSize, 512)
					parallelism := averageJobTime * 100	
					p("slots", math:round(parallelism / (blockSize * 4)))
					p("nodeGranularity", blockSize)
					p("maxNodes", blockSize)
					jt := parallelism * 2
					p("jobThrottle", jt / 100)
					print("For best results, please set the foreach.max.threads property in swift.properties to {jt}") 
				)
			)
			
			element(surveyorCoasterParams, [optional(averageJobTime, blockSize)]
				print("Setting Surveyor coaster params:")
				p("scratch", "/scratch")
				p("jobsPerNode", "4")
				p("kernelprofile", "zeptoos")
				p("alcfbgpnat", "true")
				maybe(
					discard(averageJobTime)
					blockSize := try(blockSize, 64)
					parallelism := averageJobTime * 100	
					p("slots", 12)
					p("nodeGranularity", blockSize)
					p("maxTime", 3500)
					jt := parallelism * 2
					p("jobThrottle", jt / 100)
					print("For best results, please set the foreach.max.threads property in swift.properties to {jt}") 
				)
			)

			resources(
				executeFile(file)
			)
		)
	)
)
