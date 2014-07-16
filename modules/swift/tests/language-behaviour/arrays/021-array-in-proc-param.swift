type messagefile {}

(messagefile t) greeting(int m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile <"021-array-in-proc-param.out">;

int i[] = [ 3, 1, 4, 1, 5];

outfile = greeting(i[3]);

