type messagefile;

(messagefile t) metagreeting(string m) {
string l = m;
t=greeting(l);
}

(messagefile t) greeting(string m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile <"120-local-vars.out">;

outfile = greeting("hi");

