map(
	map:entry("name", "execution")
	map:entry("description", "Execution tests")
	map:entry("code",
		element([]
			suite:singleHost("execution")
		)
	)
)
