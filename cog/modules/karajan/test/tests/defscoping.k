map(
	map:entry("name", "definition scoping")
	map:entry("test",
		element([]
			element(a, []
				element(b, [], "b")
				element([]
					//this is the b() above
					b()
				)
			)
			element(b, [], "!b")
			//define as c what a() returns which
			//is the lambda that calls the b() defined
			//inside a()
			define(c, a())
			c()
		)
	)
	map:entry("expected-result", "b")
)