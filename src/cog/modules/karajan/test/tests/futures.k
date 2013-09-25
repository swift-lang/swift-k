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
			
			a := future(wait(delay=20), 1)
			b := future(wait(delay=40), 2)
			
			list(
				max(a, b)
				con(values)
				bla
			)
		)
	)
	map:entry("expected-result", list(2, 55, "Bla"))
)
