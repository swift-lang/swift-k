type file {} 

(file t) echo (string s) {   
    app {
        echo s stdout=@filename(t);
    }
}

string greetings[] = ["how","are","you"];

foreach g in greetings {
  file hw = echo(g);
}

