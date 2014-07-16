
string d = "-";
string b = "bye";

string f = @sprintf("hi%s%s", d, b);

assert(f == "hi-bye");
