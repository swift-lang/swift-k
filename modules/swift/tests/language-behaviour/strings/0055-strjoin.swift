string sa[] = ["a", "bb", "ccc"];
string sas = @strjoin(sa, ":");
tracef("String array: %s\n", sas);

int ia[] = [1, 2, 3, 4, 5];
string ias = @strjoin(ia, ":");
tracef("Integer array: %s\n", ias);

float fa[] = [1.1, 2.2, 3.3, 4.4, 5.5];
string fas = @strjoin(fa, ":");
tracef("Float array: %s\n", fas);

boolean ba[] = [true, false, true, false];
string bas = @strjoin(ba, ":");
tracef("Boolean array: %s\n", bas);

string empty[];
string emptystring = @strjoin(empty, ":");
tracef("Empty: %s\n", emptystring);

