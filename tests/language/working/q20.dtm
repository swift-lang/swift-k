type messagefile {}

(messagefile t) greeting (string s) {
    app {
        echo s stdout=@filename(t);
    }
}

messagefile outfile <"hello20.txt">;

boolean morning = true;

if(morning) {
  outfile = greeting("good morning");
} else {
  outfile = greeting("good afternoon");
}

