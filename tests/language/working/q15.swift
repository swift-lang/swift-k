type messagefile {} 

type details {
    string name;
    int pies;
}

(messagefile t) greeting (details d) {   
    app {
        echo "Hello. Your names is" d.name "and you have eaten" d.pies "pies." stdout=@filename(t);
    }
}

details person;

person.name = "John";
person.pies = 3;

messagefile outfile <"q15.txt">;

outfile = greeting(person);

