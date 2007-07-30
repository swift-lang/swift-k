type messagefile {}

(messagefile t) greeting(int m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile <"007-add-in-proc-add.out">;

outfile = greeting(88+101);

