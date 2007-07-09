type messagefile {}

(messagefile t) greeting(int m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile <"030-mix-float-int.out">;

int i = 42 + 12.3;

outfile = greeting(i);

