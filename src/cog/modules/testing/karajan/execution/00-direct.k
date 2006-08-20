test("Execution: direct", ["gt4.0.0", "gt2", "gt3.0.2", "gt3.2.0", "gt3.2.1"], []
	list(
		element([host, provider]
			testWrapper(host = host, provider = provider, type = "execution"
				test = element([host, provider]
					task:execute("/bin/date", host=host, provider=provider)
				)
			)
		)
	)
)