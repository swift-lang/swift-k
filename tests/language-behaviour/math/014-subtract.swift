type messagefile {}

app (messagefile t) greeting(int m) { 
        echo m stdout=@filename(t);
}

messagefile outfile <"014-subtract.out">;

int i = 44-81;

outfile = greeting(i);

