map(
	map:entry("name", "try/catch")
	map:entry("test",
		element([]
			list(
				try(
					(
						throw("Kaboom")
						"uncaught"
					)
					catch(".*"
						"caught"
					)
				)
				"alldone"
			)
		)
	)
	map:entry("expected-result", ["caught", "alldone"])
)