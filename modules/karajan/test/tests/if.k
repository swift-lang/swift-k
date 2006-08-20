map(
	map:entry("name", "if")
	map:entry("test",
		element([]
			element(ift, [v]
				if(
					v == 0 "0"
					v == 1 "1"
					v == 2 "2"
					v == 3 "3"
					"?"
				)
			)
			list(
				ift(5), ift(0), ift(1), ift(2), ift(3), ift(4)
			)
		)
	)
	map:entry("expected-result", ["?", "0", "1", "2", "3", "?"])
)
