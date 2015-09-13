type file{}
file f<"/etc/fstab">;
string s = @dirname(f);
string t = @tostring(s);
tracef("dirname: %s\n", s);
