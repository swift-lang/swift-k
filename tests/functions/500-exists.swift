
string s1 = "file.txt";
string s2 = "file-missing.txt";

boolean b1, b2;

b1 = @exists(s1);
b2 = @exists(s2);

tracef("%s: %b\n%s: %b\n",
        s1, b1, s2, b2);
