type messagefile {}

app (messagefile t) greeting(string m[]) {
        echo m[1] stdout=@filename(t);
}

messagefile outfile <"027-array-assignment.out">;

string msg[];
string baz[];

baz = msg;
msg = [ "one", "two" ];

outfile = greeting(baz);

