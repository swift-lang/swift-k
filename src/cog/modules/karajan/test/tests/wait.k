import(sys)
import(file = "../testlib.k")

map(
	map:entry("name", "wait")
	map:entry("test",
		lambda() {
			wait(delay=10)
			"OK"
		}
	)
	map:entry("expected-result", "OK")
)
