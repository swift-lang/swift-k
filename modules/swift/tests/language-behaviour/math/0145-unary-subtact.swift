type messagefile {}

app (messagefile t) greeting(int m) { 
        echo m stdout=@filename(t);
}

messagefile outfile <"0145-unary-subtact.out">;

int j = 989929;
int i = -j;

outfile = greeting(i);

