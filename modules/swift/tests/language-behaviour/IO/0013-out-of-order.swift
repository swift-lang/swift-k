type messagefile;

app (messagefile t) greeting() { 
        echo "hello" stdout=@filename(t);
}

messagefile outfile <"0013-out-of-order.out">;

outfile = greeting();

