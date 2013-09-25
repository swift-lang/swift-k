type messagefile {}

(messagefile t) greeting(int m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile <"006-add.out">;

int i = 42+93;

outfile = greeting(i);

