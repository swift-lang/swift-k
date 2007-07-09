type messagefile {}

(messagefile t) greeting(string m) { 
    app {
        echo m stdout=@filename(t);
    }
}


outfile = greeting("hi");

messagefile outfile <"040-program-order.out">;

