type file {} 

app (file t) echo (string s) {   
        echo s stdout=@filename(t);
}

string greetings[] = ["how","are","you"];

foreach g in greetings {
  file hw = echo(g);
}

