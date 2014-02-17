import(sys)
import(task)

export(GLOBAL_PASS_COUNT, 10)
export(TEST_PASS_COUNT, 10)

export(GRID_RESOURCES,
	list(
		host("plussed.mcs.anl.gov:4020", cpus=2
			service("execution", provider="gt2")
			service("file", provider="gt2")
		)
	)
)
export(DEFAULT_PROVIDER, "gt2")
export(TASK_RUN_COUNT, 4)
