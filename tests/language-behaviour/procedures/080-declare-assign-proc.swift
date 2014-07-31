type messagefile {}

app (messagefile t) greeting() { 
        echo "hello" stdout=@filename(t);
}

messagefile outfile = greeting();

// can't check the output in present framework because don't know
// what filename got chosen for outfile...
