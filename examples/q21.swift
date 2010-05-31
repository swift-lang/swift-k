type messagefile {} 

(messagefile t) greeting (string s="hello") {   
    app {
        echo s stdout=@filename(t);
    }
}

messagefile english <"english2.txt">;
messagefile french <"francais2.txt">;

english = greeting();
french = greeting(s="bonjour");

