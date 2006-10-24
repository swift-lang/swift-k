map(
	map:entry("name", "another stress test")
	map:entry("test",
		element([]
			element(bla, [a, b]
				a/b
			)
			
			sum(
				for(i, range(1, 20)
					parallelFor(j, range(1, 500)
						bla(
							parallel(
								2+2
								1+1
							)
						)
					)
					echo(".", nl=false)
				)
			)
			echo()
		)
	)
	map:entry("expected-result", 20*500*2)
)
