map(
	map:entry("name", "maybe")
	map:entry("test",
		element([]
			element(a, [a, b, optional(c, d)]
				list(a, b, maybe(c), maybe(d))
			)
			
			element(b, [a, b, optional(c, d)]
				a(a = a, b = b, maybe(c = c), maybe(d = d))
			)
			
			list(
				b(1, 2)
				b(a = 1, b = 2)
				b(1, 2, c = 3)
				b(a = 1, 2, c = 3, d = 4)
				b(1, 2, d = 4)
				list(maybe(2, 3, noway))
				list(maybe(2, 3, 4))
			)
		)
	)
	map:entry("expected-result"
		list(
			list(1, 2)
			list(1, 2)
			list(1, 2, 3)
			list(1, 2, 3, 4)
			list(1, 2, 4)
			list()
			list(2, 3, 4)
		)
	)
)
