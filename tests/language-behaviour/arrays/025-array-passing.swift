type messagefile {}

(messagefile t) greeting(string m[]) {
    app {
        echo m[1] stdout=@filename(t);
    }
}

messagefile outfile <"025-array-passing.out">;

string msg[] = [ "one", "two" ];

outfile = greeting(msg);

