type messagefile {}

(messagefile t) greeting(boolean b) { 
    app {
        echo b stdout=@filename(t);
    }
}

messagefile outfile <"100-comparison.out">;

boolean r = 1==1;

outfile = greeting(r);

