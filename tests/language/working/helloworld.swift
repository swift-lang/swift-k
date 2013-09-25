type file {}						//define a type for file
(file t) echo (string s) { 			//procedure declaration
    app {
        echo s stdout=@filename(t);	//redirect stdout to a file
    }
}

file hw = echo("hello world");		//procedure call
