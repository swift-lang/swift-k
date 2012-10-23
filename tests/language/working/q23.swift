type messagefile {} 

(messagefile t) greeting (string s) {   
    app {
        echo s stdout=@filename(t);
    }
}

messagefile outfile <"q23.txt">;

string message = @arg("text");

outfile = greeting(message);

