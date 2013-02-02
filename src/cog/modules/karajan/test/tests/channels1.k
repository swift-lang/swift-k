import(sys)
import(file = "../testlib.k")

map(
	map:entry(key = "name", "channels 1")
	map:entry(key = "test",
		lambda() {
			value := 
				channel:from(channel) {
					channel:to(channel) {
						"test"
					}
				}
			
			value
		}
	)
	map:entry(key = "expected-result", "test")
	map:entry(key = "comparison", defaultComparison)
)
