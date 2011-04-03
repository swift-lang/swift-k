
string s1 = "file.txt";
string s2 = "file-missing.txt";

boolean b1, b2;

b1 = @exists_file(s1);
b2 = @exists_file(s2);

tracef("%s: %p\n%s: %p\n",
        s1, b1, s2, b2);
