type messagefile {}

(messagefile t) greeting(int m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile <"017-greater.out">;

int i = 7>9;

outfile = greeting(i);

