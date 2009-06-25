type messagefile;


(messagefile t) greeting() { 
    app {
        echo m stdout=@filename(t);
    }
}

global string m;
m = "hi";
messagefile outfile <"1232-global-separate-assign.aout">;

outfile = greeting();

