map(
	map:entry("name", "parallel for")
	map:entry("test",
		element([]
			list(
				sum(parallelfor(i, range(0, 6), 1))
				sum(parallelfor(i, range(0, 7), 1, 1))
				sum(parallelfor(i, range(0, 8), 1, 1, 1))
				r := range(0, 9)
				sum(parallelfor(i, r, 1, 1, 1, 1))
				r := range(1, 10)
				sum(parallelfor(i, r, i))
				
				choice(
					sequential(
						parallelfor(i, range(0, 10)
							if(
								i == 5 then(generateError("Kaboom"))
							)
						)
						"No failure"
					)
					
					"Yes failure"
				)
			)
		)
	)
	map:entry("expected-result", list(7, 16, 27, 40, 55, "Yes failure"))
)
