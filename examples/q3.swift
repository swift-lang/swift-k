type file {} 

(file t) echo (string s = "default greeting") {   
    app {
        echo s stdout=@filename(t);
    }
}

file hw = echo();

