test("File tests", providers="gsiftp", subtests = ["Put", "Get", "List", "Rename", "Remove", "Exists", "Make Dir", "Is Dir", "Remove Dir", "Bug"], 
	testlist = 
		list(
			element([host, provider]
				testWrapper(host = host, provider = provider, type = "file", test = 
					element([host, provider]
						task:transfer(srcdir = TEST_FILE_DIR, srcfile = TEST_FILE
							desthost = host 
							provider = provider)
					)
				)
			)

			element([host, provider]
				testWrapper(host = host, provider = provider, type = "file", test = 
					element([host, provider]
						task:transfer(srchost = host, srcfile = TEST_FILE,
							destdir = TEST_FILE_DIR, destfile = "{TEST_FILE}.received"
							provider = provider)
					)
				)
			)
			
			element([host, provider]
				testWrapper(host = host, provider = provider, type = "file", test = 
					element([host, provider]
						set(list, file:list(".", host, provider))
					)
				)
			)

			element([host, provider]
				testWrapper(host = host, provider = provider, type = "file", test = 
					element([host, provider]
						file:rename(TEST_FILE, "{TEST_FILE}.renamed", host, provider)
					)
				)
			)
			
			element([host, provider]
				testWrapper(host = host, provider = provider, type = "file", test = 
					element([host, provider]
						file:remove("{TEST_FILE}.renamed", host, provider)
					)
				)
			)
			
			element([host, provider]
				testWrapper(host = host, provider = provider, type = "file", test = 
					element([host, provider]
						if(
							file:exists("{TEST_FILE}.renamed", host, provider)
							generateError("file:exists returned true, but the file was supposed to be removed")
						)
					)
				)
			)
			
			element([host, provider]
				testWrapper(host = host, provider = provider, type = "file", test = 
					element([host, provider]
						dir:make("{TEST_FILE}.dir", host, provider)
					)
				)
			)

			element([host, provider]
				testWrapper(host = host, provider = provider, type = "file", test = 
					element([host, provider]
						if(
							not(file:isDirectory("{TEST_FILE}.dir", host, provider))
							generateError("file:isDirectory returned false")
						)
					)
				)
			)
			
			element([host, provider]
				testWrapper(host = host, provider = provider, type = "file", test = 
					element([host, provider]
						dir:remove("{TEST_FILE}.dir", host, provider)
						if(
							file:exists("{TEST_FILE}.dir", host, provider)
							generateError("Directory was not removed!")
						)
					)
				)
			)
			
			element([host, provider]
				testWrapper(host = host, provider = provider, type = "file", test = 
					element([host, provider]
						ignoreErrors(
							discard(file:list("foo", host, provider))
						)

						discard(file:exists("foo", host, provider))
					)
				)
			)
		)
)
