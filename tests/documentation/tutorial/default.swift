type file;

// s has a default value
app (file t) echo (string s="hello world") { 
        echo s stdout=@filename(t);		
}

file hw1<"default.1.txt">;
file hw2<"default.2.txt">;

// procedure call using the default value
hw1 = echo();		

// using a different value
hw2 = echo(s="hello again"); 
