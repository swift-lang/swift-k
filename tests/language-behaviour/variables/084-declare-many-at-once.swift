type messagefile {}

app (messagefile t) greeting() { 
        echo "hello" stdout=@filename(t);
}

messagefile outfile = greeting();

messagefile o2 = greeting(), o4 = greeting(), o5, o6[], o7=greeting();

o6[0] = greeting();
o6[1] = greeting();
o5 = greeting();
o6[2] = greeting();

// can't check the output in present framework because don't know
// what filename got chosen for outfile...
