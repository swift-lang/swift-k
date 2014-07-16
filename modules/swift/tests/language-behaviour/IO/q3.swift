type file {} 

app (file t) echo (string s = "default greeting") {   
        echo s stdout=@filename(t);
}

file hw = echo();

