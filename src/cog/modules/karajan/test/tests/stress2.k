map(
	map:entry("name", "another stress test")
	map:entry("test",
		element([]
			element(bla, [a, b, c, d]
				if(
					a == list(b, c, d) 1 0
				)
			)
			
			sum(
				for(i, range(1, 20)
					parallelFor(j, range(1, 250)
						bla(
							parallel(
								list(1, 2, 3)
								1, 2, 3
							)
						)
					)
					echo(".", nl=false)
				)
			)
			echo()
		)
	)
	map:entry("expected-result", 20*250*1)
)
