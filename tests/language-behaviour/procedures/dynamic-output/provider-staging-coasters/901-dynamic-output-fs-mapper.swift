type file;

file[] a <FilesysMapper; location="outs", pattern="*.out">;

app (file[] oa) gen(int i) {
	gen i;
}

a = gen(3);

trace(a);
trace(length(a));