(int r) f(int[] b) {
	foreach i in b {
		trace(i);
	}
	r = 1;
}

int a[];

int[] r;

foreach i in [0:10] {
	if (true) {
		a[i] = 1;
	}
	else {
		a[i] = 2;
	}
	r[i] = f(a);
}
