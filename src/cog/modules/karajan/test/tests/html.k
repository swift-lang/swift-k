map(
	map:entry("name", "html")
	map:entry("test",
		element([]
			html:write(file = "test1.html"
				html(
					head(title("Test1"))
					body(bgcolor="#f0f0f0"
						h2("A simple table")
						table(border="1"
							tr(th(),        th("Col 1"), th("Col 2"), th("Col 3"))
							tr(th("Row 1"), td("1, 1"),  td("1, 2"),  td("1, 3"))
							tr(th("Row 2"), td("2, 1"),  td("2, 2"),  td("2, 3"))
							tr(th("Row 3"), td("3, 1"),  td("3, 2"),  td("3, 3"))
						)
					)
				)
			)
			
			/*html:write(
				stream = outputStream(type = "file", file="test2.html")
				html(
					head(title("Test2"))
					body(bgcolor="#f0f0ff"
						h2("An automatically generated table")
						table(border="1"
							tr(th(),
								for(col, range(1, 30)
									th("Col {col}")
								)
							)
							for(row, range(1, 20)
								tr(th("Row {row}")
									for(col, range(1, 30)
										td("{row}, {col}")
									)
								)
							)
						)
					)
				)
			)*/
		)
	)
)