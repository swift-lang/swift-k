type messagefile {}

app (messagefile t) greeting(int m) { 
        echo m stdout=@filename(t);
}

messagefile outfile <"020-array.out">;

int i[] = [ 3, 1, 4, 1, 5];

int j = i[2];

outfile = greeting(j);

