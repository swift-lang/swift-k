type messagefile;

app (messagefile t) greeting (string s) {   
    echo s stdout=@filename(t);
}

messagefile english <"english.txt">;
messagefile french <"francais.txt">;
english = greeting("hello");
french = greeting("bonjour");

messagefile japanese <"nihongo.txt">;
japanese = greeting("konnichiwa");
