type file;

app (file fr) generate(string msg) {
	echo msg stdout=@filename(fr);
}

app (file fr) cat(file f1, file f2) {
	cat @filename(f1) @filename(f2) stdout=@filename(fr);
}

(file fr) proc1() {
	file t1 <test_mapper;file="001-proc-scope-cleanup.t1.tmp", temp="true">;
	file t2 <test_mapper;file="001-proc-scope-cleanup.t2.out", temp="false">;
	
	t1 = generate("f1");
	t2 = generate("f2");
	
	fr = cat(t1, t2);
}

file f <"001-proc-scope-cleanup.out">;

f = proc1();
