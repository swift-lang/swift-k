type file {}


(file t) echo (string s) {
    app {
        echo s stdout=@filename(t);
    }
}

file hw = echo("hello world");		// May need syntax to give a call a name.


