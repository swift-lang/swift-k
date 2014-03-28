type file;

file[] a <SimpleMapper; location="outs", prefix="foo", suffix=".out">;

app (file[] oa) gen(int i) {
	gen i;
}

a = gen(3);

trace(a);
trace(length(a));