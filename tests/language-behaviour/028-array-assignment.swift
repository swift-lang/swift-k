type messagefile {}

(messagefile t) greeting(string m[]) {
    app {
        echo m[1] stdout=@filename(t);
    }
}

messagefile outfile <"027-array-assignment.out">;

string msg[];
string baz[];

baz = msg;
msg = [ "one", "two" ];

outfile = greeting(baz);

