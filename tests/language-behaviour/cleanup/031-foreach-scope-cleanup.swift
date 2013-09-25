type file;

app (file f) gen(string msg) {
	echo msg stdout=@filename(f);
}

foreach i in [0:3] {
	file tmp <test_mapper;file=@strcat("031-foreach-scope-cleanup.", i, ".tmp"), temp="true">;
	
	trace("Iteration", i);
	tmp = gen(@strcat("Iteration ", i));
}
