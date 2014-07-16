type messagefile;

app (messagefile t) greeting() { 
        echo "'" stdout=@filename(t);
}

messagefile outfile <"1033-singlequote.out">;

outfile = greeting();

