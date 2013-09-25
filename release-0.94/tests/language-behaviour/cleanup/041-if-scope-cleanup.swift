type file;

app (file f) gen(string msg) {
	echo msg stdout=@filename(f);
}

foreach i in [0:1] {
	
	trace("Iteration", i);
	if (i == 0) {
		file tmp <test_mapper;file=@strcat("041-if-scope-cleanup.then.1.", i, ".tmp"), temp="true">;
		tmp = gen(@strcat("Iteration ", i));
	}
	else {
		file tmp <test_mapper;file=@strcat("041-if-scope-cleanup.else.1.", i, ".out"), temp="false">;
		tmp = gen(@strcat("Iteration ", i));
	}
	
	if (i == 0) {
		file tmp <test_mapper;file=@strcat("041-if-scope-cleanup.then.2.", i, ".out"), temp="false">;
		tmp = gen(@strcat("Iteration ", i));
	}
	else {
		file tmp <test_mapper;file=@strcat("041-if-scope-cleanup.else.2.", i, ".tmp"), temp="true">;
		tmp = gen(@strcat("Iteration ", i));
	}
}
