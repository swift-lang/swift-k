type messagefile {}

(messagefile t) greeting(boolean b) { 
    app {
        echo b stdout=@filename(t);
    }
}

messagefile outfile <"101-comparison-string.out">;

string s = "hello";

boolean r = s=="hello";

outfile = greeting(r);

