type messagefile {}

app (messagefile t) greeting(boolean m) { 
        echo m stdout=@filename(t);
}

messagefile outfile <"015-lesseq.out">;

boolean i = 7<=9;

outfile = greeting(i);

