map(
	map:entry("name", "list")
	map:entry("test",
		element([]
			list(
				list:size(
					list(
						list(1, 2)
						list(3, 4)
					)
				)
				
				list:size(
					list:join(
						list(1, 2)
						list(3, 4)
					)
				)
				
				set(mylist, list(1, 2, 3, 4))
				
				list:butFirst(mylist)
				list:butLast(mylist)
				list:first(mylist)
				list:last(mylist)
			)
		)
	)
	map:entry("expected-result", [2, 4, [2, 3, 4], [1, 2, 3], 1, 4])
	map:entry("comparison", math_comparison)
)