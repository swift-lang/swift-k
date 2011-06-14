type messagefile {}

(messagefile t) greeting(int m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile <"009-multiply.out">;

int i = 6*7;

outfile = greeting(i);

