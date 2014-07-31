type file;

app (file f) gen(string msg) {
	echo msg stdout=@filename(f);
}

iterate i
{
	file tmp <test_mapper;file=@strcat("021-iter-scope-cleanup.", i, ".tmp"), temp="true">;
	
	trace("Iteration", i);
	tmp = gen(@strcat("Iteration ", i));
} until (i > 2);

