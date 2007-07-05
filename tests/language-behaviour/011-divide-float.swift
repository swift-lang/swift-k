type messagefile {}

(messagefile t) greeting(float m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile <"011-divide-float.out">;

float i = 1/3;

outfile = greeting(i);

