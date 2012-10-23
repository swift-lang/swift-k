type messagefile {}

app (messagefile t) greeting(string left, string right) { 
        echo left right stdout=@filename(t);
}

messagefile outfile <"0021-param-bind.out">;

outfile = greeting(right="there", left="hi");
