map(
	map:entry("name", "import")
	map:entry("test",
		element([]
			import(
				parallel(
					export(one, element([], 1))
					export(two, element([], 2))
				)
			)
			
			one() + two()
		)
	)
	map:entry("expected-result", 3)
)
