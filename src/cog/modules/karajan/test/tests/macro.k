map(
	map:entry("name", "macro")
	map:entry("test",
		element([]
			define(for1to10,
				macro(
					set(ivar, quoted(executeElement(list:first(...))))
					set(body, list:butFirst(...))
				
					for(unquoted(ivar), range(1, 10)
						for(_, body, executeElement(_))
					)
				)
			)
			
			sum(
				for1to10(i, i+1) //2 + 3 + ... + 11 = 65
			)
		)
	)
	map:entry("expected-result", 65)
)
