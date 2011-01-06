map(
	map:entry("name", "parallel element")
	map:entry("test",
		element([]
			element(gen, []
				for(i, range(0, 10), i)
			)
			parallelElement(con, [test, ...]
				test
				sum(
					for(i, ..., i)
				)
			)
			
			list(
				con("Bla", gen())
			)
		)
	)
	map:entry("expected-result", list("Bla", 55))
)
