type messagefile;

app (messagefile t) greeting() { 
        echo "\"" stdout=@filename(t);
}

messagefile outfile <"103-quote.out">;

outfile = greeting();

