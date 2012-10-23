type messagefile;

type details {
    string name;
    int pies;
}

app (messagefile t) greeting (details d) {   
    echo "Hello. Your name is" d.name "and you have eaten" d.pies "pies." stdout=@filename(t);
}

details person;

person.name = "John";
person.pies = 3;

messagefile outfile <"types.pies.txt">;

outfile = greeting(person);

