type file{}
file f<"/d/f.txt">;
string s = @dirname(f);
string t = @tostring(s);
tracef("dirname: %s\n", s);
