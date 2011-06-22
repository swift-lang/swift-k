type messagefile;

(messagefile t) metagreeting(string m) {
string l = m;
t=greeting(l);
}

app (messagefile t) greeting(string m) { 
        echo m stdout=@filename(t);
}

messagefile outfile <"120-local-vars.out">;

outfile = greeting("hi");

