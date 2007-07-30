type messagefile {}

(messagefile t) greeting(float m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile <"030-mix-float-int.out">;

float f = 42 + 12.3;

outfile = greeting(f);

