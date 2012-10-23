type messagefile {}

app (messagefile t) greeting(float m) { 
        echo m stdout=@filename(t);
}

messagefile outfile <"031-add-float.out">;

float i = 42.1+93.2;

outfile = greeting(i);

