type messagefile;

app (messagefile t) greeting(string m) { 
        echo m stdout=@filename(t);
}

messagefile outfile[];

outfile[0] = greeting("hi");
outfile[1] = greeting("bye");
