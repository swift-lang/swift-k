type messagefile {}

(messagefile t) greeting(string left, string right) { 
    app {
        echo left right stdout=@filename(t);
    }
}

messagefile outfile <"0021-param-bind.out">;

outfile = greeting(right="there", left="hi");

