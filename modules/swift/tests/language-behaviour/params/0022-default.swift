type messagefile {}

app (messagefile t) greeting(string left="hi", string right) { 
        echo left right stdout=@filename(t);
}

messagefile outfile <"0022-default.out">;

outfile = greeting(right="there");

