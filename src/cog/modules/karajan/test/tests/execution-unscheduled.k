map(
	map:entry("name", "unscheduled execution")
	map:entry("test",
		element([]
			parallelFor(run, range(1, task_run_count)
				for(res, grid_resources,
					task:execute(executable="/bin/date", host=res, provider=default_provider)
				)
				print(run, nl=false())
			)
		)
	)
)
