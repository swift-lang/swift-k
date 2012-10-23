type messagefile;

app (messagefile t) greeting() { 
        echo "testing \"quotes\" in swift" stdout=@filename(t);
}

messagefile outfile <"1031-quote.out">;

outfile = greeting();

