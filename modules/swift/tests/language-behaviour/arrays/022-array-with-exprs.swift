type messagefile {}

app (messagefile t) greeting(int m) { 
        echo m stdout=@filename(t);
}

messagefile outfile <"022-array-with-exprs.out">;

int i[] = [ 1+91, 3+3, 16+3*2 , 1, 5];

int j = i[2];

outfile = greeting(j);

