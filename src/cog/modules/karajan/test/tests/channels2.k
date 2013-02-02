import(sys)
import(file = "../testlib.k")

map(
	map:entry("name", "channels 2")
	map:entry("test"
		lambda() {
			list(
				channel:from(list) {
					one :=
						channel:from(one) {
							two :=
								channel:from(two) {
									channel:to(one, 1)
									channel:to(two, 2)
								}
							
							channel:to(list, two)
						}
					
					channel:to(list, one)
				}
			)
		}
	)
	map:entry("expected-result", list(2, 1))
	map:entry("comparison", mathComparison)
)