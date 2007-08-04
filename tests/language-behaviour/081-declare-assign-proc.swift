type messagefile {}

(messagefile t) greeting(int i) { 
    app {
        echo i stdout=@filename(t);
    }
}

messagefile outfile = greeting(3 + 2);

// can't check the output in present framework because don't know
// what filename got chosen for outfile...
