type messagefile;

p() {
 m = "hi";
}

(messagefile t) greeting() { 
    app {
        echo m stdout=@filename(t);
    }
}

global string m;
messagefile outfile <"1231-global-assign-inside.out">;

outfile = greeting();

p();
