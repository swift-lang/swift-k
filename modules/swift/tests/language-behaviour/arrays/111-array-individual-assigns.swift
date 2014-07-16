int i[];

i[0]=1;
i[1]=100;
i[2]=10000;

type messagefile;

app (messagefile t) p(int inp[]) { 
        echo inp[1] stdout=@filename(t);
}

messagefile outfile <"111-array-individual-assigns.out">;

outfile = p(i);
