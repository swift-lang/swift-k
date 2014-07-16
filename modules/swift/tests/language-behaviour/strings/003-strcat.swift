type messagefile {}

app (messagefile t) greeting(string m) { 
        echo m stdout=@filename(t);
}

messagefile outfile <"003-strcat.out">;

string v = @strcat("abc","qux");

outfile = greeting(v);


