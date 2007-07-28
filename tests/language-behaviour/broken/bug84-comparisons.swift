type messagefile {}

(messagefile t) greeting(boolean b) { 
    app {
        echo b stdout=@filename(t);
    }
}

messagefile outfile <"bug84-comparisons.out">;

outfile = greeting(1 == 1);

