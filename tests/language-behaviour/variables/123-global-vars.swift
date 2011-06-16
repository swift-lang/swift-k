type messagefile;


(messagefile t) greeting() { 
    app {
        echo m stdout=@filename(t);
    }
}

global string m = "hello";
messagefile outfile <"123-global-vars.out">;

outfile = greeting();

