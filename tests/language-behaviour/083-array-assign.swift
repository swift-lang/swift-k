type messagefile;

(messagefile t) greeting(string m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile[];

outfile[0] = greeting("hi");
outfile[1] = greeting("bye");

