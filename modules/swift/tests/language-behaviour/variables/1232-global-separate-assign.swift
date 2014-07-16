type messagefile;
app (messagefile t) greeting() { 
        echo m stdout=@filename(t);
}

global string m;
m = "hi";
messagefile outfile <"1232-global-separate-assign.out">;

outfile = greeting();

