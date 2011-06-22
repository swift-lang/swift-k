type messagefile {}

(messagefile t) greeting(int m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile <"010-divide.out">;

int i = 99%/3;

outfile = greeting(i);

