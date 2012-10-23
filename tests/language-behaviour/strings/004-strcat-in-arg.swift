type messagefile {}

app (messagefile t) greeting(string m) { 
        echo m stdout=@filename(t);
}

messagefile outfile <"004-strcat-in-arg.out">;

outfile = greeting(@strcat("test004","append"));


