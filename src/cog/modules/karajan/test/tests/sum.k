map(
	map:entry("name", "sum")
	map:entry("test",
		element([]
			sum(1, 2, 3)
		)
	)
	map:entry("expected-result", 6)
)
