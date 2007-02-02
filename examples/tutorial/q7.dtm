type file {} 

(file t) echo (string s) {   
    app {
        echo s stdout=@filename(t);
    }
}

string outputNames = "one two three";

file outputFiles[] <fixed_array_mapper;files=outputNames>;

foreach f in outputFiles {
    f = echo("hello");
}

