type messagefile {}

(messagefile t) greeting(boolean b) { 
    app {
        echo b stdout=@filename(t);
    }
}

messagefile outfile <"bug84-comparisons2.out">;

int i = 2;

boolean r = i==2;

outfile = greeting(r);

