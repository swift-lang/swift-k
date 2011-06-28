type messagefile {}

(messagefile t) greeting(string left="hi", string right) { 
    app {
        echo left right stdout=@filename(t);
    }
}

messagefile outfile <"0023-default-override.out">;

outfile = greeting(left="foo",right="there");

