type messagefile {}

app (messagefile t) greeting(boolean m) { 
        echo m stdout=@filename(t);
}

messagefile outfile <"016-greatereq.out">;

boolean i = 7>=9;

outfile = greeting(i);

