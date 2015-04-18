import "stdlib.v2";

type file;

type struct {
	string a;
	int b;
	float c;
	boolean d;
}

assertEqual(struct[] a, struct[] b) {
	assertEqual(length(a), length(b));
	
	foreach i in [0:length(a) - 1] {
		assertEqual(a[i].a, b[i].a);
		assertEqual(a[i].b, b[i].b);
		assertEqual(a[i].c, b[i].c);
		assertEqual(a[i].d, b[i].d);
	}
}

struct[] s;

s = [{a: "First", b: 1, c: 1.1, d: true}, 
	 {a: "Second", b: 2, c: 2.1, d: false},
	 {a: "Third", b: 3, c: 3.1, d: true}];

file f;

f = write(s, format = "FV");

struct[] t;

t = read(f, format = "FV");

assertEqual(s, t);
