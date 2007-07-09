type messagefile {}

(messagefile t) greeting(int m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile <"032-float-int.out">;

int i = 42 + 12.3;

float f = i;

outfile = greeting(f);

