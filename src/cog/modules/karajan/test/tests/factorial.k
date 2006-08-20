map(
	map:entry("name", "factorial")
	map:entry("test",
		element([]
			element(factorial, [n],
				if(
					n <= 1 then(1)
					else(
						n * factorial(n-1)
					)
				)
			)
			
			factorial(10)
		)
	)
	map:entry("expected-result", 3628800)
	map:entry("comparison", math_comparison)
)