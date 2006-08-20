map(
	map:entry("name", "arguments order")
	map:entry("test",
		element([]
			list(
				parallel(
					(wait(delay = 100), 1)
					(wait(delay = 150),	2)
					(wait(delay = 10),  3)
					
					4
				)
			)
		)
	)
	map:entry("expected-result", [1, 2, 3, 4])
	map:entry("comparison", math_comparison)
)