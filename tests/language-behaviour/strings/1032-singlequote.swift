type messagefile;

app (messagefile t) greeting() { 
        echo "testing 'quotes' in swift" stdout=@filename(t);
}

messagefile outfile <"1032-singlequote.out">;

outfile = greeting();

