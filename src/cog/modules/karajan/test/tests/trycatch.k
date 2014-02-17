import(sys)
import(file = "../testlib.k")

map(
	map:entry("name", "try/catch")
	map:entry("test",
		lambda() {
			list(
				try {
					throw("Kaboom")
					"uncaught"
				}
				else catch(e) {
					"caught"
				}
				"alldone"
			)
		}
	)
	map:entry("expected-result", ["caught", "alldone"])
)