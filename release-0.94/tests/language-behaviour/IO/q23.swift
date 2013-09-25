type messagefile {} 

app (messagefile t) greeting (string s) {   
        echo s stdout=@filename(t);
}

messagefile outfile <"q23.out">;

string message = @arg("text");

outfile = greeting(message);

