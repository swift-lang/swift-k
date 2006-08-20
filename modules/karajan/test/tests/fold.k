map(
	map:entry("name", "fold")
	map:entry("test",
		element([]
			set(s
				fold(element([x, s], list(x+first(s), 1+last(s))), list(0, 0)
					for(i, range(1, 10), i)
				)
			)
			
			list(
				first(s)
				last(s)
				first(s)/last(s)
			)

		)
	)
	map:entry("expected-result", list(55, 10, 5.5))
)
