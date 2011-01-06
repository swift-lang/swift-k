map(
	map:entry(key = "name", "channels 1")
	map:entry(key = "test",
		element([]
			set(value,
				channel:from(channel,
					channel:to(channel,
						"test"
					)
				)
			)
			
			value
		)
	)
	map:entry(key = "expected-result", "test")
	map:entry(key = "comparison", default_comparison)
)
