
tracef("int:3:%i\n", 3);
tracef("string:4:%s\n", "4");
tracef("fraction:%f\n", 22.0/7.0);

type file;
file f<"test.txt">;
tracef("file:%M\n", f);

int i[];
i[0] = 9;
i[1] = 91;
i[2] = 19;
tracef("array:%q\n", i);

tracef("pointer:%p\n", 3);
tracef("spacing: WORD\tWORD\nWORD\tWORD\n");

// TODO: Debug the Swift parser- it handles backslash strangely
// tracef("backslash2: \\ \ qq \\\\ \\n");
