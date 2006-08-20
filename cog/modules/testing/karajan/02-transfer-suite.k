map(
	map:entry("name", "transfer")
	map:entry("description", "File transfer tests")
	map:entry("code",
		element([]
			suite:twoHost("transfer")
		)
	)
)
