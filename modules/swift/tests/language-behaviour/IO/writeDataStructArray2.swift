type file;

type S { string l; string c; string r; }

S s[];

file f <"writeDataStructArray2.out">;

f=writeData(s);

s[2].l = "baz";
s[2].c = "BAZ";
s[2].r = "Baz";
s[3].l = "qux";
s[3].c = "QUX";
s[3].r = "Qux";
s[0].l = "foo";
s[0].c = "FOO";
s[0].r = "Foo";
s[1].l = "bar";
s[1].c = "BAR";
s[1].r = "Bar";
s[4].l = "frrrr";
s[4].c = "FRRRR";
s[4].r = "Frrrr";

