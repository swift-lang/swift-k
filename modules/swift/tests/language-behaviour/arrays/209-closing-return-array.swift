int[] a;

(int[] r) f() {
	foreach i in [0:9] {
		r[i] = i;
		r[i + 10] = i + 10;
	}
}

a = f();

foreach v in a {
	trace(v);
}