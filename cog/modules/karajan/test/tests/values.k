map(
	map:entry("name", "values")
	map:entry("test",
		element([]
			list(
				1 == 1.0
				1.0 == 1.00
				0.1 == 0.100
				0.1 == 1/10
			)
		)
	)
	map:entry("expected-result", [true(), true(), true(), true()])
)
