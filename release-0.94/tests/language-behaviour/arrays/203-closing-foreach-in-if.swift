(int r) f(int[] b) {
	foreach i in b {
		trace(i);
	}
	r = 1;
}


int[] a;
int r;

if (true) {
	foreach j in [0:10] {
		if (true) {
			a[j] = 1;
		}
	}
	r = f(a);
}

