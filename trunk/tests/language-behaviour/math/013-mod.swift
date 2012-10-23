type messagefile {}

app (messagefile t) greeting(int m) { 
        echo m stdout=@filename(t);
}

messagefile outfile <"013-mod.out">;

int i = 99 %% 20;

outfile = greeting(i);

