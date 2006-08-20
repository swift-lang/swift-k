map(
	map:entry("name", "futures")
	map:entry("test",
		element([]
			element(gen, []
				for(i, range(0, 10), i)
			)
			
			element(con, [values]
				sum(
					for(i, values,  i)
				)
			)
			
			element(delayed, []
				wait(delay=50)
				"Bla"
			)
			
			set(values, futureIterator(gen()))
			set(bla, future(delayed()))
			
			list(
				con(values)
				bla
			)
		)
	)
	map:entry("expected-result", list(55, "Bla"))
)
