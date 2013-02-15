map(
	map:entry("name", "while loop")
	map:entry("test",
		element([]
			element(test_while, []
				i := 0
				list(
					while(?(i < 8)
						i
						i := i + 1
					)
				)
			)
			
			element(test_break, []
				i := 0
				list(
					while(?(i < 8)
						if(i == 3 then(break()))
						i
						i := i + 1
					)
				)
			)			
			
			element(test_continue, []
				i := 0
				list(
					while(?(i < 3)
						i := i + 1
						if(i == 2 continue())
						i
					)
				)
			)
			
			list(
				test_while()
				test_break()
				test_continue()
			)
		)
	)
	map:entry("expected-result",
		[
			[0, 1, 2, 3, 4, 5, 6, 7]
			[0, 1, 2]
			[1, 3]
		]
	)
)