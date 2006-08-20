import("sys.k")
import("task.k")

global(global_pass_count, 1)
global(test_pass_count, 10)

global(grid_resources
	list(
		host("plussed.mcs.anl.gov:4010", cpus=2
			service("execution", provider="gt2")
			service("file", provider="gt2")
		)
	)
)
global(default_provider, "gt2")
global(task_run_count, 4)
