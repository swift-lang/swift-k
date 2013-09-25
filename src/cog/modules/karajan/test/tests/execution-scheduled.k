map(
	map:entry("name", "scheduled execution")
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
				task:execute(executable="/bin/date")
				print(run, nl=false())
			)
		)
	)
)
