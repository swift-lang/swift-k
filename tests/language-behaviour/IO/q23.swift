// THIS-SCRIPT-SHOULD-FAIL
// This script should fail because there is no way to pass an argument to a specific swift file within a group.
type messagefile {} 

app (messagefile t) greeting (string s) {   
        echo s stdout=@filename(t);
}

messagefile outfile <"q23.out">;

string message = @arg("text");

outfile = greeting(message);

