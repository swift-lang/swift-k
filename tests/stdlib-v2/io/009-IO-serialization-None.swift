import "stdlib.v2";

type file;

file f1, f2, f3, f4;

f1 = write(1);
int i = read(f1);
assertEqual(i, 1);

f2 = write(2.0);
float f = read(f2);
assertEqual(f, 2.0);

f3 = write("A string");
string s = read(f3);
assertEqual(s, "A string");

f4 = write(true);
boolean b = read(f4);
assertEqual(b, true);

