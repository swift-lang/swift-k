map(
	map:entry("name", "scheduled execution - allocateHost")
	map:entry("test",
		element([]
			scheduler("default"
				property("jobsPerCpu", 1)

				resources(
					for(res, grid_resources, res)
				)
				
				handler("execution", default_provider)
				handler("file", default_provider)
			)
			
			parallelFor(run, range(1, task_run_count)
				allocateHost(host, 
					task:execute(executable="/bin/date", host = host)
				)
				print(run, nl=false())
			)
		)
	)
)
