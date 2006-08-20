map(
	map:entry("name", "channels 2")
	map:entry("test"
		element([]
			list(
				channel:from(list
					set(one,
						channel:from(one
							set(two,
								channel:from(two
									channel:to(one, 1)
									channel:to(two, 2)
								)
							)
							channel:to(list, two)
						)
					)
					channel:to(list, one)
				)
			)
		)
	)
	map:entry("expected-result", list(2, 1))
	map:entry("comparison", math_comparison)
)