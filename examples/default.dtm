type file {}

(file t) echo (string s="hello world") { //s has a default value
    app {
        echo s stdout=@filename(t);		//redirect stdout to a file
    }
}

file hw1, hw2;
hw1 = echo();		// procedure call using the default value
hw2 = echo(s="hello again"); // using a different value
