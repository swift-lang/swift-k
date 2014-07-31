type messagefile;

app (messagefile t) greeting (string s) {   
    echo s stdout=@filename(t);
}

messagefile english <"manyparam.english.txt">;
messagefile french <"manyparam.french.txt">;
messagefile japanese <"manyparam.japanese.txt">;

english = greeting("hello");
french = greeting("bonjour");
japanese = greeting("konnichiwa");
