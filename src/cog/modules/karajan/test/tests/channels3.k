import(sys)
import(file = "../testlib.k")

map(
	map:entry("name", "channels 3")
	map:entry("test",
		lambda() {
			chantest := function(channel(one), channel(two), ...) {
				for(i, ..., "0:{i}")
				for(i, one, "1:{i}")
				for(i, two, "2:{i}")
			}
			list(
				chantest(
					"a"
					"b"
					channel:to(one) {"a", "b"}
					channel:to(two) {"a", "b"}
				)
			)
		}
	)
	map:entry("expected-result", ["0:a", "0:b", "1:a", "1:b", "2:a", "2:b"])
	map:entry("comparison", defaultComparison)
)