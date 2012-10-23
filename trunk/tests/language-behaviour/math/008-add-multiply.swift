type messagefile {}

app (messagefile t) greeting(int m) { 
        echo m stdout=@filename(t);
}

messagefile outfile <"008-add-multiply.out">;

int i = 2+2*3;

outfile = greeting(i);

