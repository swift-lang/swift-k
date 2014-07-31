type messagefile {}

app (messagefile t) greeting(float m) { 
        echo m stdout=@filename(t);
}

messagefile outfile <"011-divide-float.out">;

float i = 1.0/3.0;

outfile = greeting(i);

