test("File transfer", ["gsiftp"], []
	list(
		element([host, host2, provider]
			testWrapper(host = host, host2 = host2, provider = provider, type = "file"
				test = element([host, host2, provider]
					task:transfer(
						srchost=host
						srcfile="/dev/urandom"
						length=1024*1024
						desthost=host2
						destfile="/dev/null"
						thirdParty=true
						provider=provider
					)
				)
			)
		)
	)
)