type messagefile; 

app (messagefile t) greeting (string s="hello") {   
        echo s stdout=@filename(t);
}

messagefile english <"english2.txt">;
messagefile french <"francais2.txt">;

english = greeting();
french = greeting(s="bonjour");

