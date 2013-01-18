map(
	map:entry("name", "fileops")
	map:entry("description", "File operations tests")
	map:entry("code",
		element([]
			suite:singleHost("fileops")
		)
	)
)
