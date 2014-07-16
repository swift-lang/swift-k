type messagefile {}

app (messagefile t) greeting(float m) { 
        echo m stdout=@filename(t);
}

messagefile outfile <"030-mix-float-int.out">;

float f = 42.0 + 12.3;

outfile = greeting(f);

