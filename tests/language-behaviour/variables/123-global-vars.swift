type messagefile;


app (messagefile t) greeting() { 
        echo m stdout=@filename(t);
}

global string m = "hello";
messagefile outfile <"123-global-vars.out">;

outfile = greeting();

