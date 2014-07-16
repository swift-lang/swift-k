type messagefile {}

app (messagefile t) greeting(int m) { 
        echo m stdout=@filename(t);
}

messagefile outfile <"007-add-in-proc-add.out">;

outfile = greeting(88+101);

