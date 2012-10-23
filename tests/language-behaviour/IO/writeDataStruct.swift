type file;

type S { string l; string c; string r; }

S s;

file f <"writeDataStruct.out">;

f=writeData(s);

s.l = "baz";
s.c = "BAZ";
s.r = "Baz";

