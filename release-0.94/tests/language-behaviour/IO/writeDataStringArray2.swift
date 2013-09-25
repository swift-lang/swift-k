type file;
string s[];
file f <"writeDataStringArray2.out">;
f=writeData(s);
s[2] = "baz";
s[3] = "qux";
s[0] = "foo";
s[1] = "bar";
s[4] = "frrrr";
