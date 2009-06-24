type messagefile;

(messagefile t) greeting() { 
    app {
        echo "hello" stdout=@filename(t);
    }
}

messagefile outfile <"0013-out-of-order.out">;

outfile = greeting();

