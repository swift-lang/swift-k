import(sys)

export(defaultComparison,
	lambda(value1, value2) {
		equals(value1, value2)
	}
)


export(mathComparison,
	 lambda(value1, value2) {
		equalsNumeric(value1, value2)
	}
)


export(noComparison,
	lambda(value1, value2) {
		true
	}
)
