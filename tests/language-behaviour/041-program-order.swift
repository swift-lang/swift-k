type messagefile {}

(messagefile t) greeting(int m) { 
    app {
        echo m stdout=@filename(t);
    }
}

messagefile outfile <"041-program-order.out">;

outfile = greeting(i);

int i = 42+93;

