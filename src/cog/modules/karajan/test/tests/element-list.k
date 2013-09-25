map(
	map:entry("name", "element list")
	map:entry("test",
		element([]
			set(list1,
				elementList(
					"1"
					"2"
				)
			)
			set(list2,
				list(
					element([], "1")
					element([], "2")
				)
			)
			
			list(
				for(item, list1, executeElement(item))
				for(item, list2, executeElement(item))
			)
		)
	)
	map:entry("expected-result", list("1", "2", "1", "2"))
	map:entry("comparison", math_comparison)
)