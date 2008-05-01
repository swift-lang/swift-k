type messagefile;

(messagefile t) greeting() { 
    app {
        echo "testing \"quotes\" in swift" stdout=@filename(t);
    }
}

messagefile outfile <"1031-quote.out">;

outfile = greeting();

