// This CSV business is just broken by design

import "stdlib.v2";

type file;

type struct {
	string a;
	int b;
	float c;
	boolean d;
}

struct s;

s.a = "a_string";
s.b = 1;
s.c = 2.1;
s.d = true;

file f;

f = write(s, format = "CSV");

struct t;

t = read(f, format = "CSV");

assertEqual(s.a, t.a);
assertEqual(s.b, t.b);
assertEqual(s.c, t.c);
assertEqual(s.d, t.d);
