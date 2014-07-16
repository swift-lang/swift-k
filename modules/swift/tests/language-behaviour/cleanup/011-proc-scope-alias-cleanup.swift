type file;

app (file fr) generate(string msg) {
	echo msg stdout=@filename(fr);
}

app (file fr) cat(file f1, file f2) {
	cat @filename(f1) @filename(f2) stdout=@filename(fr);
}

(file fr) proc1() {
	file t1 <test_mapper;file="011-proc-scope-alias-cleanup.1.tmp", temp="true">;
	file t2 <test_mapper;file="011-proc-scope-alias-cleanup.2.out", temp="false">;
	
	t1 = generate("f1");
	t2 = generate("f2");
	
	file t3 <test_mapper;file="011-proc-scope-alias-cleanup.3.tmp", temp="true", remappable="true">;
	file t4 <test_mapper;file="011-proc-scope-alias-cleanup.4.tmp", temp="true", remappable="true">;
	
	t3 = t1;
	t4 = t2;
	
	fr = cat(t3, t4);
}

file f <"011-proc-scope-alias-cleanup.out">;

f = proc1();
